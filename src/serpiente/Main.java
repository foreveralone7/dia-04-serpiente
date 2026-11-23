package serpiente;

import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import serpiente.datos.MejorPuntaje;
import serpiente.modelo.Juego;
import serpiente.vista.PanelJuego;

/** Punto de entrada: crea la ventana y arranca el juego. */
public class Main {

    private static final int ANCHO_TABLERO = 24; // en celdas
    private static final int ALTO_TABLERO = 18;

    public static void main(String[] args) {
        // Todo lo visual en Swing debe crearse en su propio hilo (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> {
            Juego juego = new Juego(ANCHO_TABLERO, ALTO_TABLERO, new Random());
            PanelJuego panel = new PanelJuego(juego, new MejorPuntaje());

            JFrame ventana = new JFrame("Serpiente · Día 04");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setResizable(false);
            ventana.add(panel);
            ventana.pack();
            ventana.setLocationRelativeTo(null); // centrada en la pantalla
            ventana.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
