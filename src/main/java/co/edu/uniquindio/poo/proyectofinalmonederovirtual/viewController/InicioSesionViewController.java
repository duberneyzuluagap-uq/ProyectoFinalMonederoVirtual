package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.app.App;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.Admin;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

// Controlador de login.fxml//
public class InicioSesionViewController {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button btnIniciarSesion;
    @FXML private Button btnMostrarContrasena;
    @FXML private VBox cardLogin;
    @FXML private Hyperlink hlRegistrarse;
    @FXML private Label lblDescripcion;
    @FXML private Label lblLogo;
    @FXML private Label lblMensajeError;
    @FXML private Label lblSlogan;
    @FXML private Label lblSubtituloLogin;
    @FXML private Label lblTituloLogin;
    @FXML private StackPane panelFormulario;
    @FXML private VBox panelMarca;
    @FXML private PasswordField pfContrasena;
    @FXML private HBox rootLogin;
    @FXML private TextField txtContrasenaVisible;
    @FXML private TextField txtUsuario;

    @FXML
    void initialize() {
        txtContrasenaVisible.textProperty().bindBidirectional(pfContrasena.textProperty());
        lblMensajeError.setText("");

        btnMostrarContrasena.setOnAction(e -> alternarContrasena());
        btnIniciarSesion.setOnAction(e -> iniciarSesion());
        hlRegistrarse.setOnAction(e -> registrarse());
        txtUsuario.setOnAction(e -> pfContrasena.requestFocus());
    }

    private void alternarContrasena() {
        boolean mostrar = !txtContrasenaVisible.isVisible();
        ViewUtil.mostrar(txtContrasenaVisible, mostrar);
        ViewUtil.mostrar(pfContrasena, !mostrar);
        btnMostrarContrasena.setText(mostrar ? "🙈" : "👁");
        (mostrar ? txtContrasenaVisible : pfContrasena).requestFocus();
    }
    private void iniciarSesion() {
        lblMensajeError.setText("");
        String ingresado = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String clave = pfContrasena.getText() == null ? "" : pfContrasena.getText();
        if (ingresado.isEmpty() || clave.isEmpty()) {
            lblMensajeError.setText("Ingresa tu usuario y tu contraseña.");
            return;
        }
        String user = App.getBanco().getListaUsuarios().stream()
                .filter(u -> u.getUser().equals(ingresado) || u.getCorreo().equalsIgnoreCase(ingresado))
                .map(Usuario::getUser).findFirst().orElse(ingresado);

        if (App.getOperacionController().isBloqueado(user)) {
            lblMensajeError.setText("Tu cuenta está bloqueada. Comunícate con el administrador.");
            return;
        }
        if (!App.getUsuarioController().iniciarSesion(user, clave)) {
            lblMensajeError.setText("Usuario o contraseña incorrectos.");
            return;
        }
        Usuario actual = App.getUsuarioController().getUsuarioActual();
        if (actual instanceof Admin) App.irAdmin();
        else App.irMonedero("inicio");
    }

    private void registrarse() {
        FormularioUsuarioDialog.mostrar(null, false).ifPresent(u -> {
            txtUsuario.setText(u.getUser());
            pfContrasena.clear();
            ViewUtil.info("¡Cuenta creada! Ya puedes iniciar sesión con el usuario '" + u.getUser() + "'.");
        });
    }
}
