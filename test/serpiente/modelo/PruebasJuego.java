package serpiente.modelo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import serpiente.datos.MejorPuntaje;

/**
 * Pruebas de las reglas del juego, sin librerías externas.
 * Está en el mismo paquete que Juego para poder usar ponerComidaEn().
 */
public class PruebasJuego {

    private static int pasadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        prueba("La serpiente empieza con 3 celdas y en estado LISTO", () -> {
            Juego j = nuevo();
            verificar(j.serpiente().largo() == 3, "largo inicial");
            verificar(j.estado() == EstadoJuego.LISTO, "estado inicial");
        });

        prueba("No se mueve hasta presionar Enter", () -> {
            Juego j = nuevo();
            Posicion antes = j.serpiente().cabeza();
            j.avanzar();
            verificar(j.serpiente().cabeza().equals(antes), "no debería moverse");
        });

        prueba("Avanza una celda a la derecha", () -> {
            Juego j = jugando();
            Posicion antes = j.serpiente().cabeza();
            j.avanzar();
            verificar(j.serpiente().cabeza().equals(new Posicion(antes.x() + 1, antes.y())), "una celda a la derecha");
        });

        prueba("No se puede devolver en dirección opuesta", () -> {
            Juego j = jugando();
            j.cambiarDireccion(Direccion.IZQUIERDA);
            j.avanzar();
            verificar(j.direccion() == Direccion.DERECHA, "sigue a la derecha");
            verificar(j.estado() == EstadoJuego.JUGANDO, "no debería perder");
        });

        prueba("Dos giros rápidos se aplican en orden (arriba y luego izquierda)", () -> {
            Juego j = jugando();
            j.cambiarDireccion(Direccion.ARRIBA);
            j.cambiarDireccion(Direccion.IZQUIERDA);
            j.avanzar();
            verificar(j.direccion() == Direccion.ARRIBA, "primero arriba");
            j.avanzar();
            verificar(j.direccion() == Direccion.IZQUIERDA, "luego izquierda");
        });

        prueba("Al comer crece y suma puntos", () -> {
            Juego j = jugando();
            Posicion cabeza = j.serpiente().cabeza();
            j.ponerComidaEn(new Posicion(cabeza.x() + 1, cabeza.y()));
            j.avanzar();
            verificar(j.serpiente().largo() == 4, "largo 4");
            verificar(j.puntaje() == Juego.PUNTOS_POR_COMIDA, "10 puntos");
            verificar(!j.serpiente().ocupa(j.comida()), "la nueva comida no cae sobre la serpiente");
        });

        prueba("Pierde al chocar con la pared", () -> {
            Juego j = jugando();
            for (int i = 0; i < 30; i++) {
                j.ponerComidaEn(new Posicion(0, 0)); // lejos, para que no crezca
                j.avanzar();
            }
            verificar(j.estado() == EstadoJuego.PERDIDO, "debería perder contra la pared derecha");
        });

        prueba("Pierde al chocar consigo misma", () -> {
            Juego j = jugando();
            // La hacemos crecer a 5 celdas comiendo dos veces hacia la derecha
            for (int i = 0; i < 2; i++) {
                Posicion c = j.serpiente().cabeza();
                j.ponerComidaEn(new Posicion(c.x() + 1, c.y()));
                j.avanzar();
            }
            j.ponerComidaEn(new Posicion(0, 0));
            // Giro cerrado: abajo, izquierda, arriba → choca con su propio cuerpo
            j.cambiarDireccion(Direccion.ABAJO);
            j.avanzar();
            j.cambiarDireccion(Direccion.IZQUIERDA);
            j.avanzar();
            j.cambiarDireccion(Direccion.ARRIBA);
            j.avanzar();
            verificar(j.estado() == EstadoJuego.PERDIDO, "debería perder");
        });

        prueba("Puede moverse a la celda que deja libre su cola", () -> {
            // Serpiente de 4 en un cuadrado de 2x2: perseguir la cola es válido
            Juego j = new Juego(6, 6, new Random(1));
            j.iniciar();
            Posicion c = j.serpiente().cabeza();
            j.ponerComidaEn(new Posicion(c.x() + 1, c.y()));
            j.avanzar(); // largo 4
            j.ponerComidaEn(new Posicion(0, 5));
            j.cambiarDireccion(Direccion.ABAJO);
            j.avanzar();
            j.cambiarDireccion(Direccion.IZQUIERDA);
            j.avanzar();
            j.cambiarDireccion(Direccion.ARRIBA);
            j.avanzar(); // entra a la celda donde estaba la cola
            verificar(j.estado() == EstadoJuego.JUGANDO, "perseguir la cola no es choque");
        });

        prueba("Sube de nivel cada 5 comidas y va más rápido", () -> {
            Juego j = jugando();
            int intervaloInicial = j.intervaloMs();
            for (int i = 0; i < Juego.COMIDAS_POR_NIVEL; i++) {
                Posicion c = j.serpiente().cabeza();
                Direccion d = c.x() < j.ancho() - 2 ? Direccion.DERECHA : Direccion.ABAJO;
                j.cambiarDireccion(d);
                Posicion siguiente = c.mover(j.direccion() == d || !d.esOpuestaA(j.direccion()) ? d : j.direccion());
                j.ponerComidaEn(siguiente);
                j.avanzar();
            }
            verificar(j.nivel() == 2, "nivel 2, fue " + j.nivel());
            verificar(j.intervaloMs() < intervaloInicial, "más rápido");
        });

        prueba("Pausa detiene el movimiento", () -> {
            Juego j = jugando();
            j.alternarPausa();
            Posicion antes = j.serpiente().cabeza();
            j.avanzar();
            verificar(j.serpiente().cabeza().equals(antes), "no se mueve en pausa");
            j.alternarPausa();
            j.avanzar();
            verificar(!j.serpiente().cabeza().equals(antes), "se mueve al quitar la pausa");
        });

        prueba("El récord se guarda y se vuelve a leer", () -> {
            Path archivo = Files.createTempFile("record", ".txt");
            Files.delete(archivo);
            MejorPuntaje r = new MejorPuntaje(archivo);
            verificar(r.mejor() == 0, "empieza en 0");
            verificar(r.registrar(50), "50 es récord");
            verificar(!r.registrar(30), "30 no es récord");
            verificar(new MejorPuntaje(archivo).mejor() == 50, "se lee desde el archivo");
            Files.deleteIfExists(archivo);
        });

        System.out.printf("%n%d pruebas pasaron, %d fallaron%n", pasadas, fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    // ----- Mini framework de pruebas -----

    interface Bloque {
        void ejecutar() throws Exception;
    }

    private static void prueba(String nombre, Bloque bloque) {
        try {
            bloque.ejecutar();
            pasadas++;
            System.out.println("  [OK]    " + nombre);
        } catch (Throwable e) {
            fallidas++;
            System.out.println("  [FALLO] " + nombre + "  →  " + e.getMessage());
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static Juego nuevo() {
        return new Juego(24, 18, new Random(42));
    }

    private static Juego jugando() {
        Juego j = nuevo();
        j.iniciar();
        return j;
    }
}
