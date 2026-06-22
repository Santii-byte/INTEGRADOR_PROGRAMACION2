package enumeraciones;

public enum FormaPago {
    TARJETA(1, "Tarjeta"),
    TRANSFERENCIA(2, "Transferencia"),
    EFECTIVO(3, "Efectivo");

    private final int opcion;
    private final String descripcion;

    private FormaPago(int opcion, String descripcion) {
        this.opcion = opcion;
        this.descripcion = descripcion;
    }

    public int obtenerOpcion() {
        return opcion;
    }

    public String obtenerDescripcion() {
        return descripcion;
    }

    public static FormaPago obtenerPorOpcion(int opcionIngresada) {
        for (FormaPago fp : FormaPago.values()) {
            if (fp.obtenerOpcion() == opcionIngresada) {
                return fp;
            }
        }
        throw new IllegalArgumentException("Opción de forma de pago inválida.");
    }
}