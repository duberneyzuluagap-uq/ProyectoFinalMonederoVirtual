package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.Alerta;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class AlertasDialog {
    private AlertasDialog() {}

    public static void mostrar(Cliente cliente) {
        ObservableList<Alerta> items = FXCollections.observableArrayList(App.getAlertaController().getAlertasCliente(cliente));
        FXCollections.reverse(items);
        ListView<Alerta> lista = new ListView<>(items);
        lista.setPrefSize(520, 300);
        lista.setPlaceholder(new Label("No tienes notificaciones"));
        lista.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Alerta a, boolean vacio) {
                super.updateItem(a, vacio);
                setText(vacio || a == null ? null : ViewUtil.fecha(a.getFecha()) + " · " + a.getTipo() + " — " + a.getMensaje());
            }
        });
        Button eliminar = new Button("🗑 Eliminar seleccionada");
        eliminar.setOnAction(e -> {
            Alerta sel = lista.getSelectionModel().getSelectedItem();
            if (sel == null) { ViewUtil.error("Selecciona una alerta."); return; }
            App.getAlertaController().eliminarAlerta(cliente, sel.getMensaje());
            items.remove(sel);
        });
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Notificaciones");
        d.getDialogPane().setContent(new VBox(10, lista, new HBox(eliminar)));
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }
}
