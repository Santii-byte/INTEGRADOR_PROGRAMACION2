package servicios;

import entidades.DetallePedido;
import entidades.Pedido;
import entidades.Producto;
import entidades.Usuario;
import enumeraciones.Estado;
import enumeraciones.FormaPago;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedidoServicio {

    private List<Pedido> pedidos;
    private Long generadorId;
    private UsuarioServicio usuarioServicio;
    private ProductoServicio productoServicio;

    public PedidoServicio(UsuarioServicio usuarioServicio, ProductoServicio productoServicio) {
        this.pedidos = new ArrayList<>();
        this.generadorId = 1L;
        this.usuarioServicio = usuarioServicio;
        this.productoServicio = productoServicio;
    }

    public List<Pedido> listarPedidosActivos() {
        List<Pedido> activos = new ArrayList<>();
        for (Pedido ped : pedidos) {
            if (!ped.estaEliminado()) { // Filtra los dados de baja [cite: 389]
                activos.add(ped);
            }
        }
        return activos;
    }

    public Pedido crearPedido(Long idUsuario, FormaPago formaPago, Map<Long, Integer> carrito) {
        Usuario usuarioAsociado = usuarioServicio.buscarPorId(idUsuario);
        Pedido nuevoPedido = new Pedido(Estado.PENDIENTE, formaPago);
        nuevoPedido.establecerUsuario(usuarioAsociado);

        try {
            for (Map.Entry<Long, Integer> item : carrito.entrySet()) {
                Long idProducto = item.getKey();
                Integer cantidad = item.getValue();

                if (cantidad <= 0) {
                    throw new ReglaNegocioExcepcion("La cantidad para el detalle debe ser mayor a 0.");
                }

                Producto producto = productoServicio.buscarPorId(idProducto);

                if (producto.obtenerStock() < cantidad) {
                    throw new ReglaNegocioExcepcion("Stock insuficiente para el producto: " + producto.obtenerNombre());
                }

                Double subtotal = producto.obtenerPrecio() * cantidad;

                nuevoPedido.addDetallePedido(cantidad, subtotal, producto);

                producto.establecerStock(producto.obtenerStock() - cantidad);
            }

            nuevoPedido.calcularTotal();

        } catch (Exception e) {

            throw new ReglaNegocioExcepcion("Creación de pedido cancelada. Motivo: " + e.getMessage());
        }

        nuevoPedido.establecerId(generadorId++);
        pedidos.add(nuevoPedido);

        return nuevoPedido;
    }

    public Pedido buscarPorId(Long id) {
        for (Pedido ped : pedidos) {
            if (ped.obtenerId().equals(id) && !ped.estaEliminado()) {
                return ped;
            }
        }
        throw new EntidadNoEncontradaExcepcion("No se encontró un pedido activo con ID: " + id);
    }

    public void actualizarPedido(Long idPedido, Estado nuevoEstado, FormaPago nuevaFormaPago) {
        Pedido ped = buscarPorId(idPedido);

        if (nuevoEstado != null) {
            ped.establecerEstado(nuevoEstado);
        }
        if (nuevaFormaPago != null) {
            ped.establecerFormaPago(nuevaFormaPago);
        }
    }

    public void eliminarPedido(Long idPedido) {
        Pedido ped = buscarPorId(idPedido);

        ped.marcarComoEliminado(true);

        for (DetallePedido detalle : ped.obtenerDetalles()) {
            detalle.marcarComoEliminado(true);
        }
    }
}
