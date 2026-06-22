import servicios.CategoriaServicio;
import servicios.PedidoServicio;
import servicios.ProductoServicio;
import servicios.UsuarioServicio;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // 1. Inicialización de Servicios
        CategoriaServicio categoriaServicio = new CategoriaServicio();
        ProductoServicio productoServicio = new ProductoServicio(categoriaServicio);
        UsuarioServicio usuarioServicio = new UsuarioServicio();
        PedidoServicio pedidoServicio = new PedidoServicio(usuarioServicio, productoServicio);

        // 2. Configuración del Scanner para leer la consola
        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        // 3. Bucle del Menú Principal
        while (opcion != 0) {
            System.out.println("\n=== SISTEMA DE PEDIDOS (FOOD STORE) ==="); // [cite: 162]
            System.out.println("1. Categorías"); // [cite: 163]
            System.out.println("2. Productos"); // [cite: 164]
            System.out.println("3. Usuarios"); // [cite: 165]
            System.out.println("4. Pedidos"); // [cite: 166]
            System.out.println("0. Salir"); // [cite: 167]
            System.out.print("Seleccione: "); // [cite: 168]

            try {
                // Leemos toda la línea y convertimos a número para evitar bugs clásicos del Scanner
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        // subMenuCategorias(scanner, categoriaServicio);
                        System.out.println("-> Entrando a gestión de Categorías...");
                        break;
                    case 2:
                        // subMenuProductos(scanner, productoServicio);
                        System.out.println("-> Entrando a gestión de Productos...");
                        break;
                    case 3:
                        // subMenuUsuarios(scanner, usuarioServicio);
                        System.out.println("-> Entrando a gestión de Usuarios...");
                        break;
                    case 4:
                        // subMenuPedidos(scanner, pedidoServicio);
                        System.out.println("-> Entrando a gestión de Pedidos...");
                        break;
                    case 0:
                        System.out.println("Cerrando el sistema. ¡Hasta luego!");
                        break;
                    default:
                        System.out.println("Opción incorrecta. Ingrese un número entre 0 y 4.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
            } catch (Exception e) {
                // Atrapa cualquier otro error genérico para que no se caiga la consola
                System.out.println("Ocurrió un error inesperado: " + e.getMessage());
            }
        }

        scanner.close();
    }
}