package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

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
    @FXML private Button btnInicio, btnEnviar, btnRecargar, btnHistorial, btnProgramadas, btnTarjetas, btnConfiguracion, btnCerrarSesion;
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

    @FXML private VBox cardProgramadas;
    @FXML private TableView<TransaccionProgramada> tblProgramadas;
    @FXML private TableColumn<TransaccionProgramada, String> colProgId, colProgTipo, colProgMonto, colProgFrecuencia, colProgProxima, colProgMonedero;
    @FXML private Button btnNuevaProgramada, btnEliminarProgramada;

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
        btnProgramadas.setOnAction(e -> mostrarSeccion("programadas"));
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
        btnNuevaProgramada.setOnAction(e -> abrirDialogoNuevaProgramada());
        btnEliminarProgramada.setOnAction(e -> eliminarProgramada());
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

        // Tabla de transacciones programadas
        ViewUtil.columna(colProgId, TransaccionProgramada::getIdTransaccion);
        ViewUtil.columna(colProgTipo, p -> p.getTransaccion().getClass().getSimpleName());
        ViewUtil.columna(colProgMonto, p -> ViewUtil.dinero(p.getMonto()));
        ViewUtil.columna(colProgFrecuencia, p -> p.getFrecuencia().toString());
        ViewUtil.columna(colProgProxima, p -> ViewUtil.fecha(p.getFechaEjecucion()));
        ViewUtil.columna(colProgMonedero, p -> p.getMonedero().getNumeroCuenta());
        tblProgramadas.setPlaceholder(new Label("No tienes transacciones programadas"));
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
        lblIngresos.setText(ViewUtil.dinero(sumaDelMes(base, true)));
        lblGastos.setText(ViewUtil.dinero(sumaDelMes(base, false)));

        boolean saldoBajo = cliente.getListaMonederos().stream().anyMatch(m -> m.getSaldo() < OperacionController.SALDO_BAJO);
        lblEstadoCuenta.setText(saldoBajo ? "● Saldo bajo" : "● Cuenta activa");
        int alertas = App.getAlertaController().getAlertasCliente(cliente).size();
        btnNotificaciones.setText(alertas == 0 ? "🔔" : "🔔 " + alertas);
        aplicarFiltro();
    }

    private void cargarProgramadas() {
        ObservableList<TransaccionProgramada> items = FXCollections.observableArrayList(cliente.getListaTransaccionesProgramadas());
        tblProgramadas.setItems(items);
    }

    private void abrirDialogoNuevaProgramada() {
        if (cliente.getListaMonederos().isEmpty()) {
            ViewUtil.advertencia("Primero crea al menos un monedero.");
            return;
        }

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Nueva transacción programada");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Monedero> cmbMonedero = new ComboBox<>(FXCollections.observableArrayList(cliente.getListaMonederos()));
        cmbMonedero.setPromptText("Selecciona monedero");
        cmbMonedero.setConverter(new javafx.util.StringConverter<Monedero>() {
            @Override public String toString(Monedero m) { return m == null ? "" : m.getNumeroCuenta() + " (" + (m instanceof MonederoAhorro ? "Ahorro" : "Gasto diario") + ")"; }
            @Override public Monedero fromString(String s) { return null; }
        });

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList("Depósito", "Retiro", "Transferencia"));
        cmbTipo.setPromptText("Tipo de transacción");
        cmbTipo.setValue("Depósito");

        TextField txtMonto = new TextField();
        txtMonto.setPromptText("Monto (ej: 50000)");

        TextField txtConcepto = new TextField();
        txtConcepto.setPromptText("Concepto (opcional)");

        ComboBox<Frecuencia> cmbFrecuencia = new ComboBox<>(FXCollections.observableArrayList(Frecuencia.values()));
        cmbFrecuencia.setPromptText("Frecuencia");
        cmbFrecuencia.setValue(Frecuencia.MENSUAL);

        DatePicker dpFecha = new DatePicker(LocalDate.now().plusDays(1));
        dpFecha.setPromptText("Fecha de inicio");

        ComboBox<Monedero> cmbDestino = new ComboBox<>();
        cmbDestino.setPromptText("Monedero destino (solo transferencia)");
        cmbDestino.setDisable(true);
        cmbDestino.setConverter(new javafx.util.StringConverter<Monedero>() {
            @Override public String toString(Monedero m) { return m == null ? "" : m.getNumeroCuenta() + " - " + op.nombreTitular(m); }
            @Override public Monedero fromString(String s) { return null; }
        });

        // Actualizar monederos destino según el monedero origen seleccionado
        cmbMonedero.valueProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                cmbDestino.setItems(FXCollections.observableArrayList(
                        cliente.getListaMonederos().stream().filter(m -> m != nuevo).toList()));
            }
        });

        // Habilitar/deshabilitar campo destino según el tipo
        cmbTipo.valueProperty().addListener((obs, old, nuevo) -> {
            boolean esTransferencia = "Transferencia".equals(nuevo);
            cmbDestino.setDisable(!esTransferencia);
            txtConcepto.setDisable(!esTransferencia);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Monedero"), cmbMonedero);
        grid.addRow(1, new Label("Tipo"), cmbTipo);
        grid.addRow(2, new Label("Monto"), txtMonto);
        grid.addRow(3, new Label("Frecuencia"), cmbFrecuencia);
        grid.addRow(4, new Label("Fecha inicio"), dpFecha);
        grid.addRow(5, new Label("Destino"), cmbDestino);
        grid.addRow(6, new Label("Concepto"), txtConcepto);
        d.getDialogPane().setContent(grid);

        d.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                Monedero m = cmbMonedero.getValue();
                if (m == null) throw new DatosInvalidosException("Selecciona un monedero.");
                String tipo = cmbTipo.getValue();
                if (tipo == null) throw new DatosInvalidosException("Selecciona un tipo.");
                double monto = OperacionController.parseMonto(txtMonto.getText());
                Frecuencia freq = cmbFrecuencia.getValue();
                if (freq == null) throw new DatosInvalidosException("Selecciona una frecuencia.");
                LocalDate fecha = dpFecha.getValue();
                if (fecha == null) throw new DatosInvalidosException("Selecciona una fecha de inicio.");
                if (fecha.isBefore(LocalDate.now())) throw new DatosInvalidosException("La fecha no puede ser anterior a hoy.");

                String msg;
                if ("Depósito".equals(tipo)) {
                    msg = op.programarDeposito(cliente, m, monto, fecha, freq);
                } else if ("Retiro".equals(tipo)) {
                    msg = op.programarRetiro(cliente, m, monto, fecha, freq);
                } else { // Transferencia
                    Monedero destino = cmbDestino.getValue();
                    if (destino == null) throw new DatosInvalidosException("Selecciona un monedero destino.");
                    msg = op.programarTransferencia(cliente, m, destino, monto, txtConcepto.getText(), fecha, freq);
                }
                ViewUtil.info(msg);
                cargarProgramadas();
            } catch (MonederoException ex) {
                ViewUtil.error(ex.getMessage());
                ev.consume();
            }
        });

        d.showAndWait();
    }

    private void eliminarProgramada() {
        TransaccionProgramada sel = tblProgramadas.getSelectionModel().getSelectedItem();
        if (sel == null) { ViewUtil.advertencia("Selecciona una transacción programada."); return; }
        if (!ViewUtil.confirmar("¿Eliminar la transacción programada " + sel.getIdTransaccion() + "?")) return;
        try {
            String msg = op.eliminarTransaccionProgramada(cliente, sel.getIdTransaccion());
            ViewUtil.info(msg);
            cargarProgramadas();
        } catch (MonederoException e) {
            ViewUtil.error(e.getMessage());
        }
    }

    private double sumaDelMes(List<Movimiento> movimientos, boolean esIngreso) {
        YearMonth mes = YearMonth.now();
        double total = 0.0;
        for (Movimiento m : movimientos) {
            boolean cancelado = m.estado() == EstadoTransaccion.CANCELADA;
            boolean esDelMes = YearMonth.from(m.fecha()).equals(mes);
            if (!cancelado && m.ingreso() == esIngreso && esDelMes) {
                total += m.monto();
            }
        }
        return total;
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
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String categoria = cmbFiltro.getValue() == null ? "Todos" : cmbFiltro.getValue();
        filtrados.setPredicate(m -> coincideTexto(m, texto) && coincideCategoria(m, categoria));
    }

    private boolean coincideTexto(Movimiento m, String texto) {
        if (texto.isEmpty()) return true;
        String buscado = texto.toLowerCase();
        return m.descripcion().toLowerCase().contains(buscado)
                || m.tipo().toLowerCase().contains(buscado)
                || m.id().toLowerCase().contains(buscado);
    }

    private boolean coincideCategoria(Movimiento m, String categoria) {
        switch (categoria) {
            case "Ingresos":       return m.ingreso();
            case "Gastos":         return !m.ingreso();
            case "Depósitos":      return "DEP".equals(m.categoria());
            case "Retiros":        return "RET".equals(m.categoria());
            case "Transferencias": return "TRF".equals(m.categoria());
            case "Sospechosas":    return m.sospechosa();
            default:               return true;
        }
    }

    private void enviarDinero() {
        String mensaje;
        try {
            Monedero origen = principal();
            Monedero destino = op.buscarMonederoDestino(txtDestinatario.getText());
            double monto = OperacionController.parseMonto(txtMonto.getText());
            mensaje = op.transferir(cliente, origen, destino, monto, txtConcepto.getText());
        } catch (MonederoException e) {
            ViewUtil.mensaje(lblMensajeEnvio, "✖ " + e.getMessage(), false);
            return;
        }
        txtDestinatario.clear();
        txtMonto.clear();
        txtConcepto.clear();
        refrescar();
        ViewUtil.mensaje(lblMensajeEnvio, "✔ " + mensaje, true);
        if (mensaje.contains("sospechosa")) ViewUtil.advertencia(mensaje);
    }

    private void recargarSaldo() {
        String mensaje;
        try {
            double monto = OperacionController.parseMonto(txtMontoRecarga.getText());
            mensaje = op.recargar(cliente, principal(), monto);
        } catch (MonederoException e) {
            ViewUtil.mensaje(lblMensajeRecarga, "✖ " + e.getMessage(), false);
            return;
        }
        txtMontoRecarga.clear();
        refrescar();
        ViewUtil.mensaje(lblMensajeRecarga, "✔ " + mensaje, true);
        if (mensaje.contains("sospechosa")) ViewUtil.advertencia(mensaje);
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
        String s = seccion == null ? "inicio" : seccion;
        Button activo;
        Node destino;
        boolean verProgramadas = s.equals("programadas");
        ViewUtil.mostrar(cardProgramadas, verProgramadas);
        switch (s) {
            case "enviar": activo = btnEnviar; destino = txtDestinatario; break;
            case "recargar": activo = btnRecargar; destino = txtMontoRecarga; break;
            case "historial": activo = btnHistorial; destino = tblMovimientos; break;
            case "programadas": activo = btnProgramadas; destino = tblProgramadas; break;
            default: activo = btnInicio; destino = null; break;
        }
        ViewUtil.activar(activo, "nav-button", "nav-button-active",
                btnInicio, btnEnviar, btnRecargar, btnHistorial, btnProgramadas, btnBeneficiosNav, btnTarjetas, btnConfiguracion);
        if (verProgramadas) cargarProgramadas();
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