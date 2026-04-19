package ui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import model.Admin;
import service.AuthService;

public class MenuController {

    private Admin adminLogueado;
    private final AuthService authService = new AuthService();

    public void setAdmin(Admin admin) {
        this.adminLogueado = admin;
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
    private void handlerGestionarClientes() {
        try {
            Stage stage = StageConfigurator.getCurrentStage();
            StageConfigurator.showClientes(stage, adminLogueado);
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
        authService.logout();
        System.exit(0);
    }
}