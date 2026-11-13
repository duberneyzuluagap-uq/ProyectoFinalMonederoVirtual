package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.DatosInvalidosException;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.MontoInvalidoException;
public abstract class Transaccion {
    private String idTransaccion;
    private double monto;
    private LocalDate fecha;
    private EstadoTransaccion estado;
    private boolean sospechosa;
    private Monedero monedero;

    public Transaccion(String idTransaccion, double monto, LocalDate fecha, EstadoTransaccion estado,
                       boolean sospechosa, Monedero monedero) {
        if (idTransaccion == null || idTransaccion.isBlank())
            throw new DatosInvalidosException("El id de la transacción es obligatorio.");
        if (monto <= 0) throw new MontoInvalidoException("El monto de la transacción debe ser mayor que cero.");
        if (fecha == null) throw new DatosInvalidosException("La fecha es obligatoria.");
        if (monedero == null) throw new DatosInvalidosException("La transacción requiere un monedero.");
        this.idTransaccion = idTransaccion;
        this.monto = monto;
        this.fecha = fecha;
        this.estado = estado;
        this.sospechosa = sospechosa;
        this.monedero = monedero;
    }


    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public boolean isSospechosa() { return sospechosa; }
    public void setSospechosa(boolean sospechosa) { this.sospechosa = sospechosa; }
    public Monedero getMonedero() { return monedero; }
    public void setMonedero(Monedero monedero) { this.monedero = monedero; }

    public abstract void ejecutar();

    public abstract int calcularPuntos();

    protected int bloquesDeCien() {
        return (int) Math.floor(monto / 100.0);
    }

}
