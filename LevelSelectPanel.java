import java.awt.*;
import javax.swing.*;

class LevelSelectPanel extends JPanel {//level select panel class
    MiniGolfGame app;
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public LevelSelectPanel(MiniGolfGame app) {
        this.app = app;
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Select Level", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2,4,10,10));
        grid.setOpaque(false);
        for (int i = 1; i <= 8; i++) {
            int lvl = i;
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(140, 80));
            updateButtonLabel(b, lvl, app.getCompletedLevels());
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

    void updateButtonLabel(JButton b, int lvl, boolean[] completed) {
        String text = "Level " + lvl;
        if (completed[lvl - 1]) {
            text += "  ✓";  // check mark for completed levels
            b.setForeground(new Color(0, 150, 0)); // green text
        } else {
            b.setForeground(Color.BLACK);
        }
        b.setText(text);
    }

    void refresh() {//refresh level select panel to show completed levels
        boolean[] completed = app.getCompletedLevels();
        Component[] comps = ((JPanel)((JPanel)getComponent(1)).getComponent(0)).getComponents();
        for (int i = 0; i < 8; i++) {
            JButton b = (JButton) comps[i];
            updateButtonLabel(b, i + 1, completed);
        }
        revalidate();
        repaint();
    }
}