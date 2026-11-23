package serpiente.modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Las reglas del juego. Esta clase NO sabe nada de ventanas ni de dibujar:
 * solo guarda el estado y lo actualiza. Por eso se puede probar sin abrir una ventana.
 */
public class Juego {

    public static final int PUNTOS_POR_COMIDA = 10;
    public static final int COMIDAS_POR_NIVEL = 5;
    private static final int LARGO_INICIAL = 3;

    private final int ancho;
    private final int alto;
    private final Random azar;

    private Serpiente serpiente;
    private Posicion comida;
    private Direccion direccion;
    /** Teclas presionadas que aún no se han aplicado (máximo 2), para que los giros rápidos no se pierdan. */
    private final Deque<Direccion> direccionesPendientes = new ArrayDeque<>();
    private EstadoJuego estado = EstadoJuego.LISTO;
    private int puntaje;
    private int comidas;

    public Juego(int ancho, int alto, Random azar) {
        this.ancho = ancho;
        this.alto = alto;
        this.azar = azar;
        reiniciar();
    }

    /** Deja todo como al principio y en estado LISTO. */
    public void reiniciar() {
        serpiente = new Serpiente(new Posicion(ancho / 4 + LARGO_INICIAL, alto / 2), LARGO_INICIAL);
        direccion = Direccion.DERECHA;
        direccionesPendientes.clear();
        puntaje = 0;
        comidas = 0;
        comida = nuevaComida();
        estado = EstadoJuego.LISTO;
    }

    public void iniciar() {
        if (estado == EstadoJuego.LISTO) {
            estado = EstadoJuego.JUGANDO;
        } else if (estado == EstadoJuego.PERDIDO || estado == EstadoJuego.GANADO) {
            reiniciar();
            estado = EstadoJuego.JUGANDO;
        }
    }

    public void alternarPausa() {
        if (estado == EstadoJuego.JUGANDO) {
            estado = EstadoJuego.PAUSADO;
        } else if (estado == EstadoJuego.PAUSADO) {
            estado = EstadoJuego.JUGANDO;
        }
    }

    /** Registra una tecla de dirección. Se aplica en el siguiente paso del juego. */
    public void cambiarDireccion(Direccion nueva) {
        if (estado != EstadoJuego.JUGANDO || direccionesPendientes.size() >= 2) {
            return;
        }
        Direccion ultima = direccionesPendientes.isEmpty() ? direccion : direccionesPendientes.peekLast();
        if (nueva != ultima && !nueva.esOpuestaA(ultima)) {
            direccionesPendientes.addLast(nueva);
        }
    }

    /** Un "tick" del juego: la serpiente avanza una celda. */
    public void avanzar() {
        if (estado != EstadoJuego.JUGANDO) {
            return;
        }
        if (!direccionesPendientes.isEmpty()) {
            direccion = direccionesPendientes.pollFirst();
        }

        Posicion nuevaCabeza = serpiente.cabeza().mover(direccion);
        boolean come = nuevaCabeza.equals(comida);

        if (fueraDelTablero(nuevaCabeza) || chocaConsigoMisma(nuevaCabeza, come)) {
            estado = EstadoJuego.PERDIDO;
            return;
        }

        serpiente.mover(nuevaCabeza, come);

        if (come) {
            comidas++;
            puntaje += PUNTOS_POR_COMIDA * nivel();
            comida = nuevaComida();
            if (comida == null) {
                estado = EstadoJuego.GANADO; // ya no queda espacio libre
            }
        }
    }

    private boolean fueraDelTablero(Posicion p) {
        return p.x() < 0 || p.y() < 0 || p.x() >= ancho || p.y() >= alto;
    }

    /** La cola se mueve en el mismo paso, así que chocar con ella solo cuenta si la serpiente está creciendo. */
    private boolean chocaConsigoMisma(Posicion nuevaCabeza, boolean crece) {
        if (nuevaCabeza.equals(serpiente.cola()) && !crece) {
            return false;
        }
        return serpiente.ocupa(nuevaCabeza);
    }

    /** Escoge al azar una celda libre. Devuelve null si el tablero está lleno. */
    private Posicion nuevaComida() {
        List<Posicion> libres = new ArrayList<>();
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                Posicion p = new Posicion(x, y);
                if (!serpiente.ocupa(p)) {
                    libres.add(p);
                }
            }
        }
        return libres.isEmpty() ? null : libres.get(azar.nextInt(libres.size()));
    }

    /** Sube un nivel cada 5 comidas. */
    public int nivel() {
        return comidas / COMIDAS_POR_NIVEL + 1;
    }

    /** Milisegundos entre cada paso: entre más nivel, más rápido. */
    public int intervaloMs() {
        return Math.max(55, 150 - (nivel() - 1) * 15);
    }

    // ----- Getters para la vista y las pruebas -----
    public int ancho() { return ancho; }
    public int alto() { return alto; }
    public Serpiente serpiente() { return serpiente; }
    public Posicion comida() { return comida; }
    public Direccion direccion() { return direccion; }
    public EstadoJuego estado() { return estado; }
    public int puntaje() { return puntaje; }
    public int comidas() { return comidas; }

    /** Solo para pruebas: pone la comida en una posición conocida. */
    void ponerComidaEn(Posicion p) {
        this.comida = p;
    }
}
