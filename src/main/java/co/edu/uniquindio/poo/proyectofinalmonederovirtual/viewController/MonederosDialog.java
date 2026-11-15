package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller.OperacionController;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Consumer;

public final class MonederosDialog {
    private MonederosDialog() {}

    public static void mostrar(Cliente cliente, Monedero principal, Consumer<Monedero> alElegirPrincipal) {
        OperacionController op = App.getOperacionController();
        ObservableList<Monedero> items = FXCollections.observableArrayList(cliente.getListaMonederos());
        TableView<Monedero> tabla = new TableView<>(items);
        tabla.setPrefSize(640, 260);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Monedero, String> cCuenta = new TableColumn<>("Cuenta"), cTipo = new TableColumn<>("Tipo"),
                cSaldo = new TableColumn<>("Saldo"), cDetalle = new TableColumn<>("Detalle");
        ViewUtil.columna(cCuenta, m -> m.getNumeroCuenta() + (m == principal ? "  ★" : ""));
        ViewUtil.columna(cTipo, m -> m instanceof MonederoAhorro ? "Ahorro" : "Gasto diario");
        ViewUtil.columna(cSaldo, m -> ViewUtil.dinero(m.getSaldo()));
        ViewUtil.columna(cDetalle, m -> {
            if (m instanceof MonederoAhorro a)
                return "Meta " + ViewUtil.dinero(a.getMetaAhorro()) + " · interés " + String.format("%.1f%%", a.getTasaInteres() * 100);
            if (m instanceof MonederoGastoDiario g) return "Gastado hoy " + ViewUtil.dinero(g.getGastadoHoy());
            return "";
        });
        tabla.getColumns().addAll(cCuenta, cTipo, cSaldo, cDetalle);

        Runnable recargar = () -> { items.setAll(cliente.getListaMonederos()); tabla.refresh(); };

        Button btnAhorro = new Button("➕ Monedero de ahorro");
        btnAhorro.setOnAction(e -> pedir("Meta de ahorro", "Meta de ahorro ($):", "1000000").ifPresent(txt -> {
            String err = ViewUtil.intentar(() -> op.crearMonederoAhorro(cliente, OperacionController.parseMonto(txt), 0.01));
            if (err != null) ViewUtil.error(err); else recargar.run();
        }));
        Button btnGasto = new Button("➕ Gasto diario");
        btnGasto.setOnAction(e -> { op.crearMonederoGastoDiario(cliente); recargar.run(); });
        Button btnRetirar = new Button("💵 Retirar");
        btnRetirar.setOnAction(e -> {
            Monedero sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { ViewUtil.error("Selecciona un monedero."); return; }
            pedir("Retiro", "Monto a retirar (comisión 1%):", "").ifPresent(txt -> {
                String[] ok = new String[1];
                String err = ViewUtil.intentar(() -> ok[0] = op.retirar(cliente, sel, OperacionController.parseMonto(txt)));
                if (err != null) ViewUtil.error(err); else { ViewUtil.info(ok[0]); recargar.run(); }
            });
        });
        Button btnPrincipal = new Button("★ Usar como principal");
        btnPrincipal.setOnAction(e -> {
            Monedero sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { ViewUtil.error("Selecciona un monedero."); return; }
            alElegirPrincipal.accept(sel);
            ViewUtil.info("Ahora usarás " + sel.getNumeroCuenta() + " para enviar y recargar.");
        });
        Button btnEliminar = new Button("🗑 Eliminar");
        btnEliminar.setOnAction(e -> {
            Monedero sel = tabla.getSelectionModel().getSelectedItem();
            String err = ViewUtil.intentar(() -> op.eliminarMonedero(cliente, sel));
            if (err != null) ViewUtil.error(err); else recargar.run();
        });

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Mis tarjetas / monederos");
        d.getDialogPane().setContent(new VBox(10, tabla, new HBox(8, btnAhorro, btnGasto, btnRetirar, btnPrincipal, btnEliminar)));
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }

    private static Optional<String> pedir(String titulo, String mensaje, String inicial) {
        TextInputDialog t = new TextInputDialog(inicial);
        t.setTitle(titulo);
        t.setHeaderText(null);
        t.setContentText(mensaje);
        return t.showAndWait();
    }
}

