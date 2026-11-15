package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;


public enum EstadoTransaccion {
    RECIBIDA("Recibida"), ENVIADA("Enviada"), CANCELADA("Cancelada"),
    REVERTIDA("Revertida"), COMPLETADA("Completada"), FALLIDA("Fallida");

    private final String etiqueta;

    EstadoTransaccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
