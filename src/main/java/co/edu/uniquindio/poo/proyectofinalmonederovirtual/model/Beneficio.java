package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

public class Beneficio {
    private String idBeneficio;
    private String nombre;
    private String descripcion;
    private int puntosRequeridos;

    public Beneficio(String idBeneficio, String nombre, String descripcion, int puntosRequeridos) {
        this.idBeneficio = idBeneficio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosRequeridos = puntosRequeridos;
    }

    public String getIdBeneficio() { return idBeneficio; }
    public void setIdBeneficio(String idBeneficio) { this.idBeneficio = idBeneficio; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getPuntosRequeridos() { return puntosRequeridos; }
    public void setPuntosRequeridos(int puntosRequeridos) { this.puntosRequeridos = puntosRequeridos; }
}
