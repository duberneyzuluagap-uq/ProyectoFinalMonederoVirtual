package co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class InicioSesionViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnMostrarContrasena;

    @FXML
    private VBox cardLogin;

    @FXML
    private Hyperlink hlRegistrarse;

    @FXML
    private Label lblDescripcion;

    @FXML
    private Label lblLogo;

    @FXML
    private Label lblMensajeError;

    @FXML
    private Label lblSlogan;

    @FXML
    private Label lblSubtituloLogin;

    @FXML
    private Label lblTituloLogin;

    @FXML
    private StackPane panelFormulario;

    @FXML
    private VBox panelMarca;

    @FXML
    private PasswordField pfContrasena;

    @FXML
    private HBox rootLogin;

    @FXML
    private TextField txtContrasenaVisible;

    @FXML
    private TextField txtUsuario;

    @FXML
    void initialize() {
        assert btnIniciarSesion != null : "fx:id=\"btnIniciarSesion\" was not injected: check your FXML file 'login.fxml'.";
        assert btnMostrarContrasena != null : "fx:id=\"btnMostrarContrasena\" was not injected: check your FXML file 'login.fxml'.";
        assert cardLogin != null : "fx:id=\"cardLogin\" was not injected: check your FXML file 'login.fxml'.";
        assert hlRegistrarse != null : "fx:id=\"hlRegistrarse\" was not injected: check your FXML file 'login.fxml'.";
        assert lblDescripcion != null : "fx:id=\"lblDescripcion\" was not injected: check your FXML file 'login.fxml'.";
        assert lblLogo != null : "fx:id=\"lblLogo\" was not injected: check your FXML file 'login.fxml'.";
        assert lblMensajeError != null : "fx:id=\"lblMensajeError\" was not injected: check your FXML file 'login.fxml'.";
        assert lblSlogan != null : "fx:id=\"lblSlogan\" was not injected: check your FXML file 'login.fxml'.";
        assert lblSubtituloLogin != null : "fx:id=\"lblSubtituloLogin\" was not injected: check your FXML file 'login.fxml'.";
        assert lblTituloLogin != null : "fx:id=\"lblTituloLogin\" was not injected: check your FXML file 'login.fxml'.";
        assert panelFormulario != null : "fx:id=\"panelFormulario\" was not injected: check your FXML file 'login.fxml'.";
        assert panelMarca != null : "fx:id=\"panelMarca\" was not injected: check your FXML file 'login.fxml'.";
        assert pfContrasena != null : "fx:id=\"pfContrasena\" was not injected: check your FXML file 'login.fxml'.";
        assert rootLogin != null : "fx:id=\"rootLogin\" was not injected: check your FXML file 'login.fxml'.";
        assert txtContrasenaVisible != null : "fx:id=\"txtContrasenaVisible\" was not injected: check your FXML file 'login.fxml'.";
        assert txtUsuario != null : "fx:id=\"txtUsuario\" was not injected: check your FXML file 'login.fxml'.";

    }

}
