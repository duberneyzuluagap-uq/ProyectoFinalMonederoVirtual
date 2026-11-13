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

    public boolean iniciarSesion(String user, String password) {
        Optional<Usuario> usuario = banco.buscarUsuario(user);
        return usuario.isPresent() && usuario.get().iniciarSesion(user, password);
    }

    public Optional<Usuario> buscarUsuario(String user) {
        return banco.buscarUsuario(user);
    }

    public String registrarUsuario(Usuario usuario) {
        return banco.registrarUsuario(usuario);
    }

    public String actualizarCliente(String clave, Cliente nuevo) {
        return banco.actualizarCliente(clave, nuevo);
    }

    public String eliminarCliente(String clave) {
        return banco.eliminarCliente(clave);
    }

    public List<Usuario> getListaUsuarios() {
        return banco.getListaUsuarios();
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
}