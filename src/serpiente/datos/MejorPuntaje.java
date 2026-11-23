package serpiente.datos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guarda el puntaje más alto en un archivo de texto en la carpeta del usuario,
 * para que no se pierda al cerrar el juego.
 */
public class MejorPuntaje {

    private final Path archivo;
    private int mejor;

    public MejorPuntaje() {
        this(Path.of(System.getProperty("user.home"), ".serpiente-record.txt"));
    }

    public MejorPuntaje(Path archivo) {
        this.archivo = archivo;
        this.mejor = leer();
    }

    public int mejor() {
        return mejor;
    }

    /** Si el puntaje supera el récord, lo guarda y devuelve true. */
    public boolean registrar(int puntaje) {
        if (puntaje <= mejor) {
            return false;
        }
        mejor = puntaje;
        try {
            Files.writeString(archivo, String.valueOf(mejor));
        } catch (IOException e) {
            System.err.println("No se pudo guardar el récord: " + e.getMessage());
        }
        return true;
    }

    private int leer() {
        try {
            return Integer.parseInt(Files.readString(archivo).trim());
        } catch (IOException | NumberFormatException e) {
            return 0; // primera vez que se juega, o archivo dañado
        }
    }
}
