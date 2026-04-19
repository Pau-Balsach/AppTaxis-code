package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Admin;
import java.net.URL;

public class StageConfigurator {

    private static Stage currentStage;

    public static void configure(Stage stage) throws Exception {
        currentStage = stage;
        URL url = StageConfigurator.class.getClassLoader()
                .getResource("aplicaciotaxis/UI/login.fxml");
        if (url == null) throw new RuntimeException("ERROR: No se encuentra login.fxml");
        Parent root = FXMLLoader.load(url);
        stage.setTitle("App Taxis - Login");
        stage.setScene(new Scene(root));
    }

    public static void showMenu(Stage stage, Admin admin) throws Exception {
        currentStage = stage;
        URL url = StageConfigurator.class.getClassLoader()
                .getResource("aplicaciotaxis/UI/menu.fxml");
        if (url == null) throw new RuntimeException("ERROR: No se encuentra menu.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        MenuController controller = loader.getController();
        controller.setAdmin(admin);
        stage.setTitle("App Taxis - Menú Principal");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void showConductores(Stage stage, Admin admin) throws Exception {
        currentStage = stage;
        URL url = StageConfigurator.class.getClassLoader()
                .getResource("aplicaciotaxis/UI/conductores.fxml");
        if (url == null) throw new RuntimeException("ERROR: No se encuentra conductores.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        ConductoresController controller = loader.getController();
        controller.setAdmin(admin);
        stage.setTitle("App Taxis - Gestión de Conductores");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void showClientes(Stage stage, Admin admin) throws Exception {
        currentStage = stage;
        URL url = StageConfigurator.class.getClassLoader()
                .getResource("aplicaciotaxis/UI/clientes.fxml");
        if (url == null) throw new RuntimeException("ERROR: No se encuentra clientes.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        ClientesController controller = loader.getController();
        controller.setAdmin(admin);
        stage.setTitle("App Taxis - Gestión de Clientes");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void showCalendario(Stage stage, Admin admin) throws Exception {
        currentStage = stage;
        URL url = StageConfigurator.class.getClassLoader()
                .getResource("aplicaciotaxis/UI/calendario.fxml");
        if (url == null) throw new RuntimeException("ERROR: No se encuentra calendario.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        CalendarioController controller = loader.getController();
        controller.setAdmin(admin);
        stage.setTitle("App Taxis - Calendario");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static Stage getCurrentStage() {
        return currentStage;
    }
}