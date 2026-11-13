package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public class Deposito extends Transaccion {
    public Deposito(String idTransaccion, double monto, LocalDate fecha, EstadoTransaccion estado,
                    boolean sospechosa, Monedero monedero) {
        super(idTransaccion, monto, fecha, estado, sospechosa, monedero);
    }

    @Override
    public void ejecutar() {
        getMonedero().depositar(getMonto());
        setEstado(EstadoTransaccion.RECIBIDA);
    }


    @Override
    public int calcularPuntos() {
        return bloquesDeCien();
    }
}
