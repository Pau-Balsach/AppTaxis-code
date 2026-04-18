package ui;

import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Admin;
import model.Cliente;
import service.ClienteService;

public class ClientesController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextArea  txtNotas;
    @FXML private Label     lblMensaje;

    @FXML private VBox                         seccionCrud;
    @FXML private TableView<Cliente>           tablaClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private Button                       btnEditar;
    @FXML private Button                       btnEliminar;

    private final ClienteService clienteService = new ClienteService();
    private Admin adminLogueado;

    public void setAdmin(Admin admin) {
        this.adminLogueado = admin;
    }

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            boolean haySeleccion = seleccionado != null;
            btnEditar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });
    }

    @FXML
    private void handlerAñadirCliente() {
        limpiarMensaje();

        if (adminLogueado == null) {
            mostrarError("Error: No hay administrador en sesión.");
            return;
        }

        String nombre = txtNombre.getText();
        String telefono = txtTelefono.getText();
        String email = txtEmail.getText();
        String notas = txtNotas.getText();

        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("Error: Introduce un nombre.");
            return;
        }
        if (telefono == null || telefono.trim().isEmpty()) {
            mostrarError("Error: Introduce un teléfono.");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            mostrarError("Error: Introduce un email.");
            return;
        }

        Cliente nuevo = new Cliente();
        nuevo.setNombre(nombre.trim());
        nuevo.setTelefono(telefono.trim());
        nuevo.setEmail(email.trim());
        nuevo.setNotas(notas == null ? "" : notas.trim());
        nuevo.setAdminId(adminLogueado.getId());

        boolean exito = clienteService.registrar(nuevo);
        if (exito) {
            mostrarExito("Cliente " + nombre.trim().toUpperCase() + " registrado correctamente.");
            txtNombre.clear();
            txtTelefono.clear();
            txtEmail.clear();
            txtNotas.clear();
            refrescarTabla();
        } else {
            mostrarError("Error: Revisa los campos del cliente.");
        }
    }

    @FXML
    private void handlerMostrarClientes() {
        seccionCrud.setVisible(true);
        seccionCrud.setManaged(true);
        refrescarTabla();
    }

    @FXML
    private void handlerEditar() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Editar cliente");
        dialogo.setHeaderText("Editando: " + seleccionado.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtNombreEd = new TextField(seleccionado.getNombre());
        TextField txtTelefonoEd = new TextField(seleccionado.getTelefono());
        TextField txtEmailEd = new TextField(seleccionado.getEmail());
        TextArea txtNotasEd = new TextArea(seleccionado.getNotas());
        txtNotasEd.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombreEd, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(txtTelefonoEd, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmailEd, 1, 2);
        grid.add(new Label("Notas:"), 0, 3);
        grid.add(txtNotasEd, 1, 3);

        dialogo.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialogo.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnGuardar) {
            boolean exito = clienteService.editar(
                seleccionado.getId(),
                txtNombreEd.getText(),
                txtTelefonoEd.getText(),
                txtEmailEd.getText(),
                txtNotasEd.getText());

            if (exito) {
                mostrarExito("Cliente actualizado correctamente.");
                refrescarTabla();
            } else {
                mostrarError("Error al actualizar el cliente.");
            }
        }
    }

    @FXML
    private void handlerEliminar() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cliente");
        confirmacion.setHeaderText("¿Eliminar a " + seleccionado.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            boolean exito = clienteService.eliminar(seleccionado.getId());
            if (exito) {
                mostrarExito("Cliente eliminado correctamente.");
                refrescarTabla();
            } else {
                mostrarError("Error al eliminar el cliente.");
            }
        }
    }

    @FXML
    private void handlerVolver() {
        try {
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            StageConfigurator.showMenu(stage, adminLogueado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refrescarTabla() {
        try {
            List<Cliente> lista = clienteService.listarTodos();
            tablaClientes.setItems(FXCollections.observableArrayList(lista));
            tablaClientes.getSelectionModel().clearSelection();
            if (lista.isEmpty()) mostrarError("No hay clientes en la base de datos.");
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