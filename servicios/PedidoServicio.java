package servicios;

import configuracion.ConexionDB;
import entidades.DetallePedido;
import entidades.Pedido;
import entidades.Producto;
import entidades.Usuario;
import enumeraciones.Estado;
import enumeraciones.FormaPago;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedidoServicio {

    private UsuarioServicio usuarioServicio;
    private ProductoServicio productoServicio;

    public PedidoServicio(UsuarioServicio usuarioServicio, ProductoServicio productoServicio) {
        this.usuarioServicio = usuarioServicio;
        this.productoServicio = productoServicio;
    }

    public List<Pedido> listarPedidosActivos() {
        List<Pedido> activos = new ArrayList<>();
        String sql = "SELECT id, fecha, estado, total, forma_pago, id_usuario, eliminado, fecha_creacion FROM pedidos WHERE eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Pedido ped = armarPedidoDesdeResultSet(resultado);
                activos.add(ped);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los pedidos: " + e.getMessage());
        }

        return activos;
    }

    public Pedido crearPedido(Long idUsuario, FormaPago formaPago, Map<Long, Integer> carrito) {
        Usuario usuarioAsociado = usuarioServicio.buscarPorId(idUsuario);
        Pedido nuevoPedido = new Pedido(Estado.PENDIENTE, formaPago);
        nuevoPedido.establecerUsuario(usuarioAsociado);

        String sqlPedido = "INSERT INTO pedidos (fecha, estado, total, forma_pago, id_usuario, eliminado, fecha_creacion) VALUES (NOW(), ?, ?, ?, ?, false, NOW())";
        String sqlDetalle = "INSERT INTO detalles_pedido (cantidad, subtotal, id_pedido, id_producto, eliminado, fecha_creacion) VALUES (?, ?, ?, ?, false, NOW())";
        String sqlActualizarStock = "UPDATE productos SET stock = stock - ? WHERE id = ?";

        Connection conexion = null;

        try {
            conexion = ConexionDB.obtenerConexion();
            conexion.setAutoCommit(false);

            for (Map.Entry<Long, Integer> item : carrito.entrySet()) {
                Producto prod = productoServicio.buscarPorId(item.getKey());
                int cantidad = item.getValue();

                if (cantidad <= 0) {
                    throw new ReglaNegocioExcepcion("La cantidad debe ser mayor a 0 para el producto: " + prod.obtenerNombre());
                }
                if (prod.obtenerStock() < cantidad) {
                    throw new ReglaNegocioExcepcion("Stock insuficiente para el producto: " + prod.obtenerNombre());
                }

                Double subtotal = prod.obtenerPrecio() * cantidad;
                nuevoPedido.addDetallePedido(cantidad, subtotal, prod);
            }
            nuevoPedido.calcularTotal();

            long idPedidoGenerado = 0;
            try (PreparedStatement sentenciaPedido = conexion.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                sentenciaPedido.setString(1, nuevoPedido.obtenerEstado().name());
                sentenciaPedido.setDouble(2, nuevoPedido.obtenerTotal());
                sentenciaPedido.setString(3, nuevoPedido.obtenerFormaPago().name());
                sentenciaPedido.setLong(4, usuarioAsociado.obtenerId());
                sentenciaPedido.executeUpdate();

                ResultSet llaves = sentenciaPedido.getGeneratedKeys();
                if (llaves.next()) {
                    idPedidoGenerado = llaves.getLong(1);
                    nuevoPedido.establecerId(idPedidoGenerado);
                }
            }

            try (PreparedStatement sentenciaDetalle = conexion.prepareStatement(sqlDetalle);
                 PreparedStatement sentenciaStock = conexion.prepareStatement(sqlActualizarStock)) {

                for (DetallePedido detalle : nuevoPedido.obtenerDetalles()) {
                    sentenciaDetalle.setInt(1, detalle.obtenerCantidad());
                    sentenciaDetalle.setDouble(2, detalle.obtenerSubtotal());
                    sentenciaDetalle.setLong(3, idPedidoGenerado);
                    sentenciaDetalle.setLong(4, detalle.obtenerProducto().obtenerId());
                    sentenciaDetalle.executeUpdate();

                    sentenciaStock.setInt(1, detalle.obtenerCantidad());
                    sentenciaStock.setLong(2, detalle.obtenerProducto().obtenerId());
                    sentenciaStock.executeUpdate();
                }
            }

            conexion.commit();
            return nuevoPedido;

        } catch (Exception e) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Error fatal al intentar hacer rollback: " + ex.getMessage());
                }
            }
            throw new ReglaNegocioExcepcion("Creación de pedido cancelada. Motivo: " + e.getMessage());
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Pedido buscarPorId(Long id) {
        String sql = "SELECT id, fecha, estado, total, forma_pago, id_usuario, eliminado, fecha_creacion FROM pedidos WHERE id = ? AND eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    Pedido ped = armarPedidoDesdeResultSet(resultado);
                    cargarDetallesDePedido(conexion, ped);
                    return ped;
                } else {
                    throw new EntidadNoEncontradaExcepcion("No se encontró el pedido activo con ID: " + id);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el pedido: " + e.getMessage());
        }
    }

    public void actualizarPedido(Long idPedido, Estado nuevoEstado, FormaPago nuevaFormaPago) {
        Pedido pedActual = buscarPorId(idPedido);

        String sqlActualizar = "UPDATE pedidos SET estado = ?, forma_pago = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sqlActualizar)) {

            String estadoFinal = (nuevoEstado != null) ? nuevoEstado.name() : pedActual.obtenerEstado().name();
            String pagoFinal = (nuevaFormaPago != null) ? nuevaFormaPago.name() : pedActual.obtenerFormaPago().name();

            sentencia.setString(1, estadoFinal);
            sentencia.setString(2, pagoFinal);
            sentencia.setLong(3, idPedido);

            sentencia.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el pedido: " + e.getMessage());
        }
    }

    public void eliminarPedido(Long idPedido) {
        buscarPorId(idPedido);

        String sqlEliminarPedido = "UPDATE pedidos SET eliminado = true WHERE id = ?";
        String sqlEliminarDetalles = "UPDATE detalles_pedido SET eliminado = true WHERE id_pedido = ?";

        Connection conexion = null;
        try {
            conexion = ConexionDB.obtenerConexion();
            conexion.setAutoCommit(false);

            try (PreparedStatement sentenciaPed = conexion.prepareStatement(sqlEliminarPedido);
                 PreparedStatement sentenciaDet = conexion.prepareStatement(sqlEliminarDetalles)) {

                sentenciaPed.setLong(1, idPedido);
                sentenciaPed.executeUpdate();

                sentenciaDet.setLong(1, idPedido);
                sentenciaDet.executeUpdate();
            }

            conexion.commit();

        } catch (SQLException e) {
            if (conexion != null) {
                try { conexion.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new RuntimeException("Error al eliminar el pedido: " + e.getMessage());
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    private Pedido armarPedidoDesdeResultSet(ResultSet resultado) throws SQLException {
        Estado estado = Estado.valueOf(resultado.getString("estado"));
        FormaPago formaPago = FormaPago.valueOf(resultado.getString("forma_pago"));

        Pedido ped = new Pedido(estado, formaPago);
        ped.establecerId(resultado.getLong("id"));
        ped.establecerTotal(resultado.getDouble("total"));
        ped.marcarComoEliminado(resultado.getBoolean("eliminado"));

        Timestamp fechaSql = resultado.getTimestamp("fecha");
        if (fechaSql != null) {
            ped.establecerFecha(fechaSql.toLocalDateTime().toLocalDate());
        }

        Long idUsuario = resultado.getLong("id_usuario");
        Usuario usr = usuarioServicio.buscarPorId(idUsuario);
        ped.establecerUsuario(usr);

        return ped;
    }

    private void cargarDetallesDePedido(Connection conexion, Pedido pedido) throws SQLException {
        String sql = "SELECT id, cantidad, subtotal, id_producto, eliminado, fecha_creacion FROM detalles_pedido WHERE id_pedido = ? AND eliminado = false";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, pedido.obtenerId());
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Long idProducto = resultado.getLong("id_producto");
                    Producto prod = productoServicio.buscarPorId(idProducto);

                    int cantidad = resultado.getInt("cantidad");
                    Double subtotal = resultado.getDouble("subtotal");
                    pedido.addDetallePedido(cantidad, subtotal, prod);
                }
            }
        }
    }
}