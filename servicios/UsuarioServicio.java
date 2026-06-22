package servicios;

import configuracion.ConexionDB;
import entidades.Usuario;
import enumeraciones.Rol;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioServicio {

    public UsuarioServicio() {
    }

    public List<Usuario> listarUsuariosActivos() {
        List<Usuario> activos = new ArrayList<>();
        String sql = "SELECT id, nombre, apellido, mail, celular, contrasenia, rol, eliminado, fecha_creacion FROM usuarios WHERE eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Usuario usr = armarUsuarioDesdeResultSet(resultado);
                activos.add(usr);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los usuarios en la base de datos: " + e.getMessage());
        }

        return activos;
    }

    public Usuario crearUsuario(String nombre, String apellido, String mail, String celular, String contrasenia, Rol rol) {
        if (nombre == null || nombre.trim().isEmpty() || apellido == null || apellido.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("El nombre y el apellido son obligatorios y no pueden estar vacíos.");
        }
        if (mail == null || mail.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("El correo electrónico es obligatorio.");
        }

        String sqlValidacion = "SELECT COUNT(*) FROM usuarios WHERE mail = ? AND eliminado = false";
        String sqlInsercion = "INSERT INTO usuarios (nombre, apellido, mail, celular, contrasenia, rol, eliminado, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, false, NOW())";

        try (Connection conexion = ConexionDB.obtenerConexion()) {

            try (PreparedStatement sentenciaVal = conexion.prepareStatement(sqlValidacion)) {
                sentenciaVal.setString(1, mail.trim());
                ResultSet resultado = sentenciaVal.executeQuery();
                if (resultado.next() && resultado.getInt(1) > 0) {
                    throw new ReglaNegocioExcepcion("El correo electrónico ya se encuentra registrado por otro usuario activo.");
                }
            }

            try (PreparedStatement sentenciaIns = conexion.prepareStatement(sqlInsercion, Statement.RETURN_GENERATED_KEYS)) {
                sentenciaIns.setString(1, nombre.trim());
                sentenciaIns.setString(2, apellido.trim());
                sentenciaIns.setString(3, mail.trim());
                sentenciaIns.setString(4, celular.trim());
                sentenciaIns.setString(5, contrasenia);
                sentenciaIns.setString(6, rol.name());

                sentenciaIns.executeUpdate();

                Usuario nuevoUsuario = new Usuario(nombre.trim(), apellido.trim(), mail.trim(), celular.trim(), contrasenia, rol);
                ResultSet llaves = sentenciaIns.getGeneratedKeys();
                if (llaves.next()) {
                    nuevoUsuario.establecerId(llaves.getLong(1));
                }
                return nuevoUsuario;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el usuario en la base de datos: " + e.getMessage());
        }
    }

    public Usuario buscarPorId(Long id) {
        String sql = "SELECT id, nombre, apellido, mail, celular, contrasenia, rol, eliminado, fecha_creacion FROM usuarios WHERE id = ? AND eliminado = false";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return armarUsuarioDesdeResultSet(resultado);
                } else {
                    throw new EntidadNoEncontradaExcepcion("No se encontró el usuario con ID: " + id + " o se encuentra eliminado.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario en la base de datos: " + e.getMessage());
        }
    }

    public void editarUsuario(Long id, String nuevoNombre, String nuevoApellido, String nuevoMail, String nuevoCelular) {
        Usuario usrActual = buscarPorId(id);

        String sqlValidacionMail = "SELECT COUNT(*) FROM usuarios WHERE mail = ? AND id != ? AND eliminado = false";
        String sqlActualizacion = "UPDATE usuarios SET nombre = ?, apellido = ?, mail = ?, celular = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion()) {

            String nombreFinal = (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) ? nuevoNombre.trim() : usrActual.obtenerNombre();
            String apellidoFinal = (nuevoApellido != null && !nuevoApellido.trim().isEmpty()) ? nuevoApellido.trim() : usrActual.obtenerApellido();
            String celularFinal = (nuevoCelular != null && !nuevoCelular.trim().isEmpty()) ? nuevoCelular.trim() : usrActual.obtenerCelular();
            String mailFinal = usrActual.obtenerMail();
            if (nuevoMail != null && !nuevoMail.trim().isEmpty() && !usrActual.obtenerMail().equalsIgnoreCase(nuevoMail.trim())) {
                try (PreparedStatement sentenciaVal = conexion.prepareStatement(sqlValidacionMail)) {
                    sentenciaVal.setString(1, nuevoMail.trim());
                    sentenciaVal.setLong(2, id);
                    ResultSet resultado = sentenciaVal.executeQuery();
                    if (resultado.next() && resultado.getInt(1) > 0) {
                        throw new ReglaNegocioExcepcion("El nuevo correo electrónico ya está en uso por otro usuario activo.");
                    }
                }
                mailFinal = nuevoMail.trim();
            }

            try (PreparedStatement sentenciaAct = conexion.prepareStatement(sqlActualizacion)) {
                sentenciaAct.setString(1, nombreFinal);
                sentenciaAct.setString(2, apellidoFinal);
                sentenciaAct.setString(3, mailFinal);
                sentenciaAct.setString(4, celularFinal);
                sentenciaAct.setLong(5, id);
                sentenciaAct.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el usuario en la base de datos: " + e.getMessage());
        }
    }

    public void eliminarUsuario(Long id) {
        buscarPorId(id);

        String sqlEliminar = "UPDATE usuarios SET eliminado = true WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sqlEliminar)) {

            sentencia.setLong(1, id);
            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas == 0) {
                throw new ReglaNegocioExcepcion("No se pudo aplicar la baja lógica al usuario.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el usuario en la base de datos: " + e.getMessage());
        }
    }

    private Usuario armarUsuarioDesdeResultSet(ResultSet resultado) throws SQLException {
        String nombre = resultado.getString("nombre");
        String apellido = resultado.getString("apellido");
        String mail = resultado.getString("mail");
        String celular = resultado.getString("celular");
        String contrasenia = resultado.getString("contrasenia");
        Rol rol = Rol.valueOf(resultado.getString("rol"));

        Usuario usr = new Usuario(nombre, apellido, mail, celular, contrasenia, rol);
        usr.establecerId(resultado.getLong("id"));
        usr.marcarComoEliminado(resultado.getBoolean("eliminado"));

        Timestamp fechaSql = resultado.getTimestamp("fecha_creacion");
        if (fechaSql != null) {
            usr.establecerFechaCreacion(fechaSql.toLocalDateTime());
        }

        return usr;
    }
}