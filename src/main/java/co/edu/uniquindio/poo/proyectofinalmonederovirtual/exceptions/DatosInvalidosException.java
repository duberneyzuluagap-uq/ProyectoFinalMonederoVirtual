package co.edu.uniquindio.poo.proyectofinalmonederovirtual.exceptions;

/**
 * Se lanza cuando los datos proporcionados a un método son nulos,
 * vacíos, negativos o ya existen en el sistema (duplicados).
 */
public class DatosInvalidosException extends Exception {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
