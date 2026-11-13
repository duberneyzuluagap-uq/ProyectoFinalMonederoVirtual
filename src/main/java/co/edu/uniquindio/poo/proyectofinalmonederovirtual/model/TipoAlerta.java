package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

/**
 * Enum que clasifica el tipo de alerta generada para un cliente.
 */
public enum TipoAlerta {

    SALDO_BAJO("Saldo bajo"),
    RECORDATORIO("Recordatorio"),
    TRANSACCION_ATIPICA("Transacción atípica");

    private final String etiqueta;

    TipoAlerta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
