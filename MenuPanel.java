import java.awt.*;
import javax.swing.*;

class MenuPanel extends JPanel {//starting menu panel class
    public MenuPanel(MiniGolfGame app) {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("MINI GOLF", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        JButton play = new JButton("Play");
        play.setPreferredSize(new Dimension(160,50));
        play.addActionListener(e -> app.showLevelSelect());
        JButton leaderboardBtn = new JButton("Leaderboard");
        leaderboardBtn.addActionListener(e -> app.layout.show(app.root, "LEADERBOARD"));
        center.add(play);
        center.add(leaderboardBtn);
        add(center, BorderLayout.CENTER);

        setBackground(new Color(20,120,20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0,0,new Color(30,160,30),0,getHeight(),new Color(10,80,10));
        g2.setPaint(gp);
        g2.fillRect(0,0,getWidth(),getHeight());
    }
}