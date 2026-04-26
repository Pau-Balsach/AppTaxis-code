package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import repository.ConfigLoader;
import service.SessionManager;
import ui.StageConfigurator;

public class AplicacionTaxis extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        String apiKey = ConfigLoader.get().getProperty("api.key", "").trim();
        boolean autorizado = SessionManager.inicializarAcceso(apiKey);

        if (!autorizado) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Acceso Denegado");
            alerta.setHeaderText("API Key inválida o inactiva");
            alerta.setContentText(
                "La clave de acceso configurada no es válida.\n"
                + "Contacta con el administrador del sistema.");
            alerta.showAndWait();
            Platform.exit();
            return;
        }

        // ── 2. Mostrar UI ─────────────────────────────────────────────────────
        StageConfigurator.configure(primaryStage);
        primaryStage.show();
    }

    static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.postgresql").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("jakarta.persistence").setLevel(java.util.logging.Level.SEVERE);
        launch(args);
    }
}