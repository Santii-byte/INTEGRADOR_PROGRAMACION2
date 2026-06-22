package enumeraciones;

public enum Estado {
    PENDIENTE(1, "Pendiente"),
    CONFIRMADO(2, "Confirmado"),
    TERMINADO(3, "Terminado"),
    CANCELADO(4, "Cancelado");

    private final int opcion;
    private final String descripcion;

    private Estado(int opcion, String descripcion) {
        this.opcion = opcion;
        this.descripcion = descripcion;
    }

    public int obtenerOpcion() {
        return opcion;
    }

    public String obtenerDescripcion() {
        return descripcion;
    }

    public static Estado obtenerPorOpcion(int opcionIngresada) {
        for (Estado est : Estado.values()) {
            if (est.obtenerOpcion() == opcionIngresada) {
                return est;
            }
        }
        throw new IllegalArgumentException("Opción de estado inválida.");
    }
}