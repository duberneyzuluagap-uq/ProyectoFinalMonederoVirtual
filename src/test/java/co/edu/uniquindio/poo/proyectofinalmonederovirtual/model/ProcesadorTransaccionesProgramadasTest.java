package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProcesadorTransaccionesProgramadasTest {
    private NotificacionFalsa notif;
    private ProcesadorTransaccionesProgramadas procesador;
    private Cliente cliente;
    private Monedero m;
    private final LocalDate hoy = LocalDate.of(2026, 11, 15);

    @BeforeEach
    void preparar() {
        notif = new NotificacionFalsa();
        procesador = new ProcesadorTransaccionesProgramadas(notif);
        cliente = TestDatos.clienteConMonedero("ana", "MV-1", 1_000);
        m = cliente.getListaMonederos().get(0);
    }

    private TransaccionProgramada programada(String id, Transaccion base, LocalDate fecha, Frecuencia f) {
        return new TransaccionProgramada(id, base.getMonto(), hoy, EstadoTransaccion.COMPLETADA, false, m, fecha, f, base);
    }

    @Test
    void ordenarPorFechaDeEjecucionAscendente() {
        TransaccionProgramada a = programada("A", TestDatos.deposito("a", 10, m), hoy.plusDays(5), Frecuencia.DIARIA);
        TransaccionProgramada b = programada("B", TestDatos.deposito("b", 10, m), hoy.minusDays(3), Frecuencia.DIARIA);
        TransaccionProgramada c = programada("C", TestDatos.deposito("c", 10, m), hoy, Frecuencia.DIARIA);
        List<TransaccionProgramada> orden = procesador.ordenarPorFecha(List.of(a, b, c));
        assertEquals(List.of("B", "C", "A"), orden.stream().map(Transaccion::getIdTransaccion).toList());
    }

    @Test
    void noEjecutaLoQueAunNoVence() {
        TransaccionProgramada p = programada("P", TestDatos.deposito("d", 100, m), hoy.plusDays(1), Frecuencia.DIARIA);
        cliente.agregarTransaccionProgramada(p);
        procesador.procesarPendientes(cliente, hoy);
        assertEquals(1_000, m.getSaldo(), 1e-9);
        assertEquals(hoy.plusDays(1), p.getFechaEjecucion());
        assertEquals(0, notif.total());
    }

    @Test
    void ejecutaUnaVezPorCadaPeriodoVencidoYAvanzaLaFecha() {
        TransaccionProgramada p = programada("P", TestDatos.deposito("d", 100, m), hoy.minusDays(2), Frecuencia.DIARIA);
        cliente.agregarTransaccionProgramada(p);
        procesador.procesarPendientes(cliente, hoy);
        assertEquals(1_300, m.getSaldo(), 1e-9);
        assertEquals(hoy.plusDays(1), p.getFechaEjecucion());
        assertEquals(3, notif.total());
    }

    @Test
    void laFechaAvanzaSegunLaFrecuencia() {
        for (Frecuencia f : Frecuencia.values()) {
            TransaccionProgramada p = programada("P" + f, TestDatos.deposito("d" + f, 10, m), hoy, f);
            cliente.agregarTransaccionProgramada(p);
            procesador.procesarPendientes(cliente, hoy);
            LocalDate esperada = switch (f) {
                case DIARIA -> hoy.plusDays(1);
                case SEMANAL -> hoy.plusWeeks(1);
                case MENSUAL -> hoy.plusMonths(1);
                case ANUAL -> hoy.plusYears(1);
            };
            assertEquals(esperada, p.getFechaEjecucion(), "Frecuencia " + f);
        }
    }

    @Test
    void siFallaPorSaldoNotificaYContinuaConLaSiguiente() {
        Monedero vacio = TestDatos.monedero("MV-2", 0);
        cliente.agregarMonedero(vacio);
        TransaccionProgramada falla = programada("F", TestDatos.retiro("r", 100, 0, vacio), hoy.minusDays(1), Frecuencia.MENSUAL);
        TransaccionProgramada ok = programada("OK", TestDatos.deposito("d", 100, m), hoy, Frecuencia.MENSUAL);
        cliente.agregarTransaccionProgramada(falla);
        cliente.agregarTransaccionProgramada(ok);

        procesador.procesarPendientes(cliente, hoy);

        assertEquals(0, vacio.getSaldo(), 1e-9);
        assertEquals(1_100, m.getSaldo(), 1e-9);
        assertTrue(notif.getMensajes().stream().anyMatch(s -> s.startsWith("No se pudo ejecutar")));
        assertEquals(hoy.minusDays(1).plusMonths(1), falla.getFechaEjecucion());
    }

    @Test
    void ponePorLimiteDeDoceEjecucionesPorPasada() {
        TransaccionProgramada p = programada("P", TestDatos.deposito("d", 1, m), hoy.minusDays(30), Frecuencia.DIARIA);
        cliente.agregarTransaccionProgramada(p);
        procesador.procesarPendientes(cliente, hoy);
        assertEquals(1_012, m.getSaldo(), 1e-9);
        assertEquals(hoy.minusDays(30).plusDays(12), p.getFechaEjecucion());
    }

    @Test
    void transaccionProgramadaDelegaEnLaTransaccionBase() {
        Deposito base = TestDatos.deposito("d", 250, m);
        TransaccionProgramada p = programada("P", base, hoy, Frecuencia.SEMANAL);
        assertEquals(2, p.calcularPuntos());
        p.ejecutar();
        assertEquals(1_250, m.getSaldo(), 1e-9);
    }
}
