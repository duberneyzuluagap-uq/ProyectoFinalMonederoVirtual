package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AnalizadorGastosTest {

    @Test
    void agrupaGastosPorTipoIgnorandoDepositosYTransaccionesAnuladas() {
        Cliente c = TestDatos.clienteConMonedero("ana", "MV-1", 100_000);
        Monedero m = c.getListaMonederos().get(0);
        Monedero otro = TestDatos.monedero("MV-2", 0);
        Retiro cancelado = TestDatos.retiro("R3", 999, 0, m);
        cancelado.setEstado(EstadoTransaccion.CANCELADA);
        Transferencia revertida = TestDatos.transferencia("T2", 777, 0, m, otro);
        revertida.setEstado(EstadoTransaccion.REVERTIDA);

        c.getListaTransacciones().add(TestDatos.retiro("R1", 100, 0, m));
        c.getListaTransacciones().add(TestDatos.retiro("R2", 200, 0, m));
        c.getListaTransacciones().add(TestDatos.transferencia("T1", 300, 0, m, otro));
        c.getListaTransacciones().add(TestDatos.deposito("D1", 5000, m));
        c.getListaTransacciones().add(cancelado);
        c.getListaTransacciones().add(revertida);

        Map<String, Double> reporte = new AnalizadorGastos().generarReporteGastos(c);

        assertEquals(300, reporte.get("Retiro"), 1e-9);
        assertEquals(300, reporte.get("Transferencia"), 1e-9);
        assertEquals(600, reporte.get("Total gastos"), 1e-9);
        assertFalse(reporte.containsKey("Deposito"));
        assertEquals(new ArrayList<>(reporte.keySet()), java.util.List.of("Retiro", "Transferencia", "Total gastos"));
    }

    @Test
    void clienteSinMovimientosTieneTotalCero() {
        Map<String, Double> reporte = new AnalizadorGastos().generarReporteGastos(TestDatos.cliente("luis"));
        assertEquals(1, reporte.size());
        assertEquals(0, reporte.get("Total gastos"), 1e-9);
    }
}
