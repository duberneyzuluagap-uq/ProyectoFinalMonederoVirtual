module co.edu.uniquindio.poo.proyectofinalmonederovirtual {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.junit.platform.commons;

    
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.app;
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;

    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual.app to javafx.fxml;
    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController to javafx.fxml;
    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller to org.junit.platform.commons;
}