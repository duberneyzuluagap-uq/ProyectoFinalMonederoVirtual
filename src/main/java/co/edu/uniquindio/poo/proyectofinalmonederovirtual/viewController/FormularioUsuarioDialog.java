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

        // Mientras el usuario no dé OK se revisan los campos: si algo está mal
        // se muestra el error y ev.consume() impide que el diálogo se cierre.
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                if (existente != null) {
                    App.getOperacionController().actualizarUsuario(existente, nombre.getText(), correo.getText(),
                            password.getText(), direccion.getText(), telefono.getText());
                } else if ("Administrador".equals(rol.getValue())) {
                    App.getOperacionController().registrarAdmin(nombre.getText(), user.getText(),
                            password.getText(), correo.getText());
                } else {
                    App.getOperacionController().registrarCliente(nombre.getText(), user.getText(),
                            password.getText(), correo.getText(), documento.getText(), direccion.getText(), telefono.getText());
                }
            } catch (MonederoException e) {
                ViewUtil.error(e.getMessage());
                ev.consume();
            }
        });

        Optional<ButtonType> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) return Optional.empty();

        // Al dar OK el usuario ya quedó guardado, así que solo lo buscamos para devolverlo
        if (existente != null) return Optional.of(existente);
        return App.getBanco().buscarUsuario(user.getText().trim());
    }
}

