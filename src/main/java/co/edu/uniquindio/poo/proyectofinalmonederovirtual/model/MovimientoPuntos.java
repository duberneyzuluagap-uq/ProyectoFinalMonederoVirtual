package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public class MovimientoPuntos {
    private String idMovimiento;
    private int puntos;
    private LocalDate fecha;
    private boolean anulado;

    public MovimientoPuntos(String idMovimiento, int puntos, LocalDate fecha, boolean anulado) {
        this.idMovimiento = idMovimiento;
        this.puntos = puntos;
        this.fecha = fecha;
        this.anulado = anulado;
    }




    public String getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(String idMovimiento) { this.idMovimiento = idMovimiento; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public boolean isAnulado() { return anulado; }
    public void setAnulado(boolean anulado) { this.anulado = anulado; }


    public void anular() {
        this.anulado = true;
    }
}
