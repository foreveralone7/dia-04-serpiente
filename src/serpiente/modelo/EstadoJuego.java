package serpiente.modelo;

/** En qué momento del juego estamos. */
public enum EstadoJuego {
    LISTO,      // pantalla inicial, esperando que el jugador presione Enter
    JUGANDO,
    PAUSADO,
    PERDIDO,
    GANADO      // la serpiente llenó todo el tablero
}
