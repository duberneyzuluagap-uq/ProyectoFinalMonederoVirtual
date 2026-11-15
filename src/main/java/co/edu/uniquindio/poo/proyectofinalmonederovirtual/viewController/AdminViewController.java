package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController.Canje;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminViewController {

    @FXML private BorderPane rootAdmin;
    @FXML private Label lblInicialesAdmin, lblNombreAdmin, lblCorreoAdmin, lblTituloAdmin, lblSubtituloAdmin, lblEstadoSistema;
    @FXML private Button btnPanelGeneral, btnUsuarios, btnTransacciones, btnBeneficiosAdmin, btnCatalogoBeneficios,
            btnCanjesPendientes, btnReportes, btnConfiguracionAdmin, btnCerrarSesionAdmin, btnNotificacionesAdmin;
    @FXML private VBox subMenuBeneficiosAdmin;
    @FXML private Label lblTotalUsuarios, lblSaldoCirculacion, lblTransaccionesHoy, lblCanjesPendientes, lblMensajeAdmin;

    @FXML private VBox cardUsuarios, cardTransacciones, cardBeneficiosAdmin, cardCanjesAdmin;

    @FXML private TextField txtBuscarUsuario;
    @FXML private ComboBox<String> cmbEstadoUsuario;
    @FXML private Button btnNuevoUsuario, btnVerDetalleUsuario, btnEditarUsuario, btnBloquearUsuario, btnEliminarUsuario;
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuId, colUsuNombre, colUsuCorreo, colUsuRol, colUsuSaldo, colUsuEstado;

    @FXML private TextField txtBuscarTransaccion;
    @FXML private ComboBox<String> cmbTipoTransaccion;
    @FXML private Button btnVerDetalleTransaccion, btnReversarTransaccion, btnExportarTransacciones;
    @FXML private TableView<Transaccion> tblTransacciones;
    @FXML private TableColumn<Transaccion, String> colTrxId, colTrxFecha, colTrxOrigen, colTrxDestino, colTrxTipo, colTrxMonto, colTrxEstado;

    @FXML private TextField txtBuscarBeneficioAdmin;
    @FXML private Button btnNuevoBeneficio, btnEditarBeneficio, btnActivarBeneficio, btnEliminarBeneficio;
    @FXML private TableView<Beneficio> tblBeneficiosAdmin;
    @FXML private TableColumn<Beneficio, String> colBenId, colBenNombre, colBenCategoria, colBenPuntos, colBenStock, colBenEstado;

    @FXML private ComboBox<String> cmbEstadoCanje;
    @FXML private Button btnAprobarCanje, btnRechazarCanje;
    @FXML private TableView<Canje> tblCanjesAdmin;
    @FXML private TableColumn<Canje, String> colCanId, colCanUsuario, colCanBeneficio, colCanPuntos, colCanFecha, colCanEstado;

    private Admin admin;
    private final OperacionController op = App.getOperacionController();
    private final ObservableList<Usuario> baseUsuarios = FXCollections.observableArrayList();
    private final ObservableList<Transaccion> baseTrx = FXCollections.observableArrayList();
    private final ObservableList<Beneficio> baseBeneficios = FXCollections.observableArrayList();
    private final ObservableList<Canje> baseCanjes = FXCollections.observableArrayList();
    private final FilteredList<Usuario> usuarios = new FilteredList<>(baseUsuarios, u -> true);
    private final FilteredList<Transaccion> transacciones = new FilteredList<>(baseTrx, t -> true);
    private final FilteredList<Beneficio> beneficios = new FilteredList<>(baseBeneficios, b -> true);
    private final FilteredList<Canje> canjes = new FilteredList<>(baseCanjes, c -> true);

    @FXML
    private void initialize() {
        Usuario actual = App.getUsuarioController().getUsuarioActual();
        if (!(actual instanceof Admin a)) {
            javafx.application.Platform.runLater(App::irLogin);
            return;
        }
        admin = a;
        lblInicialesAdmin.setText(ViewUtil.iniciales(a.getNombre()));
        lblNombreAdmin.setText(a.getNombre());
        lblCorreoAdmin.setText(a.getCorreo());
        lblMensajeAdmin.setText("");

        configurarTablas();
        configurarFiltros();
        configurarAcciones();
        mostrarSeccion("general");
        refrescar();
    }

    private void configurarTablas() {
        ViewUtil.columna(colUsuId, Usuario::getUser);
        ViewUtil.columna(colUsuNombre, Usuario::getNombre);
        ViewUtil.columna(colUsuCorreo, Usuario::getCorreo);
        ViewUtil.columna(colUsuRol, u -> u instanceof Admin ? "Administrador" : "Cliente");
        ViewUtil.columna(colUsuSaldo, u -> u instanceof Cliente c ? ViewUtil.dinero(saldoDe(c)) : "—");
        ViewUtil.columna(colUsuEstado, u -> op.isBloqueado(u.getUser()) ? "Bloqueado" : "Activo");
        tblUsuarios.setItems(usuarios);

        ViewUtil.columna(colTrxId, Transaccion::getIdTransaccion);
        ViewUtil.columna(colTrxFecha, t -> ViewUtil.fecha(t.getFecha()));
        ViewUtil.columna(colTrxOrigen, t -> op.nombreTitular(t.getMonedero()));
        ViewUtil.columna(colTrxDestino, t -> t instanceof Transferencia tr ? op.nombreTitular(tr.getMonederoDestino()) : "—");
        ViewUtil.columna(colTrxTipo, this::tipoDe);
        ViewUtil.columna(colTrxMonto, t -> ViewUtil.dinero(t.getMonto()));
        ViewUtil.columna(colTrxEstado, t -> t.getEstado() + (t.isSospechosa() ? " ⚠" : ""));
        Comparator<Transaccion> porFecha = Comparator.comparing(Transaccion::getFecha);
        porFecha = porFecha.thenComparing(Transaccion::getIdTransaccion).reversed();
        tblTransacciones.setItems(new SortedList<>(transacciones, porFecha));

        ViewUtil.columna(colBenId, Beneficio::getIdBeneficio);
        ViewUtil.columna(colBenNombre, Beneficio::getNombre);
        ViewUtil.columna(colBenCategoria, ViewUtil::categoriaBeneficio);
        ViewUtil.columna(colBenPuntos, b -> ViewUtil.numero(b.getPuntosRequeridos()));
        ViewUtil.columna(colBenStock, b -> "Ilimitado");
        ViewUtil.columna(colBenEstado, b -> op.isBeneficioActivo(b.getIdBeneficio()) ? "Activo" : "Inactivo");
        tblBeneficiosAdmin.setItems(beneficios);

        ViewUtil.columna(colCanId, Canje::codigo);
        ViewUtil.columna(colCanUsuario, c -> c.cliente().getNombre());
        ViewUtil.columna(colCanBeneficio, Canje::beneficio);
        ViewUtil.columna(colCanPuntos, c -> "⭐ " + ViewUtil.numero(c.puntos()));
        ViewUtil.columna(colCanFecha, c -> ViewUtil.fecha(c.movimiento().getFecha()));
        ViewUtil.columna(colCanEstado, Canje::estado);
        tblCanjesAdmin.setItems(canjes);
    }

    private void configurarFiltros() {
        cmbEstadoUsuario.setItems(FXCollections.observableArrayList("Todos", "Activo", "Bloqueado"));
        cmbEstadoUsuario.setValue("Todos");
        cmbTipoTransaccion.setItems(FXCollections.observableArrayList("Todos", "Depósito", "Retiro", "Transferencia", "Sospechosas"));
        cmbTipoTransaccion.setValue("Todos");
        cmbEstadoCanje.setItems(FXCollections.observableArrayList("Todos", "Pendiente", "Aprobado", "Rechazado"));
        cmbEstadoCanje.setValue("Todos");

        txtBuscarUsuario.textProperty().addListener((o, a, b) -> filtrarUsuarios());
        cmbEstadoUsuario.setOnAction(e -> filtrarUsuarios());
        txtBuscarTransaccion.textProperty().addListener((o, a, b) -> filtrarTransacciones());
        cmbTipoTransaccion.setOnAction(e -> filtrarTransacciones());
        txtBuscarBeneficioAdmin.textProperty().addListener((o, a, b) -> filtrarBeneficios());
        cmbEstadoCanje.setOnAction(e -> filtrarCanjes());

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((o, a, u) ->
                btnBloquearUsuario.setText(u != null && op.isBloqueado(u.getUser()) ? "✔ Desbloquear" : "⛔ Bloquear"));
    }

    private void configurarAcciones() {
        btnPanelGeneral.setOnAction(e -> mostrarSeccion("general"));
        btnUsuarios.setOnAction(e -> mostrarSeccion("usuarios"));
        btnTransacciones.setOnAction(e -> mostrarSeccion("transacciones"));
        btnBeneficiosAdmin.setOnAction(e -> {
            ViewUtil.mostrar(subMenuBeneficiosAdmin, !subMenuBeneficiosAdmin.isVisible() || !cardBeneficiosAdmin.isVisible());
            mostrarSeccion("catalogo");
        });
        btnCatalogoBeneficios.setOnAction(e -> mostrarSeccion("catalogo"));
        btnCanjesPendientes.setOnAction(e -> mostrarSeccion("canjes"));
        btnReportes.setOnAction(e -> mostrarReportes());
        btnConfiguracionAdmin.setOnAction(e -> abrirConfiguracion());
        btnNotificacionesAdmin.setOnAction(e -> mostrarNotificaciones());
        btnCerrarSesionAdmin.setOnAction(e -> { App.getUsuarioController().cerrarSesion(); App.irLogin(); });
        btnNuevoUsuario.setOnAction(e -> FormularioUsuarioDialog.mostrar(null, true).ifPresent(u -> {
            refrescar();
            ViewUtil.mensaje(lblMensajeAdmin, "✔ Usuario '" + u.getUser() + "' creado.", true);
        }));
        btnEditarUsuario.setOnAction(e -> {
            Usuario sel = tblUsuarios.getSelectionModel().getSelectedItem();
            if (sel == null) { aviso("Selecciona un usuario."); return; }
            FormularioUsuarioDialog.mostrar(sel, false).ifPresent(u -> {
                refrescar();
                ViewUtil.mensaje(lblMensajeAdmin, "✔ Usuario actualizado.", true);
            });
        });
        btnVerDetalleUsuario.setOnAction(e -> verDetalleUsuario());
        btnBloquearUsuario.setOnAction(e -> bloquearUsuario());
        btnEliminarUsuario.setOnAction(e -> eliminarUsuario());
        btnVerDetalleTransaccion.setOnAction(e -> verDetalleTransaccion());
        btnReversarTransaccion.setOnAction(e -> reversarTransaccion());
        btnExportarTransacciones.setOnAction(e -> exportarTransacciones());
        btnNuevoBeneficio.setOnAction(e -> { if (dialogoBeneficio(null)) refrescar(); });
        btnEditarBeneficio.setOnAction(e -> {
            Beneficio sel = tblBeneficiosAdmin.getSelectionModel().getSelectedItem();
            if (sel == null) { aviso("Selecciona un beneficio."); return; }
            if (dialogoBeneficio(sel)) refrescar();
        });
        btnActivarBeneficio.setOnAction(e -> {
            Beneficio sel = tblBeneficiosAdmin.getSelectionModel().getSelectedItem();
            if (sel == null) { aviso("Selecciona un beneficio."); return; }
            boolean activo = op.cambiarEstadoBeneficio(sel.getIdBeneficio());
            refrescar();
            ViewUtil.mensaje(lblMensajeAdmin, "✔ Beneficio " + (activo ? "activado." : "desactivado."), true);
        });
        btnEliminarBeneficio.setOnAction(e -> {
            Beneficio sel = tblBeneficiosAdmin.getSelectionModel().getSelectedItem();
            if (sel == null) { aviso("Selecciona un beneficio."); return; }
            if (!ViewUtil.confirmar("¿Eliminar el beneficio «" + sel.getNombre() + "»?")) return;
            String msg = App.getBeneficioController().eliminarBeneficio(sel.getIdBeneficio());
            refrescar();
            ViewUtil.mensaje(lblMensajeAdmin, "✔ " + msg, true);
        });
        btnAprobarCanje.setOnAction(e -> {
            Canje sel = tblCanjesAdmin.getSelectionModel().getSelectedItem();
            String err = ViewUtil.intentar(() -> op.aprobarCanje(sel));
            resultado(err, "Canje aprobado.");
        });
        btnRechazarCanje.setOnAction(e -> {
            Canje sel = tblCanjesAdmin.getSelectionModel().getSelectedItem();
            if (sel != null && !ViewUtil.confirmar("¿Rechazar el canje? Se devolverán los puntos al cliente.")) return;
            String err = ViewUtil.intentar(() -> op.rechazarCanje(sel));
            resultado(err, "Canje rechazado y puntos devueltos.");
        });
    }
    private void refrescar() {
        baseUsuarios.setAll(App.getUsuarioController().getListaUsuarios());
        baseTrx.setAll(App.getTransaccionController().getHistorialGeneralBanco());
        baseBeneficios.setAll(App.getBeneficioController().getListaBeneficios());
        baseCanjes.setAll(op.listarTodosLosCanjes());
        filtrarUsuarios();
        filtrarTransacciones();
        filtrarBeneficios();
        filtrarCanjes();
        tblUsuarios.refresh();
        tblBeneficiosAdmin.refresh();
        tblCanjesAdmin.refresh();

        double circulacion = App.getBanco().getListaMonederos().stream().mapToDouble(Monedero::getSaldo).sum();
        long hoy = baseTrx.stream().filter(t -> t.getFecha().equals(LocalDate.now())).count();
        long pendientes = baseCanjes.stream().filter(c -> c.estado().equals("Pendiente")).count();
        lblTotalUsuarios.setText(String.valueOf(baseUsuarios.size()));
        lblSaldoCirculacion.setText(ViewUtil.dinero(circulacion));
        lblTransaccionesHoy.setText(String.valueOf(hoy));
        lblCanjesPendientes.setText(String.valueOf(pendientes));
    }

    private double saldoDe(Cliente c) { return c.getListaMonederos().stream().mapToDouble(Monedero::getSaldo).sum(); }

    private String tipoDe(Transaccion t) {
        if (t instanceof Deposito) return "Depósito";
        if (t instanceof Retiro) return "Retiro";
        if (t instanceof Transferencia) return "Transferencia";
        return t.getClass().getSimpleName();
    }

    private void filtrarUsuarios() {
        String q = minuscula(txtBuscarUsuario.getText());
        String estado = cmbEstadoUsuario.getValue() == null ? "Todos" : cmbEstadoUsuario.getValue();
        usuarios.setPredicate(u -> {
            boolean texto = q.isEmpty() || u.getUser().toLowerCase().contains(q)
                    || u.getNombre().toLowerCase().contains(q) || u.getCorreo().toLowerCase().contains(q);
            boolean bloqueado = op.isBloqueado(u.getUser());
            boolean porEstado = estado.equals("Todos") || (estado.equals("Bloqueado") == bloqueado);
            return texto && porEstado;
        });
    }

    private void filtrarTransacciones() {
        String q = minuscula(txtBuscarTransaccion.getText());
        String tipo = cmbTipoTransaccion.getValue() == null ? "Todos" : cmbTipoTransaccion.getValue();
        transacciones.setPredicate(t -> {
            String origen = op.nombreTitular(t.getMonedero()).toLowerCase();
            String destino = t instanceof Transferencia tr ? op.nombreTitular(tr.getMonederoDestino()).toLowerCase() : "";
            boolean texto = q.isEmpty() || t.getIdTransaccion().toLowerCase().contains(q) || origen.contains(q) || destino.contains(q);
            boolean porTipo = tipo.equals("Todos") || (tipo.equals("Sospechosas") ? t.isSospechosa() : tipoDe(t).equals(tipo));
            return texto && porTipo;
        });
    }

    private void filtrarBeneficios() {
        String q = minuscula(txtBuscarBeneficioAdmin.getText());
        beneficios.setPredicate(b -> q.isEmpty() || b.getNombre().toLowerCase().contains(q)
                || b.getIdBeneficio().toLowerCase().contains(q) || ViewUtil.categoriaBeneficio(b).toLowerCase().contains(q));
    }

    private void filtrarCanjes() {
        String estado = cmbEstadoCanje.getValue() == null ? "Todos" : cmbEstadoCanje.getValue();
        canjes.setPredicate(c -> estado.equals("Todos") || c.estado().equals(estado));
    }

    private String minuscula(String s) { return s == null ? "" : s.trim().toLowerCase(); }
    private void mostrarSeccion(String seccion) {
        boolean general = seccion.equals("general");
        ViewUtil.mostrar(cardUsuarios, general || seccion.equals("usuarios"));
        ViewUtil.mostrar(cardTransacciones, general || seccion.equals("transacciones"));
        ViewUtil.mostrar(cardBeneficiosAdmin, general || seccion.equals("catalogo"));
        ViewUtil.mostrar(cardCanjesAdmin, general || seccion.equals("canjes"));

        Button activo;
        switch (seccion) {
            case "usuarios": activo = btnUsuarios; break;
            case "transacciones": activo = btnTransacciones; break;
            case "catalogo": case "canjes": activo = btnBeneficiosAdmin; break;
            default: activo = btnPanelGeneral; break;
        }
        ViewUtil.activar(activo, "nav-button", "nav-button-active",
                btnPanelGeneral, btnUsuarios, btnTransacciones, btnBeneficiosAdmin, btnReportes, btnConfiguracionAdmin);
        ViewUtil.activar(seccion.equals("canjes") ? btnCanjesPendientes : (seccion.equals("catalogo") ? btnCatalogoBeneficios : null),
                "sub-nav-button", "sub-nav-button-active", btnCatalogoBeneficios, btnCanjesPendientes);
        lblMensajeAdmin.setText("");
    }

    private void aviso(String texto) { ViewUtil.mensaje(lblMensajeAdmin, "✖ " + texto, false); }

    private void resultado(String error, String exito) {
        if (error != null) { aviso(error); return; }
        refrescar();
        ViewUtil.mensaje(lblMensajeAdmin, "✔ " + exito, true);
    }

    private void bloquearUsuario() {
        Usuario sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { aviso("Selecciona un usuario."); return; }
        if (sel == admin) { aviso("No puedes bloquear tu propia cuenta."); return; }
        boolean bloqueado = op.cambiarBloqueo(sel.getUser());
        refrescar();
        btnBloquearUsuario.setText(bloqueado ? "✔ Desbloquear" : "⛔ Bloquear");
        ViewUtil.mensaje(lblMensajeAdmin, "✔ Usuario " + sel.getUser() + (bloqueado ? " bloqueado." : " desbloqueado."), true);
    }

    private void eliminarUsuario() {
        Usuario sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { aviso("Selecciona un usuario."); return; }
        if (sel == admin) { aviso("No puedes eliminar tu propia cuenta."); return; }
        if (!ViewUtil.confirmar("¿Eliminar a " + sel.getNombre() + " junto con sus monederos y transacciones?")) return;
        String[] msg = new String[1];
        String err = ViewUtil.intentar(() -> msg[0] = op.eliminarUsuario(sel));
        resultado(err, msg[0]);
    }

    private void verDetalleUsuario() {
        Usuario u = tblUsuarios.getSelectionModel().getSelectedItem();
        if (u == null) { aviso("Selecciona un usuario."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Usuario: ").append(u.getUser()).append("\nNombre: ").append(u.getNombre())
                .append("\nCorreo: ").append(u.getCorreo())
                .append("\nEstado: ").append(op.isBloqueado(u.getUser()) ? "Bloqueado" : "Activo").append("\n");
        if (u instanceof Cliente c) {
            sb.append("Rol: Cliente\nDocumento: ").append(c.getDocumento()).append("\nTeléfono: ").append(c.getTelefono())
                    .append("\nDirección: ").append(c.getDireccion())
                    .append("\nRango: ").append(c.getRango()).append("  ·  Puntos: ").append(c.getPuntosAcumulados())
                    .append("\nSaldo total: ").append(ViewUtil.dinero(saldoDe(c))).append("\n\nMonederos:\n");
            c.getListaMonederos().forEach(m -> sb.append("  • ").append(m.getNumeroCuenta()).append(" (")
                    .append(m instanceof MonederoAhorro ? "Ahorro" : "Gasto diario").append("): ").append(ViewUtil.dinero(m.getSaldo())).append("\n"));
            long sospechosas = c.getListaTransacciones().stream().filter(Transaccion::isSospechosa).count();
            sb.append("\nTransacciones: ").append(c.getListaTransacciones().size()).append("  ·  Sospechosas: ").append(sospechosas)
                    .append("\n\nGastos (retiros y transferencias):\n");
            Map<String, Double> reporte = App.getAnalizadorController().generarReporteGastos(c);
            reporte.forEach((k, v) -> sb.append("  • ").append(k).append(": ").append(ViewUtil.dinero(v)).append("\n"));
        } else {
            sb.append("Rol: Administrador\n");
        }
        mostrarTexto("Detalle de usuario", sb.toString());
    }

    private void verDetalleTransaccion() {
        Transaccion t = tblTransacciones.getSelectionModel().getSelectedItem();
        if (t == null) { aviso("Selecciona una transacción."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(t.getIdTransaccion()).append("\nTipo: ").append(tipoDe(t))
                .append("\nFecha: ").append(ViewUtil.fecha(t.getFecha())).append("\nMonto: ").append(ViewUtil.dinero(t.getMonto()))
                .append("\nEstado: ").append(t.getEstado()).append("\nSospechosa: ").append(t.isSospechosa() ? "Sí" : "No")
                .append("\nOrigen: ").append(op.nombreTitular(t.getMonedero())).append(" (").append(t.getMonedero().getNumeroCuenta()).append(")");
        if (t instanceof Transferencia tr) {
            sb.append("\nDestino: ").append(op.nombreTitular(tr.getMonederoDestino())).append(" (")
                    .append(tr.getMonederoDestino().getNumeroCuenta()).append(")\nComisión: ").append(ViewUtil.dinero(tr.getComision()));
            String concepto = op.getConcepto(tr.getIdTransaccion());
            if (concepto != null) sb.append("\nConcepto: ").append(concepto);
        } else if (t instanceof Retiro r) {
            sb.append("\nComisión: ").append(ViewUtil.dinero(r.getComision()));
        }
        mostrarTexto("Detalle de transacción", sb.toString());
    }

    private void reversarTransaccion() {
        Transaccion t = tblTransacciones.getSelectionModel().getSelectedItem();
        if (t == null) { aviso("Selecciona una transacción."); return; }
        if (!(t instanceof Transferencia tr)) { aviso("Solo se pueden reversar transferencias."); return; }
        if (!ViewUtil.confirmar("¿Reversar la transferencia " + tr.getIdTransaccion() + "? Se recalcularán los puntos del cliente.")) return;
        String[] msg = new String[1];
        String err = ViewUtil.intentar(() -> msg[0] = op.revertirTransferencia(tr));
        resultado(err, msg[0]);
    }

    private void exportarTransacciones() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar transacciones");
        fc.setInitialFileName("transacciones.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File destino = fc.showSaveDialog(rootAdmin.getScene().getWindow());
        if (destino == null) return;
        List<String> lineas = new ArrayList<>();
        lineas.add("id;fecha;origen;destino;tipo;monto;estado;sospechosa");
        for (Transaccion t : tblTransacciones.getItems()) {
            String dest = t instanceof Transferencia tr ? op.nombreTitular(tr.getMonederoDestino()) : "";
            lineas.add(String.join(";", t.getIdTransaccion(), ViewUtil.fecha(t.getFecha()), op.nombreTitular(t.getMonedero()),
                    dest, tipoDe(t), String.valueOf(Math.round(t.getMonto())), String.valueOf(t.getEstado()), t.isSospechosa() ? "SI" : "NO"));
        }
        try {
            Files.write(destino.toPath(), lineas, StandardCharsets.UTF_8);
            ViewUtil.mensaje(lblMensajeAdmin, "✔ Se exportaron " + (lineas.size() - 1) + " transacciones a " + destino.getName(), true);
        } catch (IOException ex) {
            aviso("No se pudo guardar el archivo: " + ex.getMessage());
        }
    }

    private boolean dialogoBeneficio(Beneficio existente) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(existente == null ? "Nuevo beneficio" : "Editar beneficio");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField nombre = new TextField(existente == null ? "" : existente.getNombre());
        TextField descripcion = new TextField(existente == null ? "" : existente.getDescripcion());
        TextField puntos = new TextField(existente == null ? "" : String.valueOf(existente.getPuntosRequeridos()));
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.setPadding(new Insets(16));
        g.addRow(0, new Label("Nombre"), nombre);
        g.addRow(1, new Label("Descripción"), descripcion);
        g.addRow(2, new Label("Puntos requeridos"), puntos);
        d.getDialogPane().setContent(g);

        boolean[] guardado = new boolean[1];
        d.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            String err = ViewUtil.intentar(() -> {
                String n = nombre.getText() == null ? "" : nombre.getText().trim();
                if (n.isEmpty()) throw new DatosInvalidosException("El nombre es obligatorio.");
                int pts;
                try {
                    pts = Integer.parseInt(puntos.getText().trim());
                } catch (NumberFormatException nfe) {
                    throw new DatosInvalidosException("Los puntos deben ser un número entero.");
                }
                if (pts <= 0) throw new DatosInvalidosException("Los puntos deben ser mayores que cero.");
                String desc = descripcion.getText() == null ? "" : descripcion.getText().trim();
                if (existente == null) {
                    App.getBeneficioController().agregarBeneficio(new Beneficio(op.siguienteIdBeneficio(), n, desc, pts));
                } else {
                    existente.setNombre(n);
                    existente.setDescripcion(desc);
                    existente.setPuntosRequeridos(pts);
                }
                guardado[0] = true;
            });
            if (err != null) { ViewUtil.error(err); ev.consume(); }
        });
        d.showAndWait();
        if (guardado[0]) ViewUtil.mensaje(lblMensajeAdmin, "✔ Beneficio guardado.", true);
        return guardado[0];
    }

    private void mostrarReportes() {
        List<Cliente> clientes = baseUsuarios.stream().filter(u -> u instanceof Cliente).map(u -> (Cliente) u).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder("REPORTE GENERAL\n\nClientes: ").append(clientes.size()).append("\nPor rango:\n");
        clientes.stream().collect(Collectors.groupingBy(c -> c.getRango().toString(), Collectors.counting()))
                .forEach((k, v) -> sb.append("  • ").append(k).append(": ").append(v).append("\n"));
        sb.append("\nSaldo en circulación: ").append(ViewUtil.dinero(App.getBanco().getListaMonederos().stream().mapToDouble(Monedero::getSaldo).sum()));
        sb.append("\n\nTransacciones por tipo:\n");
        baseTrx.stream().collect(Collectors.groupingBy(this::tipoDe, Collectors.summingDouble(Transaccion::getMonto)))
                .forEach((k, v) -> sb.append("  • ").append(k).append(": ").append(ViewUtil.dinero(v)).append("\n"));
        long sospechosas = baseTrx.stream().filter(Transaccion::isSospechosa).count();
        sb.append("\nTransacciones sospechosas: ").append(sospechosas);
        sb.append("\n\nCanjes por estado:\n");
        baseCanjes.stream().collect(Collectors.groupingBy(Canje::estado, Collectors.counting()))
                .forEach((k, v) -> sb.append("  • ").append(k).append(": ").append(v).append("\n"));
        mostrarTexto("Reportes", sb.toString());
    }

    private void abrirConfiguracion() {
        Banco banco = App.getBanco();
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Configuración del banco");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField nombre = new TextField(banco.getNombre());
        TextField nit = new TextField(banco.getNit());
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.setPadding(new Insets(16));
        g.addRow(0, new Label("Nombre"), nombre);
        g.addRow(1, new Label("NIT"), nit);
        d.getDialogPane().setContent(g);
        d.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            String err = ViewUtil.intentar(() -> {
                if (nombre.getText() == null || nombre.getText().isBlank()) throw new DatosInvalidosException("El nombre del banco es obligatorio.");
                if (nit.getText() == null || nit.getText().isBlank()) throw new DatosInvalidosException("El NIT es obligatorio.");
                banco.setNombre(nombre.getText().trim());
                banco.setNit(nit.getText().trim());
            });
            if (err != null) { ViewUtil.error(err); ev.consume(); }
        });
        d.showAndWait();
    }

    private void mostrarNotificaciones() {
        StringBuilder sb = new StringBuilder();
        long pendientes = baseCanjes.stream().filter(c -> c.estado().equals("Pendiente")).count();
        sb.append("Canjes pendientes de aprobación: ").append(pendientes).append("\n\nAlertas de transacciones atípicas:\n");
        List<String> atipicas = new ArrayList<>();
        for (Usuario u : baseUsuarios) {
            if (u instanceof Cliente c) {
                c.getListaAlertas().stream().filter(a -> a.getTipo() == TipoAlerta.TRANSACCION_ATIPICA)
                        .forEach(a -> atipicas.add("  • " + c.getNombre() + " — " + a.getMensaje()));
            }
        }
        sb.append(atipicas.isEmpty() ? "  (ninguna)" : String.join("\n", atipicas));
        mostrarTexto("Notificaciones", sb.toString());
    }

    private void mostrarTexto(String titulo, String texto) {
        TextArea area = new TextArea(texto);
        area.setEditable(false);
        area.setPrefSize(520, 380);
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(titulo);
        d.getDialogPane().setContent(area);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }
}
