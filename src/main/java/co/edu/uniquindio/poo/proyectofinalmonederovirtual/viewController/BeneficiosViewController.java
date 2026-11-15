package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController.Canje;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BeneficiosViewController {

    @FXML
    private BorderPane rootBeneficios;
    @FXML
    private Label lblLogo, lblIniciales, lblUsuario, lblCorreo, lblTituloBeneficios, lblSubtituloBeneficios;
    @FXML
    private Button btnInicio, btnEnviar, btnRecargar, btnHistorial, btnBeneficios, btnCerrarSesion, btnNotificaciones;
    @FXML
    private VBox subMenuBeneficios, cardMisCanjes;
    @FXML
    private Button btnCatalogo, btnMisCanjes, btnMisPuntos;
    @FXML
    private TextField txtBuscarBeneficio;
    @FXML
    private Label lblPuntos, lblNivel, lblProgresoNivel, lblCanjesRealizados, lblPuntosMes;
    @FXML
    private ProgressBar pbNivel;
    @FXML
    private HBox panelConfirmarCanje;
    @FXML
    private Label lblConfirmarTexto, lblMensajeCanje;
    @FXML
    private Button btnConfirmarCanje, btnCancelarCanje;
    @FXML
    private Button btnCatTodos, btnCatDescuentos, btnCatRecargas, btnCatEntretenimiento, btnCatViajes;
    @FXML
    private ComboBox<String> cmbOrdenar;
    @FXML
    private FlowPane fpBeneficios;
    @FXML
    private TableView<Canje> tblMisCanjes;
    @FXML
    private TableColumn<Canje, String> colMiCanjeFecha, colMiCanjeBeneficio, colMiCanjePuntos, colMiCanjeCodigo, colMiCanjeEstado;

    private Cliente cliente;
    private String categoriaActual = "Todos";
    private Beneficio canjePendiente;
    private final ObservableList<Canje> misCanjes = FXCollections.observableArrayList();
    private final OperacionController op = App.getOperacionController();

    @FXML
    private void initialize() {
        cliente = App.clienteActual();
        if (cliente == null) {
            javafx.application.Platform.runLater(App::irLogin);
            return;
        }
        lblIniciales.setText(ViewUtil.iniciales(cliente.getNombre()));
        lblUsuario.setText(cliente.getNombre());
        lblCorreo.setText(cliente.getCorreo());
        btnInicio.setOnAction(e -> App.irMonedero("inicio"));
        btnEnviar.setOnAction(e -> App.irMonedero("enviar"));
        btnRecargar.setOnAction(e -> App.irMonedero("recargar"));
        btnHistorial.setOnAction(e -> App.irMonedero("historial"));
        btnBeneficios.setOnAction(e -> {
            boolean abierto = subMenuBeneficios.isVisible();
            ViewUtil.mostrar(subMenuBeneficios, !abierto);
            btnBeneficios.setText(abierto ? "🎁  Beneficios  ▸" : "🎁  Beneficios  ▾");
        });
        btnCatalogo.setOnAction(e -> mostrarSeccion("catalogo"));
        btnMisCanjes.setOnAction(e -> mostrarSeccion("canjes"));
        btnMisPuntos.setOnAction(e -> mostrarSeccion("puntos"));
        btnCerrarSesion.setOnAction(e -> {
            App.getUsuarioController().cerrarSesion();
            App.irLogin();
        });
        btnNotificaciones.setOnAction(e -> AlertasDialog.mostrar(cliente));
        btnCatTodos.setOnAction(e -> categoria("Todos", btnCatTodos));
        btnCatDescuentos.setOnAction(e -> categoria("Descuentos", btnCatDescuentos));
        btnCatRecargas.setOnAction(e -> categoria("Recargas", btnCatRecargas));
        btnCatEntretenimiento.setOnAction(e -> categoria("Entretenimiento", btnCatEntretenimiento));
        btnCatViajes.setOnAction(e -> categoria("Viajes", btnCatViajes));
        cmbOrdenar.setItems(FXCollections.observableArrayList("Menor costo", "Mayor costo", "Nombre (A-Z)"));
        cmbOrdenar.setValue("Menor costo");
        cmbOrdenar.setOnAction(e -> construirCatalogo());
        txtBuscarBeneficio.textProperty().addListener((o, a, b) -> construirCatalogo());
        btnConfirmarCanje.setOnAction(e -> confirmarCanje());
        btnCancelarCanje.setOnAction(e -> cancelarCanje());
        ViewUtil.mostrar(panelConfirmarCanje, false);
        lblMensajeCanje.setText("");
        ViewUtil.columna(colMiCanjeFecha, c -> ViewUtil.fecha(c.movimiento().getFecha()));
        ViewUtil.columna(colMiCanjeBeneficio, Canje::beneficio);
        ViewUtil.columna(colMiCanjePuntos, c -> "⭐ " + ViewUtil.numero(c.puntos()));
        ViewUtil.columna(colMiCanjeCodigo, Canje::codigo);
        ViewUtil.columna(colMiCanjeEstado, Canje::estado);
        tblMisCanjes.setItems(misCanjes);
        tblMisCanjes.setPlaceholder(new Label("Aún no has canjeado beneficios"));

        refrescarTodo();
    }

    private void refrescarTodo() {
        refrescarPuntos();
        misCanjes.setAll(op.listarCanjes(cliente).stream()
                .sorted(Comparator.comparing((Canje c) -> c.movimiento().getFecha()).reversed()).collect(Collectors.toList()));
        construirCatalogo();
    }

    private void refrescarPuntos() {
        int p = cliente.getPuntosAcumulados();
        RangoCliente r = cliente.getRango();
        lblPuntos.setText("⭐ " + ViewUtil.numero(p));
        lblNivel.setText(ViewUtil.emojiRango(r) + " Nivel " + r);

        int[] techos = {500, 1000, 5000};
        if (r.ordinal() < techos.length) {
            int techo = techos[r.ordinal()];
            int piso = r.ordinal() == 0 ? 0 : techos[r.ordinal() - 1];
            pbNivel.setProgress(Math.max(0, Math.min(1, (p - piso) / (double) (techo - piso))));
            lblProgresoNivel.setText(ViewUtil.numero(techo + 1 - p) + " puntos para llegar a " + RangoCliente.values()[r.ordinal() + 1]);
        } else {
            pbNivel.setProgress(1);
            lblProgresoNivel.setText("¡Alcanzaste el nivel máximo!");
        }

        long canjes = op.listarCanjes(cliente).stream().filter(c -> !c.estado().equals("Rechazado")).count();
        lblCanjesRealizados.setText(String.valueOf(canjes));
        YearMonth mes = YearMonth.now();
        int delMes = cliente.getListaMovimientosPuntos().stream()
                .filter(m -> !m.isAnulado() && m.getPuntos() > 0 && YearMonth.from(m.getFecha()).equals(mes))
                .mapToInt(MovimientoPuntos::getPuntos).sum();
        lblPuntosMes.setText("+" + ViewUtil.numero(delMes));
    }

    private void categoria(String nombre, Button boton) {
        categoriaActual = nombre;
        ViewUtil.activar(boton, "chip-button", "chip-button-active",
                btnCatTodos, btnCatDescuentos, btnCatRecargas, btnCatEntretenimiento, btnCatViajes);
        construirCatalogo();
    }

    private Comparator<Beneficio> orden() {
        String o = cmbOrdenar.getValue() == null ? "" : cmbOrdenar.getValue();
        switch (o) {
            case "Mayor costo":
                return Comparator.comparingInt(Beneficio::getPuntosRequeridos).reversed();
            case "Nombre (A-Z)":
                return Comparator.comparing((Beneficio b) -> b.getNombre().toLowerCase());
            default:
                return Comparator.comparingInt(Beneficio::getPuntosRequeridos);
        }
    }

    private void construirCatalogo() {
        String q = txtBuscarBeneficio.getText() == null ? "" : txtBuscarBeneficio.getText().trim().toLowerCase();
        List<Beneficio> lista = op.beneficiosActivos().stream()
                .filter(b -> categoriaActual.equals("Todos") || ViewUtil.categoriaBeneficio(b).equals(categoriaActual))
                .filter(b -> q.isEmpty() || (b.getNombre() + " " + b.getDescripcion()).toLowerCase().contains(q))
                .sorted(orden()).collect(Collectors.toList());

        fpBeneficios.getChildren().clear();
        if (lista.isEmpty()) {
            Label vacio = new Label("No hay beneficios que coincidan con tu búsqueda.");
            vacio.getStyleClass().add("subtitle");
            fpBeneficios.getChildren().add(vacio);
            return;
        }
        for (Beneficio b : lista) fpBeneficios.getChildren().add(crearTarjeta(b));
    }

    private VBox crearTarjeta(Beneficio b) {
        String cat = ViewUtil.categoriaBeneficio(b);
        Label icono = new Label(ViewUtil.emojiCategoria(cat));
        icono.setMaxWidth(Double.MAX_VALUE);
        icono.getStyleClass().add(ViewUtil.claseIconoCategoria(cat));
        Label titulo = new Label(b.getNombre());
        titulo.getStyleClass().add("benefit-title");
        Label desc = new Label(b.getDescripcion());
        desc.getStyleClass().add("benefit-desc");
        desc.setWrapText(true);
        Label costo = new Label("⭐ " + ViewUtil.numero(b.getPuntosRequeridos()) + " pts");
        costo.getStyleClass().add("benefit-cost");
        Label stock = new Label("Disponible");
        stock.getStyleClass().add("benefit-stock");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox pie = new HBox(8, costo, espacio, stock);
        pie.setAlignment(Pos.CENTER_LEFT);

        Button canjear = new Button("Canjear");
        canjear.setMaxWidth(Double.MAX_VALUE);
        canjear.getStyleClass().add("primary-button");
        int faltan = b.getPuntosRequeridos() - cliente.getPuntosAcumulados();
        if (faltan > 0) {
            canjear.setText("Te faltan " + ViewUtil.numero(faltan) + " pts");
            canjear.setDisable(true);
        }
        canjear.setOnAction(e -> solicitarCanje(b));

        VBox tarjeta = new VBox(8, icono, titulo, desc, pie, canjear);
        tarjeta.setPrefWidth(250);
        tarjeta.getStyleClass().add("benefit-card");
        return tarjeta;
    }

    private void solicitarCanje(Beneficio b) {
        canjePendiente = b;
        lblConfirmarTexto.setText("¿Canjear «" + b.getNombre() + "» por " + ViewUtil.numero(b.getPuntosRequeridos()) + " puntos?");
        lblMensajeCanje.setText("");
        ViewUtil.mostrar(panelConfirmarCanje, true);
    }

    private void cancelarCanje() {
        canjePendiente = null;
        ViewUtil.mostrar(panelConfirmarCanje, false);
    }

    private void confirmarCanje() {
        if (canjePendiente == null) return;
        Beneficio b = canjePendiente;
        String[] resultado = new String[1];
        String error = ViewUtil.intentar(() -> resultado[0] = op.canjearBeneficio(cliente, b));
        cancelarCanje();
        if (error != null) ViewUtil.mensaje(lblMensajeCanje, "✖ " + error, false);
        else ViewUtil.mensaje(lblMensajeCanje, "✔ " + resultado[0], true);
        refrescarTodo();
    }

    public void mostrarSeccion(String seccion) {
        if (cliente == null) return;
        String s = seccion == null ? "catalogo" : seccion;
        Node filaCategorias = btnCatTodos.getParent();      // HBox con chips y ordenar
        boolean verCanjes = s.equals("canjes");
        boolean verPuntos = s.equals("puntos");
        ViewUtil.mostrar(filaCategorias, !verCanjes);
        ViewUtil.mostrar(fpBeneficios, !verCanjes);
        ViewUtil.mostrar(cardMisCanjes, verCanjes || verPuntos);
        Button activo = verCanjes ? btnMisCanjes : (verPuntos ? btnMisPuntos : btnCatalogo);
        ViewUtil.activar(activo, "sub-nav-button", "sub-nav-button-active", btnCatalogo, btnMisCanjes, btnMisPuntos);
    }
}
