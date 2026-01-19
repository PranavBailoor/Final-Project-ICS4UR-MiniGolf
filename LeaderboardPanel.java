import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

final class LeaderboardPanel extends JPanel {

    MiniGolfGame app;

    int[] bestScores = new int[8];//array for high score and latest score
    int[] latestScores = new int[8];

    File file = new File(System.getProperty("user.home"), "minigolf_scores.txt");

    public LeaderboardPanel(MiniGolfGame app) {
        this.app = app;
        setLayout(new BorderLayout());
        loadFromFile();
    }

    // Called when the player finishes all 8 holes
    void updateScores(int[] runScores) {

        // Update latest scores
        System.arraycopy(runScores, 0, latestScores, 0, 8);

        // Update best scores
        for (int i = 0; i < 8; i++) {
            bestScores[i] = Math.min(bestScores[i], latestScores[i]);
        }

        saveToFile();
        rebuildDisplay();
    }

    // Save exactly two lines: best + latest
    void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

            // Line 1: best scores
            for (int i = 0; i < 8; i++) {
                pw.print(bestScores[i]);
                if (i < 7) pw.print(",");
            }
            pw.println();

            // Line 2: latest scores
            for (int i = 0; i < 8; i++) {
                pw.print(latestScores[i]);
                if (i < 7) pw.print(",");
            }
            pw.println();

        } catch (Exception e) {
        }
    }

    // Load best + latest from file
    void loadFromFile() {
        Arrays.fill(bestScores, Integer.MAX_VALUE);
        Arrays.fill(latestScores, Integer.MAX_VALUE);

        if (!file.exists()) return;

        try (Scanner sc = new Scanner(file)) {
            String[] best = sc.nextLine().split(",");
            String[] latest = sc.nextLine().split(",");

            for (int i = 0; i < 8; i++) {
                bestScores[i] = Integer.parseInt(best[i]);
                latestScores[i] = Integer.parseInt(latest[i]);
            }

        } catch (Exception e) {
        }
    }

    // Build the new leaderboard every time its called
    void rebuildDisplay() {
        removeAll(); 
        setLayout(new BorderLayout()); 
        setBackground(new Color(240, 240, 240)); 
        JLabel head = new JLabel("🏆 SCOREBOARD 🏆", SwingConstants.CENTER); 
        head.setFont(new Font("SansSerif", Font.BOLD, 32)); 
        head.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); 
        add(head, BorderLayout.NORTH); JPanel center = new JPanel(); 
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS)); 
        center.setOpaque(false); 
        center.add(makeCard("Best Scores", bestScores)); 
        center.add(Box.createVerticalStrut(20)); 
        center.add(makeCard("Latest Scores", latestScores)); 
        JScrollPane scroll = new JScrollPane(center); 
        scroll.setBorder(null); 
        scroll.getViewport().setOpaque(false); 
        scroll.setOpaque(false); 
        add(scroll, BorderLayout.CENTER); 
        JButton back = new JButton("Back to Menu"); 
        back.setFont(new Font("SansSerif", Font.BOLD, 18)); 
        back.setFocusPainted(false); 
        back.setBackground(new Color(70, 130, 180)); 
        back.setForeground(Color.WHITE); 
        back.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        back.addActionListener(e -> app.showMenu()); 
        JPanel bottom = new JPanel(); bottom.setOpaque(false); 
        bottom.add(back); 
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); 
        add(bottom, BorderLayout.SOUTH); revalidate(); repaint();
    }

    JPanel makeCard(String title, int[] scores) {//border creation for leaderboard panel
    JPanel card = new JPanel();
    card.setLayout(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));

    JLabel label = new JLabel(title, SwingConstants.CENTER);
    label.setFont(new Font("SansSerif", Font.BOLD, 22));
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    card.add(label, BorderLayout.NORTH);

    // Two-column layout
    JPanel row = new JPanel();
    row.setLayout(new GridLayout(1, 2, 20, 0));
    row.setOpaque(false);

    // Left column (holes)
    JPanel leftCol = new JPanel();
    leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
    leftCol.setOpaque(false);

    // Right column (scores)
    JPanel rightCol = new JPanel();
    rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
    rightCol.setOpaque(false);

    // Headers
    leftCol.add(makeHeader("Hole"));
    rightCol.add(makeHeader("Score"));

    int total = 0;

    for (int i = 0; i < 8; i++) {
        leftCol.add(makeCell("Hole " + (i + 1)));

        String text = (scores[i] == Integer.MAX_VALUE ? "-" : String.valueOf(scores[i]));
        rightCol.add(makeCell(text));

        if (scores[i] != Integer.MAX_VALUE)
            total += scores[i];
    }

    // Total row
    leftCol.add(makeHeader("TOTAL"));
    rightCol.add(makeHeader(String.valueOf(total)));

    row.add(leftCol);
    row.add(rightCol);

    card.add(row, BorderLayout.CENTER);
    return card;
}

        JLabel makeHeader(String text) { //headers
            JLabel l = new JLabel(text, SwingConstants.CENTER); 
            l.setFont(new Font("SansSerif", Font.BOLD, 18)); 
            return l; 
        } 
        JLabel makeCell(String text) { //text
            JLabel l = new JLabel(text, SwingConstants.CENTER); 
            l.setFont(new Font("SansSerif", Font.PLAIN, 16)); 
            return l; 
        }
}