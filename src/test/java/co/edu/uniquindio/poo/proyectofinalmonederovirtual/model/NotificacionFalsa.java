package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.util.ArrayList;
import java.util.List;
public class NotificacionFalsa implements Notificacion {
    private final List<String> mensajes = new ArrayList<>();

    @Override
    public boolean enviar(Cliente destinatario, String mensaje) {
        mensajes.add(mensaje);
        return true;
    }

    public List<String> getMensajes() { return mensajes; }
    public int total() { return mensajes.size(); }
}
