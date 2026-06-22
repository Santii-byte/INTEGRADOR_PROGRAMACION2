package servicios;

import entidades.Usuario;
import enumeraciones.Rol;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.util.ArrayList;
import java.util.List;

public class UsuarioServicio {

    private List<Usuario> usuarios;
    private Long generadorId;

    public UsuarioServicio() {
        this.usuarios = new ArrayList<>();
        this.generadorId = 1L;
    }

    public List<Usuario> listarUsuariosActivos() {
        List<Usuario> activos = new ArrayList<>();
        for (Usuario usr : usuarios) {

            if (!usr.estaEliminado()) {
                activos.add(usr);
            }
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

        for (Usuario usr : usuarios) {
            if (usr.obtenerMail().equalsIgnoreCase(mail.trim()) && !usr.estaEliminado()) {
                throw new ReglaNegocioExcepcion("El correo electrónico ya se encuentra registrado por otro usuario activo.");
            }
        }

        Usuario nuevoUsuario = new Usuario(nombre.trim(), apellido.trim(), mail.trim(), celular.trim(), contrasenia, rol);
        nuevoUsuario.establecerId(generadorId++);

        usuarios.add(nuevoUsuario);
        return nuevoUsuario;
    }

    public Usuario buscarPorId(Long id) {
        for (Usuario usr : usuarios) {
            if (usr.obtenerId().equals(id) && !usr.estaEliminado()) {
                return usr;
            }
        }
        // Selección por id: si no existe o está eliminado se informa [cite: 372, 373]
        throw new EntidadNoEncontradaExcepcion("No se encontró el usuario con ID: " + id + " o se encuentra eliminado.");
    }

    public void editarUsuario(Long id, String nuevoNombre, String nuevoApellido, String nuevoMail, String nuevoCelular) {
        Usuario usr = buscarPorId(id); // Selección por id [cite: 372]

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            usr.establecerNombre(nuevoNombre.trim());
        }
        if (nuevoApellido != null && !nuevoApellido.trim().isEmpty()) {
            usr.establecerApellido(nuevoApellido.trim());
        }
        if (nuevoCelular != null && !nuevoCelular.trim().isEmpty()) {
            usr.establecerCelular(nuevoCelular.trim());
        }

        if (nuevoMail != null && !nuevoMail.trim().isEmpty() && !usr.obtenerMail().equalsIgnoreCase(nuevoMail.trim())) {
            for (Usuario u : usuarios) {
                if (u.obtenerMail().equalsIgnoreCase(nuevoMail.trim()) && !u.estaEliminado()) {
                    throw new ReglaNegocioExcepcion("El nuevo correo electrónico ya está en uso por otro usuario activo.");
                }
            }
            usr.establecerMail(nuevoMail.trim());
        }
    }

    public void eliminarUsuario(Long id) {
        Usuario usr = buscarPorId(id);
        usr.marcarComoEliminado(true);
    }
}