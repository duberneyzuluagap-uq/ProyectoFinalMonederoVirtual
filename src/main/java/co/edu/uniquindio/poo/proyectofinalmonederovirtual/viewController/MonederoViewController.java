package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MonederoViewController {

    private record Movimiento(String id, LocalDate fecha, String descripcion, String tipo, String categoria,
                              double monto, boolean ingreso, boolean sospechosa, EstadoTransaccion estado) {}

    private static final Map<String, String> PRINCIPAL = new HashMap<>();

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private Label lblLogo, lblIniciales, lblUsuario, lblCorreo;
    @FXML private Button btnInicio, btnEnviar, btnRecargar, btnHistorial, btnTarjetas, btnConfiguracion, btnCerrarSesion;
    @FXML private Label lblBienvenida, lblFecha, lblEstadoCuenta;
    @FXML private Button btnNotificaciones;
    @FXML private Label lblSaldo, lblNumeroCuenta, lblIngresos, lblGastos;
    @FXML private Button btnToggleSaldo, btnRecargarRapido, btnEnviarRapido;
    @FXML private TextField txtDestinatario, txtMonto, txtConcepto, txtMontoRecarga, txtBuscar;
    @FXML private Button btnEnviarDinero, btnRecargarSaldo, btnMonto20, btnMonto50, btnMonto100;
    @FXML private Label lblMensajeEnvio, lblMensajeRecarga;
    @FXML private ComboBox<String> cmbFiltro;
    @FXML private TableView<Movimiento> tblMovimientos;
    @FXML private TableColumn<Movimiento, String> colFecha, colDescripcion, colTipo, colMonto;

    private Cliente cliente;
    private boolean saldoVisible = true;
    private Button btnBeneficiosNav;
    private final ObservableList<Movimiento> base = FXCollections.observableArrayList();
    private final FilteredList<Movimiento> filtrados = new FilteredList<>(base, m -> true);
    private final OperacionController op = App.getOperacionController();

    @FXML
    private void initialize() {
        cliente = App.clienteActual();
        if (cliente == null) {
            Platform.runLater(App::irLogin);
            return;
        }
        agregarBotonBeneficios();
        configurarTabla();
        cargarPerfil();

        btnInicio.setOnAction(e -> mostrarSeccion("inicio"));
        btnEnviar.setOnAction(e -> mostrarSeccion("enviar"));
        btnRecargar.setOnAction(e -> mostrarSeccion("recargar"));
        btnHistorial.setOnAction(e -> mostrarSeccion("historial"));
        btnBeneficiosNav.setOnAction(e -> App.irBeneficios("catalogo"));
        btnTarjetas.setOnAction(e -> abrirMonederos());
        btnConfiguracion.setOnAction(e -> abrirConfiguracion());
        btnCerrarSesion.setOnAction(e -> cerrarSesion());
        btnNotificaciones.setOnAction(e -> { AlertasDialog.mostrar(cliente); refrescar(); });
        btnToggleSaldo.setOnAction(e -> { saldoVisible = !saldoVisible; refrescar(); });
        btnRecargarRapido.setOnAction(e -> mostrarSeccion("recargar"));
        btnEnviarRapido.setOnAction(e -> mostrarSeccion("enviar"));
        btnEnviarDinero.setOnAction(e -> enviarDinero());
        btnRecargarSaldo.setOnAction(e -> recargarSaldo());
        btnMonto20.setOnAction(e -> txtMontoRecarga.setText("20000"));
        btnMonto50.setOnAction(e -> txtMontoRecarga.setText("50000"));
        btnMonto100.setOnAction(e -> txtMontoRecarga.setText("100000"));
        lblMensajeEnvio.setText("");
        lblMensajeRecarga.setText("");
        cmbFiltro.setItems(FXCollections.observableArrayList(
                "Todos", "Ingresos", "Gastos", "Depósitos", "Retiros", "Transferencias", "Sospechosas"));
        cmbFiltro.setValue("Todos");
        cmbFiltro.setOnAction(e -> aplicarFiltro());
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());

        refrescar();
    }

    private void agregarBotonBeneficios() {
        btnBeneficiosNav = new Button("🎁  Beneficios");
        btnBeneficiosNav.setMaxWidth(Double.MAX_VALUE);
        btnBeneficiosNav.getStyleClass().add("nav-button");
        Pane padre = (Pane) btnHistorial.getParent();
        padre.getChildren().add(padre.getChildren().indexOf(btnHistorial) + 1, btnBeneficiosNav);
    }

    private void configurarTabla() {
        ViewUtil.columna(colFecha, m -> ViewUtil.fecha(m.fecha()));
        ViewUtil.columna(colDescripcion, Movimiento::descripcion);
        ViewUtil.columna(colTipo, m -> m.sospechosa() ? m.tipo() + " ⚠" : m.tipo());
        ViewUtil.columna(colMonto, m -> (m.ingreso() ? "+ " : "− ") + ViewUtil.dinero(m.monto()));
        Comparator<Movimiento> orden = Comparator.comparing(Movimiento::fecha);
        orden = orden.thenComparing(Movimiento::id).reversed();
        tblMovimientos.setItems(new SortedList<>(filtrados, orden));
        tblMovimientos.setPlaceholder(new Label("Aún no tienes movimientos"));
    }

    private void cargarPerfil() {
        String primerNombre = cliente.getNombre().trim().split("\\s+")[0];
        lblIniciales.setText(ViewUtil.iniciales(cliente.getNombre()));
        lblUsuario.setText(cliente.getNombre());
        lblCorreo.setText(cliente.getCorreo());
        lblBienvenida.setText("Hola, " + primerNombre + " 👋");
        String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CO")));
        lblFecha.setText(Character.toUpperCase(hoy.charAt(0)) + hoy.substring(1));
    }

    private Monedero principal() {
        String numero = PRINCIPAL.get(cliente.getUser());
        if (numero != null) {
            Optional<Monedero> elegido = cliente.buscarMonedero(numero);
            if (elegido.isPresent()) return elegido.get();
        }
        return cliente.getListaMonederos().isEmpty() ? null : cliente.getListaMonederos().get(0);
    }

    private void refrescar() {
        Monedero p = principal();
        double total = cliente.getListaMonederos().stream().mapToDouble(Monedero::getSaldo).sum();
        lblSaldo.setText(saldoVisible ? ViewUtil.dinero(total) : "$ ••••••••");
        btnToggleSaldo.setText(saldoVisible ? "👁" : "🙈");
        int n = cliente.getListaMonederos().size();
        lblNumeroCuenta.setText(p == null ? "Sin monedero · crea uno en Mis tarjetas"
                : "N.º de cuenta  " + p.getNumeroCuenta() + (n > 1 ? "  (+" + (n - 1) + " más)" : ""));

        base.setAll(construirMovimientos());
        YearMonth mes = YearMonth.now();
        double ingresos = base.stream().filter(m -> m.estado() != EstadoTransaccion.CANCELADA && m.ingreso()
                && YearMonth.from(m.fecha()).equals(mes)).mapToDouble(Movimiento::monto).sum();
        double gastos = base.stream().filter(m -> m.estado() != EstadoTransaccion.CANCELADA && !m.ingreso()
                && YearMonth.from(m.fecha()).equals(mes)).mapToDouble(Movimiento::monto).sum();
        lblIngresos.setText(ViewUtil.dinero(ingresos));
        lblGastos.setText(ViewUtil.dinero(gastos));

        boolean saldoBajo = cliente.getListaMonederos().stream().anyMatch(m -> m.getSaldo() < OperacionController.SALDO_BAJO);
        lblEstadoCuenta.setText(saldoBajo ? "● Saldo bajo" : "● Cuenta activa");
        int alertas = App.getAlertaController().getAlertasCliente(cliente).size();
        btnNotificaciones.setText(alertas == 0 ? "🔔" : "🔔 " + alertas);
        aplicarFiltro();
    }

    private List<Movimiento> construirMovimientos() {
        List<Movimiento> lista = new ArrayList<>();
        Set<String> mias = cliente.getListaMonederos().stream().map(Monedero::getNumeroCuenta).collect(Collectors.toSet());

        for (Transaccion t : cliente.getListaTransacciones()) {
            boolean cancelada = t.getEstado() == EstadoTransaccion.CANCELADA;
            if (t instanceof Deposito) {
                lista.add(new Movimiento(t.getIdTransaccion(), t.getFecha(), "Recarga de saldo",
                        cancelada ? "Cancelada" : "Depósito", "DEP", t.getMonto(), true, t.isSospechosa(), t.getEstado()));
            } else if (t instanceof Retiro) {
                lista.add(new Movimiento(t.getIdTransaccion(), t.getFecha(), "Retiro de dinero",
                        cancelada ? "Cancelada" : "Retiro", "RET", t.getMonto(), false, t.isSospechosa(), t.getEstado()));
            } else if (t instanceof Transferencia tr) {
                String concepto = op.getConcepto(tr.getIdTransaccion());
                String desc = "Envío a " + op.nombreTitular(tr.getMonederoDestino()) + (concepto == null ? "" : " · " + concepto);
                lista.add(new Movimiento(tr.getIdTransaccion(), tr.getFecha(), desc,
                        cancelada ? "Cancelada" : "Enviada", "TRF", tr.getMonto(), false, tr.isSospechosa(), tr.getEstado()));
            }
        }
        for (Transferencia tr : App.getBanco().getListaTransferencias()) {
            boolean paraMi = mias.contains(tr.getMonederoDestino().getNumeroCuenta());
            boolean deMi = mias.contains(tr.getMonedero().getNumeroCuenta());
            if (paraMi && !deMi) {
                String concepto = op.getConcepto(tr.getIdTransaccion());
                String desc = "Recibido de " + op.nombreTitular(tr.getMonedero()) + (concepto == null ? "" : " · " + concepto);
                boolean cancelada = tr.getEstado() == EstadoTransaccion.CANCELADA;
                lista.add(new Movimiento(tr.getIdTransaccion(), tr.getFecha(), desc,
                        cancelada ? "Cancelada" : "Recibida", "TRF", tr.getMonto(), true, false, tr.getEstado()));
            }
        }
        return lista;
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String f = cmbFiltro.getValue() == null ? "Todos" : cmbFiltro.getValue();
        filtrados.setPredicate(m -> {
            boolean texto = q.isEmpty() || m.descripcion().toLowerCase().contains(q)
                    || m.tipo().toLowerCase().contains(q) || m.id().toLowerCase().contains(q);
            boolean categoria = switch (f) {
                case "Ingresos" -> m.ingreso();
                case "Gastos" -> !m.ingreso();
                case "Depósitos" -> m.categoria().equals("DEP");
                case "Retiros" -> m.categoria().equals("RET");
                case "Transferencias" -> m.categoria().equals("TRF");
                case "Sospechosas" -> m.sospechosa();
                default -> true;
            };
            return texto && categoria;
        });
    }

    private void enviarDinero() {
        String[] resultado = new String[1];
        String error = ViewUtil.intentar(() -> {
            Monedero origen = principal();
            Monedero destino = op.buscarMonederoDestino(txtDestinatario.getText());
            double monto = OperacionController.parseMonto(txtMonto.getText());
            resultado[0] = op.transferir(cliente, origen, destino, monto, txtConcepto.getText());
        });
        if (error != null) {
            ViewUtil.mensaje(lblMensajeEnvio, "✖ " + error, false);
            return;
        }
        txtDestinatario.clear();
        txtMonto.clear();
        txtConcepto.clear();
        refrescar();
        ViewUtil.mensaje(lblMensajeEnvio, "✔ " + resultado[0], true);
        if (resultado[0].contains("sospechosa")) ViewUtil.advertencia(resultado[0]);
    }

    private void recargarSaldo() {
        String[] resultado = new String[1];
        String error = ViewUtil.intentar(() ->
                resultado[0] = op.recargar(cliente, principal(), OperacionController.parseMonto(txtMontoRecarga.getText())));
        if (error != null) {
            ViewUtil.mensaje(lblMensajeRecarga, "✖ " + error, false);
            return;
        }
        txtMontoRecarga.clear();
        refrescar();
        ViewUtil.mensaje(lblMensajeRecarga, "✔ " + resultado[0], true);
        if (resultado[0].contains("sospechosa")) ViewUtil.advertencia(resultado[0]);
    }

    private void abrirMonederos() {
        MonederosDialog.mostrar(cliente, principal(), elegido -> {
            PRINCIPAL.put(cliente.getUser(), elegido.getNumeroCuenta());
            refrescar();
        });
        refrescar();
    }

    private void abrirConfiguracion() {
        FormularioUsuarioDialog.mostrar(cliente, false).ifPresent(u -> {
            cargarPerfil();
            refrescar();
        });
    }

    private void cerrarSesion() {
        App.getUsuarioController().cerrarSesion();
        App.irLogin();
    }

    public void mostrarSeccion(String seccion) {
        if (cliente == null) return;
        Button activo;
        Node destino;
        switch (seccion == null ? "inicio" : seccion) {
            case "enviar": activo = btnEnviar; destino = txtDestinatario; break;
            case "recargar": activo = btnRecargar; destino = txtMontoRecarga; break;
            case "historial": activo = btnHistorial; destino = tblMovimientos; break;
            default: activo = btnInicio; destino = null; break;
        }
        ViewUtil.activar(activo, "nav-button", "nav-button-active",
                btnInicio, btnEnviar, btnRecargar, btnHistorial, btnBeneficiosNav, btnTarjetas, btnConfiguracion);
        desplazarA(destino);
        if (destino instanceof TextField) Platform.runLater(destino::requestFocus);
    }

    private void desplazarA(Node nodo) {
        if (!(rootPane.getCenter() instanceof ScrollPane sp)) return;
        Platform.runLater(() -> {
            if (nodo == null) { sp.setVvalue(0); return; }
            sp.applyCss();
            sp.layout();
            Node contenido = sp.getContent();
            double recorrible = contenido.getBoundsInLocal().getHeight() - sp.getViewportBounds().getHeight();
            if (recorrible <= 0) return;
            double y = nodo.localToScene(nodo.getBoundsInLocal()).getMinY()
                    - contenido.localToScene(contenido.getBoundsInLocal()).getMinY();
            sp.setVvalue(Math.max(0, Math.min(1, y / recorrible)));
        });
    }
}