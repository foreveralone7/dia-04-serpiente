package serpiente.modelo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * El cuerpo de la serpiente: una lista de posiciones donde la primera es la cabeza.
 *
 * Se usa un Deque (cola de doble extremo) porque al moverse solo hay que
 * agregar una celda adelante y quitar una atrás, y eso es muy rápido.
 */
public class Serpiente {

    private final Deque<Posicion> cuerpo = new ArrayDeque<>();

    /** Crea una serpiente horizontal de cierto largo, mirando a la derecha. */
    public Serpiente(Posicion cabeza, int largo) {
        for (int i = 0; i < largo; i++) {
            cuerpo.addLast(new Posicion(cabeza.x() - i, cabeza.y()));
        }
    }

    public Posicion cabeza() {
        return cuerpo.peekFirst();
    }

    public Posicion cola() {
        return cuerpo.peekLast();
    }

    public int largo() {
        return cuerpo.size();
    }

    public boolean ocupa(Posicion posicion) {
        return cuerpo.contains(posicion);
    }

    /** Copia de solo lectura para que la vista pueda dibujarla sin modificarla. */
    public List<Posicion> celdas() {
        return List.copyOf(cuerpo);
    }

    /**
     * Mueve la serpiente a una nueva cabeza.
     * Si crece (comió), no se quita la cola y por eso queda una celda más larga.
     */
    public void mover(Posicion nuevaCabeza, boolean crecer) {
        cuerpo.addFirst(nuevaCabeza);
        if (!crecer) {
            cuerpo.removeLast();
        }
    }
}
