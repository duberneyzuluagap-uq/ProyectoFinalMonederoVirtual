package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;
import java.util.Optional;

public class AlertaController {

    public String agregarAlerta(Cliente cliente, Alerta alerta) {
        return cliente.agregarAlerta(alerta);
    }

    public String eliminarAlerta(Cliente cliente, String mensaje) {
        return cliente.eliminarAlerta(mensaje);
    }

    public Optional<Alerta> buscarAlerta(Cliente cliente, String mensaje) {
        return cliente.buscarAlerta(mensaje);
    }

    public List<Alerta> getAlertasCliente(Cliente cliente) {
        return cliente.getListaAlertas();
    }
}