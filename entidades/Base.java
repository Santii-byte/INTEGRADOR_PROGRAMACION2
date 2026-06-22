package entidades;

import java.time.LocalDateTime;

public abstract class Base {
    private Long id;
    private boolean eliminado;
    private LocalDateTime fechaCreacion;

    public Base() {
        this.eliminado = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long obtenerId() {
        return id;
    }

    public void establecerId(Long id) {
        this.id = id;
    }

    public boolean estaEliminado() {
        return eliminado;
    }

    public void marcarComoEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public LocalDateTime obtenerFechaCreacion() {
        return fechaCreacion;
    }

    public void establecerFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

}
