package serpiente.modelo;

/**
 * Las cuatro direcciones en las que se puede mover la serpiente.
 * Un enum en Java puede tener atributos y métodos, como una clase.
 */
public enum Direccion {
    ARRIBA(0, -1),
    ABAJO(0, 1),
    IZQUIERDA(-1, 0),
    DERECHA(1, 0);

    private final int dx;
    private final int dy;

    Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    /** La serpiente no puede devolverse sobre sí misma (de DERECHA a IZQUIERDA, por ejemplo). */
    public boolean esOpuestaA(Direccion otra) {
        return this.dx + otra.dx == 0 && this.dy + otra.dy == 0;
    }
}
