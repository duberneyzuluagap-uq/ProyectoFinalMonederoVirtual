package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public class TransaccionProgramada implements Comparacion, Comparable<TransaccionProgramada>{


        private LocalDate fechaEjecucion;
        private Frecuencia frecuencia;
        private Transaccion transaccion;

    public TransaccionProgramada(LocalDate fechaEjecucion, Frecuencia frecuencia, Transaccion transaccion) {
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


}
