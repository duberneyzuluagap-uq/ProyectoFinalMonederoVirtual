package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

public class NotificacionSimulada implements Notificacion {
    @Override
    public boolean enviar(Cliente destinatario, String mensaje) {
        if (destinatario == null || mensaje == null || mensaje.isBlank()) return false;
        System.out.printf("[WhatsApp -> %s] %s%n", destinatario.getTelefono(), mensaje);
        System.out.printf("[Email    -> %s] %s%n", destinatario.getCorreo(), mensaje);
        System.out.printf("[SMS      -> %s] %s%n", destinatario.getTelefono(), mensaje);
        return true;
    }
}