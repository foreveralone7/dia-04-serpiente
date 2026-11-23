package serpiente.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import serpiente.datos.MejorPuntaje;
import serpiente.modelo.Direccion;
import serpiente.modelo.EstadoJuego;
import serpiente.modelo.Juego;
import serpiente.modelo.Posicion;

/**
 * La parte visual: dibuja el juego y escucha el teclado.
 * Las reglas viven en la clase Juego; este panel solo las muestra.
 */
public class PanelJuego extends JPanel {

    private static final int CELDA = 28;
    private static final int ALTO_BARRA = 56;

    // Paleta de colores
    private static final Color FONDO = new Color(0x12, 0x1A, 0x2B);
    private static final Color CASILLA_A = new Color(0x18, 0x22, 0x36);
    private static final Color CASILLA_B = new Color(0x1B, 0x26, 0x3B);
    private static final Color CABEZA = new Color(0x4A, 0xDE, 0x80);
    private static final Color CUERPO = new Color(0x22, 0xC5, 0x5E);
    private static final Color CUERPO_FINAL = new Color(0x15, 0x80, 0x3D);
    private static final Color COMIDA = new Color(0xF4, 0x3F, 0x5E);
    private static final Color TEXTO = new Color(0xE2, 0xE8, 0xF0);
    private static final Color TEXTO_SUAVE = new Color(0x94, 0xA3, 0xB8);

    private final Juego juego;
    private final MejorPuntaje mejorPuntaje;
    private final Timer reloj;
    private boolean nuevoRecord;

    public PanelJuego(Juego juego, MejorPuntaje mejorPuntaje) {
        this.juego = juego;
        this.mejorPuntaje = mejorPuntaje;

        setPreferredSize(new Dimension(juego.ancho() * CELDA, juego.alto() * CELDA + ALTO_BARRA));
        setBackground(FONDO);
        setFocusable(true);
        addKeyListener(new Teclado());

        // El Timer de Swing llama a "paso" cada cierto tiempo: es el corazón del juego.
        reloj = new Timer(juego.intervaloMs(), e -> paso());
        reloj.start();
    }

    private void paso() {
        EstadoJuego antes = juego.estado();
        juego.avanzar();

        boolean acabaDeTerminar = antes == EstadoJuego.JUGANDO
                && (juego.estado() == EstadoJuego.PERDIDO || juego.estado() == EstadoJuego.GANADO);
        if (acabaDeTerminar) {
            nuevoRecord = mejorPuntaje.registrar(juego.puntaje());
        }

        reloj.setDelay(juego.intervaloMs()); // más rápido en cada nivel
        repaint();
    }

    // ======================= Teclado =======================

