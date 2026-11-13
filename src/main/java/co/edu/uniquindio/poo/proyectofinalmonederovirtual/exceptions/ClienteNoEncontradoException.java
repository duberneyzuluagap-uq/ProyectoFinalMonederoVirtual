package co.edu.uniquindio.poo.proyectofinalmonederovirtual.exceptions;

/**
 * Se lanza cuando se intenta operar sobre un cliente que no está
 * registrado en el banco.
 */
public class ClienteNoEncontradoException extends Exception {

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
