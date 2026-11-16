package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {
    private Cliente cliente;
    private Monedero m1;

    @BeforeEach
    void preparar() {
        cliente = TestDatos.cliente("ana");
        m1 = TestDatos.monedero("MV-1", 10_000);
        cliente.agregarMonedero(m1);
    }

    @Test
    void depositoActualizaSaldoHistorialYPuntos() {
        String msg = cliente.registrarTransaccion(TestDatos.deposito("T1", 250, m1));
        assertEquals(10_250, m1.getSaldo(), 1e-9);
        assertEquals(1, cliente.getListaTransacciones().size());
        assertEquals(2, cliente.getPuntosAcumulados());
        assertEquals(RangoCliente.BRONCE, cliente.getRango());
        assertTrue(msg.contains("Puntos ganados: 2"));
    }

    @Test
    void retiroYTransferenciaSumanPuntosDistintos() {
        Monedero destino = TestDatos.monedero("MV-9", 0);
        cliente.registrarTransaccion(TestDatos.retiro("R1", 1000, 10, m1));           // 10 bloques * 2 = 20
        cliente.registrarTransaccion(TestDatos.transferencia("T1", 1000, 20, m1, destino)); // 10 * 3 = 30
        assertEquals(50, cliente.getPuntosAcumulados());
        assertEquals(10_000 - 1010 - 1020, m1.getSaldo(), 1e-9);
    }

    @Test
    void transaccionFallidaNoDejaRastroEnSaldoHistorialNiPuntos() {
        assertThrows(SaldoInsuficienteException.class,
            () -> cliente.registrarTransaccion(TestDatos.retiro("R1", 50_000, 0, m1)));
        assertEquals(10_000, m1.getSaldo(), 1e-9);
        assertTrue(cliente.getListaTransacciones().isEmpty());
        assertEquals(0, cliente.getPuntosAcumulados());
        assertTrue(cliente.getListaMovimientosPuntos().isEmpty());
    }

    @Test
    void montoMenorACienNoGeneraMovimientoDePuntos() {
        cliente.registrarTransaccion(TestDatos.deposito("T1", 99, m1));
        assertEquals(0, cliente.getPuntosAcumulados());
        assertTrue(cliente.getListaMovimientosPuntos().isEmpty());
    }

    @Test
    void monederoQueNoEsDelClienteLanzaMonederoInexistente() {
        Monedero ajeno = TestDatos.monedero("MV-X", 500);
        assertThrows(MonederoInexistenteException.class,
            () -> cliente.registrarTransaccion(TestDatos.deposito("T1", 100, ajeno)));
        assertEquals(500, ajeno.getSaldo(), 1e-9);
    }

    @Test
    void transaccionNulaLanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> cliente.registrarTransaccion(null));
    }

    @Test
    void rangoCambiaEnLosLimitesDePuntos() {
        cliente.acumularPuntos(500);
        assertEquals(RangoCliente.BRONCE, cliente.getRango());
        cliente.acumularPuntos(1);
        assertEquals(RangoCliente.PLATA, cliente.getRango());
        cliente.acumularPuntos(499);   // 1000
        assertEquals(RangoCliente.PLATA, cliente.getRango());
        cliente.acumularPuntos(1);     // 1001
        assertEquals(RangoCliente.ORO, cliente.getRango());
        cliente.acumularPuntos(3999);  // 5000
        assertEquals(RangoCliente.ORO, cliente.getRango());
        cliente.acumularPuntos(1);     // 5001
        assertEquals(RangoCliente.PLATINO, cliente.getRango());
    }

    @Test
    void rangoBajaCuandoSeDescuentanPuntos() {
        cliente.acumularPuntos(600);
        assertEquals(RangoCliente.PLATA, cliente.getRango());
        cliente.acumularPuntos(-200);
        assertEquals(400, cliente.getPuntosAcumulados());
        assertEquals(RangoCliente.BRONCE, cliente.getRango());
    }

    @Test
    void acumularCeroLanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> cliente.acumularPuntos(0));
    }

    @Test
    void canjearMasPuntosDeLosQueTieneLanzaPuntosInsuficientes() {
        cliente.acumularPuntos(100);
        assertThrows(PuntosInsuficienteException.class, () -> cliente.acumularPuntos(-101));
        assertEquals(100, cliente.getPuntosAcumulados());
    }

    @Test
    void canjearTodosLosPuntosEsValido() {
        cliente.acumularPuntos(100);
        cliente.acumularPuntos(-100);
        assertEquals(0, cliente.getPuntosAcumulados());
    }

    @Test
    void revertirTransferenciaDevuelveDineroYRecalculaPuntos() {
        Monedero destino = TestDatos.monedero("MV-9", 0);
        Transferencia t = TestDatos.transferencia("T1", 1000, 20, m1, destino);
        cliente.registrarTransaccion(t);
        assertEquals(30, cliente.getPuntosAcumulados());

        String msg = cliente.revertirTransferencia(t);

        assertEquals(10_000, m1.getSaldo(), 1e-9);
        assertEquals(0, destino.getSaldo(), 1e-9);
        assertEquals(0, cliente.getPuntosAcumulados());
        assertEquals(EstadoTransaccion.REVERTIDA, t.getEstado());
        assertTrue(msg.contains("revertida"));
        assertTrue(cliente.getListaMovimientosPuntos().stream().allMatch(MovimientoPuntos::isAnulado));
    }

    @Test
    void revertirSoloAnulaLosPuntosDeEsaTransferencia() {
        Monedero destino = TestDatos.monedero("MV-9", 0);
        cliente.registrarTransaccion(TestDatos.deposito("D1", 1000, m1));  // 10 puntos
        Transferencia t = TestDatos.transferencia("T1", 1000, 0, m1, destino);
        cliente.registrarTransaccion(t);                                   // 30 puntos
        cliente.revertirTransferencia(t);
        assertEquals(10, cliente.getPuntosAcumulados());
    }

    @Test
    void revertirNuloLanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> cliente.revertirTransferencia(null));
    }

    @Test
    void agregarMonederoDuplicadoNoLoRepite() {
        String msg = cliente.agregarMonedero(TestDatos.monedero("MV-1", 0));
        assertEquals(1, cliente.getListaMonederos().size());
        assertTrue(msg.contains("ya tiene"));
    }

    @Test
    void buscarYEliminarMonedero() {
        assertTrue(cliente.buscarMonedero("MV-1").isPresent());
        assertTrue(cliente.buscarMonedero("NO").isEmpty());
        assertEquals("Monedero eliminado correctamente", cliente.eliminarMonedero("MV-1"));
        assertTrue(cliente.eliminarMonedero("MV-1").startsWith("No existe"));
    }

    @Test
    void movimientosDePuntosAgregarEliminarYRecalcular() {
        cliente.agregarMovimiento(new MovimientoPuntos("M1", 300, LocalDate.now(), false));
        cliente.agregarMovimiento(new MovimientoPuntos("M2", 400, LocalDate.now(), false));
        assertEquals(700, cliente.getPuntosAcumulados());
        assertEquals(RangoCliente.PLATA, cliente.getRango());
        assertTrue(cliente.agregarMovimiento(new MovimientoPuntos("M1", 1, LocalDate.now(), false)).contains("Ya existe"));
        cliente.eliminarMovimiento("M2");
        assertEquals(300, cliente.getPuntosAcumulados());
        cliente.actualizarMovimiento("M1", new MovimientoPuntos("M1", 50, LocalDate.now(), false));
        assertEquals(50, cliente.getPuntosAcumulados());
    }

    @Test
    void movimientoAnuladoNoSuma() {
        cliente.agregarMovimiento(new MovimientoPuntos("M1", 300, LocalDate.now(), true));
        assertEquals(0, cliente.getPuntosAcumulados());
    }

    @Test
    void alertasAgregarBuscarActualizarEliminar() {
        Alerta a = new Alerta("Saldo bajo", LocalDate.now(), TipoAlerta.SALDO_BAJO);
        assertEquals("Alerta agregada correctamente", cliente.agregarAlerta(a));
        assertTrue(cliente.agregarAlerta(new Alerta("Saldo bajo", LocalDate.now(), TipoAlerta.SALDO_BAJO)).contains("Ya existe"));
        assertTrue(cliente.buscarAlerta("Saldo bajo").isPresent());
        cliente.actualizarAlerta("Saldo bajo", new Alerta("Otra", LocalDate.now(), TipoAlerta.RECORDATORIO));
        assertTrue(cliente.buscarAlerta("Otra").isPresent());
        assertEquals("Alerta eliminada correctamente", cliente.eliminarAlerta("Otra"));
        assertEquals("No existe la alerta", cliente.eliminarAlerta("Otra"));
        assertThrows(DatosInvalidosException.class, () -> cliente.agregarAlerta(null));
    }
}
