package entidades;

public class Producto extends Base {
    private String nombre;
    private Double precio;
    private String descripcion;
    private int stock;
    private String imagen;
    private Boolean disponible;
    private Categoria categoria;

    public Producto() {
        super();
    }

    public Producto(String nombre, Double precio, String descripcion, int stock, String imagen, Boolean disponible, Categoria categoria) {
        super();
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.stock = stock;
        this.imagen = imagen;
        this.disponible = disponible;
        this.categoria = categoria;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public void establecerNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double obtenerPrecio() {
        return precio;
    }

    public void establecerPrecio(Double precio) {
        this.precio = precio;
    }

    public String obtenerDescripcion() {
        return descripcion;
    }

    public void establecerDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int obtenerStock() {
        return stock;
    }

    public void establecerStock(int stock) {
        this.stock = stock;
    }

    public String obtenerImagen() {
        return imagen;
    }

    public void establecerImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean estaDisponible() {
        return disponible;
    }

    public void establecerDisponibilidad(Boolean disponible) {
        this.disponible = disponible;
    }

    public Categoria obtenerCategoria() {
        return categoria;
    }

    public void establecerCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Producto [ID=" + obtenerId() +
                ", Nombre=" + nombre +
                ", Precio=$" + precio +
                ", Stock=" + stock +
                ", Disponible=" + (disponible ? "Sí" : "No") +
                ", Categoría=" + (categoria != null ? categoria.obtenerNombre() : "Ninguna") + "]";
    }
}
