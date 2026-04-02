package ui;

import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Admin;
import model.Conductor;
import service.ConductorService;

public class ConductoresController {

    @FXML private TextField txtMatricula;
    @FXML private TextField txtNombre;
    @FXML private Label     lblMensaje;

    @FXML private VBox                           seccionCrud;
    @FXML private TableView<Conductor>           tablaConductores;
    @FXML private TableColumn<Conductor, String> colNombre;
    @FXML private TableColumn<Conductor, String> colMatricula;
    @FXML private Button                         btnEditar;
    @FXML private Button                         btnEliminar;

    private static final String REGEX_MATRICULA = "^[0-9]{4}[A-Z]{3}$";

    private final ConductorService conductorService = new ConductorService();
    private Admin adminLogueado;

    public void setAdmin(Admin admin) {
        this.adminLogueado = admin;
    }

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        tablaConductores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaConductores.getSelectionModel().selectedItemProperty().addListener(
            (obs, anterior, seleccionado) -> {
                boolean haySeleccion = seleccionado != null;
                btnEditar.setDisable(!haySeleccion);
                btnEliminar.setDisable(!haySeleccion);
            });
    }

    @FXML
    private void handlerAñadirConductor() {
        limpiarMensaje();

        if (adminLogueado == null) {
            mostrarError("Error: No hay administrador en sesión.");
            return;
        }

        String matricula = txtMatricula.getText();
        if (matricula == null || matricula.trim().isEmpty()) {
            mostrarError("Error: Introduce una matrícula.");
            return;
        }
        matricula = matricula.trim().toUpperCase();
        if (!matricula.matches(REGEX_MATRICULA)) {
            mostrarError("Error: Formato inválido. Ejemplo: 1234ABC");
            return;
        }

        String nombre = txtNombre.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("Error: Introduce un nombre.");
            return;
        }

        Conductor nuevo = new Conductor();
        nuevo.setMatricula(matricula);
        nuevo.setNombre(nombre.trim());
        nuevo.setCond_admin(adminLogueado.getId());

        boolean exito = conductorService.registrar(nuevo);
        if (exito) {
            mostrarExito("Conductor " + nombre.trim().toUpperCase() + " registrado correctamente.");
            txtMatricula.clear();
            txtNombre.clear();
            refrescarTabla();
        } else {
            mostrarError("Error: Ya existe un conductor con la matrícula " + matricula + ".");
        }
    }

    @FXML
    private void handlerMostrarConductores() {
        seccionCrud.setVisible(true);
        seccionCrud.setManaged(true);
        refrescarTabla();
    }

    @FXML
    private void handlerEditar() {
        Conductor seleccionado = tablaConductores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        TextInputDialog dialogo = new TextInputDialog(seleccionado.getNombre());
        dialogo.setTitle("Editar conductor");
        dialogo.setHeaderText("Editando: " + seleccionado.getMatricula());
        dialogo.setContentText("Nuevo nombre:");

        Optional<String> resultado = dialogo.showAndWait();
        resultado.ifPresent(nuevoNombre -> {
            if (nuevoNombre.trim().isEmpty()) {
                mostrarError("Error: El nombre no puede estar vacío.");
                return;
            }
            boolean exito = conductorService.editar(seleccionado.getId(), nuevoNombre.trim());
            if (exito) {
                mostrarExito("Conductor actualizado correctamente.");
                refrescarTabla();
            } else {
                mostrarError("Error al actualizar el conductor.");
            }
        });
    }

    @FXML
    private void handlerEliminar() {
        Conductor seleccionado = tablaConductores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar conductor");
        confirmacion.setHeaderText("¿Eliminar a " + seleccionado.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            boolean exito = conductorService.eliminar(seleccionado.getId());
            if (exito) {
                mostrarExito("Conductor eliminado correctamente.");
                refrescarTabla();
            } else {
                mostrarError("Error al eliminar el conductor.");
            }
        }
    }

    @FXML
    private void handlerVolver() {
        try {
            Stage stage = (Stage) txtMatricula.getScene().getWindow();
            StageConfigurator.showMenu(stage, adminLogueado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void refrescarTabla() {
        try {
            List<Conductor> lista = conductorService.listarTodos();
            tablaConductores.setItems(FXCollections.observableArrayList(lista));
            tablaConductores.getSelectionModel().clearSelection();
            if (lista.isEmpty()) mostrarError("No hay conductores en la base de datos.");
        } catch (Exception e) {
            mostrarError("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    private void limpiarMensaje() {
        if (lblMensaje != null) lblMensaje.setText("");
    }

    private void mostrarError(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setTextFill(Color.web("#cc0000"));
            lblMensaje.setText(mensaje);
        }
    }

    private void mostrarExito(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setTextFill(Color.web("#28a745"));
            lblMensaje.setText(mensaje);
        }
    }
}