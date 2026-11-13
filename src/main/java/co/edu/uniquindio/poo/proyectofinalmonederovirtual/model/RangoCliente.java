package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

public enum RangoCliente {

    BRONCE("Bronce"),
    PLATA("Plata"),
    ORO("Oro"),
    PLATINO("Platino");

    private final String etiqueta;

    RangoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    public static RangoCliente desdePuntos(int puntos) {
        if (puntos <= 500)  return BRONCE;
        if (puntos <= 1000) return PLATA;
        if (puntos <= 5000) return ORO;
        return PLATINO;
    }
}
