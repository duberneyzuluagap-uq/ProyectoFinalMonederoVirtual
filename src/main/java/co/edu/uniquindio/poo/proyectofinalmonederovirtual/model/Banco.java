package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.util.ArrayList;
import java.util.List;

public class Banco {
    private String nombre;
    private String id;

    private List<Cliente> ListaClientes;
    private List<Transferencia> ListaTransferencias;

    public Banco(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
        ListaClientes = new ArrayList<>();
        ListaTransferencias = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Cliente> getListaClientes() {
        return ListaClientes;
    }

    public void setListaClientes(List<Cliente> listaClientes) {
        ListaClientes = listaClientes;
    }

    public List<Transferencia> getListaTransferencias() {
        return ListaTransferencias;
    }

    public void setListaTransferencias(List<Transferencia> listaTransferencias) {
        ListaTransferencias = listaTransferencias;
    }
}

