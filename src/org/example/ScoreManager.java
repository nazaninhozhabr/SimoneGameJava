package org.example;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreManager {
    private static final String DIR = System.getProperty("user.home") + "/.simon";
    private static final String FILE = DIR + "/highscores.json";

    // Helper class to hold name and score together
    public record ScoreEntry(String name, int score) implements Comparable<ScoreEntry> {
        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // Sort descending
        }
        @Override
        public String toString() {
            return name + ": " + score;
        }
    }

    public static List<ScoreEntry> loadAll() {
        List<ScoreEntry> scores = new ArrayList<>();
        try {
            File f = new File(FILE);
            if (!f.exists()) return scores;

            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                if (line.contains("\"player\"")) {
                    String name = line.split(":")[1].replaceAll("[\", ]", "");
                    int val = Integer.parseInt(lines.get(lines.indexOf(line) + 1).replaceAll("\\D+", ""));
                    scores.add(new ScoreEntry(name, val));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(scores);
        return scores;
    }

    public static void save(String name, int score) {
        try {
            List<ScoreEntry> allScores = loadAll();
            allScores.add(new ScoreEntry(name, score));
            Collections.sort(allScores);

            // Keep only top 10
            List<ScoreEntry> top10 = allScores.stream().limit(10).collect(Collectors.toList());

            new File(DIR).mkdirs();
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < top10.size(); i++) {
                json.append("  {\n")
                        .append("    \"player\": \"").append(top10.get(i).name).append("\",\n")
                        .append("    \"highScore\": ").append(top10.get(i).score).append("\n")
                        .append("  }").append(i < top10.size() - 1 ? "," : "").append("\n");
            }
            json.append("]");

            Files.writeString(Path.of(FILE), json.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignored) {}
    }
}