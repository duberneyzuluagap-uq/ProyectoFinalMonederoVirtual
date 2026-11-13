package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public class Retiro extends Transaccion {
    private double comision;

    public Retiro(String idTransaccion, double monto, LocalDate fecha, EstadoTransaccion estado,
                  boolean sospechosa, Monedero monedero, double comision) {
        super(idTransaccion, monto, fecha, estado, sospechosa, monedero);
        this.comision = comision;
    }

    public double getComision() { return comision; }
    public void setComision(double comision) { this.comision = comision; }

    @Override
    public void ejecutar() {
        getMonedero().retirar(getMonto() + comision);
        setEstado(EstadoTransaccion.ENVIADA);
    }


    @Override
    public int calcularPuntos() {
        return bloquesDeCien() * 2;
    }


}
