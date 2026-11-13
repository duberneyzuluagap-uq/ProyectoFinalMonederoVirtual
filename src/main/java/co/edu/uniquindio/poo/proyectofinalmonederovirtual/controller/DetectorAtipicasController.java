package co.edu.uniquindio.poo.proyectofinalmonederovirtual.controller;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.*;
import java.util.List;

public class DetectorAtipicasController {

    private final DetectorTransaccionesAtipicas detector;

    public DetectorAtipicasController(Notificacion notificacion) {
        this.detector = new DetectorTransaccionesAtipicas(notificacion);
    }

    public List<Transaccion> detectarTransaccionesAtipicas(Cliente cliente) {
        return detector.detectar(cliente);
    }

    public boolean esAtipica(double monto, List<Transaccion> historial) {
        return detector.esAtipica(monto, historial);
    }
}