package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RangoClienteTest {

    @Test
    void limitesDeCadaRango() {
        assertAll(
            () -> assertEquals(RangoCliente.BRONCE, RangoCliente.desdePuntos(0)),
            () -> assertEquals(RangoCliente.BRONCE, RangoCliente.desdePuntos(500)),
            () -> assertEquals(RangoCliente.PLATA, RangoCliente.desdePuntos(501)),
            () -> assertEquals(RangoCliente.PLATA, RangoCliente.desdePuntos(1000)),
            () -> assertEquals(RangoCliente.ORO, RangoCliente.desdePuntos(1001)),
            () -> assertEquals(RangoCliente.ORO, RangoCliente.desdePuntos(5000)),
            () -> assertEquals(RangoCliente.PLATINO, RangoCliente.desdePuntos(5001)),
            () -> assertEquals(RangoCliente.PLATINO, RangoCliente.desdePuntos(1_000_000))
        );
    }

    @Test
    void toStringMuestraEtiquetaLegible() {
        assertEquals("Bronce", RangoCliente.BRONCE.toString());
        assertEquals("Platino", RangoCliente.PLATINO.toString());
    }
}
