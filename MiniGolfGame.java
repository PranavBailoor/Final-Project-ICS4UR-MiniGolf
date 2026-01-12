import java.awt.*;
import javax.swing.*;

public class MiniGolfGame extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MiniGolfGame());
    }

    CardLayout layout = new CardLayout();
    JPanel root = new JPanel(layout);

    MenuPanel menu;
    LevelSelectPanel levelSelect;
    GamePanel game;
    LeaderboardPanel leaderboard;

    public MiniGolfGame() {
        setTitle("Mini Golf Game");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menu = new MenuPanel(this);
        levelSelect = new LevelSelectPanel(this);
        game = new GamePanel(this);
        leaderboard = new LeaderboardPanel(this);

        root.add(menu, "MENU");
        root.add(levelSelect, "LEVELSELECT");
        root.add(game, "GAME");
        root.add(leaderboard, "LEADERBOARD");

        add(root);
        setVisible(true);
    }

    void showMenu() { layout.show(root, "MENU"); }
    void showLevelSelect() { layout.show(root, "LEVELSELECT"); }
    void startLevel(int level) {
        game.loadLevel(level);
        layout.show(root, "GAME");
    }
    void showLeaderboard(int score, int par, int level) {
        leaderboard.load(score, par, level);
        layout.show(root, "LEADERBOARD");
    }
}