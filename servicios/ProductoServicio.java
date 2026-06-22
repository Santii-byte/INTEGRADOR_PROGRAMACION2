package servicios;

import entidades.Categoria;
import entidades.Producto;
import excepciones.EntidadNoEncontradaExcepcion;
import excepciones.ReglaNegocioExcepcion;
import validaciones.Validador;

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

    public Producto crearProducto(String nombre, Double precio, String descripcion, Integer stock, String imagen, Boolean disponible, Long idCategoria) {

        Validador.validarStr(nombre,"nombre");
        Validador.validarInt(precio,"precio");
        Validador.validarInt(stock,"stock");

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

    public void editarProducto(Long id, String nuevoNombre, Double nuevoPrecio, String nuevaDescripcion, int nuevoStock, Long nuevoIdCategoria) {
        Producto prod = buscarPorId(id);

        Validador.validarStr(nuevoNombre, "nombre");
        prod.establecerNombre(nuevoNombre.trim());

        Validador.validarInt(nuevoPrecio, "precio");
        prod.establecerPrecio(nuevoPrecio);

        Validador.validarStr(nuevaDescripcion, "descripcion");
        prod.establecerDescripcion(nuevaDescripcion.trim());

        Validador.validarInt(nuevoStock, "stock");
        prod.establecerStock(nuevoStock);

        Categoria nuevaCategoria = categoriaServicio.buscarPorId(nuevoIdCategoria);
        prod.establecerCategoria(nuevaCategoria);

    }
  public void eliminarProducto(Long id) {
        Producto prod = buscarPorId(id);
        prod.marcarComoEliminado(true);
    }
}
