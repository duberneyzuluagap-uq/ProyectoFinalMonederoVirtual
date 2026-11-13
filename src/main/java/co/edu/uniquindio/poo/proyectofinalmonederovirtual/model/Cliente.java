package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.DatosInvalidosException;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Cliente extends Usuario {
    private String documento;
    private String direccion;
    private String telefono;
    private int puntosAcumulados;
    private RangoCliente rango;

    // Colecciones que salen de las asociaciones del diagrama (ver notas de entrega)
    private List<Monedero> listaMonederos;
    private List<MovimientoPuntos> listaMovimientosPuntos;
    private List<Alerta> listaAlertas;
    private List<Transaccion> listaTransacciones;
    private List<TransaccionProgramada> listaTransaccionesProgramadas;

    public Cliente(String nombre, String user, String password, String correo, String documento,
                   String direccion, String telefono, int puntosAcumulados, RangoCliente rango) {
        super(nombre, user, password, correo);
        if (documento == null || documento.isBlank())
            throw new DatosInvalidosException("El documento es obligatorio.");
        this.documento = documento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.puntosAcumulados = puntosAcumulados;
        this.rango = rango;
        this.listaMonederos = new LinkedList<>();
        this.listaMovimientosPuntos = new LinkedList<>();
        this.listaAlertas = new LinkedList<>();
        this.listaTransacciones = new LinkedList<>();
        this.listaTransaccionesProgramadas = new LinkedList<>();
    }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public int getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(int puntosAcumulados) { this.puntosAcumulados = puntosAcumulados; }
    public RangoCliente getRango() { return rango; }
    public void setRango(RangoCliente rango) { this.rango = rango; }
    public List<Monedero> getListaMonederos() { return listaMonederos; }
    public void setListaMonederos(List<Monedero> listaMonederos) { this.listaMonederos = listaMonederos; }
    public List<MovimientoPuntos> getListaMovimientosPuntos() { return listaMovimientosPuntos; }
    public void setListaMovimientosPuntos(List<MovimientoPuntos> l) { this.listaMovimientosPuntos = l; }
    public List<Alerta> getListaAlertas() { return listaAlertas; }
    public void setListaAlertas(List<Alerta> listaAlertas) { this.listaAlertas = listaAlertas; }
    public List<Transaccion> getListaTransacciones() { return listaTransacciones; }
    public void setListaTransacciones(List<Transaccion> listaTransacciones) { this.listaTransacciones = listaTransacciones; }
    public List<TransaccionProgramada> getListaTransaccionesProgramadas() { return listaTransaccionesProgramadas; }
    public void setListaTransaccionesProgramadas(List<TransaccionProgramada> l) { this.listaTransaccionesProgramadas = l; }

    /** Suma puntos (positivos) o los descuenta en un canje (negativos), y recalcula puntos y rango. */
    public void acumularPuntos(int puntos) {
        if (puntos == 0) throw new DatosInvalidosException("Los puntos deben ser distintos de cero.");
        if (puntos < 0 && puntosAcumulados + puntos < 0)
            throw new PuntosInsuficienteException("Puntos insuficientes: tiene " + puntosAcumulados
                    + " y se requieren " + (-puntos));
        listaMovimientosPuntos.add(new MovimientoPuntos(
                "MOV-" + UUID.randomUUID().toString().substring(0, 8), puntos, LocalDate.now(), false));
        recalcular();
    }

    /** Recalcula los puntos sumando solo los movimientos no anulados y actualiza el rango. */
    public void recalcular() {
        puntosAcumulados = listaMovimientosPuntos.stream()
                .filter(m -> !m.isAnulado())
                .mapToInt(MovimientoPuntos::getPuntos)
                .sum();
        calcularRango();
    }

    public RangoCliente calcularRango() {
        rango = RangoCliente.desdePuntos(puntosAcumulados);
        return rango;
    }


    /** Ejecuta la transacción sobre un monedero del cliente, la guarda en el historial y suma sus puntos. */
    public String registrarTransaccion(Transaccion transaccion) {
        if (transaccion == null) throw new DatosInvalidosException("La transacción no puede ser nula.");
        if (buscarMonedero(transaccion.getMonedero().getNumeroCuenta()).isEmpty())
            throw new MonederoInexistenteException("El monedero " + transaccion.getMonedero().getNumeroCuenta()
                    + " no pertenece al cliente " + getNombre());
        transaccion.ejecutar();
        listaTransacciones.add(transaccion);
        int puntos = transaccion.calcularPuntos();
        if (puntos > 0) {
            listaMovimientosPuntos.add(new MovimientoPuntos(
                    "MOV-" + transaccion.getIdTransaccion() + "-" + UUID.randomUUID().toString().substring(0, 8),
                    puntos, transaccion.getFecha(), false));
            recalcular();
        }
        return "Transacción " + transaccion.getIdTransaccion() + " ejecutada. Puntos ganados: " + puntos;
    }

    /** Revierte la transferencia y anula los puntos que generó; los puntos se recalculan dinámicamente. */
    public String revertirTransferencia(Transferencia transferencia) {
        if (transferencia == null) throw new DatosInvalidosException("La transferencia no puede ser nula.");
        transferencia.revertir();
        String prefijo = "MOV-" + transferencia.getIdTransaccion() + "-";
        listaMovimientosPuntos.stream()
                .filter(m -> m.getIdMovimiento().startsWith(prefijo) && !m.isAnulado())
                .forEach(MovimientoPuntos::anular);
        recalcular();
        return "Transferencia " + transferencia.getIdTransaccion() + " revertida. Puntos actuales: " + puntosAcumulados;
    }

    public String agregarTransaccionProgramada(TransaccionProgramada programada) {
        if (programada == null) throw new DatosInvalidosException("La transacción programada no puede ser nula.");
        listaTransaccionesProgramadas.add(programada);
        return "Transacción programada agregada correctamente";
    }

    public String agregarMonedero(Monedero monedero) {
        if (monedero == null) throw new DatosInvalidosException("El monedero no puede ser nulo.");
        if (buscarMonedero(monedero.getNumeroCuenta()).isPresent())
            return "El cliente ya tiene el monedero " + monedero.getNumeroCuenta();
        listaMonederos.add(monedero);
        return "Monedero agregado correctamente";
    }

    public Optional<Monedero> buscarMonedero(String numeroCuenta) {
        return listaMonederos.stream().filter(m -> m.getNumeroCuenta().equals(numeroCuenta)).findFirst();
    }

    public String eliminarMonedero(String numeroCuenta) {
        Optional<Monedero> monedero = buscarMonedero(numeroCuenta);
        if (monedero.isEmpty()) return "No existe el monedero " + numeroCuenta;
        listaMonederos.remove(monedero.get());
        return "Monedero eliminado correctamente";
    }


    public String agregarMovimiento(MovimientoPuntos movimiento) {
        if (movimiento == null) throw new DatosInvalidosException("El movimiento no puede ser nulo.");
        if (buscarMovimiento(movimiento.getIdMovimiento()).isPresent())
            return "Ya existe el movimiento " + movimiento.getIdMovimiento();
        listaMovimientosPuntos.add(movimiento);
        recalcular();
        return "Movimiento agregado correctamente";
    }

    public Optional<MovimientoPuntos> buscarMovimiento(String idMovimiento) {
        return listaMovimientosPuntos.stream().filter(m -> m.getIdMovimiento().equals(idMovimiento)).findFirst();
    }

    public String eliminarMovimiento(String idMovimiento) {
        Optional<MovimientoPuntos> movimiento = buscarMovimiento(idMovimiento);
        if (movimiento.isEmpty()) return "No existe el movimiento " + idMovimiento;
        listaMovimientosPuntos.remove(movimiento.get());
        recalcular();
        return "Movimiento eliminado correctamente";
    }

    public String actualizarMovimiento(String idMovimiento, MovimientoPuntos nuevo) {
        Optional<MovimientoPuntos> movimiento = buscarMovimiento(idMovimiento);
        if (movimiento.isEmpty()) return "No existe el movimiento " + idMovimiento;
        listaMovimientosPuntos.set(listaMovimientosPuntos.indexOf(movimiento.get()), nuevo);
        recalcular();
        return "Movimiento actualizado correctamente";
    }


    public String agregarAlerta(Alerta alerta) {
        if (alerta == null) throw new DatosInvalidosException("La alerta no puede ser nula.");
        if (buscarAlerta(alerta.getMensaje()).isPresent()) return "Ya existe una alerta con ese mensaje";
        listaAlertas.add(alerta);
        return "Alerta agregada correctamente";
    }

    public Optional<Alerta> buscarAlerta(String mensaje) {
        return listaAlertas.stream().filter(a -> a.getMensaje().equals(mensaje)).findFirst();
    }

    public String eliminarAlerta(String mensaje) {
        Optional<Alerta> alerta = buscarAlerta(mensaje);
        if (alerta.isEmpty()) return "No existe la alerta";
        listaAlertas.remove(alerta.get());
        return "Alerta eliminada correctamente";
    }

    public String actualizarAlerta(String mensaje, Alerta nueva) {
        Optional<Alerta> alerta = buscarAlerta(mensaje);
        if (alerta.isEmpty()) return "No existe la alerta";
        listaAlertas.set(listaAlertas.indexOf(alerta.get()), nueva);
        return "Alerta actualizada correctamente";
    }





}
