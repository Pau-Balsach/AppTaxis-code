package service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import model.Admin;
import repository.ConfigLoader;

public class AuthService {

    private static final String BASE_URL;
    private static final String ANON_KEY;
    private static final Properties props;

    static {
        props    = ConfigLoader.get();
        BASE_URL = props.getProperty("supabase.url");
        ANON_KEY = props.getProperty("supabase.anon_key");
        System.out.println("[AuthService] BASE_URL cargado: " + BASE_URL);
        System.out.println("[AuthService] ANON_KEY presente: " + (ANON_KEY != null && !ANON_KEY.isBlank()));
    }

    public Admin login(String email, String password) {
        System.out.println("[AuthService] login() — email: " + email);
        try {
            String json      = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
            String respuesta = post("/auth/v1/token?grant_type=password", json, null);

            System.out.println("[AuthService] Respuesta Supabase login (primeros 200 chars): "
                    + (respuesta != null ? respuesta.substring(0, Math.min(200, respuesta.length())) : "null"));

            if (respuesta == null || respuesta.contains("\"error\"")) {
                System.err.println("[AuthService] ERROR: login fallido. Respuesta: " + respuesta);
                return null;
            }

            String accessToken = extractJson(respuesta, "access_token");
            if (accessToken == null) {
                System.err.println("[AuthService] ERROR: access_token no encontrado en la respuesta.");
                return null;
            }
            System.out.println("[AuthService] access_token obtenido correctamente.");

            String userId = extractJsonNested(respuesta, "user", "id");
            if (userId == null) {
                System.err.println("[AuthService] ERROR: user.id no encontrado en la respuesta.");
                return null;
            }
            System.out.println("[AuthService] userId: " + userId);

            Admin admin = new Admin();
            admin.setId(UUID.fromString(userId));
            admin.setEmail(email);
            admin.setAccessToken(accessToken);

            SessionManager.iniciarSesion(admin);

            System.out.println("[AuthService] Login completado. appAutorizada="
                    + SessionManager.isAppAutorizada()
                    + " — si es false, las llamadas a ConductorService/ViajeService fallarán.");

            return admin;

        } catch (Exception e) {
            System.err.println("[AuthService] EXCEPCIÓN en login(): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void logout() {
        System.out.println("[AuthService] logout()");
        SessionManager.cerrarSesion();
    }

    public boolean registrar(String email, String password) {
        System.out.println("[AuthService] registrar() — email: " + email);
        try {
            String json = "{\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"options\":{\"emailRedirectTo\":\"https://pau-balsach.github.io/AppTaxis-auth\"}}";
            String respuesta = post("/auth/v1/signup", json, null);
            System.out.println("[AuthService] Respuesta signup: "
                    + (respuesta != null ? respuesta.substring(0, Math.min(200, respuesta.length())) : "null"));

            if (respuesta == null || respuesta.contains("\"error\"")) {
                System.err.println("[AuthService] ERROR: signup fallido.");
                return false;
            }

            String userId = extractJsonNested(respuesta, "user", "id");
            if (userId == null) userId = extractJson(respuesta, "id");
            System.out.println("[AuthService] userId tras signup: " + userId);

            postAdmin("/rest/v1/admins",
                "{\"id\":\"" + userId + "\", \"email\":\"" + email + "\"}");
            return true;
        } catch (Exception e) {
            System.err.println("[AuthService] EXCEPCIÓN en registrar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean tokenValido() {
        String token = SessionManager.getToken();
        System.out.println("[AuthService] tokenValido() — token presente: " + (token != null));
        if (token == null) return false;
        try {
            URL url = new URL(BASE_URL + "/auth/v1/user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            int code = conn.getResponseCode();
            System.out.println("[AuthService] tokenValido() — HTTP " + code);
            return code == 200;
        } catch (Exception e) {
            System.err.println("[AuthService] EXCEPCIÓN en tokenValido(): " + e.getMessage());
            return false;
        }
    }

    private String post(String endpoint, String json, String token) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        System.out.println("[AuthService] POST " + url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", ANON_KEY);
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        int status = conn.getResponseCode();
        System.out.println("[AuthService] HTTP " + status + " ← " + endpoint);
        InputStream stream = (status >= 200 && status < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();
        if (stream == null) return "{\"error\":\"http_" + status + "\"}";
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String postAdmin(String endpoint, String json) throws IOException {
        String serviceKey = props.getProperty("supabase.service_key");
        System.out.println("[AuthService] postAdmin() — service_key presente: "
                + (serviceKey != null && !serviceKey.isBlank()));
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", serviceKey);
        conn.setRequestProperty("Authorization", "Bearer " + serviceKey);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
        int status = conn.getResponseCode();
        System.out.println("[AuthService] postAdmin() HTTP " + status);
        return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extractJson(String json, String clave) {
        String marca = "\"" + clave + "\":\"";
        int ini = json.indexOf(marca);
        if (ini == -1) return null;
        ini += marca.length();
        return json.substring(ini, json.indexOf("\"", ini));
    }

    private String extractJsonNested(String json, String bloque, String clave) {
        int ini = json.indexOf("\"" + bloque + "\"");
        if (ini == -1) return null;
        return extractJson(json.substring(ini), clave);
    }
}