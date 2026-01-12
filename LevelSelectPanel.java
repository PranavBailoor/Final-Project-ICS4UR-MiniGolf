import java.awt.*;
import javax.swing.*;

class LevelSelectPanel extends JPanel {
    public LevelSelectPanel(MiniGolfGame app) {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Select Level", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2,4,10,10));
        grid.setOpaque(false);
        for (int i=1;i<=8;i++) {
            int lvl = i;
            JButton b = new JButton("Level " + i);
            b.setPreferredSize(new Dimension(140,80));
            b.addActionListener(e -> app.startLevel(lvl));
            grid.add(b);
        }

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.add(grid);
        add(center, BorderLayout.CENTER);

        JButton back = new JButton("Back");
        back.addActionListener(e -> app.showMenu());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0,0,new Color(40,180,40),0,getHeight(),new Color(0,80,40));
        g2.setPaint(gp);
        g2.fillRect(0,0,getWidth(),getHeight());
    }
}