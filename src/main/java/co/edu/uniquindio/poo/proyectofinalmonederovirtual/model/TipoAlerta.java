package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

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
