package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

/**
 * Enum que representa el estado actual de una transacción.
 */
public enum EstadoTransaccion {

    RECIBIDA("Recibida"),
    ENVIADA("Enviada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoTransaccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
