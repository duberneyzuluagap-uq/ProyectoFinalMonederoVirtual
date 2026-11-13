package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

/**
 * Enum que indica con qué periodicidad se ejecuta una TransaccionProgramada.
 */
public enum Frecuencia {

    DIARIA("Diaria"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual"),
    ANUAL("Anual");

    private final String etiqueta;

    Frecuencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
