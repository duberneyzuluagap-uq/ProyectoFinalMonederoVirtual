package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OperacionControllerTest {
    private Banco banco;
    private NotificacionFalsa notif;
    private OperacionController op;
    private Cliente ana, luis;
    private Monedero anaM, luisM;

    @BeforeEach
    void preparar() {
        banco = new Banco("MiMonedero", "900123456-7");
        notif = new NotificacionFalsa();
        ProcesadorTransaccionesProgramadas proc = new ProcesadorTransaccionesProgramadas(notif);
        op = new OperacionController(banco, new TransaccionController(banco), new MonederoController(banco),
                new AlertaController(), new DetectorAtipicasController(notif), notif, proc);
        ana = op.registrarCliente("Ana Pérez", "ana", "1234", "ana@mail.com", "1001001001", "Calle 1", "3001112233");
        luis = op.registrarCliente("Luis Gómez", "luis", "1234", "luis@mail.com", "2002002002", "Calle 2", "3109998877");
        anaM = ana.getListaMonederos().get(0);
        luisM = luis.getListaMonederos().get(0);
    }


    @Test
    void parseMontoAceptaFormatosColombianosYSimples() {
        assertAll(
            () -> assertEquals(1500, OperacionController.parseMonto("1500"), 1e-9),
            () -> assertEquals(1500, OperacionController.parseMonto("1.500"), 1e-9),
            () -> assertEquals(1500.5, OperacionController.parseMonto("$ 1.500,50"), 1e-9),
            () -> assertEquals(1500.5, OperacionController.parseMonto("1500.50"), 1e-9),
            () -> assertEquals(2_000_000, OperacionController.parseMonto("2.000.000"), 1e-9)
        );
    }

    @Test
    void parseMontoRechazaTextoVacioNoNumericoYNoPositivo() {
        assertAll(
            () -> assertThrows(DatosInvalidosException.class, () -> OperacionController.parseMonto(null)),
            () -> assertThrows(DatosInvalidosException.class, () -> OperacionController.parseMonto("  ")),
            () -> assertThrows(DatosInvalidosException.class, () -> OperacionController.parseMonto("abc")),
            () -> assertThrows(MontoInvalidoException.class, () -> OperacionController.parseMonto("0")),
            () -> assertThrows(MontoInvalidoException.class, () -> OperacionController.parseMonto("-100"))
        );
    }

    @Test
    void comisionDeTransferenciaBajaConElRango() {
        assertAll(
            () -> { ana.setRango(RangoCliente.BRONCE);  assertEquals(20.0, op.comisionTransferencia(ana, 1000), 1e-9); },
            () -> { ana.setRango(RangoCliente.PLATA);   assertEquals(18.0, op.comisionTransferencia(ana, 1000), 1e-9); },
            () -> { ana.setRango(RangoCliente.ORO);     assertEquals(16.0, op.comisionTransferencia(ana, 1000), 1e-9); },
            () -> { ana.setRango(RangoCliente.PLATINO); assertEquals(13.0, op.comisionTransferencia(ana, 1000), 1e-9); },
            () -> { ana.setRango(null);                 assertEquals(20.0, op.comisionTransferencia(ana, 1000), 1e-9); }
        );
    }

    @Test
    void comisionDeRetiroEsUnoPorCiento() {
        assertEquals(10.0, op.comisionRetiro(1000), 1e-9);
    }


    @Test
    void registrarClienteCreaMonederoInicialYLoGuardaEnElBanco() {
        assertEquals(1, ana.getListaMonederos().size());
        assertInstanceOf(MonederoGastoDiario.class, anaM);
        assertEquals("MV-0001", anaM.getNumeroCuenta());
        assertEquals("MV-0002", luisM.getNumeroCuenta());
        assertTrue(banco.buscarUsuario("ana").isPresent());
        assertTrue(banco.buscarMonedero("MV-0001").isPresent());
        assertEquals(RangoCliente.BRONCE, ana.getRango());
    }

    @Test
    void registrarClienteValidaCadaCampo() {
        assertAll(
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente(" ", "nuevo", "1234", "n@m.com", "3003003003", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "ab", "1234", "n@m.com", "3003003003", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "nuevo", "123", "n@m.com", "3003003003", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "nuevo", "1234", "n@m", "3003003003", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "nuevo", "1234", "n@m.com", "123", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "nuevo", "1234", "n@m.com", "3003003003", "d", "12")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "ana", "1234", "otro@m.com", "3003003003", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "nuevo", "1234", "ANA@mail.com", "3003003003", "d", "3001112233")),
            () -> assertThrows(DatosInvalidosException.class, () -> op.registrarCliente("N", "nuevo", "1234", "n@m.com", "1001001001", "d", "3001112233"))
        );
        assertEquals(2, banco.getListaUsuarios().size());
    }

    @Test
    void registrarAdminYActualizarUsuario() {
        Admin a = op.registrarAdmin("Root", "admin", "admin123", "admin@mail.com");
        assertTrue(banco.buscarUsuario("admin").isPresent());
        op.actualizarUsuario(a, "Root 2", "root2@mail.com", "", null, null);
        assertEquals("Root 2", a.getNombre());
        assertTrue(a.iniciarSesion("admin", "admin123"));          // contraseña vacía = no cambia
        op.actualizarUsuario(ana, "Ana P.", "ana2@mail.com", "9999", "Nueva dir", "3112223344");
        assertEquals("3112223344", ana.getTelefono());
        assertTrue(ana.iniciarSesion("ana", "9999"));
        assertThrows(DatosInvalidosException.class, () -> op.actualizarUsuario(ana, "Ana", "ana@mail.com", "", "d", "12"));
    }

    @Test
    void recargarSumaSaldoYPuntos() {
        String r = op.recargar(ana, anaM, 50_000);
        assertEquals(50_000, anaM.getSaldo(), 1e-9);
        assertEquals(500, ana.getPuntosAcumulados());
        assertTrue(r.contains("Puntos ganados: 500"));
        assertEquals(1, banco.getListaTransacciones().size());
    }

    @Test
    void retirarCobraComisionYNoDejaRastroSiNoHaySaldo() {
        op.recargar(ana, anaM, 50_000);
        op.retirar(ana, anaM, 10_000);
        assertEquals(50_000 - 10_000 - 100, anaM.getSaldo(), 1e-9);

        int antes = banco.getListaTransacciones().size();
        assertThrows(SaldoInsuficienteException.class, () -> op.retirar(ana, anaM, 1_000_000));
        assertEquals(antes, banco.getListaTransacciones().size());
    }

    @Test
    void sinMonederoSeguroLanzaMonederoInexistente() {
        assertThrows(MonederoInexistenteException.class, () -> op.recargar(ana, null, 100));
        assertThrows(MonederoInexistenteException.class, () -> op.retirar(ana, null, 100));
    }

    @Test
    void transferirMueveDineroEntreClientesYGuardaConcepto() {
        op.recargar(ana, anaM, 100_000);
        op.transferir(ana, anaM, luisM, 10_000, "Arriendo");
        assertEquals(100_000 - 10_000 - 180, anaM.getSaldo(), 1e-9);
        assertEquals(10_000, luisM.getSaldo(), 1e-9);
        assertEquals(1000 + 300, ana.getPuntosAcumulados());
        assertEquals(1, banco.getListaTransferencias().size());
        String id = banco.getListaTransferencias().get(0).getIdTransaccion();
        assertEquals("Arriendo", op.getConcepto(id));
    }

    @Test
    void transferirMasDelSaldoFalla() {
        op.recargar(ana, anaM, 1_000);
        assertThrows(SaldoInsuficienteException.class, () -> op.transferir(ana, anaM, luisM, 5_000, null));
        assertEquals(1_000, anaM.getSaldo(), 1e-9);
        assertEquals(0, luisM.getSaldo(), 1e-9);
    }

    @Test
    void revertirTransferenciaDesdeElControladorRestauraSaldosYPuntos() {
        op.recargar(ana, anaM, 100_000);
        op.transferir(ana, anaM, luisM, 10_000, null);
        Transferencia t = banco.getListaTransferencias().get(0);
        op.revertirTransferencia(t);
        assertEquals(100_000, anaM.getSaldo(), 1e-9);
        assertEquals(0, luisM.getSaldo(), 1e-9);
        assertEquals(1000, ana.getPuntosAcumulados());
    }

    @Test
    void alertaDeSaldoBajoCuandoQuedaPorDebajoDelUmbral() {
        op.recargar(ana, anaM, 50_000);
        long antes = ana.getListaAlertas().stream().filter(a -> a.getTipo() == TipoAlerta.SALDO_BAJO).count();
        op.retirar(ana, anaM, 40_000);   // quedan 9.600
        long despues = ana.getListaAlertas().stream().filter(a -> a.getTipo() == TipoAlerta.SALDO_BAJO).count();
        assertEquals(0, antes);
        assertEquals(1, despues);
        assertTrue(notif.getMensajes().stream().anyMatch(s -> s.contains("Saldo bajo") && s.contains("9600")));
    }

    @Test
    void transaccionAtipicaSeMarcaSospechosaYSeAvisaAlCliente() {
        for (int i = 0; i < 5; i++) op.recargar(ana, anaM, 100);
        String r = op.recargar(ana, anaM, 100_000);
        assertTrue(r.contains("sospechosa"));
        assertEquals(1, ana.getListaAlertas().stream().filter(a -> a.getTipo() == TipoAlerta.TRANSACCION_ATIPICA).count());
        assertTrue(ana.getListaTransacciones().get(5).isSospechosa());
    }

    @Test
    void buscarMonederoDestinoPorCuentaCorreoOUsuario() {
        assertSame(luisM, op.buscarMonederoDestino("MV-0002"));
        assertSame(luisM, op.buscarMonederoDestino("LUIS@mail.com"));
        assertSame(luisM, op.buscarMonederoDestino("luis"));
        assertThrows(MonederoInexistenteException.class, () -> op.buscarMonederoDestino("nadie"));
        assertThrows(DatosInvalidosException.class, () -> op.buscarMonederoDestino("  "));
    }

    @Test
    void titularDeUnMonedero() {
        assertSame(ana, op.buscarTitular(anaM).orElseThrow());
        assertEquals("Luis Gómez", op.nombreTitular(luisM));
        assertEquals("—", op.nombreTitular(TestDatos.monedero("ZZ", 0)));
    }

    @Test
    void crearEliminarMonederos() {
        Monedero ahorro = op.crearMonederoAhorro(ana, 1_000_000, 0.02);
        assertInstanceOf(MonederoAhorro.class, ahorro);
        assertEquals(2, ana.getListaMonederos().size());
        assertTrue(banco.buscarMonedero(ahorro.getNumeroCuenta()).isPresent());

        op.eliminarMonedero(ana, ahorro);
        assertEquals(1, ana.getListaMonederos().size());
        assertTrue(banco.buscarMonedero(ahorro.getNumeroCuenta()).isEmpty());
    }

    @Test
    void noSePuedeEliminarElUltimoMonederoUnoConSaldoNiNulo() {
        assertThrows(DatosInvalidosException.class, () -> op.eliminarMonedero(ana, anaM));
        Monedero extra = op.crearMonederoGastoDiario(ana);
        op.recargar(ana, extra, 5_000);
        assertThrows(DatosInvalidosException.class, () -> op.eliminarMonedero(ana, extra));
        assertThrows(DatosInvalidosException.class, () -> op.eliminarMonedero(ana, null));
    }

    @Test
    void bloqueoSeAlternaPorUsuario() {
        assertFalse(op.isBloqueado("ana"));
        assertTrue(op.cambiarBloqueo("ana"));
        assertTrue(op.isBloqueado("ana"));
        assertFalse(op.cambiarBloqueo("ana"));
        assertFalse(op.isBloqueado("ana"));
    }

    @Test
    void eliminarClienteBorraSusMonederosDelBanco() {
        op.eliminarUsuario(ana);
        assertTrue(banco.buscarUsuario("ana").isEmpty());
        assertTrue(banco.buscarMonedero("MV-0001").isEmpty());
        assertTrue(banco.buscarUsuario("luis").isPresent());
    }

    private Beneficio beneficio(String id, int puntos) {
        Beneficio b = new Beneficio(id, "Beneficio " + id, "desc", puntos);
        banco.agregarBeneficio(b);
        return b;
    }

    @Test
    void siguienteIdDeBeneficioEsConsecutivo() {
        assertEquals("BEN-001", op.siguienteIdBeneficio());
        beneficio("BEN-001", 100);
        assertEquals("BEN-002", op.siguienteIdBeneficio());
    }

    @Test
    void beneficiosInactivosNoSeOfrecen() {
        beneficio("BEN-001", 100);
        beneficio("BEN-002", 200);
        assertFalse(op.cambiarEstadoBeneficio("BEN-001"));
        assertEquals(List.of("BEN-002"), op.beneficiosActivos().stream().map(Beneficio::getIdBeneficio).toList());
        assertTrue(op.cambiarEstadoBeneficio("BEN-001"));
        assertEquals(2, op.beneficiosActivos().size());
    }

    @Test
    void canjeDescuentaPuntosYQuedaPendiente() {
        Beneficio b = beneficio("BEN-001", 100);
        op.recargar(ana, anaM, 20_000);
        String msg = op.canjearBeneficio(ana, b);
        assertEquals(100, ana.getPuntosAcumulados());
        assertTrue(msg.contains("pendiente"));
        List<OperacionController.Canje> canjes = op.listarCanjes(ana);
        assertEquals(1, canjes.size());
        assertEquals("Pendiente", canjes.get(0).estado());
        assertEquals(100, canjes.get(0).puntos());
        assertEquals("Beneficio BEN-001", canjes.get(0).beneficio());
    }

    @Test
    void canjeSinPuntosSuficientesLanzaPuntosInsuficiente() {
        Beneficio b = beneficio("BEN-001", 100);
        assertThrows(PuntosInsuficienteException.class, () -> op.canjearBeneficio(ana, b));
        assertEquals(0, ana.getPuntosAcumulados());
    }

    @Test
    void canjeDeBeneficioInactivoONuloFalla() {
        Beneficio b = beneficio("BEN-001", 0);
        op.cambiarEstadoBeneficio("BEN-001");
        assertThrows(DatosInvalidosException.class, () -> op.canjearBeneficio(ana, b));
        assertThrows(DatosInvalidosException.class, () -> op.canjearBeneficio(ana, null));
    }

    @Test
    void aprobarCanjeLoMarcaAprobadoYNoSePuedeRepetir() {
        Beneficio b = beneficio("BEN-001", 100);
        op.recargar(ana, anaM, 20_000);
        op.canjearBeneficio(ana, b);
        OperacionController.Canje c = op.listarCanjes(ana).get(0);
        op.aprobarCanje(c);
        assertEquals("Aprobado", op.listarCanjes(ana).get(0).estado());
        assertThrows(DatosInvalidosException.class, () -> op.aprobarCanje(op.listarCanjes(ana).get(0)));
        assertThrows(DatosInvalidosException.class, () -> op.aprobarCanje(null));
    }

    @Test
    void rechazarCanjeDevuelveLosPuntos() {
        Beneficio b = beneficio("BEN-001", 100);
        op.recargar(ana, anaM, 20_000);
        op.canjearBeneficio(ana, b);
        assertEquals(100, ana.getPuntosAcumulados());
        op.rechazarCanje(op.listarCanjes(ana).get(0));
        assertEquals(200, ana.getPuntosAcumulados());
        assertEquals("Rechazado", op.listarCanjes(ana).get(0).estado());
        assertEquals(1, op.listarTodosLosCanjes().size());
    }
}
