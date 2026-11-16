package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MonederoTest {

    @Test
    void constructorValidaDatos() {
        assertAll(
            () -> assertThrows(DatosInvalidosException.class, () -> new MonederoGastoDiario(" ", 0, 0)),
            () -> assertThrows(MontoInvalidoException.class, () -> new MonederoGastoDiario("MV-1", -1, 0))
        );
    }

    @Test
    void depositarSumaAlSaldo() {
        Monedero m = TestDatos.monedero("MV-1", 100);
        m.depositar(50);
        assertEquals(150, m.consultarSaldo(), 1e-9);
    }

    @Test
    void depositarMontoNoPositivoLanzaMontoInvalido() {
        Monedero m = TestDatos.monedero("MV-1", 100);
        assertAll(
            () -> assertThrows(MontoInvalidoException.class, () -> m.depositar(0)),
            () -> assertThrows(MontoInvalidoException.class, () -> m.depositar(-5))
        );
        assertEquals(100, m.getSaldo(), 1e-9);
    }

    @Test
    void retirarDescuentaDelSaldo() {
        Monedero m = TestDatos.monedero("MV-1", 100);
        m.retirar(40);
        assertEquals(60, m.getSaldo(), 1e-9);
    }

    @Test
    void retirarTodoElSaldoEsValido() {
        Monedero m = TestDatos.monedero("MV-1", 100);
        m.retirar(100);
        assertEquals(0, m.getSaldo(), 1e-9);
    }

    @Test
    void retirarMasDelSaldoLanzaSaldoInsuficienteYNoCambiaElSaldo() {
        Monedero m = TestDatos.monedero("MV-1", 100);
        assertThrows(SaldoInsuficienteException.class, () -> m.retirar(100.01));
        assertEquals(100, m.getSaldo(), 1e-9);
    }

    @Test
    void retirarMontoNoPositivoLanzaMontoInvalido() {
        Monedero m = TestDatos.monedero("MV-1", 100);
        assertThrows(MontoInvalidoException.class, () -> m.retirar(0));
    }

    @Test
    void monederoAhorroCalculaInteres() {
        MonederoAhorro a = new MonederoAhorro("MV-2", 1000, 5000, 0.01);
        assertEquals(10, a.calcularInteres(), 1e-9);
        assertInstanceOf(Monedero.class, a);
    }

    @Test
    void monederoGastoDiarioRegistraGasto() {
        MonederoGastoDiario g = new MonederoGastoDiario("MV-3", 1000, 0);
        g.registrarGasto(300);
        assertEquals(700, g.getSaldo(), 1e-9);
        assertEquals(300, g.getGastadoHoy(), 1e-9);
    }

    @Test
    void gastoSinSaldoNoModificaElAcumuladoDelDia() {
        MonederoGastoDiario g = new MonederoGastoDiario("MV-3", 100, 0);
        assertThrows(SaldoInsuficienteException.class, () -> g.registrarGasto(500));
        assertEquals(0, g.getGastadoHoy(), 1e-9);
        assertEquals(100, g.getSaldo(), 1e-9);
    }
}
