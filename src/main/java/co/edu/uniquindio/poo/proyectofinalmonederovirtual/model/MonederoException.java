package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

/**
 * Superclase de todas las excepciones del proyecto.
 *
 * Las cinco excepciones concretas (DatosInvalidosException, MontoInvalidoException,
 * SaldoInsuficienteException, MonederoInexistenteException y PuntosInsuficienteException)
 * heredan de esta, así que en cualquier parte del programa se puede atrapar una sola vez:
 *
 *     try {
 *         operacion();
 *     } catch (MonederoException e) {
 *         mostrarError(e.getMessage());
 *     }
 *
 * Esto se llama polimorfismo: un catch de la superclase atrapa a todas las hijas.
 */
public class MonederoException extends RuntimeException {

    public MonederoException(String message) {
        super(message);
    }
}
