package co.edu.uniquindio.poo.proyectofinalmonederovirtual.model;

import co.edu.uniquindio.poo.proyectofinalmonederovirtual.model.DatosInvalidosException;

public abstract class Usuario {
    private String nombre;
    private String user;
    private String password;
    private String correo;



    public Usuario(String nombre, String user, String password, String correo){
        if (nombre == null || nombre.isBlank()) throw new DatosInvalidosException("El nombre es obligatorio.");
        if (user == null || user.isBlank()) throw new DatosInvalidosException("El usuario es obligatorio.");
        if (password == null || password.isBlank()) throw new DatosInvalidosException("La contraseña es obligatoria.");
        if (correo == null || !correo.contains("@")) throw new DatosInvalidosException("El correo no es válido.");
        this.nombre = nombre;
        this.user = user;
        this.password = password;
        this.correo = correo;
    }

    public boolean iniciarSesion(String user, String password) {
        return this.user.equals(user) && this.password.equals(password);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}

