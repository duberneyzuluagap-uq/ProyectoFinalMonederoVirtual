package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.DatosInvalidosException;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.SaldoInsuficienteException;

public abstract class Monedero {
    private String numeroCuenta;
    private double saldo;

    public Monedero(String numeroCuenta, double saldo) {
        if (numeroCuenta == null || numeroCuenta.isBlank()) throw new DatosInvalidosException("El número de cuenta es obligatorio.");
        if (saldo < 0) throw new MontoInvalidoException("El saldo inicial no puede ser negativo.");
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public void depositar(double monto) {
        if (monto <= 0) throw new MontoInvalidoException("El monto a depositar debe ser mayor que cero.");
        saldo += monto;
    }

    public void retirar(double monto) {
        if (monto <= 0) throw new MontoInvalidoException("El monto a retirar debe ser mayor que cero.");
        if (monto > saldo) throw new SaldoInsuficienteException(
                "Saldo insuficiente en " + numeroCuenta + ": disponible " + saldo + ", requerido " + monto);
        saldo -= monto;
    }

    public double consultarSaldo() {
        return saldo;
    }


}
