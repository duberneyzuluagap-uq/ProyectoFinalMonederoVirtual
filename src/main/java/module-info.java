module co.edu.uniquindio.poo.proyectofinalmonederovirtual {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.app;
    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual.app to javafx.fxml;
}