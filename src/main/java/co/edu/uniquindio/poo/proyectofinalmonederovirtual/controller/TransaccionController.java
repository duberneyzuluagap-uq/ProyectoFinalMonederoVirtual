package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;
import java.util.Optional;

public class TransaccionController {

    private final Banco banco;

    public TransaccionController(Banco banco) {
        this.banco = banco;
    }

    public String registrarTransaccion(Cliente cliente, Transaccion transaccion) {
        String resultado = cliente.registrarTransaccion(transaccion);
        banco.agregarTransaccion(transaccion);
        if (transaccion instanceof Transferencia t) {
            banco.agregarTransferencia(t);
        }
        return resultado;
    }

    public String revertirTransferencia(Cliente cliente, Transferencia transferencia) {
        return cliente.revertirTransferencia(transferencia);
    }

    public Optional<Transaccion> buscarTransaccion(String idTransaccion) {
        return banco.buscarTransaccion(idTransaccion);
    }

    public List<Transaccion> getHistorialCliente(Cliente cliente) {
        return cliente.getListaTransacciones();
    }

    public List<Transaccion> getHistorialGeneralBanco() {
        return banco.getListaTransacciones();
    }
}