package serpiente.modelo;

/**
 * Una celda del tablero (columna x, fila y).
 * Un "record" crea automáticamente el constructor, los getters, equals y hashCode.
 */
public record Posicion(int x, int y) {

    /** Devuelve la celda vecina en la dirección indicada. */
    public Posicion mover(Direccion direccion) {
        return new Posicion(x + direccion.dx(), y + direccion.dy());
    }
}
