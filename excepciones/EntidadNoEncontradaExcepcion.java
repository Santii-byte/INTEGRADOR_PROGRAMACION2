package excepciones;

public class EntidadNoEncontradaExcepcion extends RuntimeException {
    public EntidadNoEncontradaExcepcion(String mensaje) {
        super(mensaje);
    }
}