package ui;

import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Admin;
import service.SessionManager;

public class StageConfigurator {

    private static final String BASE_PATH = "aplicaciotaxis/UI/";

    private static Stage currentStage;
    private static Scene escenaPrincipal;
    private static boolean listenersRegistrados = false;

    public static void configure(Stage stage) throws Exception {
        currentStage = stage;
        registrarListeners(stage);
        Parent root = FXMLLoader.load(getResource("login.fxml"));
        escenaPrincipal = new Scene(root);
        stage.setTitle("App Taxis - Login");
        stage.setScene(escenaPrincipal);
        stage.show();
    }

    public static void showMenu(Stage stage, Admin admin) throws Exception {
        FXMLLoader loader = new FXMLLoader(getResource("menu.fxml"));
        Parent root = loader.load();
        MenuController controller = loader.getController();
        controller.setAdmin(admin);
        cambiarRoot(stage, "App Taxis - Menu Principal", root);
    }

    public static void showConductores(Stage stage, Admin admin) throws Exception {
        FXMLLoader loader = new FXMLLoader(getResource("conductores.fxml"));
        Parent root = loader.load();
        ConductoresController controller = loader.getController();
        controller.setAdmin(admin);
        cambiarRoot(stage, "App Taxis - Gestion de Conductores", root);
    }

    public static void showClientes(Stage stage, Admin admin) throws Exception {
        FXMLLoader loader = new FXMLLoader(getResource("clientes.fxml"));
        Parent root = loader.load();
        ClientesController controller = loader.getController();
        controller.setAdmin(admin);
        cambiarRoot(stage, "App Taxis - Gestion de Clientes", root);
    }

    public static void showCalendario(Stage stage, Admin admin) throws Exception {
        FXMLLoader loader = new FXMLLoader(getResource("calendario.fxml"));
        Parent root = loader.load();
        CalendarioController controller = loader.getController();
        controller.setAdmin(admin);
        cambiarRoot(stage, "App Taxis - Calendario", root);
    }

    public static Stage getCurrentStage() {
        return currentStage;
    }

    private static URL getResource(String fileName) {
        URL url = StageConfigurator.class.getClassLoader().getResource(BASE_PATH + fileName);
        if (url == null) {
            throw new RuntimeException("ERROR: No se encuentra " + fileName);
        }
        return url;
    }

    private static void cambiarRoot(Stage stage, String title, Parent root) {
        stage.setTitle(title);
        escenaPrincipal.setRoot(root);
    }

    private static void registrarListeners(Stage stage) {
        if (listenersRegistrados) return;

        stage.fullScreenProperty().addListener((obs, anterior, actual) ->
            SessionManager.actualizarEstadoVentana(stage.isMaximized(), actual));

        stage.maximizedProperty().addListener((obs, anterior, actual) ->
            SessionManager.actualizarEstadoVentana(actual, stage.isFullScreen()));

        listenersRegistrados = true;
    }
}