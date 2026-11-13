package co.edu.uniquindio.poo.proyectofinalmonederovirtual.exceptions;

/**
 * Se lanza cuando un monedero no tiene fondos suficientes para
 * completar un retiro o transferencia.
 */
public class SaldoInsuficienteException extends Exception {

    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
