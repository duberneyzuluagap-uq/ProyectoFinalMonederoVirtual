package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;
import java.util.Optional;

public class BancoController {

    private final Banco banco;

    public BancoController(Banco banco) {
        this.banco = banco;
    }

    public Banco getBanco() {
        return banco;
    }

    public String agregarBeneficio(Beneficio beneficio) {
        return banco.agregarBeneficio(beneficio);
    }

    public String actualizarBeneficio(String clave, Beneficio nuevo) {
        return banco.actualizarBeneficio(clave, nuevo);
    }

    public String eliminarBeneficio(String clave) {
        return banco.eliminarBeneficio(clave);
    }

    public Optional<Beneficio> buscarBeneficio(String clave) {
        return banco.buscarBeneficio(clave);
    }

    public List<Beneficio> getListaBeneficios() {
        return banco.getListaBeneficios();
    }

    public String agregarMonedero(Monedero monedero) {
        return banco.agregarMonedero(monedero);
    }

    public String eliminarMonedero(String clave) {
        return banco.eliminarMonedero(clave);
    }

    public Optional<Monedero> buscarMonedero(String clave) {
        return banco.buscarMonedero(clave);
    }

    public List<Transaccion> getListaTransacciones() {
        return banco.getListaTransacciones();
    }

    public List<Transferencia> getListaTransferencias() {
        return banco.getListaTransferencias();
    }
}