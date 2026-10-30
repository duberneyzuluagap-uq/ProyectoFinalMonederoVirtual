module co.edu.uniquindio.poo.proyectofinalmonederovirtual {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.app;
    exports co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;
    exports  co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;
    exports  co.edu.uniquindio.poo.proyectofinalmonederovirtual.viewController;
    opens co.edu.uniquindio.poo.proyectofinalmonederovirtual.app to javafx.fxml;
}