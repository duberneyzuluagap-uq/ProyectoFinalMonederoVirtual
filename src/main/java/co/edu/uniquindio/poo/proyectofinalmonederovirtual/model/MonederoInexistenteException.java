package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

public class MonederoInexistenteException extends RuntimeException {
    public MonederoInexistenteException(String message) {
        super(message);
    }
}
