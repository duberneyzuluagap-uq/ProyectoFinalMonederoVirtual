package co.edu.uniquindio.poo.proyectofinalmonederovirtual.exceptions;

/**
 * Se lanza cuando se intenta operar sobre un monedero que no existe
 * en el sistema (ni en el banco ni en el cliente).
 */
public class MonederoNoEncontradoException extends Exception {

    public MonederoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
