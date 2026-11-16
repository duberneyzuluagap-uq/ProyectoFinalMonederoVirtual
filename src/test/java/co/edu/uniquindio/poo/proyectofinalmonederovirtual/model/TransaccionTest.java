package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TransaccionTest {

    // ---------- puntos ----------
    @Test
    void depositoGeneraUnPuntoPorCadaCien() {
        Monedero m = TestDatos.monedero("A", 0);
        assertAll(
            () -> assertEquals(0, TestDatos.deposito("t", 99, m).calcularPuntos()),
            () -> assertEquals(1, TestDatos.deposito("t", 100, m).calcularPuntos()),
            () -> assertEquals(1, TestDatos.deposito("t", 199, m).calcularPuntos()),
            () -> assertEquals(2, TestDatos.deposito("t", 250, m).calcularPuntos())
        );
    }

    @Test
    void retiroGeneraDosPuntosPorCadaCien() {
        Monedero m = TestDatos.monedero("A", 1000);
        assertEquals(4, TestDatos.retiro("t", 250, 0, m).calcularPuntos());
    }

    @Test
    void transferenciaGeneraTresPuntosPorCadaCien() {
        Monedero a = TestDatos.monedero("A", 1000), b = TestDatos.monedero("B", 0);
        assertEquals(6, TestDatos.transferencia("t", 250, 0, a, b).calcularPuntos());
    }

    @Test
    void polimorfismoCalcularPuntosSegunElTipoReal() {
        Monedero a = TestDatos.monedero("A", 1000), b = TestDatos.monedero("B", 0);
        List<Transaccion> lista = List.of(
            TestDatos.deposito("d", 300, a), TestDatos.retiro("r", 300, 0, a), TestDatos.transferencia("t", 300, 0, a, b));
        assertEquals(List.of(3, 6, 9), lista.stream().map(Transaccion::calcularPuntos).toList());
    }

    // ---------- validaciones del constructor ----------
    @Test
    void constructorValidaMontoIdFechaYMonedero() {
        Monedero m = TestDatos.monedero("A", 0);
        assertAll(
            () -> assertThrows(MontoInvalidoException.class, () -> TestDatos.deposito("t", 0, m)),
            () -> assertThrows(MontoInvalidoException.class, () -> TestDatos.deposito("t", -10, m)),
            () -> assertThrows(DatosInvalidosException.class, () -> TestDatos.deposito(" ", 10, m)),
            () -> assertThrows(DatosInvalidosException.class, () -> TestDatos.deposito("t", 10, null)),
            () -> assertThrows(DatosInvalidosException.class, () ->
                new Deposito("t", 10, null, EstadoTransaccion.RECIBIDA, false, m))
        );
    }

    // ---------- ejecutar ----------
    @Test
    void depositoEjecutarAumentaSaldoYMarcaRecibida() {
        Monedero m = TestDatos.monedero("A", 100);
        Deposito d = new Deposito("d", 50, LocalDate.now(), EstadoTransaccion.COMPLETADA, false, m);
        d.ejecutar();
        assertEquals(150, m.getSaldo(), 1e-9);
        assertEquals(EstadoTransaccion.RECIBIDA, d.getEstado());
    }

    @Test
    void retiroEjecutarDescuentaMontoMasComision() {
        Monedero m = TestDatos.monedero("A", 1000);
        Retiro r = TestDatos.retiro("r", 500, 5, m);
        r.ejecutar();
        assertEquals(495, m.getSaldo(), 1e-9);
        assertEquals(EstadoTransaccion.ENVIADA, r.getEstado());
    }

    @Test
    void retiroSinSaldoParaMontoMasComisionFalla() {
        Monedero m = TestDatos.monedero("A", 500);
        Retiro r = TestDatos.retiro("r", 500, 5, m);
        assertThrows(SaldoInsuficienteException.class, r::ejecutar);
        assertEquals(500, m.getSaldo(), 1e-9);
    }

    @Test
    void transferenciaMueveDineroYCobraComisionAlOrigen() {
        Monedero a = TestDatos.monedero("A", 1000), b = TestDatos.monedero("B", 10);
        Transferencia t = TestDatos.transferencia("t", 400, 8, a, b);
        t.ejecutar();
        assertEquals(592, a.getSaldo(), 1e-9);
        assertEquals(410, b.getSaldo(), 1e-9);
    }

    @Test
    void transferenciaPorElSaldoExactoDejaOrigenEnCero() {
        Monedero a = TestDatos.monedero("A", 408), b = TestDatos.monedero("B", 0);
        TestDatos.transferencia("t", 400, 8, a, b).ejecutar();
        assertEquals(0, a.getSaldo(), 1e-9);
    }

    @Test
    void transferenciaSinSaldoNoTocaNingunMonedero() {
        Monedero a = TestDatos.monedero("A", 100), b = TestDatos.monedero("B", 0);
        Transferencia t = TestDatos.transferencia("t", 400, 8, a, b);
        assertThrows(SaldoInsuficienteException.class, t::ejecutar);
        assertEquals(100, a.getSaldo(), 1e-9);
        assertEquals(0, b.getSaldo(), 1e-9);
    }

    @Test
    void transferenciaSinDestinoLanzaMonederoInexistente() {
        Monedero a = TestDatos.monedero("A", 1000);
        assertThrows(MonederoInexistenteException.class, () -> TestDatos.transferencia("t", 10, 0, a, null).ejecutar());
    }

    @Test
    void transferenciaAlMismoMonederoLanzaDatosInvalidos() {
        Monedero a = TestDatos.monedero("A", 1000);
        assertThrows(DatosInvalidosException.class, () -> TestDatos.transferencia("t", 10, 0, a, a).ejecutar());
    }

    // ---------- revertir ----------
    @Test
    void revertirDevuelveMontoYComisionAlOrigen() {
        Monedero a = TestDatos.monedero("A", 1000), b = TestDatos.monedero("B", 0);
        Transferencia t = TestDatos.transferencia("t", 400, 8, a, b);
        t.ejecutar();
        t.revertir();
        assertEquals(1000, a.getSaldo(), 1e-9);
        assertEquals(0, b.getSaldo(), 1e-9);
        assertEquals(EstadoTransaccion.REVERTIDA, t.getEstado());
    }

    @Test
    void noSePuedeRevertirDosVecesNiUnaTransferenciaNoEnviada() {
        Monedero a = TestDatos.monedero("A", 1000), b = TestDatos.monedero("B", 0);
        Transferencia t = TestDatos.transferencia("t", 400, 8, a, b);
        t.ejecutar();
        t.revertir();
        assertThrows(DatosInvalidosException.class, t::revertir);
        Transferencia sinEjecutar = new Transferencia("t2", 10, LocalDate.now(), EstadoTransaccion.COMPLETADA, false, a, 0, b);
        assertThrows(DatosInvalidosException.class, sinEjecutar::revertir);
    }

    @Test
    void noSePuedeRevertirSiElDestinoYaGastoElDinero() {
        Monedero a = TestDatos.monedero("A", 1000), b = TestDatos.monedero("B", 0);
        Transferencia t = TestDatos.transferencia("t", 400, 0, a, b);
        t.ejecutar();
        b.retirar(300);
        assertThrows(SaldoInsuficienteException.class, t::revertir);
        assertEquals(EstadoTransaccion.ENVIADA, t.getEstado());
        assertEquals(100, b.getSaldo(), 1e-9);
    }
}
