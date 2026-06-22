package entidades;

public class Categoria extends Base {
    private String nombre;
    private String descripcion;

    public Categoria() {
        super();
    }

    public Categoria(String nombre, String descripcion) {
        super();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public void establecerNombre(String nombre) {
        this.nombre = nombre;
    }

    public String obtenerDescripcion() {
        return descripcion;
    }

    public void establecerDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Categoría [ID=" + obtenerId() +
                ", Nombre=" + nombre +
                ", Descripción=" + descripcion + "]";
    }
}
