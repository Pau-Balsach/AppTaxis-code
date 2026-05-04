package ui;

/**
 * Resultado de una búsqueda de dirección en Nominatim.
 * Equivalente al PlaceResult de Flutter: { direccion, lat, lng }
 */
public class PlaceResult {
    private final String direccion;
    private final double lat;
    private final double lng;

    public PlaceResult(String direccion, double lat, double lng) {
        this.direccion = direccion;
        this.lat       = lat;
        this.lng       = lng;
    }

    public String getDireccion() { return direccion; }
    public double getLat()       { return lat; }
    public double getLng()       { return lng; }
}