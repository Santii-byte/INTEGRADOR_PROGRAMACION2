package servicios;

import configuracion.ConexionDB;
import entidades.Categoria;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaServicio {

    public CategoriaServicio() {
    }

    public List<Categoria> listarCategoriasActivas() {
        List<Categoria> activas = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, eliminado, fecha_creacion FROM categorias WHERE eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Categoria cat = new Categoria();
                cat.establecerId(resultado.getLong("id"));
                cat.establecerNombre(resultado.getString("nombre"));
                cat.establecerDescripcion(resultado.getString("descripcion"));
                cat.marcarComoEliminado(resultado.getBoolean("eliminado"));

                Timestamp fechaSql = resultado.getTimestamp("fecha_creacion");
                if (fechaSql != null) {
                    cat.establecerFechaCreacion(fechaSql.toLocalDateTime());
                }

                activas.add(cat);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las categorías en la base de datos: " + e.getMessage());
        }

        return activas;
    }

    public Categoria crearCategoria(String nombre, String descripcion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("El nombre de la categoría no puede estar vacío.");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("La descripción no puede estar vacía.");
        }
        String sqlValidacion = "SELECT COUNT(*) FROM categorias WHERE nombre = ? AND eliminado = false";
        String sqlInsercion = "INSERT INTO categorias (nombre, descripcion, eliminado, fecha_creacion) VALUES (?, ?, false, NOW())";

        try (Connection conexion = ConexionDB.obtenerConexion()) {

            try (PreparedStatement sentenciaVal = conexion.prepareStatement(sqlValidacion)) {
                sentenciaVal.setString(1, nombre.trim());
                ResultSet resultado = sentenciaVal.executeQuery();
                if (resultado.next() && resultado.getInt(1) > 0) {
                    throw new ReglaNegocioExcepcion("Ya existe una categoría activa con el nombre: " + nombre);
                }
            }

            try (PreparedStatement sentenciaIns = conexion.prepareStatement(sqlInsercion, Statement.RETURN_GENERATED_KEYS)) {
                sentenciaIns.setString(1, nombre.trim());
                sentenciaIns.setString(2, descripcion.trim());
                sentenciaIns.executeUpdate();

                Categoria nuevaCategoria = new Categoria(nombre.trim(), descripcion.trim());
                ResultSet llaves = sentenciaIns.getGeneratedKeys();
                if (llaves.next()) {
                    nuevaCategoria.establecerId(llaves.getLong(1));
                }
                return nuevaCategoria;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la categoría en la base de datos: " + e.getMessage());
        }
    }

    public Categoria buscarPorId(Long id) {
        String sql = "SELECT id, nombre, descripcion, eliminado, fecha_creacion FROM categorias WHERE id = ? AND eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    Categoria cat = new Categoria();
                    cat.establecerId(resultado.getLong("id"));
                    cat.establecerNombre(resultado.getString("nombre"));
                    cat.establecerDescripcion(resultado.getString("descripcion"));
                    cat.marcarComoEliminado(resultado.getBoolean("eliminado"));

                    Timestamp fechaSql = resultado.getTimestamp("fecha_creacion");
                    if (fechaSql != null) {
                        cat.establecerFechaCreacion(fechaSql.toLocalDateTime());
                    }
                    return cat;
                } else {
                    throw new EntidadNoEncontradaExcepcion("No se encontró la categoría con ID: " + id + " o se encuentra eliminada.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la categoría en la base de datos: " + e.getMessage());
        }
    }

    public void editarCategoria(Long id, String nuevoNombre, String nuevaDescripcion) {
        Categoria catActual = buscarPorId(id);

        String sqlValidacion = "SELECT COUNT(*) FROM categorias WHERE nombre = ? AND id != ? AND eliminado = false";
        String sqlActualizacion = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion()) {

            String nombreFinal = catActual.obtenerNombre();
            String descripcionFinal = catActual.obtenerDescripcion();

            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                if (!nuevoNombre.trim().equalsIgnoreCase(catActual.obtenerNombre())) {
                    try (PreparedStatement sentenciaVal = conexion.prepareStatement(sqlValidacion)) {
                        sentenciaVal.setString(1, nuevoNombre.trim());
                        sentenciaVal.setLong(2, id);
                        ResultSet resultado = sentenciaVal.executeQuery();
                        if (resultado.next() && resultado.getInt(1) > 0) {
                            throw new ReglaNegocioExcepcion("Ya existe otra categoría con el nombre: " + nuevoNombre);
                        }
                    }
                }
                nombreFinal = nuevoNombre.trim();
            }

            if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty()) {
                descripcionFinal = nuevaDescripcion.trim();
            }

            try (PreparedStatement sentenciaAct = conexion.prepareStatement(sqlActualizacion)) {
                sentenciaAct.setString(1, nombreFinal);
                sentenciaAct.setString(2, descripcionFinal);
                sentenciaAct.setLong(3, id);
                sentenciaAct.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la categoría en la base de datos: " + e.getMessage());
        }
    }

    public void eliminarCategoria(Long id) {
        buscarPorId(id);

        String sqlEliminar = "UPDATE categorias SET eliminado = true WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sqlEliminar)) {

            sentencia.setLong(1, id);
            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas == 0) {
                throw new ReglaNegocioExcepcion("No se pudo aplicar la baja lógica a la categoría.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la categoría en la base de datos: " + e.getMessage());
        }
    }
}