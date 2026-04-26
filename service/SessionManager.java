package service;

import model.Admin;
import repository.ApiKeyRepository;

public class SessionManager {

    private static Admin adminActual = null;
    private static boolean appAutorizada = false;
    private static boolean modoDemo = false;
    private static boolean ventanaMaximizada = false;
    private static boolean pantallaCompleta = false;

    private SessionManager() {}

    public static boolean inicializarAcceso(String apiKeyRaw) {
        if (apiKeyRaw == null || apiKeyRaw.isBlank()) {
            modoDemo = true;
            appAutorizada = true;
            return true;
        }
        String hash = SecurityUtils.generarHash(apiKeyRaw);
        ApiKeyRepository repo = new ApiKeyRepository();
        if (repo.esValida(hash)) {
            appAutorizada = true;
            modoDemo = false;
            return true;
        }
        return false;
    }

    public static void checkAuth() {
        if (!appAutorizada) throw new SecurityException("Acceso Denegado: Aplicacion no autorizada.");
        if (!haySesion()) throw new SecurityException("Acceso Denegado: No hay sesion de usuario activa.");
    }

    public static void iniciarSesion(Admin admin) {
        adminActual = admin;
        ventanaMaximizada = false;
        pantallaCompleta = false;
    }

    public static void cerrarSesion() {
        adminActual = null;
        ventanaMaximizada = false;
        pantallaCompleta = false;
    }

    public static void actualizarEstadoVentana(boolean maximizada, boolean enPantallaCompleta) {
        ventanaMaximizada = maximizada;
        pantallaCompleta = enPantallaCompleta;
    }

    public static Admin getAdmin() { return adminActual; }
    public static String getToken() { return adminActual != null ? adminActual.getAccessToken() : null; }
    public static boolean haySesion() { return adminActual != null && adminActual.getAccessToken() != null; }
    public static boolean isAppAutorizada() { return appAutorizada; }
    public static boolean isModoDemo() { return modoDemo; }
    public static boolean isVentanaMaximizada() { return ventanaMaximizada; }
    public static boolean isPantallaCompleta() { return pantallaCompleta; }
}