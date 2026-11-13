package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;
import java.util.Optional;

public class MonederoController {

    private final Banco banco;

    public MonederoController(Banco banco) {
        this.banco = banco;
    }

    public String agregarMonedero(Cliente cliente, Monedero monedero) {
        banco.agregarMonedero(monedero);
        return cliente.agregarMonedero(monedero);
    }

    public String eliminarMonedero(Cliente cliente, String numeroCuenta) {
        banco.eliminarMonedero(numeroCuenta);
        return cliente.eliminarMonedero(numeroCuenta);
    }

    public Optional<Monedero> buscarMonederoEnBanco(String numeroCuenta) {
        return banco.buscarMonedero(numeroCuenta);
    }

    public Optional<Monedero> buscarMonederoEnCliente(Cliente cliente, String numeroCuenta) {
        return cliente.buscarMonedero(numeroCuenta);
    }

    public double consultarSaldo(Monedero monedero) {
        return monedero.consultarSaldo();
    }

    public List<Monedero> getMonederosCliente(Cliente cliente) {
        return cliente.getListaMonederos();
    }
}