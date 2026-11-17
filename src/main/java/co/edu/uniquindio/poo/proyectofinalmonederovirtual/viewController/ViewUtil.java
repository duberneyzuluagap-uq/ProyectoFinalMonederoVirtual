package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;


public final class ViewUtil {
    private static final Locale CO = Locale.forLanguageTag("es-CO");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ViewUtil() {}


    public static String dinero(double v) {
        NumberFormat nf = NumberFormat.getIntegerInstance(CO);
        return "$ " + nf.format(Math.round(v));
    }


    public static String numero(int v) { return NumberFormat.getIntegerInstance(CO).format(v); }

    public static String fecha(LocalDate f) { return f == null ? "" : f.format(FECHA); }

    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] p = nombre.trim().split("\\s+");
        String r = p[0].substring(0, 1) + (p.length > 1 ? p[1].substring(0, 1) : "");
        return r.toUpperCase();
    }

    public static String emojiRango(RangoCliente r) {
        if (r == null) return "🥉";
        switch (r) {
            case PLATA: return "🥈";
            case ORO: return "🥇";
            case PLATINO: return "💎";
            default: return "🥉";
        }
    }

    public static void error(String msg) { alerta(Alert.AlertType.ERROR, "Error", msg); }

    public static void info(String msg) { alerta(Alert.AlertType.INFORMATION, "Información", msg); }

    public static void advertencia(String msg) { alerta(Alert.AlertType.WARNING, "Atención", msg); }

    private static void alerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo, msg, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public static boolean confirmar(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmar");
        a.setHeaderText(null);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    public static void mensaje(Label lbl, String texto, boolean exito) {
        lbl.getStyleClass().removeAll("message", "message-error");
        lbl.getStyleClass().add(exito ? "message" : "message-error");
        lbl.setText(texto == null ? "" : texto);
    }

    public static <T> void columna(TableColumn<T, String> col, Function<T, String> f) {
        col.setCellValueFactory(d -> new SimpleStringProperty(f.apply(d.getValue())));
    }


    public static void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }


    public static void activar(Button activo, String claseNormal, String claseActiva, Button... todos) {
        for (Button b : todos) {
            b.getStyleClass().removeAll(claseNormal, claseActiva);
            b.getStyleClass().add(b == activo ? claseActiva : claseNormal);
        }
    }

    public static String categoriaBeneficio(Beneficio b) {
        String t = ((b.getNombre() == null ? "" : b.getNombre()) + " " + (b.getDescripcion() == null ? "" : b.getDescripcion())).toLowerCase();
        if (t.contains("recarga") || t.contains("celular") || t.contains("bono") || t.contains("saldo")) return "Recargas";
        if (t.contains("cine") || t.contains("entretenimiento") || t.contains("streaming") || t.contains("concierto")) return "Entretenimiento";
        if (t.contains("viaje") || t.contains("tiquete") || t.contains("vuelo") || t.contains("hotel")) return "Viajes";
        if (t.contains("descuento") || t.contains("%") || t.contains("comisión") || t.contains("cargos")) return "Descuentos";
        return "Otros";
    }


    public static String emojiCategoria(String categoria) {
        switch (categoria) {
            case "Descuentos": return "🏷";
            case "Recargas": return "📱";
            case "Entretenimiento": return "🎬";
            case "Viajes": return "✈";
            default: return "🎁";
        }
    }


    public static String claseIconoCategoria(String categoria) {
        switch (categoria) {
            case "Descuentos": return "benefit-icon-orange";
            case "Recargas": return "benefit-icon-blue";
            case "Entretenimiento": return "benefit-icon-purple";
            case "Viajes": return "benefit-icon-green";
            default: return "benefit-icon-purple";
        }
    }
}
