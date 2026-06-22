package servicios;

import entidades.Categoria;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.util.ArrayList;
import java.util.List;

public class CategoriaServicio {

    private List<Categoria> categorias;
    private Long generadorId;

    public CategoriaServicio() {
        this.categorias = new ArrayList<>();
        this.generadorId = 1L; // Simulamos el comportamiento autoincremental de una base de datos
    }

    public List<Categoria> listarCategoriasActivas() {
        List<Categoria> activas = new ArrayList<>();
        for (Categoria cat : categorias) {
            if (!cat.estaEliminado()) {
                activas.add(cat);
            }
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

        for (Categoria cat : categorias) {
            if (cat.obtenerNombre().equalsIgnoreCase(nombre.trim()) && !cat.estaEliminado()) {
                throw new ReglaNegocioExcepcion("Ya existe una categoría activa con el nombre: " + nombre);
            }
        }

        Categoria nuevaCategoria = new Categoria(nombre.trim(), descripcion.trim());
        nuevaCategoria.establecerId(generadorId++);

        categorias.add(nuevaCategoria);

        return nuevaCategoria;
    }

    public Categoria buscarPorId(Long id) {
        for (Categoria cat : categorias) {
            if (cat.obtenerId().equals(id) && !cat.estaEliminado()) {
                return cat;
            }
        }
        throw new EntidadNoEncontradaExcepcion("No se encontró la categoría con ID: " + id + " o se encuentra eliminada.");
    }

    public void editarCategoria(Long id, String nuevoNombre, String nuevaDescripcion) {
        Categoria cat = buscarPorId(id);

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            if (!cat.obtenerNombre().equalsIgnoreCase(nuevoNombre.trim())) {
                for (Categoria c : categorias) {
                    if (c.obtenerNombre().equalsIgnoreCase(nuevoNombre.trim()) && !c.estaEliminado()) {
                        throw new ReglaNegocioExcepcion("Ya existe otra categoría con el nombre: " + nuevoNombre);
                    }
                }
            }
            cat.establecerNombre(nuevoNombre.trim());
        }

        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty()) {
            cat.establecerDescripcion(nuevaDescripcion.trim());
        }
    }

    public void eliminarCategoria(Long id) {
        Categoria cat = buscarPorId(id);
        cat.marcarComoEliminado(true);
    }
}