package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BancoTest {
    private Banco banco;

    @BeforeEach
    void preparar() { banco = new Banco("MiBanco", "900123456-7"); }

    @Test
    void constructorExigeNombreYNit() {
        assertAll(
            () -> assertThrows(DatosInvalidosException.class, () -> new Banco(" ", "1")),
            () -> assertThrows(DatosInvalidosException.class, () -> new Banco("B", null))
        );
    }

    @Test
    void crudUsuario() {
        Cliente ana = TestDatos.cliente("ana");
        assertEquals("Cliente agregado correctamente", banco.registrarUsuario(ana));
        assertTrue(banco.registrarUsuario(TestDatos.cliente("ana")).startsWith("Ya existe"));
        assertEquals(1, banco.getListaUsuarios().size());
        assertSame(ana, banco.buscarUsuario("ana").orElseThrow());
        assertTrue(banco.buscarUsuario("zzz").isEmpty());

        Cliente nueva = TestDatos.cliente("ana");
        nueva.setNombre("Ana Actualizada");
        assertEquals("Cliente actualizado correctamente", banco.actualizarUsuario("ana", nueva));
        assertEquals("Ana Actualizada", banco.buscarUsuario("ana").orElseThrow().getNombre());
        assertTrue(banco.actualizarUsuario("zzz", nueva).startsWith("No existe"));

        assertEquals("Usuario eliminado correctamente", banco.eliminarUsuario("ana"));
        assertTrue(banco.eliminarUsuario("ana").startsWith("No existe"));
    }

    @Test
    void nulosLanzanDatosInvalidos() {
        assertAll(
            () -> assertThrows(DatosInvalidosException.class, () -> banco.registrarUsuario(null)),
            () -> assertThrows(DatosInvalidosException.class, () -> banco.agregarBeneficio(null)),
            () -> assertThrows(DatosInvalidosException.class, () -> banco.agregarMonedero(null)),
            () -> assertThrows(DatosInvalidosException.class, () -> banco.agregarTransaccion(null)),
            () -> assertThrows(DatosInvalidosException.class, () -> banco.agregarTransferencia(null)),
            () -> assertThrows(DatosInvalidosException.class, () -> banco.actualizarUsuario("x", null))
        );
    }

    @Test
    void crudBeneficio() {
        Beneficio b = new Beneficio("BEN-1", "Cine", "20%", 500);
        assertEquals("Beneficio agregado correctamente", banco.agregarBeneficio(b));
        assertTrue(banco.agregarBeneficio(new Beneficio("BEN-1", "Otro", "x", 1)).startsWith("Ya existe"));
        assertTrue(banco.buscarBeneficio("BEN-1").isPresent());
        assertEquals("Beneficio actualizado correctamente",
            banco.actualizarBeneficio("BEN-1", new Beneficio("BEN-1", "Cine+", "30%", 600)));
        assertEquals(600, banco.buscarBeneficio("BEN-1").orElseThrow().getPuntosRequeridos());
        assertEquals("Beneficio eliminado correctamente", banco.eliminarBeneficio("BEN-1"));
        assertTrue(banco.eliminarBeneficio("BEN-1").startsWith("No existe"));
    }

    @Test
    void listaDeBeneficiosEsDeSoloLectura() {
        banco.agregarBeneficio(new Beneficio("BEN-1", "Cine", "20%", 500));
        assertThrows(UnsupportedOperationException.class,
            () -> banco.getListaBeneficios().add(new Beneficio("X", "x", "x", 1)));
    }

    @Test
    void crudMonedero() {
        Monedero m = TestDatos.monedero("MV-1", 0);
        assertEquals("Monedero agregado correctamente", banco.agregarMonedero(m));
        assertTrue(banco.agregarMonedero(TestDatos.monedero("MV-1", 5)).startsWith("Ya existe"));
        assertSame(m, banco.buscarMonedero("MV-1").orElseThrow());
        assertEquals("Monedero actualizado correctamente", banco.actualizarMonedero("MV-1", TestDatos.monedero("MV-1", 50)));
        assertEquals(50, banco.buscarMonedero("MV-1").orElseThrow().getSaldo(), 1e-9);
        assertEquals("Monedero eliminado correctamente", banco.eliminarMonedero("MV-1"));
        assertTrue(banco.buscarMonedero("MV-1").isEmpty());
    }

    @Test
    void crudTransaccionYTransferencia() {
        Monedero a = TestDatos.monedero("A", 100), b = TestDatos.monedero("B", 0);
        Transaccion t = TestDatos.deposito("T1", 10, a);
        Transferencia tr = TestDatos.transferencia("TR1", 10, 0, a, b);

        assertEquals("Transaccion agregado correctamente", banco.agregarTransaccion(t));
        assertTrue(banco.agregarTransaccion(t).startsWith("Ya existe"));
        assertEquals("Transferencia agregado correctamente", banco.agregarTransferencia(tr));
        assertTrue(banco.buscarTransaccion("T1").isPresent());
        assertTrue(banco.buscarTransferencia("TR1").isPresent());

        assertEquals("Transaccion eliminado correctamente", banco.eliminarTransaccion("T1"));
        assertEquals("Transferencia eliminado correctamente", banco.eliminarTransferencia("TR1"));
        assertTrue(banco.eliminarTransaccion("T1").startsWith("No existe"));
        assertTrue(banco.eliminarTransferencia("TR1").startsWith("No existe"));
    }
}
