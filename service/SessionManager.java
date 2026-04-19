package service;

import model.Admin;
import repository.ApiKeyRepository;

public class SessionManager {

    private static Admin   adminActual  = null;
    private static boolean appAutorizada = false;
    private static boolean modoDemo      = false;

    private SessionManager() {}
    public static boolean inicializarAcceso(String apiKeyRaw) {
        if (apiKeyRaw == null || apiKeyRaw.isBlank()) {
            System.out.println("[SessionManager] api.key no configurada → MODO DEMO activado.");
            modoDemo      = true;
            appAutorizada = true;
            return true;
        }

        System.out.println("[SessionManager] Validando API key contra BD...");
        String hash = SecurityUtils.generarHash(apiKeyRaw);
        System.out.println("[SessionManager] Hash: " + hash);

        ApiKeyRepository repo = new ApiKeyRepository();
        if (repo.esValida(hash)) {
            appAutorizada = true;
            modoDemo      = false;
            System.out.println("[SessionManager] API key válida → MODO PRIVADO activado.");
            return true;
        }

        System.err.println("[SessionManager] ERROR: API key inválida o inactiva en BD.");
        return false;
    }

    public static void checkAuth() {
        System.out.println("[SessionManager] checkAuth() — appAutorizada=" + appAutorizada
                + " | modoDemo=" + modoDemo + " | haySesion=" + haySesion());
        if (!appAutorizada) {
            System.err.println("[SessionManager] BLOQUEADO: app no autorizada (API key inválida).");
            throw new SecurityException("Acceso Denegado: Aplicación no autorizada.");
        }
        if (!haySesion()) {
            System.err.println("[SessionManager] BLOQUEADO: sin sesión de usuario activa.");
            throw new SecurityException("Acceso Denegado: No hay sesión de usuario activa.");
        }
    }

    public static void iniciarSesion(Admin admin) {
        System.out.println("[SessionManager] iniciarSesion() — "
                + (admin != null ? admin.getEmail() : "null"));
        adminActual = admin;
    }

    public static void cerrarSesion() {
        System.out.println("[SessionManager] cerrarSesion()");
        adminActual = null;
    }

    public static Admin   getAdmin()        { return adminActual; }
    public static String  getToken()        { return adminActual != null ? adminActual.getAccessToken() : null; }
    public static boolean haySesion()       { return adminActual != null && adminActual.getAccessToken() != null; }
    public static boolean isAppAutorizada() { return appAutorizada; }
    public static boolean isModoDemo()      { return modoDemo; }
}