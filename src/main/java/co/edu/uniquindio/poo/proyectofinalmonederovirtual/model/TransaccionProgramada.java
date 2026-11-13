package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public class TransaccionProgramada extends Transaccion implements Comparacion, Comparable<TransaccionProgramada>{


    private LocalDate fechaEjecucion;
    private Frecuencia frecuencia;
    private Transaccion transaccion;

    public TransaccionProgramada(String idTransaccion, double monto, LocalDate fecha, EstadoTransaccion estado, boolean sospechosa,
                                 Monedero monedero, LocalDate fechaEjecucion,
                                 Frecuencia frecuencia,
                                 Transaccion transaccion) {
        super(idTransaccion, monto, fecha, estado, sospechosa, monedero);
        this.fechaEjecucion = fechaEjecucion;
        this.frecuencia = frecuencia;
        this.transaccion = transaccion;
    }

    public LocalDate getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDate fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Frecuencia frecuencia) { this.frecuencia = frecuencia; }
    public Transaccion getTransaccion() { return transaccion; }
    public void setTransaccion(Transaccion transaccion) { this.transaccion = transaccion; }

        @Override
        public int compareTo(TransaccionProgramada otra) {
            return fechaEjecucion.compareTo(otra.fechaEjecucion);
        }

        @Override
        public int comparteTo(TransaccionProgramada otra) {
            return compareTo(otra);
        }


    @Override
    public void ejecutar() {

    }

    @Override
    public int calcularPuntos() {
        return 0;
    }
}
