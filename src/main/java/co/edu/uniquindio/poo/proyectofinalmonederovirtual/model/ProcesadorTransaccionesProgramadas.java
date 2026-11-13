package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.SaldoInsuficienteException;

import java.time.LocalDate;
import java.util.List;

public class ProcesadorTransaccionesProgramadas {
    private Notificacion notificacion;

    public ProcesadorTransaccionesProgramadas(Notificacion notificacion) {
        this.notificacion = notificacion;
    }

    public Notificacion getNotificacion() { return notificacion; }
    public void setNotificacion(Notificacion notificacion) { this.notificacion = notificacion; }



    public List<TransaccionProgramada> ordenarPorFecha(List<TransaccionProgramada> lista) {
        return lista.stream().sorted().toList();
    }

    public void procesarPendientes(Cliente cliente, LocalDate hoy) {
        for (TransaccionProgramada programada : ordenarPorFecha(cliente.getListaTransaccionesProgramadas())) {
            while (!programada.getFechaEjecucion().isAfter(hoy)) {
                try {
                    cliente.registrarTransaccion(programada.getTransaccion());
                    notificar(cliente, "Se ejecutó la transacción programada "
                            + programada.getTransaccion().getIdTransaccion() + " del "
                            + programada.getFechaEjecucion());
                } catch (SaldoInsuficienteException | MontoInvalidoException | MonederoInexistenteException e) {
                    notificar(cliente, "No se pudo ejecutar la transacción programada "
                            + programada.getTransaccion().getIdTransaccion() + ": " + e.getMessage());
                }
                programada.setFechaEjecucion(siguienteFecha(programada.getFechaEjecucion(), programada.getFrecuencia()));
            }
        }
    }

    private LocalDate siguienteFecha(LocalDate fecha, Frecuencia frecuencia) {
        return switch (frecuencia) {
            case DIARIA -> fecha.plusDays(1);
            case SEMANAL -> fecha.plusWeeks(1);
            case MENSUAL -> fecha.plusMonths(1);
            case ANUAL -> fecha.plusYears(1);
        };
    }

    private void notificar(Cliente cliente, String mensaje) {
        if (notificacion != null) notificacion.enviar(cliente, mensaje);
    }


}
