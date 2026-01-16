import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public class MiniGolfGame extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MiniGolfGame());
    }

    CardLayout layout = new CardLayout();
    JPanel root = new JPanel(layout);

    MenuPanel menu;
    GamePanel game;
    LevelSelectPanel levelSelect;
    LeaderboardPanel leaderboard;

    public MiniGolfGame() {
        setTitle("Mini Golf Game");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menu = new MenuPanel(this);
        game = new GamePanel(this);
        leaderboard = new LeaderboardPanel(this);
        levelSelect = new LevelSelectPanel(this);
        leaderboard.loadFromFile();
        leaderboard.rebuildDisplay();

        root.add(menu, "MENU");
        root.add(levelSelect, "LEVELSELECT");
        root.add(game, "GAME");
        root.add(leaderboard, "LEADERBOARD");

        add(root);
        setVisible(true);
    }

    boolean[] getCompletedLevels() { 
        return game.levelCompleted;
    }

    void showMenu() { 
        Arrays.fill(game.levelCompleted, false);
        Arrays.fill(game.allScores, 0);
        layout.show(root, "MENU");
    }


    void startLevel(int level) {
        if (level == 1) { 
            Arrays.fill(game.levelCompleted, false);
            Arrays.fill(game.allScores, 0); }
        game.loadLevel(level);
        layout.show(root, "GAME");
    }

    void showLeaderboard() {
        leaderboard.rebuildDisplay();
        layout.show(root, "LEADERBOARD");
    }
    void showLevelSelect() {
        layout.show(root, "LEVELSELECT");
        levelSelect.refresh();
    }

}