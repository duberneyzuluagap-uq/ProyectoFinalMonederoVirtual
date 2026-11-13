package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.DatosInvalidosException;
import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.SaldoInsuficienteException;

import java.time.LocalDate;

public class Transferencia extends Transaccion {
    private double comision;
    private Monedero monederoDestino;

    public Transferencia(String idTransaccion, double monto, LocalDate fecha, EstadoTransaccion estado,
                         boolean sospechosa, Monedero monedero, double comision, Monedero monederoDestino) {
        super(idTransaccion, monto, fecha, estado, sospechosa, monedero);
        this.comision = comision;
        this.monederoDestino = monederoDestino;
    }

    public double getComision() { return comision; }
    public void setComision(double comision) { this.comision = comision; }
    public Monedero getMonederoDestino() { return monederoDestino; }
    public void setMonederoDestino(Monedero monederoDestino) { this.monederoDestino = monederoDestino; }

    @Override
    public void ejecutar() {
        if (monederoDestino == null)
            throw new MonederoInexistenteException("La transferencia no tiene monedero destino.");
        if (monederoDestino == getMonedero())
            throw new DatosInvalidosException("El monedero origen y destino no pueden ser el mismo.");
        getMonedero().retirar(getMonto() + comision);
        monederoDestino.depositar(getMonto());
        setEstado(EstadoTransaccion.ENVIADA);
    }


    @Override
    public int calcularPuntos() {
        return bloquesDeCien() * 3;
    }


    public void revertir() {
        if (getEstado() != EstadoTransaccion.ENVIADA)
            throw new DatosInvalidosException("Solo se puede revertir una transferencia enviada.");
        if (monederoDestino.consultarSaldo() < getMonto())
            throw new SaldoInsuficienteException("El monedero destino ya no tiene saldo para revertir la transferencia.");
        monederoDestino.retirar(getMonto());
        getMonedero().depositar(getMonto() + comision);
        setEstado(EstadoTransaccion.CANCELADA);
    }


}
