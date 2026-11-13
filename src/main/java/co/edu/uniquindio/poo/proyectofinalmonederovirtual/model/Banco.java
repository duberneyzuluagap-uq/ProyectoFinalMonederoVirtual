package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Banco {
    private String nombre;
    private String nit;

    private List<Usuario> listaUsuarios;
    private List<Beneficio> listaBeneficios;
    private List<Monedero> listaMonederos;
    private List<Transaccion> listaTransacciones;
    private List<Transferencia> listaTransferencias;

    public Banco(String nombre, String nit) {
        if (nombre == null || nombre.isBlank()) throw new DatosInvalidosException("El nombre del banco es obligatorio.");
        if (nit == null || nit.isBlank()) throw new DatosInvalidosException("El NIT es obligatorio.");
        this.nombre = nombre;
        this.nit = nit;
        this.listaUsuarios = new LinkedList<>();
        this.listaBeneficios = new LinkedList<>();
        this.listaMonederos = new LinkedList<>();
        this.listaTransacciones = new LinkedList<>();
        this.listaTransferencias = new LinkedList<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public List<Usuario> getListaUsuarios() { return listaUsuarios; }
    public void setListaUsuarios(List<Usuario> listaUsuarios) { this.listaUsuarios = listaUsuarios; }
    public List<Beneficio> getListaBeneficios() { return Collections.unmodifiableList(listaBeneficios); }
    public void setListaBeneficios(List<Beneficio> listaBeneficios) { this.listaBeneficios = listaBeneficios; }
    public List<Monedero> getListaMonederos() { return listaMonederos; }
    public void setListaMonederos(List<Monedero> listaMonederos) { this.listaMonederos = listaMonederos; }
    public List<Transaccion> getListaTransacciones() { return listaTransacciones; }
    public void setListaTransacciones(List<Transaccion> listaTransacciones) { this.listaTransacciones = listaTransacciones; }
    public List<Transferencia> getListaTransferencias() { return listaTransferencias; }
    public void setListaTransferencias(List<Transferencia> listaTransferencias) { this.listaTransferencias = listaTransferencias; }

// CRUD usuario
    public String registrarUsuario(Usuario usuario) {
        if (usuario == null) throw new DatosInvalidosException("un cliente no puede ser nulo.");
        if (buscarUsuario(usuario.getUser()).isPresent()) return "Ya existe un cliente con esa clave: " + usuario.getUser();
        listaUsuarios.add(usuario);
        return "Cliente agregado correctamente";
    }

    public String eliminarUsuario(String user) {
        Optional<Usuario> encontrado = buscarUsuario(user);
        if (encontrado.isEmpty()) return "No existe el usuario " + user;
        listaUsuarios.remove(encontrado.get());
        return "Usuario eliminado correctamente";
    }

    public Optional<Usuario> buscarUsuario(String user) {
        return listaUsuarios.stream().filter(x -> x.getUser().equals(user)).findFirst();
    }

    public String actualizarUsuario(String user, Usuario nuevo) {
        if (nuevo == null) throw new DatosInvalidosException("un usuario no puede ser nulo.");
        Optional<Usuario> encontrado = buscarUsuario(user);
        if (encontrado.isEmpty()) return "No existe un usuario: " + user;
        listaUsuarios.set(listaUsuarios.indexOf(encontrado.get()), nuevo);
        return "Cliente actualizado correctamente";
    }

//CRUD beneficio
    public String agregarBeneficio(Beneficio beneficio) {
        if (beneficio == null) throw new DatosInvalidosException("un beneficio no puede ser nulo.");
        if (buscarBeneficio(beneficio.getIdBeneficio()).isPresent()) return "Ya existe un beneficio con esa clave: " + beneficio.getIdBeneficio();
        listaBeneficios.add(beneficio);
        return "Beneficio agregado correctamente";
    }

    public String eliminarBeneficio(String clave) {
        Optional<Beneficio> encontrado = buscarBeneficio(clave);
        if (encontrado.isEmpty()) return "No existe un beneficio con clave " + clave;
        listaBeneficios.remove(encontrado.get());
        return "Beneficio eliminado correctamente";
    }

    public Optional<Beneficio> buscarBeneficio(String clave) {
        return listaBeneficios.stream().filter(x -> x.getIdBeneficio().equals(clave)).findFirst();
    }

    public String actualizarBeneficio(String clave, Beneficio nuevo) {
        if (nuevo == null) throw new DatosInvalidosException("un beneficio no puede ser nulo.");
        Optional<Beneficio> encontrado = buscarBeneficio(clave);
        if (encontrado.isEmpty()) return "No existe un beneficio con clave " + clave;
        listaBeneficios.set(listaBeneficios.indexOf(encontrado.get()), nuevo);
        return "Beneficio actualizado correctamente";
    }

//CRUD Monedero
    public String agregarMonedero(Monedero monedero) {
        if (monedero == null) throw new DatosInvalidosException("un monedero no puede ser nulo.");
        if (buscarMonedero(monedero.getNumeroCuenta()).isPresent()) return "Ya existe un monedero con esa clave: " + monedero.getNumeroCuenta();
        listaMonederos.add(monedero);
        return "Monedero agregado correctamente";
    }

    public String eliminarMonedero(String clave) {
        Optional<Monedero> encontrado = buscarMonedero(clave);
        if (encontrado.isEmpty()) return "No existe un monedero con clave " + clave;
        listaMonederos.remove(encontrado.get());
        return "Monedero eliminado correctamente";
    }

    public Optional<Monedero> buscarMonedero(String clave) {
        return listaMonederos.stream().filter(x -> x.getNumeroCuenta().equals(clave)).findFirst();
    }

    public String actualizarMonedero(String clave, Monedero nuevo) {
        if (nuevo == null) throw new DatosInvalidosException("un monedero no puede ser nulo.");
        Optional<Monedero> encontrado = buscarMonedero(clave);
        if (encontrado.isEmpty()) return "No existe un monedero con clave " + clave;
        listaMonederos.set(listaMonederos.indexOf(encontrado.get()), nuevo);
        return "Monedero actualizado correctamente";
    }

//CRUD transaccion
    public String agregarTransaccion(Transaccion transaccion) {
        if (transaccion == null) throw new DatosInvalidosException("una transacción no puede ser nulo.");
        if (buscarTransaccion(transaccion.getIdTransaccion()).isPresent()) return "Ya existe una transacción con esa clave: " + transaccion.getIdTransaccion();
        listaTransacciones.add(transaccion);
        return "Transaccion agregado correctamente";
    }

    public String eliminarTransaccion(String clave) {
        Optional<Transaccion> encontrado = buscarTransaccion(clave);
        if (encontrado.isEmpty()) return "No existe una transacción con clave " + clave;
        listaTransacciones.remove(encontrado.get());
        return "Transaccion eliminado correctamente";
    }

    public Optional<Transaccion> buscarTransaccion(String clave) {
        return listaTransacciones.stream().filter(x -> x.getIdTransaccion().equals(clave)).findFirst();
    }

    public String actualizarTransaccion(String clave, Transaccion nuevo) {
        if (nuevo == null) throw new DatosInvalidosException("una transacción no puede ser nulo.");
        Optional<Transaccion> encontrado = buscarTransaccion(clave);
        if (encontrado.isEmpty()) return "No existe una transacción con clave " + clave;
        listaTransacciones.set(listaTransacciones.indexOf(encontrado.get()), nuevo);
        return "Transaccion actualizado correctamente";
    }

//CRUD transferencia
    public String agregarTransferencia(Transferencia transferencia) {
        if (transferencia == null) throw new DatosInvalidosException("una transferencia no puede ser nulo.");
        if (buscarTransferencia(transferencia.getIdTransaccion()).isPresent()) return "Ya existe una transferencia con esa clave: " + transferencia.getIdTransaccion();
        listaTransferencias.add(transferencia);
        return "Transferencia agregado correctamente";
    }

    public String eliminarTransferencia(String clave) {
        Optional<Transferencia> encontrado = buscarTransferencia(clave);
        if (encontrado.isEmpty()) return "No existe una transferencia con clave " + clave;
        listaTransferencias.remove(encontrado.get());
        return "Transferencia eliminado correctamente";
    }

    public Optional<Transferencia> buscarTransferencia(String clave) {
        return listaTransferencias.stream().filter(x -> x.getIdTransaccion().equals(clave)).findFirst();
    }

    public String actualizarTransferencia(String clave, Transferencia nuevo) {
        if (nuevo == null) throw new DatosInvalidosException("una transferencia no puede ser nulo.");
        Optional<Transferencia> encontrado = buscarTransferencia(clave);
        if (encontrado.isEmpty()) return "No existe una transferencia con clave " + clave;
        listaTransferencias.set(listaTransferencias.indexOf(encontrado.get()), nuevo);
        return "Transferencia actualizado correctamente";
    }




}

