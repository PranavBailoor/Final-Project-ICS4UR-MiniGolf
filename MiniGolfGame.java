import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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

/* ===================== MENU ===================== */

class MenuPanel extends JPanel {
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

/* ===================== GAME PANEL ===================== */

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


class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    MiniGolfGame app;
    javax.swing.Timer timer = new javax.swing.Timer(16, this);

    int level, strokes, par;
    boolean dragging, paused;
    Point dragStart, dragCurrent;

    Ball ball;
    Hole hole;
    List<Obstacle> obstacles = new ArrayList<>();
    Rectangle2D slowGrass, fastGrass;

    JButton pauseBtn = new JButton("Pause");

    public GamePanel(MiniGolfGame app) {
        this.app = app;
        setLayout(null);
        pauseBtn.setBounds(10,10,80,30);
        pauseBtn.addActionListener(e -> paused = !paused);
        add(pauseBtn);

        SwingUtilities.invokeLater(() -> {
            addMouseListener(this);
            addMouseMotionListener(this);
        });
        // default empty grass areas so paintComponent is safe before level load
        slowGrass = new Rectangle2D.Double(0,0,0,0);
        fastGrass = new Rectangle2D.Double(0,0,0,0);
        timer.start();
    }

    void loadLevel(int lvl) {
        this.level = lvl;
        strokes = 0;
        paused = false;

        obstacles.clear();

        // Simple seeded layouts per level (visual variety)
        ball = new Ball(100, 300);
        if (lvl == 1) {
            hole = new Hole(750, 300);
            obstacles.add(new Obstacle(new Rectangle2D.Double(350,200,40,200), Color.DARK_GRAY));
            obstacles.add(new Obstacle(new Ellipse2D.Double(450,100,60,60), new Color(100,100,100)));
            slowGrass = new Rectangle2D.Double(200,150,150,300);
            fastGrass = new Rectangle2D.Double(500,150,150,300);
        } else if (lvl == 2) {
            hole = new Hole(700, 120);
            ball = new Ball(120, 480);
            obstacles.add(new Obstacle(new RoundRectangle2D.Double(300,250,200,40,20,20), Color.DARK_GRAY));
            obstacles.add(new Obstacle(new Ellipse2D.Double(420,60,80,80), new Color(80,80,80)));
            slowGrass = new Rectangle2D.Double(100,100,200,400);
            fastGrass = new Rectangle2D.Double(520,100,260,200);
        } else {
            hole = new Hole(700, 300 + (lvl%3 -1)*40);
            ball = new Ball(120, 300 - (lvl%3 -1)*40);
            obstacles.add(new Obstacle(new Rectangle2D.Double(350,150,40+lvl*5,220 - lvl*10), Color.DARK_GRAY));
            obstacles.add(new Obstacle(new Ellipse2D.Double(480,220,80,80), new Color(90,90,90)));
            slowGrass = new Rectangle2D.Double(150,120,180,360);
            fastGrass = new Rectangle2D.Double(460,140,200,320);
        }

        // Par based on distance and obstacle count (more meaningful)
        double dist = Point.distance(ball.x, ball.y, hole.x, hole.y);
        par = 2 + (int)(dist / 250) + Math.max(0, obstacles.size()/1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Grass
        g2.setColor(new Color(50,180,50));
        g2.fillRect(0,0,getWidth(),getHeight());

        g2.setColor(new Color(30,130,30));
        g2.fill(slowGrass);

        g2.setColor(new Color(70,220,70));
        g2.fill(fastGrass);

        // Obstacles
        for (Obstacle o : obstacles) o.draw(g2);

        hole.draw(g2);
        ball.draw(g2);

        // Power line and indicator
        if (dragging && dragCurrent != null) {
            g2.setColor(new Color(255,50,50,180));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(ball.x, ball.y, dragCurrent.x, dragCurrent.y);
            double dx = ball.x - dragCurrent.x;
            double dy = ball.y - dragCurrent.y;
            double dist = Math.min(60, Math.hypot(dx,dy));
            g2.setColor(new Color(255,150,150,120));
            g2.fillOval((int)(ball.x - dist/2), (int)(ball.y - dist/2), (int)dist, (int)dist);
            g2.setStroke(new BasicStroke(1));
        }

        // UI
        g2.setColor(Color.WHITE);
        g2.drawString("Strokes: " + strokes + " | Par: " + par, 120, 30);

        if (paused) {
            g2.drawString("PAUSED", getWidth()/2 - 30, getHeight()/2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused) {
            ball.update(this);
            if (hole.contains(ball)) {
                timer.stop();
                app.showLeaderboard(strokes, par, level);
            }
        }
        repaint();
    }

    /* ========= MOUSE ========= */

    @Override
    public void mousePressed(MouseEvent e) {
        if (ball.isMoving() || paused) return;
        if (ball.contains(e.getPoint())) {
            dragging = true;
            // Use the ball center so dragging direction is always from ball
            dragStart = new Point(ball.x, ball.y);
            dragCurrent = dragStart;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            dragCurrent = e.getPoint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging && dragCurrent != null) {
            dragging = false;
            double dx = ball.x - dragCurrent.x; // aim opposite to drag
            double dy = ball.y - dragCurrent.y;
            double dist = Math.hypot(dx, dy);
            if (dist > 5) {
                double maxPower = 12.0;
                double power = Math.min(maxPower, dist * 0.08);
                double nx = dx / dist;
                double ny = dy / dist;
                ball.vx = nx * power;
                ball.vy = ny * power;
                strokes++;
            }
            dragCurrent = null;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e){}
    @Override
    public void mouseClicked(MouseEvent e){}
    @Override
    public void mouseEntered(MouseEvent e){}
    @Override
    public void mouseExited(MouseEvent e){}

    public javax.swing.Timer getTimer() {
        return timer;
    }

    public void setTimer(javax.swing.Timer timer) {
        this.timer = timer;
    }
}

/* ===================== BALL ===================== */

class Ball {
    int x,y;
    double vx,vy;
    int r = 8;
    java.util.LinkedList<Point> trail = new java.util.LinkedList<>();

    Ball(int x,int y){ this.x=x; this.y=y; }

    void update(GamePanel g) {
        x += vx;
        y += vy;

        // trail
        trail.addLast(new Point(x,y));
        if (trail.size() > 20) trail.removeFirst();

        double friction = 0.988;
        if (g.slowGrass.contains(x,y)) friction = 0.960;
        if (g.fastGrass.contains(x,y)) friction = 0.995;

        vx *= friction;
        vy *= friction;

        if (Math.hypot(vx,vy) < 0.05) {
            vx = vy = 0;
        }

        // wall bounce
        if (x < r) { x = r; vx = -vx*0.6; }
        if (x > g.getWidth()-r) { x = g.getWidth()-r; vx = -vx*0.6; }
        if (y < r) { y = r; vy = -vy*0.6; }
        if (y > g.getHeight()-r) { y = g.getHeight()-r; vy = -vy*0.6; }

        // obstacle collisions
        for (Obstacle o : g.obstacles) {
            if (o.shape.contains(x,y)) {
                Rectangle2D b = o.shape.getBounds2D();
                if (x < b.getMinX() || x > b.getMaxX()) vx = -vx * 0.7;
                if (y < b.getMinY() || y > b.getMaxY()) vy = -vy * 0.7;
                if (o.shape.contains(x,y)) {
                    x += (int)Math.signum(vx)*(r+1);
                    y += (int)Math.signum(vy)*(r+1);
                }
            }
        }
    }

    boolean isMoving() {
        return Math.hypot(vx,vy) > 0.05;
    }

    boolean contains(Point p) {
        return p.distance(x,y) <= r;
    }

    void draw(Graphics2D g) {
        // light trail
        float alpha = 0.9f;
        int i=0;
        for (Point p : trail) {
            float a = Math.max(0.03f, alpha * (i++ / (float)trail.size()));
            g.setColor(new Color(255,255,255, (int)(a*255)));
            g.fillOval(p.x - r/2, p.y - r/2, r, r);
        }
        g.setColor(Color.WHITE);
        g.fillOval(x-r,y-r,r*2,r*2);
        g.setColor(Color.GRAY);
        g.drawOval(x-r,y-r,r*2,r*2);
    }
}

/* ===================== HOLE ===================== */

class Hole {
    int x,y;
    int r = 12;

    Hole(int x,int y){ this.x=x; this.y=y; }

    boolean contains(Ball b) {
        return Point.distance(x,y,b.x,b.y) < r;
    }

    void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillOval(x-r,y-r,r*2,r*2);
    }
}

/* ===================== OBSTACLES & LEADERBOARD ===================== */

class Obstacle {
    Shape shape;
    Color color;
    Obstacle(Shape s, Color c) { shape = s; color = c; }
    void draw(Graphics2D g) {
        g.setColor(color);
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.draw(shape);
    }
}

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
