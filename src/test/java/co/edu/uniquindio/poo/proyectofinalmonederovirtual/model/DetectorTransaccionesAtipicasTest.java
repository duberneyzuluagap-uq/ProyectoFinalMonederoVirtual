package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DetectorTransaccionesAtipicasTest {
    private NotificacionFalsa notif;
    private DetectorTransaccionesAtipicas detector;
    private Cliente cliente;
    private Monedero m;

    @BeforeEach
    void preparar() {
        notif = new NotificacionFalsa();
        detector = new DetectorTransaccionesAtipicas(notif);
        cliente = TestDatos.clienteConMonedero("ana", "MV-1", 1_000_000);
        m = cliente.getListaMonederos().get(0);
    }

    private void historial(double... montos) {
        for (int i = 0; i < montos.length; i++)
            cliente.getListaTransacciones().add(TestDatos.deposito("H" + i, montos[i], m));
    }

    @Test
    void promedioYDesviacionPoblacionalCoincidenConElEjemploClasico() {
        List<Double> montos = List.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0);
        double prom = detector.calcularPromedio(montos);
        assertEquals(5.0, prom, 1e-9);
        assertEquals(2.0, detector.calcularDesviacion(montos, prom), 1e-9);
    }

    @Test
    void listaVaciaDevuelveCeroSinFallar() {
        assertEquals(0, detector.calcularPromedio(List.of()), 1e-9);
        assertEquals(0, detector.calcularDesviacion(List.of(), 0), 1e-9);
    }

    @Test
    void detectaMontoSuperiorAPromedioMasDosDesviaciones() {
        historial(100, 100, 100, 100, 100, 1000);
        List<Transaccion> atipicas = detector.detectar(cliente);
        assertEquals(1, atipicas.size());
        assertEquals(1000, atipicas.get(0).getMonto(), 1e-9);
    }

    @Test
    void laTransaccionAtipicaQuedaMarcadaGeneraAlertaYNotifica() {
        historial(100, 100, 100, 100, 100, 1000);
        Transaccion atipica = detector.detectar(cliente).get(0);
        assertTrue(atipica.isSospechosa());
        assertEquals(1, cliente.getListaAlertas().size());
        assertEquals(TipoAlerta.TRANSACCION_ATIPICA, cliente.getListaAlertas().get(0).getTipo());
        assertEquals(1, notif.total());
        assertTrue(notif.getMensajes().get(0).contains(atipica.getIdTransaccion()));
    }

    @Test
    void ejecutarDeteccionDosVecesNoDuplicaAlertasNiNotificaciones() {
        historial(100, 100, 100, 100, 100, 1000);
        detector.detectar(cliente);
        detector.detectar(cliente);
        assertEquals(1, cliente.getListaAlertas().size());
        assertEquals(1, notif.total());
    }

    @Test
    void montosHomogeneosNoGenerabAtipicas() {
        historial(100, 100, 100, 100, 100, 100);
        assertTrue(detector.detectar(cliente).isEmpty());
        assertTrue(cliente.getListaAlertas().isEmpty());
    }

    @Test
    void conMenosDeDosTransaccionesNoDetectaNada() {
        assertTrue(detector.detectar(cliente).isEmpty());
        historial(5000);
        assertTrue(detector.detectar(cliente).isEmpty());
    }

    @Test
    void elUmbralEsEstrictoUnMontoIgualAlUmbralNoEsAtipico() {
        historial(100, 100, 100, 100, 1000);
        assertTrue(detector.detectar(cliente).isEmpty());
    }

    @Test
    void transaccionesRevertidasOCanceladasNoEntranAlAnalisis() {
        historial(100, 100, 100, 100, 100, 1000);
        cliente.getListaTransacciones().get(5).setEstado(EstadoTransaccion.REVERTIDA);
        assertTrue(detector.detectar(cliente).isEmpty());
    }

    @Test
    void esAtipicaCompruebaUnaTransaccionContraUnHistorial() {
        historial(100, 100, 100, 100, 100);
        Transaccion grande = TestDatos.deposito("G", 5000, m);
        Transaccion normal = TestDatos.deposito("N", 100, m);
        assertTrue(detector.esAtipica(grande, cliente.getListaTransacciones()));
        assertFalse(detector.esAtipica(normal, cliente.getListaTransacciones()));
        assertFalse(detector.esAtipica(grande, List.of(normal)));
    }

    @Test
    void funcionaSinNotificacionConfigurada() {
        DetectorTransaccionesAtipicas sinNotif = new DetectorTransaccionesAtipicas(null);
        historial(100, 100, 100, 100, 100, 1000);
        assertEquals(1, sinNotif.detectar(cliente).size());
    }
}
