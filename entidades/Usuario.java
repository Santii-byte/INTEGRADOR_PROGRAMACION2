package entidades;

import enumeraciones.Rol;

public class Usuario extends Base {
    private String nombre;
    private String apellido;
    private String mail;
    private String celular;
    private String contrasenia;
    private Rol rol;

    public Usuario() {
        super();
    }

    public Usuario(String nombre, String apellido, String mail, String celular, String contrasenia, Rol rol) {
        super();
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.celular = celular;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    public String obtenerNombre() { return nombre; }
    public void establecerNombre(String nombre) { this.nombre = nombre; }

    public String obtenerApellido() { return apellido; }
    public void establecerApellido(String apellido) { this.apellido = apellido; }

    public String obtenerMail() { return mail; }
    public void establecerMail(String mail) { this.mail = mail; }

    public String obtenerCelular() { return celular; }
    public void establecerCelular(String celular) { this.celular = celular; }

    public String obtenerContrasenia() { return contrasenia; }
    public void establecerContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public Rol obtenerRol() { return rol; }
    public void establecerRol(Rol rol) { this.rol = rol; }

    @Override
    public String toString() {
        return "Usuario [ID=" + obtenerId() +
                ", Nombre=" + nombre + " " + apellido +
                ", Mail=" + mail +
                ", Rol=" + rol + "]";
    }
}
