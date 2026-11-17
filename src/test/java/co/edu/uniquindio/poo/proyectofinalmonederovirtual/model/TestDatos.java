package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public final class TestDatos {
    private TestDatos() {}

    public static Cliente cliente(String user) {
        return new Cliente("Cliente " + user, user, "1234", user + "@mail.com",
                String.valueOf(1_000_000 + Math.abs(user.hashCode() % 8_000_000)), "Calle 1", "3001112233",
                0, RangoCliente.BRONCE);
    }

    public static MonederoGastoDiario monedero(String cuenta, double saldo) {
        return new MonederoGastoDiario(cuenta, saldo, 0);
    }

    public static Cliente clienteConMonedero(String user, String cuenta, double saldo) {
        Cliente c = cliente(user);
        c.agregarMonedero(monedero(cuenta, saldo));
        return c;
    }

    public static Deposito deposito(String id, double monto, Monedero m) {
        return new Deposito(id, monto, LocalDate.now(), EstadoTransaccion.RECIBIDA, false, m);
    }

    public static Retiro retiro(String id, double monto, double comision, Monedero m) {
        return new Retiro(id, monto, LocalDate.now(), EstadoTransaccion.ENVIADA, false, m, comision);
    }

    public static Transferencia transferencia(String id, double monto, double comision, Monedero origen, Monedero destino) {
        return new Transferencia(id, monto, LocalDate.now(), EstadoTransaccion.ENVIADA, false, origen, comision, destino);
    }
}
