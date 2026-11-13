package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

public class MonederoGastoDiario extends Monedero {
    private double gastadoHoy;

    public MonederoGastoDiario(String numeroCuenta, double saldo, double gastadoHoy) {
        super(numeroCuenta, saldo);
        this.gastadoHoy = gastadoHoy;
    }

    public double getGastadoHoy() { return gastadoHoy; }
    public void setGastadoHoy(double gastadoHoy) { this.gastadoHoy = gastadoHoy; }

    public void registrarGasto(double monto) {
        retirar(monto);
        gastadoHoy += monto;
    }


}
