package enumeraciones;

public enum Rol {
    ADMIN(1, "Administrador"),
    USUARIO(2, "Usuario General");

    private final int opcion;
    private final String descripcion;

    private Rol(int opcion, String descripcion) {
        this.opcion = opcion;
        this.descripcion = descripcion;
    }

    public int obtenerOpcion() {
        return opcion;
    }

    public String obtenerDescripcion() {
        return descripcion;
    }

    public static Rol obtenerPorOpcion(int opcionIngresada) {
        for (Rol r : Rol.values()) {
            if (r.obtenerOpcion() == opcionIngresada) {
                return r;
            }
        }
        throw new IllegalArgumentException("Opción de rol inválida.");
    }
}