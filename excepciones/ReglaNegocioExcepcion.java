package excepciones;

public class ReglaNegocioExcepcion extends RuntimeException {
    public ReglaNegocioExcepcion(String message) {
        super(message);
    }
}
