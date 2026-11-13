package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AnalizadorGastos {
    public Map<String, Double> generarReporteGastos(Cliente cliente) {
        Map<String, Double> porTipo = cliente.getListaTransacciones().stream()
                .filter(t -> t instanceof Retiro || t instanceof Transferencia)
                .filter(t -> t.getEstado() != EstadoTransaccion.CANCELADA)
                .collect(Collectors.groupingBy(t -> t.getClass().getSimpleName(), TreeMap::new,
                        Collectors.summingDouble(Transaccion::getMonto)));
        double total = porTipo.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Double> reporte = new LinkedHashMap<>(porTipo);
        reporte.put("Total gastos", total);
        return reporte;
    }
}
