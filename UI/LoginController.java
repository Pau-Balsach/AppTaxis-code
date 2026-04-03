package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Admin;
import service.AuthService;

public class LoginController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    private final AuthService authService = new AuthService();

    @FXML
    private void handlerLogin() {
        limpiarMensaje();

        String email    = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Error: Introduce correo y contraseña.", true);
            return;
        }

        Admin admin = authService.login(email, password);

        if (admin != null) {
            try {
                Stage stage = (Stage) txtEmail.getScene().getWindow();
                StageConfigurator.showMenu(stage, admin);
            } catch (Exception e) {
                mostrarMensaje("Error al cargar el menú principal.", true);
                e.printStackTrace();
            }
        } else {
            mostrarMensaje("Error: Correo o contraseña incorrectos.", true);
            txtPassword.clear();
        }
    }

    @FXML
    private void handlerIrARegistro() {
        limpiarMensaje();

        String email    = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Error: Rellena los campos para registrarte.", true);
            return;
        }

        boolean ok = authService.registrar(email, password);
        if (ok) {
            mostrarMensaje("Registro iniciado. Revisa tu correo para confirmar.", false);
        } else {
            mostrarMensaje("Error: No se pudo completar el registro.", true);
        }
        
    }

    private void limpiarMensaje() {
        if (lblError != null) lblError.setText("");
    }

    private void mostrarMensaje(String msg, boolean esError) {
        if (lblError != null) {
            lblError.setTextFill(esError ? Color.web("#cc0000") : Color.web("#28a745"));
            lblError.setText(msg);
        }
    }
}