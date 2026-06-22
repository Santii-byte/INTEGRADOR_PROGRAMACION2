package servicios;

import configuracion.ConexionDB;
import entidades.Categoria;
import entidades.Producto;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoServicio {

    private CategoriaServicio categoriaServicio; // Dependencia para validar y armar categorías

    public ProductoServicio(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    public List<Producto> listarProductosActivos() {
        List<Producto> activos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, fecha_creacion FROM productos WHERE eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Producto prod = armarProductoDesdeResultSet(resultado);
                activos.add(prod);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los productos en la base de datos: " + e.getMessage());
        }

        return activos;
    }

    public Producto crearProducto(String nombre, Double precio, String descripcion, int stock, String imagen, Boolean disponible, Long idCategoria) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("El nombre del producto no puede estar vacío.");
        }
        if (precio == null || precio < 0) {
            throw new ReglaNegocioExcepcion("El precio no puede ser menor a 0.");
        }
        if (stock < 0) {
            throw new ReglaNegocioExcepcion("El stock no puede ser negativo.");
        }

        Categoria categoriaAsignada = categoriaServicio.buscarPorId(idCategoria);

        String sqlInsercion = "INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, false, NOW())";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sqlInsercion, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, nombre.trim());
            sentencia.setDouble(2, precio);
            sentencia.setString(3, descripcion.trim());
            sentencia.setInt(4, stock);
            sentencia.setString(5, imagen.trim());
            sentencia.setBoolean(6, disponible);
            sentencia.setLong(7, idCategoria);

            sentencia.executeUpdate();

            Producto nuevoProducto = new Producto(nombre.trim(), precio, descripcion.trim(), stock, imagen.trim(), disponible, categoriaAsignada);
            ResultSet llaves = sentencia.getGeneratedKeys();
            if (llaves.next()) {
                nuevoProducto.establecerId(llaves.getLong(1));
            }
            return nuevoProducto;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el producto en la base de datos: " + e.getMessage());
        }
    }

    public Producto buscarPorId(Long id) {
        String sql = "SELECT id, nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, fecha_creacion FROM productos WHERE id = ? AND eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return armarProductoDesdeResultSet(resultado);
                } else {
                    throw new EntidadNoEncontradaExcepcion("No se encontró el producto con ID: " + id + " o se encuentra eliminado.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el producto en la base de datos: " + e.getMessage());
        }
    }

    public void editarProducto(Long id, String nuevoNombre, Double nuevoPrecio, String nuevaDescripcion, Integer nuevoStock, Long nuevoIdCategoria) {
        Producto prodActual = buscarPorId(id); // Valida que exista [cite: 334]

        String sqlActualizacion = "UPDATE productos SET nombre = ?, precio = ?, descripcion = ?, stock = ?, id_categoria = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sqlActualizacion)) {
            String nombreFinal = (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) ? nuevoNombre.trim() : prodActual.obtenerNombre();

            Double precioFinal = prodActual.obtenerPrecio();
            if (nuevoPrecio != null) {
                if (nuevoPrecio < 0) throw new ReglaNegocioExcepcion("El precio no puede ser menor a 0.");
                precioFinal = nuevoPrecio;
            }

            String descripcionFinal = (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty()) ? nuevaDescripcion.trim() : prodActual.obtenerDescripcion();

            Integer stockFinal = prodActual.obtenerStock();
            if (nuevoStock != null) {
                if (nuevoStock < 0) throw new ReglaNegocioExcepcion("El stock no puede ser negativo.");
                stockFinal = nuevoStock;
            }

            Long idCategoriaFinal = prodActual.obtenerCategoria().obtenerId();
            if (nuevoIdCategoria != null) {
                categoriaServicio.buscarPorId(nuevoIdCategoria);
                idCategoriaFinal = nuevoIdCategoria;
            }

            sentencia.setString(1, nombreFinal);
            sentencia.setDouble(2, precioFinal);
            sentencia.setString(3, descripcionFinal);
            sentencia.setInt(4, stockFinal);
            sentencia.setLong(5, idCategoriaFinal);
            sentencia.setLong(6, id);

            sentencia.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el producto en la base de datos: " + e.getMessage());
        }
    }

    public void eliminarProducto(Long id) {
        buscarPorId(id);

        String sqlEliminar = "UPDATE productos SET eliminado = true WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sqlEliminar)) {

            sentencia.setLong(1, id);
            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas == 0) {
                throw new ReglaNegocioExcepcion("No se pudo aplicar la baja lógica al producto.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el producto en la base de datos: " + e.getMessage());
        }
    }

    private Producto armarProductoDesdeResultSet(ResultSet resultado) throws SQLException {
        Producto prod = new Producto();
        prod.establecerId(resultado.getLong("id"));
        prod.establecerNombre(resultado.getString("nombre"));
        prod.establecerPrecio(resultado.getDouble("precio"));
        prod.establecerDescripcion(resultado.getString("descripcion"));
        prod.establecerStock(resultado.getInt("stock"));
        prod.establecerImagen(resultado.getString("imagen"));
        prod.establecerDisponibilidad(resultado.getBoolean("disponible"));
        prod.marcarComoEliminado(resultado.getBoolean("eliminado"));

        Timestamp fechaSql = resultado.getTimestamp("fecha_creacion");
        if (fechaSql != null) {
            prod.establecerFechaCreacion(fechaSql.toLocalDateTime());
        }

        Long idCategoria = resultado.getLong("id_categoria");
        Categoria cat = categoriaServicio.buscarPorId(idCategoria);
        prod.establecerCategoria(cat);

        return prod;
    }
}