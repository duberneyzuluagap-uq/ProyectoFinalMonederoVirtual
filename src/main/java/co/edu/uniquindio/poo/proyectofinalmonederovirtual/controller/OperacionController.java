package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class OperacionController {
    public static final double TASA_TRANSFERENCIA = 0.02;
    public static final double TASA_RETIRO = 0.01;
    public static final double SALDO_BAJO = 20000;
    public static final String PREFIJO_CANJE = "CANJE-";

    public record Canje(Cliente cliente, MovimientoPuntos movimiento, String beneficio, String codigo, String estado) {
        public int puntos() { return Math.abs(movimiento.getPuntos()); }
    }

    private final Banco banco;
    private final TransaccionController transaccionController;
    private final MonederoController monederoController;
    private final AlertaController alertaController;
    private final DetectorAtipicasController detectorController;
    private final Notificacion notificacion;
    private final ProcesadorTransaccionesProgramadas procesadorProgramadas;

    private final Set<String> usuariosBloqueados = new HashSet<>();
    private final Set<String> beneficiosInactivos = new HashSet<>();
    private final Set<String> canjesAprobados = new HashSet<>();
    private final Map<String, String> conceptos = new HashMap<>();
    private int consecutivoCuenta = 1;

    public OperacionController(Banco banco, TransaccionController transaccionController,
                               MonederoController monederoController, AlertaController alertaController,
                               DetectorAtipicasController detectorController, Notificacion notificacion,
                               ProcesadorTransaccionesProgramadas procesadorProgramadas) {
        this.banco = banco;
        this.transaccionController = transaccionController;
        this.monederoController = monederoController;
        this.alertaController = alertaController;
        this.detectorController = detectorController;
        this.notificacion = notificacion;
        this.procesadorProgramadas = procesadorProgramadas;
    }

    public String nuevoId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    public String siguienteCuenta() { return String.format("MV-%04d", consecutivoCuenta++); }

    public static double parseMonto(String texto) {
        if (texto == null || texto.isBlank()) throw new DatosInvalidosException("Ingresa un monto.");
        String t = texto.replace("$", "").replace(" ", "").trim();

        // Formato colombiano: "1.500,50" -> el punto es de miles y la coma es el decimal
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        }
        // Sin coma: "2.000.000" -> los puntos separan miles y se quitan
        else if (esFormatoDeMiles(t)) {
            t = t.replace(".", "");
        }

        try {
            double v = Double.parseDouble(t);
            if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0) throw new MontoInvalidoException("El monto debe ser mayor que cero.");
            return v;
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El monto debe ser numérico.");
        }
    }
    public double comisionTransferencia(Cliente c, double monto) {
        RangoCliente r = c.getRango() == null ? RangoCliente.BRONCE : c.getRango();
        double descuento = switch (r) { case BRONCE -> 0.0; case PLATA -> 0.10; case ORO -> 0.20; case PLATINO -> 0.35; };
        return monto * TASA_TRANSFERENCIA * (1 - descuento);
    }
    public double comisionRetiro(double monto) { return monto * TASA_RETIRO; }

    public String recargar(Cliente c, Monedero m, double monto) {
        requerir(m);
        return registrarOperacion(c, new Deposito(nuevoId("TX"), monto, LocalDate.now(), EstadoTransaccion.RECIBIDA, false, m));
    }
    public String retirar(Cliente c, Monedero m, double monto) {
        requerir(m);
        return registrarOperacion(c, new Retiro(nuevoId("TX"), monto, LocalDate.now(), EstadoTransaccion.ENVIADA, false, m,
                comisionRetiro(monto)));
    }
    public String transferir(Cliente c, Monedero origen, Monedero destino, double monto, String concepto) {
        requerir(origen);
        Transferencia t = new Transferencia(nuevoId("TX"), monto, LocalDate.now(), EstadoTransaccion.ENVIADA, false, origen,
                comisionTransferencia(c, monto), destino);
        String r = registrarOperacion(c, t);
        if (concepto != null && !concepto.isBlank()) conceptos.put(t.getIdTransaccion(), concepto.trim());
        return r;
    }
    private void requerir(Monedero m) {
        if (m == null) throw new MonederoInexistenteException("Primero crea un monedero en 'Mis tarjetas'.");
    }

    public String registrarOperacion(Cliente c, Transaccion t) {
        String r = transaccionController.registrarTransaccion(c, t);
        detectorController.detectarTransaccionesAtipicas(c);
        verificarSaldoBajo(c);
        return t.isSospechosa() ? r + " ⚠ La operación fue marcada como potencialmente sospechosa." : r;
    }

    private void verificarSaldoBajo(Cliente c) {
        for (Monedero m : c.getListaMonederos()) {
            if (m.getSaldo() < SALDO_BAJO) {
                String msg = "Saldo bajo en " + m.getNumeroCuenta() + ": quedan $" + Math.round(m.getSaldo());
                if (c.buscarAlerta(msg).isEmpty()) {
                    alertaController.agregarAlerta(c, new Alerta(msg, LocalDate.now(), TipoAlerta.SALDO_BAJO));
                    notificacion.enviar(c, msg);
                }
            }
        }
    }

    public String getConcepto(String idTransaccion) { return conceptos.get(idTransaccion); }

    public Monedero buscarMonederoDestino(String texto) {
        if (texto == null || texto.isBlank())
            throw new DatosInvalidosException("Ingresa el destinatario (n.º de cuenta o correo).");
        String buscado = texto.trim();

        // 1) Primero se busca directo por número de cuenta
        Optional<Monedero> porCuenta = banco.buscarMonedero(buscado);
        if (porCuenta.isPresent()) return porCuenta.get();

        // 2) Si no, se busca por el correo o el usuario de cada cliente
        for (Usuario usuario : banco.getListaUsuarios()) {
            if (!(usuario instanceof Cliente cliente)) continue;
            boolean coincide = usuario.getCorreo().equalsIgnoreCase(buscado)
                    || usuario.getUser().equalsIgnoreCase(buscado);
            if (coincide && !cliente.getListaMonederos().isEmpty()) {
                return cliente.getListaMonederos().get(0);
            }
        }
        throw new MonederoInexistenteException("No se encontró ningún monedero para '" + buscado + "'.");
    }

    public Optional<Cliente> buscarTitular(Monedero m) {
        return banco.getListaUsuarios().stream().filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u).filter(cl -> cl.getListaMonederos().contains(m)).findFirst();
    }
    public String nombreTitular(Monedero m) { return buscarTitular(m).map(Usuario::getNombre).orElse("—"); }

    public String revertirTransferencia(Transferencia t) {
        Cliente dueno = buscarTitular(t.getMonedero())
                .orElseThrow(() -> new MonederoInexistenteException("No se encontró el titular de la transferencia."));
        return transaccionController.revertirTransferencia(dueno, t);
    }

    public Monedero crearMonederoAhorro(Cliente c, double meta, double tasa) {
        MonederoAhorro m = new MonederoAhorro(siguienteCuenta(), 0, meta, tasa);
        monederoController.agregarMonedero(c, m);
        return m;
    }
    public Monedero crearMonederoGastoDiario(Cliente c) {
        MonederoGastoDiario m = new MonederoGastoDiario(siguienteCuenta(), 0, 0);
        monederoController.agregarMonedero(c, m);
        return m;
    }
    public void eliminarMonedero(Cliente c, Monedero m) {
        if (m == null) throw new DatosInvalidosException("Selecciona un monedero.");
        if (c.getListaMonederos().size() <= 1) throw new DatosInvalidosException("Debes conservar al menos un monedero.");
        if (m.getSaldo() > 0) throw new DatosInvalidosException("Solo puedes eliminar un monedero sin saldo.");
        monederoController.eliminarMonedero(c, m.getNumeroCuenta());
    }

    public Cliente registrarCliente(String nombre, String user, String password, String correo,
                                    String documento, String direccion, String telefono) {
        validarComunes(nombre, user, password, correo, true);
        validar(documentoValido(documento), "El documento debe tener entre 6 y 12 dígitos.");
        validar(telefonoValido(telefono), "El teléfono debe tener entre 7 y 10 dígitos.");
        validar(banco.getListaUsuarios().stream().noneMatch(u -> u instanceof Cliente cl && cl.getDocumento().equals(documento)),
                "Ya existe un cliente con ese documento.");
        Cliente c = new Cliente(nombre.trim(), user.trim(), password, correo.trim(), documento,
                direccion == null ? "" : direccion.trim(), telefono, 0, RangoCliente.BRONCE);
        banco.registrarUsuario(c);
        crearMonederoGastoDiario(c);
        return c;
    }

    public Admin registrarAdmin(String nombre, String user, String password, String correo) {
        validarComunes(nombre, user, password, correo, true);
        Admin a = new Admin(nombre.trim(), user.trim(), password, correo.trim());
        banco.registrarUsuario(a);
        return a;
    }

    public void actualizarUsuario(Usuario u, String nombre, String correo, String password, String direccion, String telefono) {
        validarComunes(nombre, u.getUser(), (password == null || password.isEmpty()) ? "1234" : password, correo, false);
        if (u instanceof Cliente c) {
            validar(telefonoValido(telefono), "El teléfono debe tener entre 7 y 10 dígitos.");
            c.setDireccion(direccion == null ? "" : direccion.trim());
            c.setTelefono(telefono);
        }
        u.setNombre(nombre.trim());
        u.setCorreo(correo.trim());
        if (password != null && !password.isEmpty()) u.setPassword(password);
    }

    private void validarComunes(String nombre, String user, String password, String correo, boolean nuevo) {
        validar(nombre != null && !nombre.isBlank(), "El nombre es obligatorio.");
        validar(usuarioValido(user), "Usuario: 3 a 20 caracteres (letras, números, _ o .).");
        validar(password != null && password.length() >= 4, "La contraseña debe tener mínimo 4 caracteres.");
        validar(correoValido(correo), "El correo no es válido.");
        if (nuevo) {
            validar(banco.buscarUsuario(user.trim()).isEmpty(), "Ya existe un usuario con el login '" + user + "'.");
            validar(banco.getListaUsuarios().stream().noneMatch(u -> u.getCorreo().equalsIgnoreCase(correo.trim())),
                    "Ya existe un usuario con ese correo.");
        }
    }
    private void validar(boolean condicion, String mensaje) {
        if (!condicion) throw new DatosInvalidosException(mensaje);
    }

    // ------------------------------------------------------------------
    //  Validaciones escritas con ciclos y condicionales (sin regex)
    // ------------------------------------------------------------------

    private static boolean soloDigitos(String texto) {
        if (texto == null || texto.isEmpty()) return false;
        for (int i = 0; i < texto.length(); i++) {
            if (!esDigito(texto.charAt(i))) return false;
        }
        return true;
    }

    private static boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean esLetraONumero(char c) {
        boolean letra = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        return letra || esDigito(c);
    }

    private static boolean correoValido(String correo) {
        if (correo == null) return false;
        String texto = correo.trim();

        int posicionArroba = texto.indexOf('@');
        if (posicionArroba <= 0) return false;                       // no tiene @ o empieza con @

        String antesDelArroba = texto.substring(0, posicionArroba);
        String dominio = texto.substring(posicionArroba + 1);

        boolean localOk = letrasNumerosY(antesDelArroba, "+-");
        boolean dominioOk = letrasNumerosY(dominio, "-") && !dominio.contains("..");
        int punto = dominio.indexOf('.');
        boolean hayPunto = punto > 0 && punto < dominio.length() - 1;

        return localOk && dominioOk && hayPunto;
    }

    private static boolean letrasNumerosY(String texto, String simbolosExtra) {
        if (texto.isEmpty()) return false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            boolean permitido = esLetraONumero(c) || c == '.' || c == '_'
                    || simbolosExtra.indexOf(c) >= 0;
            if (!permitido) return false;
        }
        return true;
    }

    private static boolean documentoValido(String documento) {
        if (documento == null) return false;
        return documento.length() >= 6 && documento.length() <= 12 && soloDigitos(documento);
    }

    private static boolean telefonoValido(String telefono) {
        if (telefono == null) return false;
        return telefono.length() >= 7 && telefono.length() <= 10 && soloDigitos(telefono);
    }

    private static boolean usuarioValido(String user) {
        if (user == null || user.length() < 3 || user.length() > 20) return false;
        for (int i = 0; i < user.length(); i++) {
            char c = user.charAt(i);
            if (!esLetraONumero(c) && c != '_' && c != '.') return false;
        }
        return true;
    }

    private static boolean esFormatoDeMiles(String texto) {
        int posicion = 0;

        // 1) primer grupo: de 1 a 3 dígitos
        int digitos = 0;
        while (posicion < texto.length() && esDigito(texto.charAt(posicion))) {
            digitos++;
            posicion++;
        }
        if (digitos < 1 || digitos > 3 || posicion == texto.length()) return false;

        // 2) después: uno o más grupos de "." + 3 dígitos
        int grupos = 0;
        while (posicion < texto.length()) {
            if (texto.charAt(posicion) != '.') return false;
            posicion++;
            digitos = 0;
            while (posicion < texto.length() && esDigito(texto.charAt(posicion))) {
                digitos++;
                posicion++;
            }
            if (digitos != 3) return false;
            grupos++;
        }
        return grupos > 0;
    }

    public String eliminarUsuario(Usuario u) {
        if (u instanceof Cliente c) {
            new ArrayList<>(c.getListaMonederos()).forEach(m -> banco.eliminarMonedero(m.getNumeroCuenta()));
            new ArrayList<>(c.getListaTransacciones()).forEach(t -> {
                banco.eliminarTransaccion(t.getIdTransaccion());
                banco.eliminarTransferencia(t.getIdTransaccion());
            });
        }
        usuariosBloqueados.remove(u.getUser());
        return banco.eliminarUsuario(u.getUser());
    }

    public boolean isBloqueado(String user) { return usuariosBloqueados.contains(user); }
    public boolean cambiarBloqueo(String user) {
        if (!usuariosBloqueados.remove(user)) { usuariosBloqueados.add(user); return true; }
        return false;
    }

    public boolean isBeneficioActivo(String id) { return !beneficiosInactivos.contains(id); }
    public boolean cambiarEstadoBeneficio(String id) {
        if (!beneficiosInactivos.remove(id)) { beneficiosInactivos.add(id); return false; }
        return true;
    }
    public List<Beneficio> beneficiosActivos() {
        return banco.getListaBeneficios().stream().filter(b -> isBeneficioActivo(b.getIdBeneficio())).collect(Collectors.toList());
    }
    public String siguienteIdBeneficio() {
        int n = banco.getListaBeneficios().size() + 1;
        while (banco.buscarBeneficio(String.format("BEN-%03d", n)).isPresent()) n++;
        return String.format("BEN-%03d", n);
    }

    public String canjearBeneficio(Cliente c, Beneficio b) {
        if (b == null) throw new DatosInvalidosException("Selecciona un beneficio.");
        if (!isBeneficioActivo(b.getIdBeneficio())) throw new DatosInvalidosException("Este beneficio no está disponible.");
        int requeridos = b.getPuntosRequeridos();
        if (c.getPuntosAcumulados() < requeridos)
            throw new PuntosInsuficienteException("Puntos insuficientes: tienes " + c.getPuntosAcumulados() + " y necesitas " + requeridos + ".");
        String id = PREFIJO_CANJE + b.getIdBeneficio() + "-" + UUID.randomUUID().toString().substring(0, 8);
        c.agregarMovimiento(new MovimientoPuntos(id, -requeridos, LocalDate.now(), false));
        String msg = "Canje de «" + b.getNombre() + "» registrado. Queda pendiente de aprobación.";
        alertaController.agregarAlerta(c, new Alerta(msg + " (" + id.substring(id.length() - 8).toUpperCase() + ")", LocalDate.now(), TipoAlerta.RECORDATORIO));
        notificacion.enviar(c, msg);
        return msg;
    }

    public List<Canje> listarCanjes(Cliente c) {
        return c.getListaMovimientosPuntos().stream()
                .filter(m -> m.getIdMovimiento().startsWith(PREFIJO_CANJE))
                .map(m -> aCanje(c, m)).collect(Collectors.toList());
    }
    public List<Canje> listarTodosLosCanjes() {
        return banco.getListaUsuarios().stream().filter(u -> u instanceof Cliente)
                .flatMap(u -> listarCanjes((Cliente) u).stream()).collect(Collectors.toList());
    }

    private Canje aCanje(Cliente c, MovimientoPuntos m) {
        String idMovimiento = m.getIdMovimiento();


        String codigo = idMovimiento.substring(idMovimiento.length() - 8).toUpperCase();


        String sinCodigo = idMovimiento.substring(0, idMovimiento.length() - 9);

        String idBeneficio = sinCodigo.startsWith(PREFIJO_CANJE)
                ? sinCodigo.substring(PREFIJO_CANJE.length())
                : sinCodigo;

        String nombre = banco.buscarBeneficio(idBeneficio)
                .map(Beneficio::getNombre).orElse(idBeneficio);
        String estado = m.isAnulado() ? "Rechazado"
                : (canjesAprobados.contains(idMovimiento) ? "Aprobado" : "Pendiente");

        return new Canje(c, m, nombre, codigo, estado);
    }

    public void aprobarCanje(Canje canje) {
        if (canje == null) throw new DatosInvalidosException("Selecciona un canje.");
        if (!"Pendiente".equals(canje.estado())) throw new DatosInvalidosException("Solo se pueden aprobar canjes pendientes.");
        canjesAprobados.add(canje.movimiento().getIdMovimiento());
        notificacion.enviar(canje.cliente(), "Tu canje de «" + canje.beneficio() + "» fue aprobado.");
    }
    public void rechazarCanje(Canje canje) {
        if (canje == null) throw new DatosInvalidosException("Selecciona un canje.");
        if (!"Pendiente".equals(canje.estado())) throw new DatosInvalidosException("Solo se pueden rechazar canjes pendientes.");
        canje.movimiento().anular();
        canje.cliente().recalcular();
        notificacion.enviar(canje.cliente(), "Tu canje de «" + canje.beneficio() + "» fue rechazado. Tus puntos fueron devueltos.");
    }


    public String programarDeposito(Cliente c, Monedero m, double monto, LocalDate fechaInicio, Frecuencia frecuencia) {
        requerir(m);
        Transaccion t = new Deposito(nuevoId("TX"), monto, fechaInicio, EstadoTransaccion.PENDIENTE, false, m);
        TransaccionProgramada prog = new TransaccionProgramada(nuevoId("SCH"), monto, fechaInicio, EstadoTransaccion.PENDIENTE, false, m, fechaInicio, frecuencia, t);
        c.agregarTransaccionProgramada(prog);
        return "Depósito programado: " + frecuencia + " de $" + String.format("%,.0f", monto) + " a partir del " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String programarRetiro(Cliente c, Monedero m, double monto, LocalDate fechaInicio, Frecuencia frecuencia) {
        requerir(m);
        double comision = comisionRetiro(monto);
        Transaccion t = new Retiro(nuevoId("TX"), monto, fechaInicio, EstadoTransaccion.PENDIENTE, false, m, comision);
        TransaccionProgramada prog = new TransaccionProgramada(nuevoId("SCH"), monto, fechaInicio, EstadoTransaccion.PENDIENTE, false, m, fechaInicio, frecuencia, t);
        c.agregarTransaccionProgramada(prog);
        return "Retiro programado: " + frecuencia + " de $" + String.format("%,.0f", monto) + " a partir del " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }


    public String programarTransferencia(Cliente c, Monedero origen, Monedero destino, double monto, String concepto, LocalDate fechaInicio, Frecuencia frecuencia) {
        requerir(origen);
        if (destino == null) throw new MonederoInexistenteException("El monedero destino no existe.");
        if (origen == destino) throw new DatosInvalidosException("El monedero origen y destino no pueden ser el mismo.");
        double comision = comisionTransferencia(c, monto);
        Transaccion t = new Transferencia(nuevoId("TX"), monto, fechaInicio, EstadoTransaccion.PENDIENTE, false, origen, comision, destino);
        TransaccionProgramada prog = new TransaccionProgramada(nuevoId("SCH"), monto, fechaInicio, EstadoTransaccion.PENDIENTE, false, origen, fechaInicio, frecuencia, t);
        c.agregarTransaccionProgramada(prog);
        if (concepto != null && !concepto.isBlank()) conceptos.put(t.getIdTransaccion(), concepto);
        return "Transferencia programada: " + frecuencia + " de $" + String.format("%,.0f", monto) + " a partir del " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }


    public String eliminarTransaccionProgramada(Cliente c, String idProgramada) {
        Optional<TransaccionProgramada> prog = c.getListaTransaccionesProgramadas().stream()
                .filter(p -> p.getIdTransaccion().equals(idProgramada)).findFirst();
        if (prog.isEmpty()) throw new DatosInvalidosException("No existe una transacción programada con ese id.");
        c.getListaTransaccionesProgramadas().remove(prog.get());
        return "Transacción programada eliminada.";
    }


    public void procesarTodasProgramadas() {
        LocalDate hoy = LocalDate.now();
        for (Usuario u : banco.getListaUsuarios()) {
            if (u instanceof Cliente c) {
                procesadorProgramadas.procesarPendientes(c, hoy);
            }
        }
    }
}