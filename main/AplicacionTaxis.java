package main;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.StageConfigurator;
public class AplicacionTaxis extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StageConfigurator.configure(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("org.postgresql").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("jakarta.persistence").setLevel(java.util.logging.Level.SEVERE);
        launch(args);
    }
}