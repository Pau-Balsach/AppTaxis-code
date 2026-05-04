package ui;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * Campo de texto JavaFX con autocompletado de direcciones reales (Nominatim)
 * y botón para abrir la dirección en Google Maps.
 *
 * Equivalente combinado de PlacesAutocompleteField.dart + maps_launcher.dart
 *
 * Uso en CalendarioController:
 *   AddressAutocompleteField recogida = new AddressAutocompleteField("Dirección de recogida", valorInicial);
 *   AddressAutocompleteField dejada   = new AddressAutocompleteField("Dirección de dejada", valorInicial);
 *   grid.add(recogida, 1, fila);
 *
 *   // Al guardar:
 *   PlaceResult r = recogida.getResult();  // null si el usuario escribió a mano sin seleccionar
 *   String dir   = recogida.getText();
 *   Double lat   = r != null ? r.getLat() : null;
 */
public class AddressAutocompleteField extends HBox {

    // ── Constantes ────────────────────────────────────────────────────────────
    private static final int    DEBOUNCE_MS  = 400;
    private static final int    MAX_VISIBLE  = 5;
    private static final double CELL_HEIGHT  = 36.0;
    private static final double POPUP_WIDTH  = 360.0;

    // ── Componentes UI ────────────────────────────────────────────────────────
    private final TextField           textField = new TextField();
    private final javafx.scene.control.Button btnMapa;
    private final Popup               popup     = new Popup();
    private final ListView<PlaceResult> listView  = new ListView<>();

    // ── Estado ────────────────────────────────────────────────────────────────
    private PlaceResult       resultado   = null;   // último resultado seleccionado
    private PauseTransition   debounce;
    private final NominatimService nominatim = new NominatimService();

    // ── Constructor ───────────────────────────────────────────────────────────
    public AddressAutocompleteField(String placeholder, String valorInicial) {
        super(6); // spacing entre TextField y botón

        // Campo de texto
        textField.setPromptText(placeholder);
        textField.setText(valorInicial != null ? valorInicial : "");
        textField.setStyle("-fx-pref-width: 260px;");
        HBox.setHgrow(textField, Priority.ALWAYS);

        // Botón del mapa
        btnMapa = new javafx.scene.control.Button("🗺");
        btnMapa.setTooltip(new Tooltip("Abrir en Google Maps"));
        btnMapa.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: #f0f0f0;"
            + " -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4;");
        btnMapa.setOnAction(e -> abrirEnMaps());

        getChildren().addAll(textField, btnMapa);

        // ListView del popup
        listView.setPrefWidth(POPUP_WIDTH);
        listView.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 4;");
        listView.setCellFactory(lv -> new PlaceCell());

        popup.getContent().add(listView);
        popup.setAutoHide(true);

        // Debounce al escribir
        debounce = new PauseTransition(Duration.millis(DEBOUNCE_MS));
        debounce.setOnFinished(e -> buscarAsync(textField.getText()));

        textField.textProperty().addListener((obs, anterior, nuevo) -> {
            // Si el usuario escribe, invalidamos el resultado guardado
            if (resultado != null && !nuevo.equals(resultado.getDireccion())) {
                resultado = null;
            }
            if (nuevo == null || nuevo.trim().length() < 3) {
                cerrarPopup();
                return;
            }
            debounce.playFromStart();
        });

