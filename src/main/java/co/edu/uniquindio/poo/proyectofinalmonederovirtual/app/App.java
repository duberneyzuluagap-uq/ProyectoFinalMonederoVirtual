package co.edu.uniquindio.poo.proyectofinalmonederovirtual.app;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.*;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController.BeneficiosViewController;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController.MonederoViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;

public class App extends Application {
    private static final String RUTA = "/co/edu/uniquindio/poo/proyectofinalmonederovirtual/";

    private static Stage stage;
    private static Banco banco;
    private static Notificacion notificacion;
    private static UsuarioController usuarioController;
    private static BeneficioController beneficioController;
    private static AlertaController alertaController;
    private static AnalizadorGastosController analizadorController;
    private static TransaccionController transaccionController;
    private static OperacionController operacionController;

    @Override
    public void start(Stage primaryStage) {
        inicializarModelo();
        stage = primaryStage;
        stage.setTitle("MiMonedero");
        irLogin();
        stage.show();
    }

    private static void inicializarModelo() {
        banco = new Banco("MiMonedero Banco", "900123456-7");
        notificacion = new NotificacionSimulada();
        usuarioController = new UsuarioController(banco);
        beneficioController = new BeneficioController(banco);
        alertaController = new AlertaController();
        analizadorController = new AnalizadorGastosController();
        transaccionController = new TransaccionController(banco);
        operacionController = new OperacionController(banco, transaccionController, new MonederoController(banco),
                alertaController, new DetectorAtipicasController(notificacion), notificacion);
        sembrarDatos();
    }

    private static void sembrarDatos() {
        operacionController.registrarAdmin("Administrador", "admin", "admin123", "admin@mimonedero.com");
        Cliente ana = operacionController.registrarCliente("Ana Pérez", "ana", "1234", "ana@mail.com", "1001001001", "Calle 1 # 2-3", "3001112233");
        Cliente luis = operacionController.registrarCliente("Luis Gómez", "luis", "1234", "luis@mail.com", "2002002002", "Carrera 5 # 6-7", "3109998877");
        Monedero anaGastos = ana.getListaMonederos().get(0);
        Monedero luisGastos = luis.getListaMonederos().get(0);
        operacionController.crearMonederoAhorro(ana, 5_000_000, 0.01);

        LocalDate hoy = LocalDate.now();
        transaccionController.registrarTransaccion(ana, new Deposito(operacionController.nuevoId("TX"), 2_000_000, hoy.minusDays(40),
                EstadoTransaccion.RECIBIDA, false, anaGastos));
        transaccionController.registrarTransaccion(luis, new Deposito(operacionController.nuevoId("TX"), 800_000, hoy.minusDays(35),
                EstadoTransaccion.RECIBIDA, false, luisGastos));
        double[] retiros = {80_000, 90_000, 100_000, 85_000, 95_000};
        for (int i = 0; i < retiros.length; i++) {
            transaccionController.registrarTransaccion(ana, new Retiro(operacionController.nuevoId("TX"), retiros[i],
                    hoy.minusDays(30 - i * 5L), EstadoTransaccion.ENVIADA, false, anaGastos, operacionController.comisionRetiro(retiros[i])));
        }
        transaccionController.registrarTransaccion(ana, new Transferencia(operacionController.nuevoId("TX"), 300_000, hoy.minusDays(3),
                EstadoTransaccion.ENVIADA, false, anaGastos, operacionController.comisionTransferencia(ana, 300_000), luisGastos));

        beneficioController.agregarBeneficio(new Beneficio("BEN-001", "20% en Cine", "Descuento en entradas para cualquier función.", 500));
        beneficioController.agregarBeneficio(new Beneficio("BEN-002", "Descuento en transferencias", "10% menos de comisión en tus transferencias.", 100));
        beneficioController.agregarBeneficio(new Beneficio("BEN-003", "Mes sin cargos por retiro", "Retira sin comisión durante un mes.", 500));
        beneficioController.agregarBeneficio(new Beneficio("BEN-004", "Recarga de celular", "Recarga de 10.000 pesos sin costo.", 300));
        beneficioController.agregarBeneficio(new Beneficio("BEN-005", "Bono de saldo", "Bono de 50.000 pesos en tu monedero.", 1000));
        beneficioController.agregarBeneficio(new Beneficio("BEN-006", "15% en tiquetes de viaje", "Descuento en vuelos nacionales.", 2000));
    }

    public static Banco getBanco() { return banco; }
    public static UsuarioController getUsuarioController() { return usuarioController; }
    public static BeneficioController getBeneficioController() { return beneficioController; }
    public static AlertaController getAlertaController() { return alertaController; }
    public static AnalizadorGastosController getAnalizadorController() { return analizadorController; }
    public static TransaccionController getTransaccionController() { return transaccionController; }
    public static OperacionController getOperacionController() { return operacionController; }
    public static Stage getStage() { return stage; }

    public static Cliente clienteActual() {
        Usuario u = usuarioController.getUsuarioActual();
        return (u instanceof Cliente c) ? c : null;
    }

    public static void irLogin() { cargar("login.fxml"); }

    public static void irMonedero(String seccion) {
        MonederoViewController c = cargar("monedero.fxml");
        c.mostrarSeccion(seccion);
    }

    public static void irBeneficios(String seccion) {
        BeneficiosViewController c = cargar("beneficios.fxml");
        c.mostrarSeccion(seccion);
    }

    public static void irAdmin() { cargar("admin.fxml"); }

    private static <T> T cargar(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(RUTA + fxml));
            Parent raiz = loader.load();
            stage.setScene(new Scene(raiz));
            stage.sizeToScene();
            stage.centerOnScreen();
            return loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar " + fxml, e);
        }
    }

    public static void main(String[] args) { launch(args); }
}