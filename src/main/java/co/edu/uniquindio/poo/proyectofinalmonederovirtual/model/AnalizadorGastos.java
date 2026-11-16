package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class AnalizadorGastos {

    /** Suma los montos de retiros y transferencias agrupados por tipo de transacción. */
    public Map<String, Double> generarReporteGastos(Cliente cliente) {
        Map<String, Double> gastosPorTipo = new TreeMap<>(); // TreeMap deja las llaves ordenadas (A-Z)

        for (Transaccion t : cliente.getListaTransacciones()) {
            if (!esGastoValido(t)) continue;
            String tipo = t.getClass().getSimpleName(); // "Retiro" o "Transferencia"
            double montoAcumulado = gastosPorTipo.getOrDefault(tipo, 0.0);
            gastosPorTipo.put(tipo, montoAcumulado + t.getMonto());
        }

        double total = 0.0;
        for (double monto : gastosPorTipo.values()) {
            total += monto;
        }

        Map<String, Double> reporte = new LinkedHashMap<>(gastosPorTipo);
        reporte.put("Total gastos", total);
        return reporte;
    }

    /** Solo cuentan los gastos que todavía están vigentes (no cancelados ni revertidos). */
    private boolean esGastoValido(Transaccion t) {
        boolean esGasto = t instanceof Retiro || t instanceof Transferencia;
        boolean anulada = t.getEstado() == EstadoTransaccion.CANCELADA
                || t.getEstado() == EstadoTransaccion.REVERTIDA;
        return esGasto && !anulada;
    }
}
