package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;
import java.util.Optional;

public class UsuarioController {

    private final Banco banco;
    private Usuario usuarioActual;

    public UsuarioController(Banco banco) {
        this.banco = banco;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean iniciarSesion(String user, String password) {
        Optional<Usuario> encontrado = banco.buscarUsuario(user);
        if (encontrado.isPresent() && encontrado.get().iniciarSesion(user, password)) {
            this.usuarioActual = encontrado.get();
            return true;
        }
        return false;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public String registrarUsuario(Usuario usuario) {
        return banco.registrarUsuario(usuario);
    }

    public Optional<Usuario> buscarUsuario(String user) {
        return banco.buscarUsuario(user);
    }

    public String actualizarCliente(String clave, Cliente nuevo) {
        return banco.actualizarUsuario(clave, nuevo);
    }

    public String eliminarCliente(String clave) {
        return banco.eliminarUsuario(clave);
    }

    public List<Usuario> getListaUsuarios() {
        return banco.getListaUsuarios();
    }
}