package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameTile extends JPanel {
    final int index;
    final Color base;
    Color current;
    private final SimonGameJava game;

    public GameTile(int index, Color color, SimonGameJava game) {
        this.index = index;
        this.base = color;
        this.current = color;
        this.game = game;

        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Communicates back to the main game logic
                game.handleUserInput(index);
            }
        });
    }

    public void blink(int delay) {
        current = Color.WHITE;
        repaint();

        Timer t = new Timer(delay, e -> {
            current = base;
            repaint();
            ((Timer) e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(current.darker());
        g2.fillRoundRect(12, 12, getWidth() - 24, getHeight() - 24, 30, 30);

        g2.setColor(current);
        g2.fillRoundRect(8, 8, getWidth() - 24, getHeight() - 24, 30, 30);
    }
}