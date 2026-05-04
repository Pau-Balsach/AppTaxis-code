package ui;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que consulta la API gratuita de Nominatim (OpenStreetMap)
 * para autocompletar direcciones reales.
 *
 * Sin dependencias externas: usa solo HttpURLConnection nativo de Java.
 *
 * Equivalente al bloque _buscar() de PlacesAutocompleteField.dart
 */
public class NominatimService {

    private static final String BASE_URL    = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT  = "AppTaxis/1.0";
    private static final int    TIMEOUT_MS  = 5000;
    private static final int    MAX_RESULTS = 5;

    // Sesgo geográfico por defecto (Barcelona). Se puede ajustar en config.properties.
    // Radio aproximado: ~50 km → delta en grados
    private static final double DEFAULT_LAT       = 41.3851;
    private static final double DEFAULT_LNG       = 2.1734;
    private static final double DEFAULT_RADIUS_KM = 50.0;

    private final double biasLat;
    private final double biasLng;
    private final double radiusKm;

    /** Constructor con sesgo geográfico personalizado. */
    public NominatimService(double biasLat, double biasLng, double radiusKm) {
        this.biasLat  = biasLat;
        this.biasLng  = biasLng;
        this.radiusKm = radiusKm;
    }

    /** Constructor con valores por defecto (Barcelona). */
    public NominatimService() {
        this(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_RADIUS_KM);
    }

    /**
     * Busca sugerencias de dirección para la query dada.
     * Devuelve lista vacía si hay error o la query es muy corta.
     * Llamar siempre desde un hilo de fondo (Task/Thread), nunca desde el FX thread.
     */
    public List<PlaceResult> buscar(String query) {
        List<PlaceResult> resultados = new ArrayList<>();
        if (query == null || query.trim().length() < 3) return resultados;

        try {
            double delta   = radiusKm / 111.0;
            String viewbox = (biasLng - delta) + "," + (biasLat + delta)
                           + "," + (biasLng + delta) + "," + (biasLat - delta);

            String url = BASE_URL
                + "?q="            + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8)
                + "&format=json"
                + "&addressdetails=1"
                + "&limit="        + MAX_RESULTS
                + "&countrycodes=es"
                + "&viewbox="      + viewbox
                + "&bounded=0";    // prioriza el área pero no excluye fuera

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",      USER_AGENT);
            conn.setRequestProperty("Accept-Language", "es");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            int status = conn.getResponseCode();
            if (status != 200) return resultados;

            try (InputStream in = conn.getInputStream()) {
                String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                resultados  = parsearJson(json);
            }

        } catch (Exception e) {
            // Red no disponible o timeout: devolvemos lista vacía silenciosamente
        }

        return resultados;
    }

    /**
     * Parser JSON mínimo sin librerías externas.
     * Nominatim devuelve un array de objetos con al menos: display_name, lat, lon
     */
    private List<PlaceResult> parsearJson(String json) {
        List<PlaceResult> lista = new ArrayList<>();
        // Eliminamos los corchetes exteriores del array
        json = json.trim();
        if (!json.startsWith("[")) return lista;
        json = json.substring(1, json.lastIndexOf(']'));

        // Dividimos por objetos: cada objeto empieza en "{"
        String[] objetos = splitObjetos(json);
        for (String obj : objetos) {
            try {
                String displayName = extraer(obj, "display_name");
                String latStr      = extraer(obj, "lat");
                String lonStr      = extraer(obj, "lon");
                if (displayName == null || latStr == null || lonStr == null) continue;
                lista.add(new PlaceResult(displayName,
                    Double.parseDouble(latStr),
                    Double.parseDouble(lonStr)));
            } catch (NumberFormatException ignored) {}
        }
        return lista;
    }

    /**
     * Extrae el valor de una clave de string en un objeto JSON plano.
     * Solo funciona para valores de tipo string (entre comillas) o número.
     */
    private String extraer(String obj, String clave) {
        String marca = "\"" + clave + "\"";
        int idx = obj.indexOf(marca);
        if (idx == -1) return null;
        idx += marca.length();
        // Saltar espacios y ':'
        while (idx < obj.length() && (obj.charAt(idx) == ':' || obj.charAt(idx) == ' ')) idx++;
        if (idx >= obj.length()) return null;

        char inicio = obj.charAt(idx);
        if (inicio == '"') {
            // Valor de tipo string
            int fin = idx + 1;
            while (fin < obj.length()) {
                if (obj.charAt(fin) == '"' && obj.charAt(fin - 1) != '\\') break;
                fin++;
            }
            return obj.substring(idx + 1, fin);
        } else {
            // Valor numérico u otro (hasta coma, '}' o espacio)
            int fin = idx;
            while (fin < obj.length() && ",}] \r\n\t".indexOf(obj.charAt(fin)) == -1) fin++;
            return obj.substring(idx, fin).trim();
        }
    }

    /** Divide el array JSON en objetos individuales respetando el anidamiento. */
    private String[] splitObjetos(String arrayContent) {
        List<String> objetos = new ArrayList<>();
        int profundidad = 0;
        int inicio = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (profundidad == 0) inicio = i;
                profundidad++;
            } else if (c == '}') {
                profundidad--;
                if (profundidad == 0 && inicio != -1) {
                    objetos.add(arrayContent.substring(inicio, i + 1));
                    inicio = -1;
                }
            }
        }
        return objetos.toArray(new String[0]);
    }
}