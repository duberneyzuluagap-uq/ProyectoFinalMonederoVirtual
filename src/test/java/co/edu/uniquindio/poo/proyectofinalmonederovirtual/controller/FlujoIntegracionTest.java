package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Recorre un caso de uso completo de punta a punta usando los controladores, como lo haría la interfaz gráfica. */
class FlujoIntegracionTest {

    @Test
    void loginController() {
        Banco banco = new Banco("MiMonedero", "900123456-7");
        NotificacionFalsa notif = new NotificacionFalsa();
        UsuarioController usuarios = new UsuarioController(banco);
        ProcesadorTransaccionesProgramadas proc = new ProcesadorTransaccionesProgramadas(notif);
        OperacionController op = new OperacionController(banco, new TransaccionController(banco), new MonederoController(banco),
                new AlertaController(), new DetectorAtipicasController(notif), notif, proc);
        op.registrarAdmin("Root", "admin", "admin123", "admin@mail.com");
        op.registrarCliente("Ana", "ana", "1234", "ana@mail.com", "1001001001", "d", "3001112233");

        assertFalse(usuarios.iniciarSesion("ana", "mala"));
        assertNull(usuarios.getUsuarioActual());
        assertFalse(usuarios.iniciarSesion("noexiste", "1234"));
        assertTrue(usuarios.iniciarSesion("ana", "1234"));
        assertInstanceOf(Cliente.class, usuarios.getUsuarioActual());
        usuarios.cerrarSesion();
        assertNull(usuarios.getUsuarioActual());
        assertTrue(usuarios.iniciarSesion("admin", "admin123"));
        assertInstanceOf(Admin.class, usuarios.getUsuarioActual());
    }

    @Test
    void casoCompletoClienteConPuntosRangoReportesYProgramadas() {
        Banco banco = new Banco("MiMonedero", "900123456-7");
        NotificacionFalsa notif = new NotificacionFalsa();
        TransaccionController tc = new TransaccionController(banco);
        ProcesadorTransaccionesProgramadas proc = new ProcesadorTransaccionesProgramadas(notif);
        OperacionController op = new OperacionController(banco, tc, new MonederoController(banco),
                new AlertaController(), new DetectorAtipicasController(notif), notif, proc);
        Cliente ana = op.registrarCliente("Ana", "ana", "1234", "ana@mail.com", "1001001001", "d", "3001112233");
        Cliente luis = op.registrarCliente("Luis", "luis", "1234", "luis@mail.com", "2002002002", "d", "3109998877");
        Monedero am = ana.getListaMonederos().get(0), lm = luis.getListaMonederos().get(0);

        // 1) Depósito grande: 60.000 -> 600 puntos -> PLATA
        op.recargar(ana, am, 60_000);
        assertEquals(600, ana.getPuntosAcumulados());
        assertEquals(RangoCliente.PLATA, ana.getRango());

        // 2) Como es PLATA, la comisión de transferencia tiene 10 % de descuento (1,8 % en vez de 2 %)
        double comision = op.comisionTransferencia(ana, 10_000);
        assertEquals(180, comision, 1e-9);
        op.transferir(ana, am, lm, 10_000, "Pago");
        assertEquals(60_000 - 10_000 - 180, am.getSaldo(), 1e-9);

        // 3) Retiro
        op.retirar(ana, am, 5_000);
        assertEquals(60_000 - 10_000 - 180 - 5_000 - 50, am.getSaldo(), 1e-9);

        // 4) Reporte de gastos: retiro 5.000 + transferencia 10.000
        Map<String, Double> reporte = new AnalizadorGastosController().generarReporteGastos(ana);
        assertEquals(5_000, reporte.get("Retiro"), 1e-9);
        assertEquals(10_000, reporte.get("Transferencia"), 1e-9);
        assertEquals(15_000, reporte.get("Total gastos"), 1e-9);

        // 5) Revertir la transferencia: baja 300 puntos
        int antes = ana.getPuntosAcumulados();
        op.revertirTransferencia(banco.getListaTransferencias().get(0));
        assertEquals(antes - 300, ana.getPuntosAcumulados());

        // 6) Programada: depósito mensual vencido hace 1 mes se ejecuta 2 veces (hace 1 mes y hoy)
        LocalDate hoy = LocalDate.of(2026, 11, 15);
        Deposito base = new Deposito("PROG-1", 1_000, hoy, EstadoTransaccion.RECIBIDA, false, am);
        TransaccionProgramada prog = new TransaccionProgramada("P1", 1_000, hoy, EstadoTransaccion.COMPLETADA, false, am,
                hoy.minusMonths(1), Frecuencia.MENSUAL, base);
        ana.agregarTransaccionProgramada(prog);
        double saldoAntes = am.getSaldo();
        new ProcesadorTransaccionesProgramadas(notif).procesarPendientes(ana, hoy);
        assertEquals(saldoAntes + 2_000, am.getSaldo(), 1e-9);
        assertEquals(hoy.plusMonths(1), prog.getFechaEjecucion());
    }
}