    private class Teclado extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> juego.cambiarDireccion(Direccion.ARRIBA);
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> juego.cambiarDireccion(Direccion.ABAJO);
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> juego.cambiarDireccion(Direccion.IZQUIERDA);
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> juego.cambiarDireccion(Direccion.DERECHA);
                case KeyEvent.VK_ENTER -> {
                    nuevoRecord = false;
                    juego.iniciar();
                }
                case KeyEvent.VK_SPACE, KeyEvent.VK_P -> juego.alternarPausa();
                case KeyEvent.VK_ESCAPE -> System.exit(0);
                default -> { }
            }
            repaint();
        }
    }

    // ======================= Dibujo =======================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        dibujarBarra(g2);
        g2.translate(0, ALTO_BARRA); // de aquí en adelante, (0,0) es la esquina del tablero
        dibujarTablero(g2);
        dibujarComida(g2);
        dibujarSerpiente(g2);
        dibujarMensaje(g2);
    }

    private void dibujarBarra(Graphics2D g) {
        // Ícono: un cuadrito verde con un ojo (los emojis no se ven bien en Java en Windows)
        g.setColor(CABEZA);
        g.fillRoundRect(16, 18, 22, 22, 8, 8);
        g.setColor(FONDO);
        g.fillOval(28, 23, 6, 6);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(TEXTO);
        g.drawString("Serpiente", 48, 36);

        String datos = "Puntaje " + juego.puntaje() + "     Nivel " + juego.nivel()
                + "     Récord " + Math.max(mejorPuntaje.mejor(), juego.puntaje());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(TEXTO_SUAVE);
        g.drawString(datos, getWidth() - fm.stringWidth(datos) - 16, 35);
    }

    private void dibujarTablero(Graphics2D g) {
        for (int x = 0; x < juego.ancho(); x++) {
            for (int y = 0; y < juego.alto(); y++) {
                g.setColor((x + y) % 2 == 0 ? CASILLA_A : CASILLA_B); // efecto de tablero de ajedrez
                g.fillRect(x * CELDA, y * CELDA, CELDA, CELDA);
            }
        }
    }

    private void dibujarComida(Graphics2D g) {
        Posicion c = juego.comida();
        if (c == null) {
            return;
        }
        int margen = 5;
        g.setColor(COMIDA);
        g.fillOval(c.x() * CELDA + margen, c.y() * CELDA + margen, CELDA - 2 * margen, CELDA - 2 * margen);
        g.setColor(new Color(255, 255, 255, 120)); // brillo
        g.fillOval(c.x() * CELDA + margen + 4, c.y() * CELDA + margen + 3, 6, 6);
    }

    private void dibujarSerpiente(Graphics2D g) {
        List<Posicion> celdas = juego.serpiente().celdas();
        for (int i = celdas.size() - 1; i >= 0; i--) {
            Posicion p = celdas.get(i);
            // El color se va oscureciendo hacia la cola
            float t = celdas.size() == 1 ? 0 : (float) i / (celdas.size() - 1);
            g.setColor(i == 0 ? CABEZA : mezclar(CUERPO, CUERPO_FINAL, t));
            int margen = i == 0 ? 1 : 3;
            g.fillRoundRect(p.x() * CELDA + margen, p.y() * CELDA + margen,
                    CELDA - 2 * margen, CELDA - 2 * margen, 10, 10);
        }
        dibujarOjos(g, celdas.get(0));
    }

    private void dibujarOjos(Graphics2D g, Posicion cabeza) {
        int cx = cabeza.x() * CELDA + CELDA / 2;
        int cy = cabeza.y() * CELDA + CELDA / 2;
        Direccion d = juego.direccion();
        // Los ojos se ubican hacia donde mira la serpiente
        int adelanteX = d.dx() * 5, adelanteY = d.dy() * 5;
        int ladoX = d.dy() * 6, ladoY = d.dx() * 6;

        g.setColor(Color.WHITE);
        g.fillOval(cx + adelanteX + ladoX - 4, cy + adelanteY + ladoY - 4, 8, 8);
        g.fillOval(cx + adelanteX - ladoX - 4, cy + adelanteY - ladoY - 4, 8, 8);
        g.setColor(FONDO);
        g.fillOval(cx + adelanteX + ladoX - 2 + d.dx(), cy + adelanteY + ladoY - 2 + d.dy(), 4, 4);
        g.fillOval(cx + adelanteX - ladoX - 2 + d.dx(), cy + adelanteY - ladoY - 2 + d.dy(), 4, 4);
    }

    private void dibujarMensaje(Graphics2D g) {
        String titulo;
        String detalle;
        switch (juego.estado()) {
            case LISTO -> {
                titulo = "Presiona Enter para jugar";
                detalle = "Flechas o WASD para moverte  ·  Espacio para pausar";
            }
            case PAUSADO -> {
                titulo = "Pausa";
                detalle = "Presiona Espacio para continuar";
            }
            case PERDIDO -> {
                titulo = nuevoRecord ? "¡Nuevo récord! " + juego.puntaje() : "Fin del juego";
                detalle = "Puntaje: " + juego.puntaje() + "  ·  Enter para jugar otra vez";
            }
            case GANADO -> {
                titulo = "¡Ganaste! Llenaste el tablero";
                detalle = "Puntaje: " + juego.puntaje() + "  ·  Enter para jugar otra vez";
            }
            default -> {
                return; // mientras se juega no hay mensaje
            }
        }

        int anchoTablero = juego.ancho() * CELDA;
        int altoTablero = juego.alto() * CELDA;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, anchoTablero, altoTablero);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        centrar(g, titulo, anchoTablero, altoTablero / 3 - 8, TEXTO);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        centrar(g, detalle, anchoTablero, altoTablero / 3 + 26, TEXTO_SUAVE);

        g.setStroke(new BasicStroke(1));
    }

    private void centrar(Graphics2D g, String texto, int ancho, int y, Color color) {
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (ancho - fm.stringWidth(texto)) / 2, y);
    }

    private static Color mezclar(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
