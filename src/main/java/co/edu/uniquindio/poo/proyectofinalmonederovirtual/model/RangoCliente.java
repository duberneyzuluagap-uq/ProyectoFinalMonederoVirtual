package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

/**
 * Enum que representa el rango de un cliente según sus puntos acumulados.
 *
 * Reglas de clasificación (fuente: guía del proyecto):
 *   - BRONCE  :    0 –   500 puntos
 *   - PLATA   :  501 – 1 000 puntos
 *   - ORO     : 1 001 – 5 000 puntos
 *   - PLATINO : más de 5 000 puntos
 *
 * El metodo estático {@link #desdePuntos(int)} aplica estas reglas.
 */
public enum RangoCliente {

    BRONCE("Bronce"),
    PLATA("Plata"),
    ORO("Oro"),
    PLATINO("Platino");

    private final String etiqueta;

    RangoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Devuelve solo la etiqueta legible (p.ej. "Bronce"). */
    @Override
    public String toString() {
        return etiqueta;
    }

    /**
     * Determina el rango que corresponde a la cantidad de puntos indicada.
     *
     * @param puntos puntos acumulados del cliente (>= 0).
     * @return el {@link RangoCliente} correspondiente.
     */
    public static RangoCliente desdePuntos(int puntos) {
        if (puntos <= 500)  return BRONCE;
        if (puntos <= 1000) return PLATA;
        if (puntos <= 5000) return ORO;
        return PLATINO;
    }
}
