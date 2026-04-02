package ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Admin;
import model.Conductor;
import model.Viaje;
import service.ConductorService;
import service.ViajeService;

public class CalendarioController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private ComboBox<Conductor>          comboConductor;
    @FXML private Label                        lblMesAnio;
    @FXML private GridPane                     gridCalendario;
    @FXML private VBox                         panelViajes;
    @FXML private Label                        lblDiaSeleccionado;
    @FXML private TableView<Viaje>             tablaViajes;
    @FXML private TableColumn<Viaje, String>   colHora;
    @FXML private TableColumn<Viaje, String>   colRecogida;
    @FXML private TableColumn<Viaje, String>   colDejada;
    @FXML private TableColumn<Viaje, String>   colTelefono;
    @FXML private TableColumn<Viaje, String>   colConductor;
    @FXML private Button                       btnEditarViaje;
    @FXML private Button                       btnEliminarViaje;
    @FXML private Label                        lblMensaje;

    // ── Conductor centinela para "Todos" ──────────────────────────────────────
    private static final Conductor TODOS = new Conductor() {
        @Override public String getNombre()    { return "Todos los conductores"; }
        @Override public String getMatricula() { return ""; }
    };

    // ── Paleta de colores ─────────────────────────────────────────────────────
    private static final String[] PALETA = {
        "#e74c3c", "#3498db", "#2ecc71", "#9b59b6",
        "#f39c12", "#1abc9c", "#e67e22", "#2980b9",
        "#27ae60", "#8e44ad", "#d35400", "#16a085"
    };

    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ConductorService conductorService = new ConductorService();
    private final ViajeService     viajeService     = new ViajeService();

    private Admin     adminLogueado;
    private YearMonth mesActual       = YearMonth.now();
    private LocalDate diaSeleccionado;

    private final Map<Integer, String> coloresConductores = new HashMap<>();

    // ── Inicialización ────────────────────────────────────────────────────────

    public void setAdmin(Admin admin) {
        this.adminLogueado = admin;
    }

    @FXML
    public void initialize() {

        // Columnas de la tabla
        colHora.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getHora() != null
                    ? data.getValue().getHora().format(FMT_HORA) : ""));
        colRecogida.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPuntorecogida()));
        colDejada.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPuntodejada()));
        colTelefono.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTelefonocliente()));
        colConductor.setCellValueFactory(data -> {
            Conductor c = data.getValue().getConductor();
            return new javafx.beans.property.SimpleStringProperty(
                c != null ? c.getNombre() : "");
        });

        tablaViajes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaViajes.getSelectionModel().selectedItemProperty().addListener(
            (obs, ant, sel) -> {
                btnEditarViaje.setDisable(sel == null);
                btnEliminarViaje.setDisable(sel == null);
            });

        // Cargar conductores y asignar colores
        List<Conductor> conductores = conductorService.listarTodos();
        asignarColores(conductores);

        // Combo: "Todos" primero, luego cada conductor
        List<Conductor> itemsCombo = new java.util.ArrayList<>();
        itemsCombo.add(TODOS);
        itemsCombo.addAll(conductores);
        comboConductor.setItems(FXCollections.observableArrayList(itemsCombo));

        comboConductor.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Conductor c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); setStyle(""); return; }
                if (c == TODOS) {
                    setText("Todos los conductores");
                    setStyle("-fx-font-weight: bold;");
                } else {
                    String color = coloresConductores.getOrDefault(c.getId(), "#333333");
                    setText(c.getNombre() + " — " + c.getMatricula());
                    setStyle("-fx-text-fill: " + color + ";");
                }
            }
        });

        comboConductor.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Conductor c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); return; }
                setText(c == TODOS ? "Todos los conductores" : c.getNombre() + " — " + c.getMatricula());
            }
        });

        comboConductor.valueProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                diaSeleccionado = null;
                panelViajes.setVisible(false);
                panelViajes.setManaged(false);
                dibujarCalendario();
            }
        });

        comboConductor.getSelectionModel().selectFirst();
        actualizarLabelMes();
    }

    // ── Colores ───────────────────────────────────────────────────────────────

    private void asignarColores(List<Conductor> conductores) {
        coloresConductores.clear();
        for (int i = 0; i < conductores.size(); i++) {
            coloresConductores.put(conductores.get(i).getId(), PALETA[i % PALETA.length]);
        }
    }

    // ── Navegación de mes ─────────────────────────────────────────────────────

    @FXML
    private void handlerMesAnterior() {
        mesActual = mesActual.minusMonths(1);
        resetPanel();
        dibujarCalendario();
    }

    @FXML
    private void handlerMesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        resetPanel();
        dibujarCalendario();
    }

    private void resetPanel() {
        diaSeleccionado = null;
        panelViajes.setVisible(false);
        panelViajes.setManaged(false);
    }

    // ── Dibujo del calendario ─────────────────────────────────────────────────

    private void dibujarCalendario() {
        gridCalendario.getChildren().clear();
        actualizarLabelMes();
        if (esModoTodos()) dibujarCalendarioTodos();
        else               dibujarCalendarioConductor(comboConductor.getValue());
    }

    private void dibujarCalendarioConductor(Conductor conductor) {
        Set<LocalDate> diasConViajes = viajeService.listarPorConductor(conductor.getId()).stream()
            .map(Viaje::getDia)
            .filter(d -> d != null && YearMonth.from(d).equals(mesActual))
            .collect(Collectors.toSet());

        iterarDias((dia, fecha, col, fila) -> {
            StackPane celda = crearCelda(dia, fecha,
                diasConViajes.contains(fecha),
                fecha.equals(LocalDate.now()),
                fecha.equals(diaSeleccionado),
                esFinDeSemana(fecha));
            gridCalendario.add(celda, col, fila);
        });
    }

    private void dibujarCalendarioTodos() {
        List<Conductor> conductores = conductorService.listarTodos();

        Map<Integer, Set<LocalDate>> diasPorConductor = new LinkedHashMap<>();
        for (Conductor c : conductores) {
            Set<LocalDate> dias = viajeService.listarPorConductor(c.getId()).stream()
                .map(Viaje::getDia)
                .filter(d -> d != null && YearMonth.from(d).equals(mesActual))
                .collect(Collectors.toSet());
            diasPorConductor.put(c.getId(), dias);
        }

        iterarDias((dia, fecha, col, fila) -> {
            List<Conductor> conViaje = conductores.stream()
                .filter(c -> diasPorConductor.getOrDefault(c.getId(), Set.of()).contains(fecha))
                .collect(Collectors.toList());

            StackPane celda = crearCeldaTodos(dia, fecha, conViaje,
                fecha.equals(LocalDate.now()),
                fecha.equals(diaSeleccionado),
                esFinDeSemana(fecha));
            gridCalendario.add(celda, col, fila);
        });
    }

    // ── Celdas ────────────────────────────────────────────────────────────────

    private StackPane crearCelda(int dia, LocalDate fecha, boolean tieneViajes,
                                  boolean esHoy, boolean esSeleccionado, boolean esFinDeSemana) {
        StackPane celda = baseCelda(dia, fecha, tieneViajes, esHoy, esSeleccionado, esFinDeSemana);

        if (tieneViajes) {
            Label punto = new Label("●");
            punto.setFont(Font.font(10));
            punto.setTextFill(esSeleccionado ? Color.WHITE : Color.web("#28a745"));
            StackPane.setAlignment(punto, Pos.BOTTOM_CENTER);
            punto.setTranslateY(-4);
            celda.getChildren().add(punto);
        }

        celda.setStyle(celda.getStyle() + " -fx-cursor: hand;");
        celda.setOnMouseClicked(e -> seleccionarDia(fecha));
        return celda;
    }

    private StackPane crearCeldaTodos(int dia, LocalDate fecha,
                                       List<Conductor> conductoresConViaje,
                                       boolean esHoy, boolean esSeleccionado, boolean esFinDeSemana) {
        boolean tieneViajes = !conductoresConViaje.isEmpty();
        StackPane celda = baseCelda(dia, fecha, tieneViajes, esHoy, esSeleccionado, esFinDeSemana);

        if (tieneViajes) {
            FlowPane puntos = new FlowPane();
            puntos.setHgap(2);
            puntos.setAlignment(Pos.CENTER);
            for (Conductor c : conductoresConViaje) {
                String color = esSeleccionado
                    ? "#ffffff"
                    : coloresConductores.getOrDefault(c.getId(), "#333333");
                Label punto = new Label("●");
                punto.setFont(Font.font(9));
                punto.setTextFill(Color.web(color));
                puntos.getChildren().add(punto);
            }
            StackPane.setAlignment(puntos, Pos.BOTTOM_CENTER);
            puntos.setTranslateY(-3);
            celda.getChildren().add(puntos);
        }

        celda.setStyle(celda.getStyle() + " -fx-cursor: hand;");
        celda.setOnMouseClicked(e -> seleccionarDia(fecha));
        return celda;
    }

    private StackPane baseCelda(int dia, LocalDate fecha, boolean tieneViajes,
                                 boolean esHoy, boolean esSeleccionado, boolean esFinDeSemana) {
        StackPane celda = new StackPane();
        celda.setMinSize(80, 60);
        celda.setMaxSize(80, 60);

        String fondo;
        if (esSeleccionado)   fondo = "-fx-background-color: #3a7bd5; -fx-background-radius: 8; -fx-border-color: #1a57b0; -fx-border-radius: 8;";
        else if (esHoy)       fondo = "-fx-background-color: #fff3cd; -fx-background-radius: 8; -fx-border-color: #ffcc00; -fx-border-radius: 8;";
        else if (tieneViajes) fondo = "-fx-background-color: #e8f5e9; -fx-background-radius: 8;";
        else                  fondo = "-fx-background-color: #f8f9fa; -fx-background-radius: 8;";
        celda.setStyle(fondo);

        Label numDia = new Label(String.valueOf(dia));
        numDia.setFont(Font.font("System", FontWeight.BOLD, 14));
        if (esSeleccionado)     numDia.setTextFill(Color.WHITE);
        else if (esFinDeSemana) numDia.setTextFill(Color.web(
            fecha.getDayOfWeek() == DayOfWeek.SUNDAY ? "#cc0000" : "#999999"));
        else                    numDia.setTextFill(Color.web("#1a1b2e"));
        StackPane.setAlignment(numDia, Pos.TOP_LEFT);
        numDia.setTranslateX(6);
        numDia.setTranslateY(4);
        celda.getChildren().add(numDia);
        return celda;
    }

    // ── Iterador de días ──────────────────────────────────────────────────────

    @FunctionalInterface
    private interface DiaConsumer {
        void accept(int dia, LocalDate fecha, int col, int fila);
    }

    private void iterarDias(DiaConsumer consumer) {
        int offset = mesActual.atDay(1).getDayOfWeek().getValue() - 1;
        int total  = mesActual.lengthOfMonth();
        int col = offset, fila = 0;
        for (int dia = 1; dia <= total; dia++) {
            consumer.accept(dia, mesActual.atDay(dia), col, fila);
            if (++col == 7) { col = 0; fila++; }
        }
    }

    private boolean esFinDeSemana(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SATURDAY
            || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    // ── Selección de día ──────────────────────────────────────────────────────

    private void seleccionarDia(LocalDate fecha) {
        diaSeleccionado = fecha;
        dibujarCalendario();

        panelViajes.setVisible(true);
        panelViajes.setManaged(true);

        String nombreMes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
        lblDiaSeleccionado.setText("Viajes del " + fecha.getDayOfMonth() + " de " + nombreMes);

        if (esModoTodos()) {
            colConductor.setVisible(true);
            cargarViajesTodosDia(fecha);
        } else {
            colConductor.setVisible(false);
            cargarViajesDelDia(comboConductor.getValue().getId(), fecha);
        }
    }

    private void cargarViajesDelDia(int conductorId, LocalDate fecha) {
        tablaViajes.setItems(FXCollections.observableArrayList(
            viajeService.listarPorFecha(conductorId, fecha)));
        tablaViajes.getSelectionModel().clearSelection();
        limpiarMensaje();
    }

    private void cargarViajesTodosDia(LocalDate fecha) {
        List<Viaje> todos = conductorService.listarTodos().stream()
            .flatMap(c -> viajeService.listarPorFecha(c.getId(), fecha).stream())
            .sorted(Comparator.comparing(v -> v.getHora() != null ? v.getHora() : LocalTime.MAX))
            .collect(Collectors.toList());

        tablaViajes.setItems(FXCollections.observableArrayList(todos));
        tablaViajes.getSelectionModel().clearSelection();
        limpiarMensaje();

        tablaViajes.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override protected void updateItem(Viaje v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setStyle(""); return; }
                if (isSelected()) {
                    setStyle("-fx-background-color: #1a57b0; -fx-text-fill: white; -fx-font-weight: bold;");
                    return;
                }
                if (v.getConductor() != null) {
                    String hex = coloresConductores.getOrDefault(v.getConductor().getId(), "#333333");
                    Color c = Color.web(hex);
                    int r = (int)(c.getRed()   * 255 * 0.12 + 255 * 0.88);
                    int g = (int)(c.getGreen() * 255 * 0.12 + 255 * 0.88);
                    int b = (int)(c.getBlue()  * 255 * 0.12 + 255 * 0.88);
                    setStyle("-fx-background-color: rgb(" + r + "," + g + "," + b + "); -fx-text-fill: #1a1b2e;");
                } else {
                    setStyle("");
                }
            }
            { selectedProperty().addListener((obs, was, now) -> {
                if (getItem() != null) updateItem(getItem(), false);
            }); }
        });
    }

    // ── CRUD Viajes ───────────────────────────────────────────────────────────

    @FXML
    private void handlerNuevoViaje() {
        if (diaSeleccionado == null) return;

        if (esModoTodos()) {
            mostrarDialogoConductor().ifPresent(conductor ->
                mostrarDialogoViaje(null).ifPresent(viaje -> {
                    viaje.setDia(diaSeleccionado);
                    if (viajeService.crear(viaje, conductor.getId())) {
                        mostrarExito("Viaje añadido correctamente.");
                        cargarViajesTodosDia(diaSeleccionado);
                        dibujarCalendario();
                    } else {
                        mostrarError("Error al guardar el viaje.");
                    }
                })
            );
        } else {
            if (comboConductor.getValue() == null) return;
            mostrarDialogoViaje(null).ifPresent(viaje -> {
                viaje.setDia(diaSeleccionado);
                if (viajeService.crear(viaje, comboConductor.getValue().getId())) {
                    mostrarExito("Viaje añadido correctamente.");
                    cargarViajesDelDia(comboConductor.getValue().getId(), diaSeleccionado);
                    dibujarCalendario();
                } else {
                    mostrarError("Error al guardar el viaje.");
                }
            });
        }
    }

    @FXML
    private void handlerEditarViaje() {
        Viaje seleccionado = tablaViajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        mostrarDialogoViaje(seleccionado).ifPresent(viaje -> {
            viaje.setId(seleccionado.getId());
            viaje.setDia(diaSeleccionado);
            if (viajeService.editar(viaje)) {
                mostrarExito("Viaje actualizado correctamente.");
                if (esModoTodos()) cargarViajesTodosDia(diaSeleccionado);
                else               cargarViajesDelDia(comboConductor.getValue().getId(), diaSeleccionado);
            } else {
                mostrarError("Error al actualizar el viaje.");
            }
        });
    }

    @FXML
    private void handlerEliminarViaje() {
        Viaje seleccionado = tablaViajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Eliminar viaje");
        alert.setHeaderText("¿Eliminar este viaje?");
        alert.setContentText("Recogida: " + seleccionado.getPuntorecogida()
            + "\nHora: " + (seleccionado.getHora() != null
                ? seleccionado.getHora().format(FMT_HORA) : "-"));

        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (viajeService.eliminar(seleccionado.getId())) {
                mostrarExito("Viaje eliminado correctamente.");
                if (esModoTodos()) cargarViajesTodosDia(diaSeleccionado);
                else               cargarViajesDelDia(comboConductor.getValue().getId(), diaSeleccionado);
                dibujarCalendario();
            } else {
                mostrarError("Error al eliminar el viaje.");
            }
        });
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    private Optional<Conductor> mostrarDialogoConductor() {
        List<Conductor> conductores = conductorService.listarTodos();
        if (conductores.isEmpty()) {
            mostrarError("No hay conductores registrados.");
            return Optional.empty();
        }

        Dialog<Conductor> dialogo = new Dialog<>();
        dialogo.setTitle("Seleccionar conductor");
        dialogo.setHeaderText("¿Para qué conductor es el viaje del " + diaSeleccionado + "?");

        ButtonType btnOk     = new ButtonType("Continuar", ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar",  ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnOk, btnCancel);

        ComboBox<Conductor> combo = new ComboBox<>(FXCollections.observableArrayList(conductores));
        combo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Conductor c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre() + " — " + c.getMatricula());
            }
        });
        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Conductor c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre() + " — " + c.getMatricula());
            }
        });
        combo.getSelectionModel().selectFirst();
        combo.setPrefWidth(300);

        VBox box = new VBox(8, new Label("Conductor:"), combo);
        box.setPadding(new Insets(16));
        dialogo.getDialogPane().setContent(box);
        dialogo.setResultConverter(b -> b == btnOk ? combo.getValue() : null);
        return dialogo.showAndWait();
    }

    private Optional<Viaje> mostrarDialogoViaje(Viaje existente) {
        Dialog<Viaje> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Nuevo viaje" : "Editar viaje");
        dialogo.setHeaderText(existente == null
            ? "Añadir viaje para el " + diaSeleccionado
            : "Editando viaje del " + diaSeleccionado);

        ButtonType btnGuardar  = new ButtonType("Guardar",   ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar",  ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        String inputStyle = "-fx-background-color: #f8f9fa; -fx-padding: 8; "
            + "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-pref-width: 280px;";

        TextField txtHora     = new TextField(existente != null && existente.getHora() != null
                                    ? existente.getHora().format(FMT_HORA) : "");
        TextField txtRecogida = new TextField(existente != null ? existente.getPuntorecogida() : "");
        TextField txtDejada   = new TextField(existente != null ? existente.getPuntodejada()   : "");
        TextField txtTelefono = new TextField(existente != null ? existente.getTelefonocliente(): "");

        txtHora.setPromptText("HH:mm  (Ej: 09:30)");
        txtRecogida.setPromptText("Dirección de recogida");
        txtDejada.setPromptText("Dirección de dejada");
        txtTelefono.setPromptText("Teléfono cliente");

        for (TextField tf : new TextField[]{txtHora, txtRecogida, txtDejada, txtTelefono})
            tf.setStyle(inputStyle);

        grid.add(new Label("Hora:"),     0, 0); grid.add(txtHora,     1, 0);
        grid.add(new Label("Recogida:"), 0, 1); grid.add(txtRecogida, 1, 1);
        grid.add(new Label("Dejada:"),   0, 2); grid.add(txtDejada,   1, 2);
        grid.add(new Label("Teléfono:"), 0, 3); grid.add(txtTelefono, 1, 3);

        dialogo.getDialogPane().setContent(grid);

        dialogo.setResultConverter(boton -> {
            if (boton != btnGuardar) return null;
            Viaje v = new Viaje();
            try {
                if (!txtHora.getText().trim().isEmpty())
                    v.setHora(LocalTime.parse(txtHora.getText().trim(), FMT_HORA));
            } catch (Exception ignored) {}
            v.setPuntorecogida(txtRecogida.getText().trim());
            v.setPuntodejada(txtDejada.getText().trim());
            v.setTelefonocliente(txtTelefono.getText().trim());
            return v;
        });

        return dialogo.showAndWait();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private boolean esModoTodos() {
        return comboConductor.getValue() == TODOS;
    }

    private void actualizarLabelMes() {
        String mes = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
        lblMesAnio.setText(mes.substring(0, 1).toUpperCase() + mes.substring(1)
            + " " + mesActual.getYear());
    }

    private void limpiarMensaje() {
        if (lblMensaje != null) lblMensaje.setText("");
    }

    private void mostrarError(String msg) {
        if (lblMensaje != null) {
            lblMensaje.setTextFill(Color.web("#cc0000"));
            lblMensaje.setText(msg);
        }
    }

    private void mostrarExito(String msg) {
        if (lblMensaje != null) {
            lblMensaje.setTextFill(Color.web("#28a745"));
            lblMensaje.setText(msg);
        }
    }

    @FXML
    private void handlerVolver() {
        try {
            StageConfigurator.showMenu(StageConfigurator.getCurrentStage(), adminLogueado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}