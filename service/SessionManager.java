package service;

import model.Admin;

public class SessionManager {

    private static Admin adminActual = null;

    private SessionManager() {}

    public static void iniciarSesion(Admin admin) {
        adminActual = admin;
    }

    public static void cerrarSesion() {
        adminActual = null;
    }

    public static Admin getAdmin() {
        return adminActual;
    }

    public static String getToken() {
        return adminActual != null ? adminActual.getAccessToken() : null;
    }

    public static boolean haySesion() {
        return adminActual != null && adminActual.getAccessToken() != null;
    }
}