package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

public class MonederoAhorro extends Monedero {
    private double metaAhorro;
    private double tasaInteres;

    public MonederoAhorro(String numeroCuenta, double saldo, double metaAhorro, double tasaInteres) {
        super(numeroCuenta, saldo);
        this.metaAhorro = metaAhorro;
        this.tasaInteres = tasaInteres;
    }

    public double getMetaAhorro() { return metaAhorro; }
    public void setMetaAhorro(double metaAhorro) { this.metaAhorro = metaAhorro; }
    public double getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(double tasaInteres) { this.tasaInteres = tasaInteres; }

    public double calcularInteres() {
        return getSaldo() * tasaInteres;
    }


}
