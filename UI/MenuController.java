package ui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import model.Admin;

public class MenuController {

    private Admin adminLogueado;

    public void setAdmin(Admin admin) {
        this.adminLogueado = admin;
        if (admin != null) {
            System.out.println("Sesión iniciada como: " + admin.getEmail());
        }
    }

    @FXML
    private void handlerGestionarConductores() {
        try {
            Stage stage = StageConfigurator.getCurrentStage();
            StageConfigurator.showConductores(stage, adminLogueado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlerCalendario() {
        try {
            Stage stage = StageConfigurator.getCurrentStage();
            StageConfigurator.showCalendario(stage, adminLogueado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlerSortir() {
        System.exit(0);
    }
}