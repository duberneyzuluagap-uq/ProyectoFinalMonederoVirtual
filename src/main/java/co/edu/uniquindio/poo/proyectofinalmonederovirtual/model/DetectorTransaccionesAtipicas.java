package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;
import java.util.List;

public class DetectorTransaccionesAtipicas {
    private Notificacion notificacion;

    public DetectorTransaccionesAtipicas(Notificacion notificacion) {
        this.notificacion = notificacion;
    }

    public double calcularPromedio(List<Double> montos) {
        return montos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public double calcularDesviacion(List<Double> montos, double promedio) {
        if (montos.isEmpty()) return 0;
        double sumaCuadrados = montos.stream().mapToDouble(m -> Math.pow(m - promedio, 2)).sum();
        return Math.sqrt(sumaCuadrados / montos.size());
    }


    public boolean esAtipica(double monto, List<Transaccion> historial) {
        if (historial.size() < 2) return false;
        List<Double> montos = historial.stream().map(Transaccion::getMonto).toList();
        double promedio = calcularPromedio(montos);
        return monto > promedio + 2 * calcularDesviacion(montos, promedio);
    }


    public List<Transaccion> detectar(Cliente cliente) {
        List<Transaccion> historial = cliente.getListaTransacciones().stream()
                .filter(t -> t.getEstado() != EstadoTransaccion.CANCELADA)
                .toList();
        if (historial.size() < 2) return List.of();
        List<Double> montos = historial.stream().map(Transaccion::getMonto).toList();
        double promedio = calcularPromedio(montos);
        double umbral = promedio + 2 * calcularDesviacion(montos, promedio);
        List<Transaccion> atipicas = historial.stream().filter(t -> t.getMonto() > umbral).toList();
        atipicas.stream().filter(t -> !t.isSospechosa()).forEach(t -> {
            t.setSospechosa(true);
            String mensaje = "Transacción atípica " + t.getIdTransaccion() + " por " + t.getMonto();
            cliente.agregarAlerta(new Alerta(mensaje, LocalDate.now(), TipoAlerta.TRANSACCION_ATIPICA));
            if (notificacion != null) notificacion.enviar(cliente, mensaje);
        });
        return atipicas;
    }

    public Notificacion getNotificacion() { return notificacion; }
    public void setNotificacion(Notificacion notificacion) { this.notificacion = notificacion; }
}
