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
    }

    public Admin login(String email, String password) {
        try {
            String json      = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
            String respuesta = post("/auth/v1/token?grant_type=password", json, null);

            if (respuesta == null || respuesta.contains("\"error\"")) return null;

            String accessToken = extractJson(respuesta, "access_token");
            if (accessToken == null) return null;

            String userId = extractJsonNested(respuesta, "user", "id");
            if (userId == null) return null;

            Admin admin = new Admin();
            admin.setId(UUID.fromString(userId));
            admin.setEmail(email);
            admin.setAccessToken(accessToken);

            SessionManager.iniciarSesion(admin);
            return admin;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void logout() {
        SessionManager.cerrarSesion();
    }

    public boolean registrar(String email, String password) {
        try {
            String json = "{\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\","
                + "\"options\":{\"emailRedirectTo\":\"https://pau-balsach.github.io/AppTaxis-auth\"}}";
            String respuesta = post("/auth/v1/signup", json, null);

            if (respuesta == null || respuesta.contains("\"error\"")) return false;

            String userId = extractJsonNested(respuesta, "user", "id");
            if (userId == null) userId = extractJson(respuesta, "id");

            postAdmin("/rest/v1/admins",
                "{\"id\":\"" + userId + "\", \"email\":\"" + email + "\"}");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean tokenValido() {
        String token = SessionManager.getToken();
        if (token == null) return false;
        try {
            URL url = new URL(BASE_URL + "/auth/v1/user");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private String post(String endpoint, String json, String token) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
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
        InputStream stream = (status >= 200 && status < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();
        if (stream == null) return "{\"error\":\"http_" + status + "\"}";
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String postAdmin(String endpoint, String json) throws IOException {
        String serviceKey = props.getProperty("supabase.service_key");
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
        conn.getResponseCode();
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