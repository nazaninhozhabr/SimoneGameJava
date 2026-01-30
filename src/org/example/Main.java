package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launches the game using your main game class
        SwingUtilities.invokeLater(SimonGameJava::new);
    }
}