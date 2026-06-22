package entidades;

import enumeraciones.Estado;
import enumeraciones.FormaPago;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pedido extends Base implements Calculable {
    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private FormaPago formaPago;
    private Usuario usuario;
    private List<DetallePedido> detalles;

    public Pedido() {
        super();
        this.detalles = new ArrayList<>();
        this.total = 0.0;
        this.fecha = LocalDate.now();
    }

    public Pedido(Estado estado, FormaPago formaPago) {
        super();
        this.fecha = LocalDate.now();
        this.estado = estado;
        this.formaPago = formaPago;
        this.total = 0.0;
        this.detalles = new ArrayList<>();
    }

    public void addDetallePedido(int cantidad, Double subtotal, Producto producto) {
        DetallePedido nuevoDetalle = new DetallePedido(cantidad, subtotal, producto);
        this.detalles.add(nuevoDetalle);
        calcularTotal();
    }

    public DetallePedido findeDetallePedidoByProducto(Producto producto) { // [cite: 115, 154]
        for (DetallePedido detalle : detalles) {
            if (detalle.obtenerProducto().obtenerId().equals(producto.obtenerId())) {
                return detalle;
            }
        }
        return null;
    }

    public void deleteDetallePedidoByProducto(Producto producto) { // [cite: 116, 155]
        DetallePedido detalleAEliminar = findeDetallePedidoByProducto(producto);
        if (detalleAEliminar != null) {
            detalles.remove(detalleAEliminar);
            calcularTotal(); // Actualizamos el total al remover un detalle
        }
    }

    // Implementación de la interfaz Calculable
    @Override
    public void calcularTotal() { // [cite: 111]
        double sumaTotal = 0.0;
        for (DetallePedido detalle : detalles) {
            sumaTotal += detalle.obtenerSubtotal();
        }
        this.total = sumaTotal;
    }

    // Getters y Setters
    public Usuario obtenerUsuario() {
        return usuario;
    }

    public void establecerUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public LocalDate obtenerFecha() { return fecha; }
    public void establecerFecha(LocalDate fecha) { this.fecha = fecha; }

    public Estado obtenerEstado() { return estado; }
    public void establecerEstado(Estado estado) { this.estado = estado; }

    public Double obtenerTotal() { return total; }

    public FormaPago obtenerFormaPago() { return formaPago; }
    public void establecerFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }

    public List<DetallePedido> obtenerDetalles() { return detalles; }

    @Override
    public String toString() {
        return "Pedido [ID=" + obtenerId() +
                ", Fecha=" + fecha +
                ", Estado=" + estado +
                ", FormaPago=" + formaPago +
                ", Usuario=" + (usuario != null ? usuario.obtenerNombre() + " " + usuario.obtenerApellido() : "Ninguno") +
                ", Total=$" + total +
                ", Cantidad Detalles=" + detalles.size() + "]";
    }
}