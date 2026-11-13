package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import java.time.LocalDate;

public class Alerta {
    private String mensaje;
    private LocalDate fecha;
    private TipoAlerta tipo;

    public Alerta(String mensaje, LocalDate fecha, TipoAlerta tipo) {
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.tipo = tipo;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public TipoAlerta getTipo() { return tipo; }
    public void setTipo(TipoAlerta tipo) { this.tipo = tipo; }
}
