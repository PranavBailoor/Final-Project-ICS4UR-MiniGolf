import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

class LeaderboardPanel extends JPanel {

    MiniGolfGame app;
    JTextArea area = new JTextArea();
    JTextField nameField = new JTextField();

    int score, par, level;

    File file = new File(System.getProperty("user.home"), ".minigolf_leaderboard.txt");

    public LeaderboardPanel(MiniGolfGame app) {
        this.app = app;
        setLayout(new BorderLayout());

        JLabel head = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        head.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(head, BorderLayout.NORTH);

        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(3,1));
        bottom.add(new JLabel("Enter Name:"));
        bottom.add(nameField);

        JPanel row = new JPanel();
        JButton submit = new JButton("Submit & Menu");
        submit.addActionListener(e -> save());
        JButton back = new JButton("Back");
        back.addActionListener(e -> app.showMenu());
        row.add(submit);
        row.add(back);
        bottom.add(row);

        add(bottom, BorderLayout.SOUTH);
    }

    void load(int score, int par, int level) {
        this.score = score;
        this.par = par;
        this.level = level;
        area.setText("Level "+level+" - Final Score: "+score+" (Par "+par+")\n\nTop scores for level "+level+":\n");
        loadFile();
    }

    void loadFile() {
        if (!file.exists()) return;
        Map<String,Integer> m = new HashMap<>();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split(":");
                if (p.length < 3) continue;
                String name = p[0];
                int lvl = Integer.parseInt(p[1]);
                int scv = Integer.parseInt(p[2]);
                if (lvl == level) m.put(name, scv);
            }
        } catch (Exception ignored) {}
        List<Map.Entry<String,Integer>> list = new ArrayList<>(m.entrySet());
        list.sort((a,b)->Integer.compare(a.getValue(), b.getValue()));
        int rank = 1;
        for (Map.Entry<String,Integer> e : list) {
            area.append(rank++ + ". " + e.getKey() + " - " + e.getValue() + "\n");
        }
    }

    void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) return;
        Map<String,Integer> global = new HashMap<>();
        if (file.exists()) {
            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNextLine()) {
                    String[] p = sc.nextLine().split(":");
                    if (p.length < 3) continue;
                    String key = p[0] + ":" + p[1];
                    global.put(key, Integer.valueOf(p[2]));
                }
            } catch (Exception ignored) {}
        }
        String myKey = name + ":" + level;
        global.put(myKey, Math.min(global.getOrDefault(myKey, Integer.MAX_VALUE), score));
        try (PrintWriter pw = new PrintWriter(file)) {
            for (Map.Entry<String,Integer> e : global.entrySet()) {
                String[] k = e.getKey().split(":");
                pw.println(k[0] + ":" + k[1] + ":" + e.getValue());
            }
        } catch (Exception ignored) {}
        app.showMenu();
    }
}