        // Selección con teclado en el popup
        textField.setOnKeyPressed(e -> {
            if (!popup.isShowing()) return;
            if (e.getCode() == KeyCode.DOWN) {
                listView.requestFocus();
                listView.getSelectionModel().selectFirst();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cerrarPopup();
            }
        });

        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                seleccionar(listView.getSelectionModel().getSelectedItem());
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cerrarPopup();
                textField.requestFocus();
            }
        });

        listView.setOnMouseClicked(e -> {
            PlaceResult sel = listView.getSelectionModel().getSelectedItem();
            if (sel != null) seleccionar(sel);
        });

        // Cierra el popup si el campo pierde el foco (y no va hacia el popup)
        textField.focusedProperty().addListener((obs, tenia, tiene) -> {
            if (!tiene) {
                // Pequeño delay para permitir el click en el popup
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(ev -> {
                    if (!listView.isFocused()) cerrarPopup();
                });
                delay.play();
            }
        });
    }

    // ── API pública ────────────────────────────────────────────────────────────

    /** Texto actual del campo (lo que ve el usuario). */
    public String getText() {
        return textField.getText();
    }

    /**
     * PlaceResult seleccionado (con lat/lng), o null si el usuario
     * escribió a mano sin elegir de la lista.
     */
    public PlaceResult getResult() {
        return resultado;
    }

    /** Permite pre-rellenar el campo (al editar un viaje existente). */
    public void setText(String texto) {
        textField.setText(texto != null ? texto : "");
        resultado = null;
    }

    // ── Búsqueda asíncrona ─────────────────────────────────────────────────────

    private void buscarAsync(String query) {
        Task<List<PlaceResult>> task = new Task<>() {
            @Override
            protected List<PlaceResult> call() {
                return nominatim.buscar(query);
            }
        };
        task.setOnSucceeded(e -> {
            List<PlaceResult> lista = task.getValue();
            if (lista == null || lista.isEmpty()) {
                cerrarPopup();
                return;
            }
            listView.getItems().setAll(lista);
            double altura = Math.min(lista.size(), MAX_VISIBLE) * CELL_HEIGHT + 4;
            listView.setPrefHeight(altura);
            mostrarPopup();
        });
        task.setOnFailed(e -> cerrarPopup());
        new Thread(task, "nominatim-search").start();
    }

    // ── Popup ─────────────────────────────────────────────────────────────────

    private void mostrarPopup() {
        Platform.runLater(() -> {
            if (!textField.isVisible() || textField.getScene() == null) return;
            // Calculamos la posición justo debajo del TextField
            Point2D pos = textField.localToScreen(0, textField.getHeight());
            if (pos == null) return;
            popup.show(textField, pos.getX(), pos.getY());
        });
    }

    private void cerrarPopup() {
        Platform.runLater(popup::hide);
    }

    // ── Selección ─────────────────────────────────────────────────────────────

    private void seleccionar(PlaceResult place) {
        if (place == null) return;
        resultado = place;
        textField.setText(place.getDireccion());
        textField.positionCaret(place.getDireccion().length());
        cerrarPopup();
        textField.requestFocus();
    }

    // ── Abrir en Google Maps ───────────────────────────────────────────────────

    /**
     * Equivalente a maps_launcher.dart:
     * - Si hay resultado con coordenadas → Google Maps por lat/lng
     * - Si solo hay texto → búsqueda por nombre
     */
    private void abrirEnMaps() {
        try {
            String url;
            if (resultado != null) {
                url = "https://www.google.com/maps/search/?api=1&query="
                    + resultado.getLat() + "," + resultado.getLng();
            } else {
                String texto = textField.getText().trim();
                if (texto.isEmpty()) return;
                url = "https://www.google.com/maps/search/?api=1&query="
                    + URI.create(texto.replace(" ", "+")).toASCIIString()
                        .replace("%2B", "+");
                // Codificación manual segura:
                url = "https://www.google.com/maps/search/?api=1&query="
                    + java.net.URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8);
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Celda del ListView ─────────────────────────────────────────────────────

    private static class PlaceCell extends javafx.scene.control.ListCell<PlaceResult> {
        private final javafx.scene.control.Label titulo    = new javafx.scene.control.Label();
        private final javafx.scene.control.Label subtitulo = new javafx.scene.control.Label();
        private final javafx.scene.layout.VBox   caja      = new javafx.scene.layout.VBox(1, titulo, subtitulo);
        private final javafx.scene.control.Label icono     = new javafx.scene.control.Label("📍");
        private final HBox fila;

        PlaceCell() {
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            subtitulo.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
            subtitulo.setMaxWidth(300);
            icono.setStyle("-fx-font-size: 14px;");
            fila = new HBox(8, icono, caja);
            fila.setStyle("-fx-padding: 4 8 4 8; -fx-alignment: center-left;");
        }

        @Override
        protected void updateItem(PlaceResult place, boolean empty) {
            super.updateItem(place, empty);
            if (empty || place == null) {
                setGraphic(null);
                return;
            }
            // Dividimos "Calle X, Ciudad, Provincia, País" en título y subtítulo
            String[] partes = place.getDireccion().split(", ", 3);
            titulo.setText(partes.length >= 2 ? partes[0] + ", " + partes[1] : partes[0]);
            subtitulo.setText(partes.length >= 3 ? partes[2] : "");
            setGraphic(fila);
        }
    }
}