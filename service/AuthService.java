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

public class AuthService {
    private static final Properties props = new Properties();
    private static final String BASE_URL;
    private static final String ANON_KEY;

    static {
        try (InputStream in = AuthService.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(in);
        } catch (Exception e) { throw new RuntimeException("Error config: " + e.getMessage()); }
        String rawUrl = props.getProperty("supabase.url");
        BASE_URL = "https://" + rawUrl.replace("db.", "").replace(".supabase.co", "") + ".supabase.co";
        ANON_KEY = props.getProperty("supabase.anon_key");
    }

    public static Admin login(String email, String contrasenya) {
        try {
            String json = "{\"email\":\"" + email + "\",\"password\":\"" + contrasenya + "\"}";
            String resposta = post("/auth/v1/token?grant_type=password", json, null);
            
            if (resposta == null || resposta.contains("error")) return null;

            String userIdStr = extractJsonNested(resposta, "user", "id");
            if (userIdStr == null) return null;

            Admin admin = new Admin();
            admin.setId(UUID.fromString(userIdStr));
            admin.setEmail(email);
            return admin;
        } catch (Exception e) { return null; }
    }

    public static boolean registrar(String email, String contrasenya) {
        try {
            String json = "{\"email\":\"" + email + "\",\"password\":\"" + contrasenya + "\",\"options\":{\"emailRedirectTo\":\"https://pau-balsach.github.io/AppTaxis-auth\"}}";
            String resposta = post("/auth/v1/signup", json, null);
            
            if (resposta == null || resposta.contains("error")) return false;

            String userId = extractJsonNested(resposta, "user", "id");
            if (userId == null) userId = extractJson(resposta, "id");

            String jsonAdmin = "{\"id\":\"" + userId + "\", \"email\":\"" + email + "\"}";
            postAdmin("/rest/v1/admins", jsonAdmin);
            return true;
        } catch (Exception e) { return false; }
    }

    private static String post(String endpoint, String json, String token) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", ANON_KEY);
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(json.getBytes(StandardCharsets.UTF_8)); }
        return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String postAdmin(String endpoint, String json) throws IOException {
        String serviceKey = props.getProperty("supabase.service_key");
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", serviceKey);
        conn.setRequestProperty("Authorization", "Bearer " + serviceKey);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(json.getBytes(StandardCharsets.UTF_8)); }
        return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extractJson(String json, String clave) {
        String cerca = "\"" + clave + "\":\"";
        int ini = json.indexOf(cerca);
        if (ini == -1) return null;
        ini += cerca.length();
        int fi = json.indexOf("\"", ini);
        return json.substring(ini, fi);
    }

    private static String extractJsonNested(String json, String bloque, String clave) {
        int iniBloque = json.indexOf("\"" + bloque + "\"");
        if (iniBloque == -1) return null;
        return extractJson(json.substring(iniBloque), clave);
    }
}