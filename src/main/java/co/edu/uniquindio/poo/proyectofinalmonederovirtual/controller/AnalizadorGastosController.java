package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.Map;

public class AnalizadorGastosController {

    private final AnalizadorGastos analizadorGastos;

    public AnalizadorGastosController() {
        this.analizadorGastos = new AnalizadorGastos();
    }

    public Map<String, Double> generarReporteGastos(Cliente cliente) {
        return analizadorGastos.generarReporteGastos(cliente);
    }
}