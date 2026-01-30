package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimonGameJava {
    /* ================= CONFIG ================= */
    private static final int BLINK_DELAY = 350;
    private static final int STEP_DELAY = 750;

    /* ================= STATE ================= */
    public enum GameState { IDLE, SHOWING, USER_TURN, GAME_OVER }
    private GameState state = GameState.IDLE;

    /* ================= DATA ================= */
    private final List<Integer> sequence = new ArrayList<>();
    private final List<Integer> userInput = new ArrayList<>();
    private final Random random = new Random();
    private String playerName = "Player";

    /* ================= UI ================= */
    private JFrame frame;
    private JLabel statusLabel;
    private JLabel subtitleLabel;
    private JButton startButton;
    private Tile[] tiles;

    public SimonGameJava() {
        initUI();
    }

    private void initUI() {
        frame = new JFrame("Simon Game");
        frame.setSize(560, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(25, 25, 25));

        // ===== TOP =====
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(new Color(30, 30, 30));
        top.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        statusLabel = new JLabel("🎵 SIMON GAME 🎵", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitleLabel = new JLabel("Press START to play", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        startButton = new JButton("START");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> startGame());

        top.add(statusLabel);
        top.add(Box.createVerticalStrut(5));
        top.add(subtitleLabel);
        top.add(Box.createVerticalStrut(10));
        top.add(startButton);

        frame.add(top, BorderLayout.NORTH);

        // ===== BOARD =====
        JPanel board = new JPanel(new GridLayout(2, 2, 20, 20));
        board.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        board.setBackground(new Color(25, 25, 25));

        Color[] colors = {
                new Color(220, 60, 60),
                new Color(60, 200, 120),
                new Color(80, 130, 255),
                new Color(240, 200, 70)
        };

        tiles = new Tile[4];
        for (int i = 0; i < 4; i++) {
            tiles[i] = new Tile(i, colors[i]);
            board.add(tiles[i]);
        }

        frame.add(board, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /* ================= GAME FLOW ================= */

    private void startGame() {
        if (state != GameState.IDLE && state != GameState.GAME_OVER) return;

        playerName = JOptionPane.showInputDialog(frame, "Enter your name:");
        if (playerName == null || playerName.isBlank()) playerName = "Player";

        sequence.clear();
        userInput.clear();
        state = GameState.SHOWING;
        startButton.setEnabled(false);

        nextLevel();
    }

    private void nextLevel() {
        userInput.clear();
        sequence.add(random.nextInt(4));
        subtitleLabel.setText("Level " + sequence.size());
        playSequence(0);
    }

    private void playSequence(int index) {
        if (index >= sequence.size()) {
            state = GameState.USER_TURN;
            subtitleLabel.setText(playerName + ", your turn");
            return;
        }

        int tileIndex = sequence.get(index);
        tiles[tileIndex].blink();
        SoundManager.playTone(600 + tileIndex * 180);

        Timer t = new Timer(STEP_DELAY, e -> {
            ((Timer) e.getSource()).stop();
            playSequence(index + 1);
        });
        t.setRepeats(false);
        t.start();
    }

    protected void handleUserInput(int index) {
        if (state != GameState.USER_TURN) return;

        userInput.add(index);
        tiles[index].blink();
        SoundManager.playTone(600 + index * 180);

        int step = userInput.size() - 1;

        if (!userInput.get(step).equals(sequence.get(step))) {
            gameOver();
            return;
        }

        if (userInput.size() == sequence.size()) {
            Timer t = new Timer(900, e -> {
                ((Timer) e.getSource()).stop();
                state = GameState.SHOWING;
                nextLevel();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void gameOver() {
        state = GameState.GAME_OVER;

        int score = sequence.size() - 1;
        ScoreManager.save(playerName, score);
        SoundManager.playFail();

        // Build the Leaderboard string for the popup
        List<ScoreManager.ScoreEntry> topScores = ScoreManager.loadAll();
        StringBuilder sb = new StringBuilder("❌ GAME OVER\n\n");
        sb.append("Your Score: ").append(score).append("\n\n");
        sb.append("--- TOP 10 LEADERBOARD ---\n");

        for (int i = 0; i < topScores.size(); i++) {
            sb.append(String.format("%d. %-12s %d\n",
                    (i + 1),
                    topScores.get(i).name(),
                    topScores.get(i).score()));
        }

        JOptionPane.showMessageDialog(frame, sb.toString());

        subtitleLabel.setText("Press START to try again");
        startButton.setEnabled(true);
    }

    /* ================= TILE ================= */

    private class Tile extends JPanel {
        final int index;
        final Color base;
        Color current;

        Tile(int index, Color color) {
            this.index = index;
            this.base = color;
            this.current = color;

            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (state == GameState.USER_TURN) {
                        handleUserInput(index);
                    }
                }
            });
        }

        void blink() {
            current = Color.WHITE;
            repaint();

            Timer t = new Timer(BLINK_DELAY, e -> {
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

    /* ================= GETTERS FOR TESTING ================= */
    public List<Integer> getSequence() { return sequence; }
    public List<Integer> getUserInput() { return userInput; }
    public GameState getState() { return state; }
    public JButton getStartButton() { return startButton; }
}