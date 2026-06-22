package configuracion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/food_store";
    private static final String USUARIO = "root";
    private static final String CONTRASENIA = "";

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENIA);
    }
    // Esto es para probar si tienen la conexion con la base de datos
//   public static void main(String[] args) {
//        try {
//            Connection conexion = obtenerConexion();
//            System.out.println("¡Conexión exitosa a la base de datos!");
//            conexion.close();
//        } catch (SQLException e) {
//            System.out.println("Falló la conexión. Error: " + e.getMessage());
//        }
//    }
}