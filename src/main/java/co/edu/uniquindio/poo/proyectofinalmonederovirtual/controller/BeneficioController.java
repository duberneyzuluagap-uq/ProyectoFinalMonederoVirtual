package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;
import java.util.Optional;

public class BeneficioController {

    private final Banco banco;

    public BeneficioController(Banco banco) {
        this.banco = banco;
    }

    public String agregarBeneficio(Beneficio beneficio) {
        return banco.agregarBeneficio(beneficio);
    }

    public String actualizarBeneficio(String idBeneficio, Beneficio nuevo) {
        return banco.actualizarBeneficio(idBeneficio, nuevo);
    }

    public String eliminarBeneficio(String idBeneficio) {
        return banco.eliminarBeneficio(idBeneficio);
    }

    public Optional<Beneficio> buscarBeneficio(String idBeneficio) {
        return banco.buscarBeneficio(idBeneficio);
    }

    public List<Beneficio> getListaBeneficios() {
        return banco.getListaBeneficios();
    }
}