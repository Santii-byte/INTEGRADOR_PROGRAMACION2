package servicios;

import entidades.Categoria;
import entidades.Producto;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;

import java.util.ArrayList;
import java.util.List;

public class ProductoServicio {

    private List<Producto> productos;
    private Long generadorId;
    private CategoriaServicio categoriaServicio;

    public ProductoServicio(CategoriaServicio categoriaServicio) {
        this.productos = new ArrayList<>();
        this.generadorId = 1L;
        this.categoriaServicio = categoriaServicio;
    }

    public List<Producto> listarProductosActivos() {
        List<Producto> activos = new ArrayList<>();
        for (Producto prod : productos) {
            if (!prod.estaEliminado()) {
                activos.add(prod);
            }
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

        Producto nuevoProducto = new Producto(nombre.trim(), precio, descripcion.trim(), stock, imagen, disponible, categoriaAsignada);
        nuevoProducto.establecerId(generadorId++);

        productos.add(nuevoProducto);

        return nuevoProducto;
    }

    public Producto buscarPorId(Long id) {
        for (Producto prod : productos) {
            if (prod.obtenerId().equals(id) && !prod.estaEliminado()) {
                return prod;
            }
        }
        throw new EntidadNoEncontradaExcepcion("No se encontró el producto con ID: " + id + " o se encuentra eliminado."); // [cite: 334]
    }

    public void editarProducto(Long id, String nuevoNombre, Double nuevoPrecio, String nuevaDescripcion, Integer nuevoStock, Long nuevoIdCategoria) {
        Producto prod = buscarPorId(id);

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            prod.establecerNombre(nuevoNombre.trim());
        }
        if (nuevoPrecio != null) {
            if (nuevoPrecio < 0) throw new ReglaNegocioExcepcion("El precio no puede ser menor a 0.");
            prod.establecerPrecio(nuevoPrecio);
        }
        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty()) {
            prod.establecerDescripcion(nuevaDescripcion.trim());
        }
        if (nuevoStock != null) {
            if (nuevoStock < 0) throw new ReglaNegocioExcepcion("El stock no puede ser negativo.");
            prod.establecerStock(nuevoStock);
        }
        if (nuevoIdCategoria != null) {
            Categoria nuevaCategoria = categoriaServicio.buscarPorId(nuevoIdCategoria);
            prod.establecerCategoria(nuevaCategoria);
        }
    }

  public void eliminarProducto(Long id) {
        Producto prod = buscarPorId(id);
        prod.marcarComoEliminado(true);
    }
}
