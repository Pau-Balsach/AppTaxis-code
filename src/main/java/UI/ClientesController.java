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
import javafx.scene.text.Font;
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

    private static final String REGEX_EMAIL    = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String REGEX_TELEFONO = "^(\\+34)?[6789][0-9]{8}$";

    private static final String STYLE_OK  = "-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-radius: 4;";
    private static final String STYLE_ERR = "-fx-background-color: #fff0f0; -fx-border-color: #cc0000; -fx-border-radius: 4;";

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
        if (adminLogueado == null) { mostrarError("No hay administrador en sesión."); return; }

        String nombre   = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String telefono = txtTelefono.getText() == null ? "" : txtTelefono.getText().trim();
        String email    = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String notas    = txtNotas.getText() == null ? "" : txtNotas.getText().trim();

        if (nombre.isEmpty())                         { mostrarError("El nombre no puede estar vacío."); return; }
        if (telefono.isEmpty())                       { mostrarError("El teléfono no puede estar vacío."); return; }
        if (!telefono.matches(REGEX_TELEFONO))        { mostrarError("Teléfono inválido. Usa 9 dígitos empezando por 6, 7, 8 o 9 (ej: 612345678) o con prefijo +34."); return; }
        if (email.isEmpty())                          { mostrarError("El email no puede estar vacío."); return; }
        if (!email.matches(REGEX_EMAIL))              { mostrarError("Email inválido. Usa el formato nombre@dominio.com."); return; }

        Cliente nuevo = new Cliente();
        nuevo.setNombre(nombre);
        nuevo.setTelefono(telefono);
        nuevo.setEmail(email);
        nuevo.setNotas(notas);
        nuevo.setAdminId(adminLogueado.getId());

        if (clienteService.registrar(nuevo)) {
            mostrarExito("Cliente " + nombre.toUpperCase() + " registrado correctamente.");
            txtNombre.clear(); txtTelefono.clear(); txtEmail.clear(); txtNotas.clear();
            refrescarTabla();
        } else {
            mostrarError("No se pudo registrar el cliente. Revisa los datos e inténtalo de nuevo.");
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

        TextField txtNombreEd   = new TextField(seleccionado.getNombre());
        TextField txtTelefonoEd = new TextField(seleccionado.getTelefono());
        TextField txtEmailEd    = new TextField(seleccionado.getEmail());
        TextArea  txtNotasEd    = new TextArea(seleccionado.getNotas());
        txtNotasEd.setPrefRowCount(3);

        Label lblErrNombre   = new Label();
        Label lblErrTelefono = new Label();
        Label lblErrEmail    = new Label();
        for (Label l : new Label[]{lblErrNombre, lblErrTelefono, lblErrEmail}) {
            l.setTextFill(Color.web("#cc0000"));
            l.setFont(Font.font(10));
        }

        txtNombreEd.setStyle(STYLE_OK);
        txtTelefonoEd.setStyle(STYLE_OK);
        txtEmailEd.setStyle(STYLE_OK);

        txtNombreEd.textProperty().addListener((obs, ant, val) -> {
            if (val.trim().isEmpty()) {
                txtNombreEd.setStyle(STYLE_ERR);
                lblErrNombre.setText("El nombre no puede estar vacío.");
            } else {
                txtNombreEd.setStyle(STYLE_OK);
                lblErrNombre.setText("");
            }
        });

        txtTelefonoEd.textProperty().addListener((obs, ant, val) -> {
            if (!val.matches("[+0-9]*")) { txtTelefonoEd.setText(ant); return; }
            if (val.trim().isEmpty()) {
                txtTelefonoEd.setStyle(STYLE_ERR);
                lblErrTelefono.setText("El teléfono no puede estar vacío.");
            } else if (!val.trim().matches(REGEX_TELEFONO)) {
                txtTelefonoEd.setStyle(STYLE_ERR);
                lblErrTelefono.setText("Formato inválido. Ej: 612345678 o +34612345678");
            } else {
                txtTelefonoEd.setStyle(STYLE_OK);
                lblErrTelefono.setText("");
            }
        });

        txtEmailEd.textProperty().addListener((obs, ant, val) -> {
            if (val.trim().isEmpty()) {
                txtEmailEd.setStyle(STYLE_ERR);
                lblErrEmail.setText("El email no puede estar vacío.");
            } else if (!val.trim().matches(REGEX_EMAIL)) {
                txtEmailEd.setStyle(STYLE_ERR);
                lblErrEmail.setText("Formato inválido. Ej: nombre@dominio.com");
            } else {
                txtEmailEd.setStyle(STYLE_OK);
                lblErrEmail.setText("");
            }
        });

        javafx.scene.Node guardarBtn = dialogo.getDialogPane().lookupButton(btnGuardar);
        Runnable checkValido = () -> {
            boolean nombreOk   = !txtNombreEd.getText().trim().isEmpty();
            boolean telefonoOk = txtTelefonoEd.getText().trim().matches(REGEX_TELEFONO);
            boolean emailOk    = txtEmailEd.getText().trim().matches(REGEX_EMAIL);
            guardarBtn.setDisable(!nombreOk || !telefonoOk || !emailOk);
        };
        txtNombreEd.textProperty().addListener((obs, a, b) -> checkValido.run());
        txtTelefonoEd.textProperty().addListener((obs, a, b) -> checkValido.run());
        txtEmailEd.textProperty().addListener((obs, a, b) -> checkValido.run());
        checkValido.run();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new javafx.geometry.Insets(16, 16, 8, 16));

        grid.add(new Label("Nombre:"),   0, 0); grid.add(txtNombreEd,   1, 0);
        grid.add(lblErrNombre,           1, 1);
        grid.add(new Label("Teléfono:"), 0, 2); grid.add(txtTelefonoEd, 1, 2);
        grid.add(lblErrTelefono,         1, 3);
        grid.add(new Label("Email:"),    0, 4); grid.add(txtEmailEd,    1, 4);
        grid.add(lblErrEmail,            1, 5);
        grid.add(new Label("Notas:"),    0, 6); grid.add(txtNotasEd,    1, 6);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        Optional<ButtonType> resultado = dialogo.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnGuardar) {
            if (clienteService.editar(seleccionado.getId(), txtNombreEd.getText().trim(),
                    txtTelefonoEd.getText().trim(), txtEmailEd.getText().trim(), txtNotasEd.getText())) {
                mostrarExito("Cliente actualizado correctamente.");
                refrescarTabla();
            } else {
                mostrarError("No se pudo actualizar el cliente.");
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
            if (clienteService.eliminar(seleccionado.getId())) {
                mostrarExito("Cliente eliminado correctamente.");
                refrescarTabla();
            } else {
                mostrarError("No se pudo eliminar el cliente.");
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

    private void limpiarMensaje() { if (lblMensaje != null) lblMensaje.setText(""); }
    private void mostrarError(String mensaje) {
        if (lblMensaje != null) { lblMensaje.setTextFill(Color.web("#cc0000")); lblMensaje.setText(mensaje); }
    }
    private void mostrarExito(String mensaje) {
        if (lblMensaje != null) { lblMensaje.setTextFill(Color.web("#28a745")); lblMensaje.setText(mensaje); }
    }
}