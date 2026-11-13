package co.edu.uniquindio.poo.proyectofinalmonederovirtual.exceptions;

/**
 * Se lanza cuando un cliente intenta canjear un beneficio sin tener
 * los puntos acumulados requeridos.
 */
public class PuntosInsuficientesException extends Exception {

    public PuntosInsuficientesException(String mensaje) {
        super(mensaje);
    }
}
