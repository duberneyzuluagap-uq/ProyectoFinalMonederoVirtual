package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;

public class PuntosController {

    public void acumularOCanjearPuntos(Cliente cliente, int puntos) {
        cliente.acumularPuntos(puntos);
    }

    public void recalcularPuntos(Cliente cliente) {
        cliente.recalcular();
    }

    public int consultarPuntos(Cliente cliente) {
        return cliente.getPuntosAcumulados();
    }

    public RangoCliente calcularRango(Cliente cliente) {
        return cliente.calcularRango();
    }

    public List<MovimientoPuntos> getHistorialPuntos(Cliente cliente) {
        return cliente.getListaMovimientosPuntos();
    }
}