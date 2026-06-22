package entidades;

public class DetallePedido extends Base {
    private int cantidad; //
    private Double subtotal; //
    private Producto producto; // Relación N:1 con Producto [cite: 156]

    public DetallePedido() {
        super();
    }

    public DetallePedido(int cantidad, Double subtotal, Producto producto) {
        super();
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.producto = producto;
    }

    // Getters y Setters
    public int obtenerCantidad() { return cantidad; }
    public void establecerCantidad(int cantidad) { this.cantidad = cantidad; }

    public Double obtenerSubtotal() { return subtotal; }
    public void establecerSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Producto obtenerProducto() { return producto; }
    public void establecerProducto(Producto producto) { this.producto = producto; }

    @Override
    public String toString() {
        return "Detalle [Producto=" + (producto != null ? producto.obtenerNombre() : "Ninguno") +
                ", Cantidad=" + cantidad +
                ", Subtotal=$" + subtotal + "]";
    }
}
