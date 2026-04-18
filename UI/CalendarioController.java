package ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
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
import javafx.scene.control.ListCell;
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
import model.Cliente;
import model.Conductor;
import model.Viaje;
import service.ClienteService;
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
    @FXML private TableColumn<Viaje, String>   colHoraFin;
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
    private final ClienteService   clienteService   = new ClienteService();

    private Admin     adminLogueado;
    private YearMonth mesActual       = YearMonth.now();
    private LocalDate diaSeleccionado;

    private final Map<Integer, String> coloresConductores = new HashMap<>();

    // ── Cache del mes ─────────────────────────────────────────────────────────
    private List<Viaje>   viajesDelMes = List.of();
    private YearMonth     mesCargado   = null;

    // ── Cache de clientes (se carga una vez por sesión del diálogo) ───────────
    private List<Cliente> clientesCache = null;

    // ── Inicializacion ────────────────────────────────────────────────────────

    public void setAdmin(Admin admin) {
        this.adminLogueado = admin;
    }

    @FXML
    public void initialize() {

        colHora.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getHora() != null
                    ? data.getValue().getHora().format(FMT_HORA) : ""));
        colHoraFin.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getHoraFinalizacion() != null
                    ? data.getValue().getHoraFinalizacion().format(FMT_HORA) : ""));
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

        mostrarCargando("Cargando...");
        Task<List<Conductor>> taskConductores = new Task<>() {
            @Override protected List<Conductor> call() {
                return conductorService.listarTodos();
            }
        };
        taskConductores.setOnSucceeded(e -> {
            List<Conductor> conductores = taskConductores.getValue();
            asignarColores(conductores);

            List<Conductor> items = new java.util.ArrayList<>();
            items.add(TODOS);
            items.addAll(conductores);
            comboConductor.setItems(FXCollections.observableArrayList(items));

            comboConductor.setCellFactory(lv -> new ListCell<>() {
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

            comboConductor.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Conductor c, boolean empty) {
                    super.updateItem(c, empty);
                    if (empty || c == null) { setText(null); return; }
                    setText(c == TODOS ? "Todos los conductores" : c.getNombre() + " — " + c.getMatricula());
                }
            });

            comboConductor.valueProperty().addListener((obs2, ant, nuevo) -> {
                if (nuevo != null) {
                    diaSeleccionado = null;
                    panelViajes.setVisible(false);
                    panelViajes.setManaged(false);
                    cargarMesYDibujar();
                }
            });

            comboConductor.getSelectionModel().selectFirst();
            actualizarLabelMes();
        });
        taskConductores.setOnFailed(e -> mostrarError("Error al cargar conductores."));
        new Thread(taskConductores, "task-conductores").start();
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
        diaSeleccionado = null;
        panelViajes.setVisible(false);
        panelViajes.setManaged(false);
        cargarMesYDibujar();
    }

    @FXML
    private void handlerMesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        diaSeleccionado = null;
        panelViajes.setVisible(false);
        panelViajes.setManaged(false);
        cargarMesYDibujar();
    }

    private void cargarMesYDibujar() {
        if (mesActual.equals(mesCargado)) {
            dibujarCalendario();
            return;
        }

        mostrarCargando("Cargando calendario...");
        gridCalendario.getChildren().clear();
        actualizarLabelMes();

        YearMonth mesACargar = mesActual;
        Task<List<Viaje>> task = new Task<>() {
            @Override protected List<Viaje> call() {
                return viajeService.listarPorMes(mesACargar.getYear(), mesACargar.getMonthValue());
            }
        };
        task.setOnSucceeded(e -> {
            viajesDelMes = task.getValue();
            mesCargado   = mesACargar;
            limpiarMensaje();
            dibujarCalendario();
            if (diaSeleccionado != null) actualizarTablaDesdeCache(diaSeleccionado);
        });
        task.setOnFailed(e -> mostrarError("Error al cargar los viajes."));
        new Thread(task, "task-mes").start();
    }

    private void dibujarCalendario() {
        gridCalendario.getChildren().clear();
        actualizarLabelMes();
        if (esModoTodos()) dibujarCalendarioTodos();
        else               dibujarCalendarioConductor(comboConductor.getValue());
    }

    private void dibujarCalendarioConductor(Conductor conductor) {
        Set<LocalDate> diasConViajes = viajesDelMes.stream()
            .filter(v -> v.getConductor() != null && v.getConductor().getId() == conductor.getId())
            .map(Viaje::getDia)
            .filter(d -> d != null)
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
        Map<LocalDate, List<Conductor>> conductoresPorDia = new HashMap<>();
        for (Viaje v : viajesDelMes) {
            if (v.getDia() == null || v.getConductor() == null) continue;
            conductoresPorDia
                .computeIfAbsent(v.getDia(), d -> new java.util.ArrayList<>())
                .add(v.getConductor());
        }

        iterarDias((dia, fecha, col, fila) -> {
            List<Conductor> conViaje = conductoresPorDia.getOrDefault(fecha, List.of());
            StackPane celda = crearCeldaTodos(dia, fecha, conViaje,
                fecha.equals(LocalDate.now()),
                fecha.equals(diaSeleccionado),
                esFinDeSemana(fecha));
            gridCalendario.add(celda, col, fila);
        });
    }

    // ── Celdas del calendario ─────────────────────────────────────────────────

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

    // ── Selección de día y tabla ──────────────────────────────────────────────

    private void seleccionarDia(LocalDate fecha) {
        diaSeleccionado = fecha;
        dibujarCalendario();
        panelViajes.setVisible(true);
        panelViajes.setManaged(true);
        String nombreMes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
        lblDiaSeleccionado.setText("Viajes del " + fecha.getDayOfMonth() + " de " + nombreMes);
        colConductor.setVisible(esModoTodos());
        actualizarTablaDesdeCache(fecha);
    }

    private void actualizarTablaDesdeCache(LocalDate fecha) {
        List<Viaje> viajesDia;
        if (esModoTodos()) {
            viajesDia = viajesDelMes.stream()
                .filter(v -> fecha.equals(v.getDia()))
                .sorted(Comparator.comparing(v -> v.getHora() != null ? v.getHora() : LocalTime.MAX))
                .collect(Collectors.toList());
            aplicarRowFactoryColores();
        } else {
            int conductorId = comboConductor.getValue().getId();
            viajesDia = viajesDelMes.stream()
                .filter(v -> fecha.equals(v.getDia())
                          && v.getConductor() != null
                          && v.getConductor().getId() == conductorId)
                .sorted(Comparator.comparing(v -> v.getHora() != null ? v.getHora() : LocalTime.MAX))
                .collect(Collectors.toList());
        }
        tablaViajes.setItems(FXCollections.observableArrayList(viajesDia));
        tablaViajes.getSelectionModel().clearSelection();
        limpiarMensaje();
    }

    private void aplicarRowFactoryColores() {
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

    // ── Handlers de viaje ─────────────────────────────────────────────────────

    @FXML
    private void handlerNuevoViaje() {
        if (diaSeleccionado == null) return;
        if (esModoTodos()) {
            mostrarDialogoConductor().ifPresent(conductor ->
                mostrarDialogoViaje(null).ifPresent(viaje -> {
                    viaje.setDia(diaSeleccionado);
                    if (viajeService.crear(viaje, conductor.getId())) {
                        mostrarExito("Viaje anadido correctamente.");
                        invalidarCacheYRecargar();
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
                    mostrarExito("Viaje anadido correctamente.");
                    invalidarCacheYRecargar();
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
                invalidarCacheYRecargar();
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
        alert.setHeaderText("Eliminar este viaje?");
        alert.setContentText("Recogida: " + seleccionado.getPuntorecogida()
            + "\nHora inicio: " + (seleccionado.getHora() != null
                ? seleccionado.getHora().format(FMT_HORA) : "-")
            + "\nHora fin: " + (seleccionado.getHoraFinalizacion() != null
                ? seleccionado.getHoraFinalizacion().format(FMT_HORA) : "-"));
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (viajeService.eliminar(seleccionado.getId())) {
                mostrarExito("Viaje eliminado correctamente.");
                invalidarCacheYRecargar();
            } else {
                mostrarError("Error al eliminar el viaje.");
            }
        });
    }

    private void invalidarCacheYRecargar() {
        mesCargado = null;
        cargarMesYDibujar();
    }

    // ── Diálogo selección de conductor ────────────────────────────────────────

    private Optional<Conductor> mostrarDialogoConductor() {
        List<Conductor> conductores = conductorService.listarTodos();
        if (conductores.isEmpty()) {
            mostrarError("No hay conductores registrados.");
            return Optional.empty();
        }
        Dialog<Conductor> dialogo = new Dialog<>();
        dialogo.setTitle("Seleccionar conductor");
        dialogo.setHeaderText("Para que conductor es el viaje del " + diaSeleccionado + "?");
        ButtonType btnOk     = new ButtonType("Continuar", ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar",  ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnOk, btnCancel);
        ComboBox<Conductor> combo = new ComboBox<>(FXCollections.observableArrayList(conductores));
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Conductor c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre() + " — " + c.getMatricula());
            }
        });
        combo.setButtonCell(new ListCell<>() {
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

    // ── Diálogo crear/editar viaje CON ComboBox de clientes ──────────────────

    private Optional<Viaje> mostrarDialogoViaje(Viaje existente) {

        // Cargar clientes (cache por sesión de la pantalla)
        if (clientesCache == null) {
            try {
                clientesCache = clienteService.listarTodos();
            } catch (Exception e) {
                clientesCache = List.of();
            }
        }

        Dialog<Viaje> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Nuevo viaje" : "Editar viaje");
        dialogo.setHeaderText(existente == null
            ? "Anadir viaje para el " + diaSeleccionado
            : "Editando viaje del " + diaSeleccionado);

        ButtonType btnGuardar  = new ButtonType("Guardar",  ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        // ── Estilos ───────────────────────────────────────────────────────────
        String inputOk  = "-fx-background-color: #f8f9fa; -fx-padding: 8; "
            + "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-pref-width: 280px;";
        String inputErr = "-fx-background-color: #fff0f0; -fx-padding: 8; "
            + "-fx-border-color: #cc0000; -fx-border-radius: 5; -fx-pref-width: 280px;";

        // ── Campos de hora y dirección ────────────────────────────────────────
        TextField txtHora     = new TextField(existente != null && existente.getHora() != null
                                    ? existente.getHora().format(FMT_HORA) : "");
        TextField txtHoraFin  = new TextField(existente != null && existente.getHoraFinalizacion() != null
                                    ? existente.getHoraFinalizacion().format(FMT_HORA) : "");
        TextField txtRecogida = new TextField(existente != null ? existente.getPuntorecogida() : "");
        TextField txtDejada   = new TextField(existente != null ? existente.getPuntodejada()   : "");

        txtHora.setPromptText("HH:mm  (Ej: 09:30)");
        txtHoraFin.setPromptText("HH:mm  (Ej: 10:15)");
        txtRecogida.setPromptText("Direccion de recogida");
        txtDejada.setPromptText("Direccion de dejada");

        for (TextField tf : new TextField[]{txtHora, txtHoraFin, txtRecogida, txtDejada})
            tf.setStyle(inputOk);

        // ── ComboBox de clientes (editable/filtrable) ─────────────────────────
        // Usamos un ComboBox editable: el usuario puede escribir para filtrar
        // o seleccionar directamente de la lista desplegable.
        ComboBox<Cliente> comboCliente = new ComboBox<>();
        comboCliente.setEditable(true);
        comboCliente.setPrefWidth(280);
        comboCliente.setPromptText("Escribe o selecciona un cliente...");
        comboCliente.setStyle(inputOk.replace("-fx-pref-width: 280px;", ""));

        // Lista filtrable basada en lo que escribe el usuario
        FilteredList<Cliente> clientesFiltrados =
            new FilteredList<>(FXCollections.observableArrayList(clientesCache), c -> true);
        comboCliente.setItems(clientesFiltrados);

        // Cómo se muestra cada cliente en la lista desplegable
        comboCliente.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Cliente c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); return; }
                setText(c.getNombre() + " — " + c.getTelefono());
            }
        });

        // Cómo se muestra el cliente seleccionado en el campo del combo
        comboCliente.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Cliente c) {
                return c == null ? "" : c.getNombre() + " — " + c.getTelefono();
            }
            @Override public Cliente fromString(String s) {
                // Busca coincidencia exacta por el texto mostrado
                return clientesCache.stream()
                    .filter(c -> (c.getNombre() + " — " + c.getTelefono()).equals(s))
                    .findFirst().orElse(null);
            }
        });

        // Filtrar la lista mientras el usuario escribe
        comboCliente.getEditor().textProperty().addListener((obs, anterior, texto) -> {
            // Si el texto corresponde a un cliente ya seleccionado, no filtrar
            Cliente seleccionado = comboCliente.getValue();
            if (seleccionado != null) {
                String textoEsperado = seleccionado.getNombre() + " — " + seleccionado.getTelefono();
                if (textoEsperado.equals(texto)) return;
            }
            String filtro = texto == null ? "" : texto.toLowerCase();
            clientesFiltrados.setPredicate(c ->
                filtro.isEmpty()
                || c.getNombre().toLowerCase().contains(filtro)
                || c.getTelefono().contains(filtro));
            // Mostrar el desplegable si hay texto
            if (!filtro.isEmpty() && !comboCliente.isShowing()) {
                comboCliente.show();
            }
        });

        // ── Campo de teléfono (se rellena al seleccionar cliente) ─────────────
        TextField txtTelefono = new TextField(existente != null ? existente.getTelefonocliente() : "");
        txtTelefono.setPromptText("Ej: 612345678");
        txtTelefono.setStyle(inputOk);

        // Al seleccionar un cliente del combo → rellenar teléfono automáticamente
        comboCliente.valueProperty().addListener((obs, anterior, clienteSeleccionado) -> {
            if (clienteSeleccionado != null) {
                txtTelefono.setText(clienteSeleccionado.getTelefono());
            }
        });

        // Si estamos editando y el teléfono coincide con algún cliente, preseleccionarlo
        if (existente != null && existente.getTelefonocliente() != null) {
            String telExistente = existente.getTelefonocliente();
            clientesCache.stream()
                .filter(c -> telExistente.equals(c.getTelefono()))
                .findFirst()
                .ifPresent(comboCliente::setValue);
        }

        // ── Labels de error ───────────────────────────────────────────────────
        Label lblErrHora     = new Label();
        Label lblErrHoraFin  = new Label();
        Label lblErrTelefono = new Label();
        lblErrHora.setTextFill(Color.web("#cc0000"));
        lblErrHoraFin.setTextFill(Color.web("#cc0000"));
        lblErrTelefono.setTextFill(Color.web("#cc0000"));
        lblErrHora.setFont(Font.font(10));
        lblErrHoraFin.setFont(Font.font(10));
        lblErrTelefono.setFont(Font.font(10));

        // ── Validaciones en tiempo real ───────────────────────────────────────
        txtHora.textProperty().addListener((obs, ant, val) -> {
            String v = val.trim();
            if (v.isEmpty() || v.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
                txtHora.setStyle(inputOk); lblErrHora.setText("");
            } else {
                txtHora.setStyle(inputErr);
                lblErrHora.setText("Formato invalido. Usa HH:mm (ej: 09:30, 14:00)");
            }
        });

        txtHoraFin.textProperty().addListener((obs, ant, val) -> {
            String v = val.trim();
            if (v.isEmpty() || v.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
                txtHoraFin.setStyle(inputOk); lblErrHoraFin.setText("");
            } else {
                txtHoraFin.setStyle(inputErr);
                lblErrHoraFin.setText("Formato invalido. Usa HH:mm (ej: 10:15, 17:45)");
            }
        });

        txtTelefono.textProperty().addListener((obs, ant, val) -> {
            if (!val.matches("[+0-9]*")) { txtTelefono.setText(ant); return; }
            String v = val.trim();
            if (v.isEmpty() || v.matches("^(\\+34)?[6789][0-9]{8}$")) {
                txtTelefono.setStyle(inputOk); lblErrTelefono.setText("");
            } else {
                txtTelefono.setStyle(inputErr);
                lblErrTelefono.setText("Telefono invalido. Ej: 612345678 o +34612345678");
            }
        });

        // ── Grid del diálogo ──────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setPadding(new Insets(20, 20, 10, 20));

        grid.add(new Label("Hora inicio:"), 0, 0);  grid.add(txtHora,      1, 0);
        grid.add(lblErrHora,                1, 1);
        grid.add(new Label("Hora fin:"),    0, 2);  grid.add(txtHoraFin,   1, 2);
        grid.add(lblErrHoraFin,             1, 3);
        grid.add(new Label("Recogida:"),    0, 4);  grid.add(txtRecogida,  1, 4);
        grid.add(new Label("Dejada:"),      0, 5);  grid.add(txtDejada,    1, 5);
        // ── Fila nueva: selector de cliente ───────────────────────────────────
        grid.add(new Label("Cliente:"),     0, 6);  grid.add(comboCliente, 1, 6);
        // ── Fila teléfono (se rellena automáticamente) ────────────────────────
        grid.add(new Label("Telefono:"),    0, 7);  grid.add(txtTelefono,  1, 7);
        grid.add(lblErrTelefono,            1, 8);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        dialogo.getDialogPane().setPrefWidth(480);
        dialogo.getDialogPane().setPrefHeight(480);

        // ── Bloquear Guardar si hay errores ───────────────────────────────────
        javafx.scene.Node guardarBtn = dialogo.getDialogPane().lookupButton(btnGuardar);
        Runnable checkValido = () -> {
            boolean horaOk    = txtHora.getText().trim().isEmpty()
                             || txtHora.getText().trim().matches("^([01][0-9]|2[0-3]):[0-5][0-9]$");
            boolean horaFinOk = txtHoraFin.getText().trim().isEmpty()
                             || txtHoraFin.getText().trim().matches("^([01][0-9]|2[0-3]):[0-5][0-9]$");
            boolean telefonoOk = txtTelefono.getText().trim().isEmpty()
                             || txtTelefono.getText().trim().matches("^(\\+34)?[6789][0-9]{8}$");
            boolean rangoHorasOk = true;
            if (horaOk && horaFinOk
                && !txtHora.getText().trim().isEmpty()
                && !txtHoraFin.getText().trim().isEmpty()) {
                LocalTime inicio = LocalTime.parse(txtHora.getText().trim(), FMT_HORA);
                LocalTime fin    = LocalTime.parse(txtHoraFin.getText().trim(), FMT_HORA);
                rangoHorasOk = fin.isAfter(inicio);
            }
            if (!rangoHorasOk) {
                txtHoraFin.setStyle(inputErr);
                lblErrHoraFin.setText("La hora de fin debe ser posterior a la de inicio.");
            } else if (horaFinOk && !txtHoraFin.getText().trim().isEmpty()) {
                txtHoraFin.setStyle(inputOk);
                lblErrHoraFin.setText("");
            }
            guardarBtn.setDisable(!horaOk || !horaFinOk || !telefonoOk || !rangoHorasOk);
        };
        txtHora.textProperty().addListener((obs, a, b) -> checkValido.run());
        txtHoraFin.textProperty().addListener((obs, a, b) -> checkValido.run());
        txtTelefono.textProperty().addListener((obs, a, b) -> checkValido.run());
        checkValido.run();

        // ── Construir el Viaje al guardar ─────────────────────────────────────
        dialogo.setResultConverter(boton -> {
            if (boton != btnGuardar) return null;
            Viaje v = new Viaje();
            try {
                if (!txtHora.getText().trim().isEmpty())
                    v.setHora(LocalTime.parse(txtHora.getText().trim(), FMT_HORA));
                if (!txtHoraFin.getText().trim().isEmpty())
                    v.setHoraFinalizacion(LocalTime.parse(txtHoraFin.getText().trim(), FMT_HORA));
            } catch (Exception ignored) {}
            v.setPuntorecogida(txtRecogida.getText().trim());
            v.setPuntodejada(txtDejada.getText().trim());
            v.setTelefonocliente(txtTelefono.getText().trim());
            return v;
        });

        return dialogo.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private void mostrarCargando(String msg) {
        if (lblMensaje != null) {
            lblMensaje.setTextFill(Color.web("#888888"));
            lblMensaje.setText(msg);
        }
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