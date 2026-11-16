package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void adminValidoSeCreaYPuedeIniciarSesion() {
        Admin a = new Admin("Root", "admin", "admin123", "admin@mail.com");
        assertTrue(a.iniciarSesion("admin", "admin123"));
        assertInstanceOf(Usuario.class, a);
    }

    @Test
    void iniciarSesionFallaConCredencialesIncorrectas() {
        Admin a = new Admin("Root", "admin", "admin123", "admin@mail.com");
        assertFalse(a.iniciarSesion("admin", "otra"));
        assertFalse(a.iniciarSesion("otro", "admin123"));
    }

    @Test
    void camposObligatoriosLanzanDatosInvalidos() {
        assertAll(
            () -> assertThrows(DatosInvalidosException.class, () -> new Admin(" ", "u", "p", "a@b.c")),
            () -> assertThrows(DatosInvalidosException.class, () -> new Admin("N", null, "p", "a@b.c")),
            () -> assertThrows(DatosInvalidosException.class, () -> new Admin("N", "u", "", "a@b.c")),
            () -> assertThrows(DatosInvalidosException.class, () -> new Admin("N", "u", "p", "sin-arroba")),
            () -> assertThrows(DatosInvalidosException.class, () -> new Admin("N", "u", "p", null))
        );
    }

    @Test
    void clienteExigeDocumento() {
        assertThrows(DatosInvalidosException.class, () ->
            new Cliente("Ana", "ana", "1234", "ana@mail.com", " ", "dir", "3001112233", 0, RangoCliente.BRONCE));
    }
}
