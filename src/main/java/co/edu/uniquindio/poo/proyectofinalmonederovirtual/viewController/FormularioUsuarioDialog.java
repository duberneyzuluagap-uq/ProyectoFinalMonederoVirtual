package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public final class FormularioUsuarioDialog {
    private FormularioUsuarioDialog() {}

    public static Optional<Usuario> mostrar(Usuario existente, boolean permitirElegirRol) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Nuevo usuario" : "Editar usuario");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombre = new TextField(), user = new TextField(), correo = new TextField(),
                documento = new TextField(), direccion = new TextField(), telefono = new TextField();
        PasswordField password = new PasswordField();
        ChoiceBox<String> rol = new ChoiceBox<>();
        rol.getItems().addAll("Cliente", "Administrador");
        rol.setValue("Cliente");
        rol.setDisable(!(permitirElegirRol && existente == null));

        boolean esAdmin = existente instanceof Admin;
        if (existente != null) {
            nombre.setText(existente.getNombre());
            user.setText(existente.getUser());
            correo.setText(existente.getCorreo());
            password.setPromptText("(vacío = no cambiar)");
            user.setDisable(true);
            documento.setDisable(true);
            if (existente instanceof Cliente c) {
                documento.setText(c.getDocumento());
                direccion.setText(c.getDireccion());
                telefono.setText(c.getTelefono());
            }
            rol.setValue(esAdmin ? "Administrador" : "Cliente");
        }
        if (esAdmin) { direccion.setDisable(true); telefono.setDisable(true); }

        rol.valueProperty().addListener((o, a, nuevo) -> {
            boolean admin = "Administrador".equals(nuevo);
            documento.setDisable(admin); direccion.setDisable(admin); telefono.setDisable(admin);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));
        String[] etiquetas = {"Rol", "Nombre", "Usuario", "Contraseña", "Correo", "Documento", "Dirección", "Teléfono"};
        Node[] campos = {rol, nombre, user, password, correo, documento, direccion, telefono};
        for (int i = 0; i < etiquetas.length; i++) {
            grid.add(new Label(etiquetas[i]), 0, i);
            grid.add(campos[i], 1, i);
        }
        dialogo.getDialogPane().setContent(grid);

        Usuario[] resultado = new Usuario[1];
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            String error = ViewUtil.intentar(() -> {
                if (existente != null) {
                    App.getOperacionController().actualizarUsuario(existente, nombre.getText(), correo.getText(),
                            password.getText(), direccion.getText(), telefono.getText());
                    resultado[0] = existente;
                } else if ("Administrador".equals(rol.getValue())) {
                    resultado[0] = App.getOperacionController().registrarAdmin(nombre.getText(), user.getText(),
                            password.getText(), correo.getText());
                } else {
                    resultado[0] = App.getOperacionController().registrarCliente(nombre.getText(), user.getText(),
                            password.getText(), correo.getText(), documento.getText(), direccion.getText(), telefono.getText());
                }
            });
            if (error != null) {
                ViewUtil.error(error);
                ev.consume();
            }
        });
        dialogo.showAndWait();
        return Optional.ofNullable(resultado[0]);
    }
}

