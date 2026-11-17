package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

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
