import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    MiniGolfGame app;
    javax.swing.Timer timer = new javax.swing.Timer(16, this);

    int[] allScores = new int[8];
    int[] bestScores = new int[8];
    int[] manualPar = { 3, 3, 4, 6, 2, 3, 4, 4 };
    boolean[] levelCompleted = new boolean[8];
    boolean playingConfetti = false; 
    int confettiTimer = 0; 
    ArrayList<Confetti> confetti = new ArrayList<>();
    static final int MAX_LEVEL = 8;
    int level, strokes, par;
    boolean dragging, paused;
    Point dragStart, dragCurrent;

    Ball ball;
    Hole hole;
    List<Obstacle> obstacles = new ArrayList<>();
    List<Spinner> spinners = new ArrayList<>();
    Path2D courseShape;
    Rectangle2D slowGrass, fastGrass;
    @SuppressWarnings("unused")
    boolean entitiesPlaced = false;
    @SuppressWarnings("unused")
    public ArrayList<double[][]> levelArrayList = new ArrayList<>();

    JButton pauseBtn = new JButton("Pause");
    JButton resumeBtn = new JButton("Resume");
    JButton levelSelectBtn = new JButton("Level Select");

    public GamePanel(MiniGolfGame app) {
        this.app = app;
        Arrays.fill(levelCompleted, false);
        setLayout(null);
        pauseBtn.setBounds(10,10,80,30);//set pase, resume, lesvel select button positions and initialize
        pauseBtn.addActionListener(e -> { paused = !paused; 
            resumeBtn.setVisible(paused); 
            levelSelectBtn.setVisible(paused);
        });
        add(pauseBtn);

        resumeBtn.setBounds( getWidth()/2 - 60, getHeight()/2 - 20, 120, 40 ); 
        resumeBtn.setVisible(false); 
        resumeBtn.addActionListener(e -> { 
            paused = false; 
            resumeBtn.setVisible(false); 
            levelSelectBtn.setVisible(false);
        }); 
        add(resumeBtn);

        levelSelectBtn.setBounds(getWidth()/2 - 60, getHeight()/2 + 30, 120, 40);
        levelSelectBtn.setVisible(false);
        levelSelectBtn.addActionListener(e -> {
            paused = false;
            resumeBtn.setVisible(false);
            levelSelectBtn.setVisible(false);
            app.showLevelSelect();
        });
        add(levelSelectBtn);

        SwingUtilities.invokeLater(() -> {
            addMouseListener(this);
            addMouseMotionListener(this);
        });
        // default empty grass areas so paintComponent is safe before level load
        slowGrass = new Rectangle2D.Double(0,0,0,0);
        fastGrass = new Rectangle2D.Double(0,0,0,0);
        // placeholder ball/hole so the timer can safely run before a level is started
        ball = new Ball(100,100);
        hole = new Hole(700,300);
        timer.start();
    }

    void loadLevel(int lvl) {
        this.level = lvl;
        strokes = 0; // only reset when starting a NEW level, not after finishing all 8
        paused = false;

        obstacles.clear();
        spinners.clear();

        // Defer entity placement until we have a valid component size
        // clear existing entities and mark as not placed
        obstacles.clear();
        spinners.clear();
        slowGrass = new Rectangle2D.Double(0,0,0,0);
        fastGrass = new Rectangle2D.Double(0,0,0,0);
        // placeholder ball/hole until placement
        ball = new Ball(100,100);
        hole = new Hole(700,300);
        entitiesPlaced = false;
        
    }

    // build course shape 
    private double[][] getCoursePoints(int level) {
    return switch (level) {
        case 1 -> new double[][] {
            // Simple L-shape
            {0.05,0.10},{0.35,0.10},{0.40,0.30},{0.55,0.30},{0.65,0.10},{0.95,0.10},
            {0.95,0.70},{0.62,0.70},{0.62,0.55},{0.47,0.55},{0.47,0.88},{0.28,0.88},{0.28,0.55},{0.05,0.55}
        };
        case 2 -> new double[][] {
            // Figure-8: Two lobes connected
            {0.10,0.25},{0.10,0.05},{0.40,0.05},{0.50,0.15},{0.60,0.05},{0.90,0.05},
            {0.90,0.25},{0.60,0.35},{0.50,0.45},{0.40,0.35},
            {0.40,0.55},{0.50,0.65},{0.60,0.55},{0.90,0.65},{0.90,0.95},{0.10,0.95}
        };
        case 3 -> new double[][] {
            // Winding path with obstacles
            {0.05,0.05},{0.45,0.05},{0.45,0.35},{0.75,0.35},{0.75,0.05},{0.95,0.05},
            {0.95,0.95},{0.55,0.95},{0.55,0.65},{0.25,0.65},{0.25,0.35},{0.05,0.35}
        };
        case 4 -> new double[][] {
            // Four connected chambers with open pathways
            // Start top-left, wind through all chambers to bottom-right
            {0.05,0.05},{0.45,0.05},{0.45,0.40},
            {0.55,0.40},{0.95,0.40},{0.95,0.50},
            {0.95,0.95},{0.50,0.95},{0.50,0.50},
            {0.05,0.50},{0.05,0.40}
        };
        case 5 -> new double[][] {
            // Diamond shape
            {0.50,0.05},{0.95,0.30},{0.95,0.70},{0.50,0.95},
            {0.05,0.70},{0.05,0.30}
        };
        case 6 -> new double[][] {
            // Dual lanes separated by wall - single continuous path
            {0.05,0.05},{0.45,0.05},{0.45,0.40},{0.55,0.40},{0.55,0.05},{0.95,0.05},
            {0.95,0.95},{0.55,0.95},{0.55,0.50},{0.45,0.50},{0.45,0.95},{0.05,0.95}
        };
        case 7 -> new double[][] {
            // S-curve snaking path
            {0.10,0.05},{0.90,0.05},{0.90,0.30},{0.20,0.35},{0.20,0.60},
            {0.80,0.65},{0.80,0.95},{0.10,0.95}
        };
        case 8 -> new double[][] {
            // Simple continuous winding path - all connected
            {0.05,0.05},{0.50,0.05},{0.50,0.25},
            {0.95,0.25},{0.95,0.50},{0.50,0.50},
            {0.50,0.75},{0.95,0.75},{0.95,0.95},
            {0.05,0.95},{0.05,0.50},{0.05,0.05}
        };
        default -> new double[][] {
            {0.05,0.10},{0.35,0.10},{0.40,0.30},{0.55,0.30},{0.65,0.10},{0.95,0.10},
            {0.95,0.70},{0.62,0.70},{0.62,0.55},{0.47,0.55},{0.47,0.88},{0.28,0.88},{0.28,0.55},{0.05,0.55}
        };
    };
}

private boolean buildCourseShape() {//use points to fill in course shape
    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) return false;
    double margin = Math.min(w,h) * 0.06;
    double bx = margin;
    double by = margin;
    double bw = w - margin*2;
    double bh = h - margin*2;

    double[][] pts = getCoursePoints(level);

    Path2D p = new Path2D.Double();
    p.moveTo(bx + pts[0][0]*bw, by + pts[0][1]*bh);
    for (int i=1;i<pts.length;i++) {
        p.lineTo(bx + pts[i][0]*bw, by + pts[i][1]*bh);
    }
    p.closePath();
    courseShape = p;
    return true;
}


private void placeLevelEntitiesIfNeeded() {
    if (entitiesPlaced) return; // Skip if already placed
    if (courseShape == null) return;
    placeLevelEntities(level);
    entitiesPlaced = true;
}

private void placeLevelEntities(int lvl) {
    Rectangle2D bounds = courseShape.getBounds2D();
    double minX = bounds.getMinX();
    double minY = bounds.getMinY();
    double boxW = bounds.getWidth();
    double boxH = bounds.getHeight();

    obstacles.clear();
    spinners.clear();

    switch (lvl) {//place level entities based on level
        case 1 -> {
            ball = new Ball((int)(minX + boxW*0.15), (int)(minY + boxH*0.35));
            hole = new Hole((int)(minX + boxW*0.83), (int)(minY + boxH*0.18));
        }
        case 2 -> {
            // Figure-8: start in left lobe, hole in right lobe
            ball = new Ball((int)(minX + boxW*0.85), (int)(minY + boxH*0.10));
            hole = new Hole((int)(minX + boxW*0.75), (int)(minY + boxH*0.80));
            spinners.add(new Spinner(minX + boxW*0.20, minY + boxH*0.50, Math.min(boxW,boxH)*0.15, 3, 0.05));
        }
        case 3 -> {
            // Winding path
            ball = new Ball((int)(minX + boxW*0.10), (int)(minY + boxH*0.15));
            hole = new Hole((int)(minX + boxW*0.89), (int)(minY + boxH*0.15));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.65, minY + boxH*0.45, boxW*0.30, boxH*0.10), Color.WHITE));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.30, minY + boxH*0.05, boxW*0.10, boxH*0.25), Color.WHITE));
            spinners.add(new Spinner(minX + boxW*0.50, minY + boxH*0.50, Math.min(boxW,boxH)*0.04, 3, 0.04));
        }
        case 4 -> {
            // Islands: start top-left, hole bottom-right
            ball = new Ball((int)(minX + boxW*0.10), (int)(minY + boxH*0.10));
            hole = new Hole((int)(minX + boxW*0.95), (int)(minY + boxH*0.70));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.55, minY + boxH*0.40, boxW*0.05, boxH*0.45), Color.WHITE));
            spinners.add(new Spinner(minX + boxW*0.25, minY + boxH*0.25, Math.min(boxW,boxH)*0.05, 3, 0.04));
            spinners.add(new Spinner(minX + boxW*0.58, minY + boxH*0.90, Math.min(boxW,boxH)*0.04, 3, 0.045));
        }
        case 5 -> {
            // Diamond: left to right
            ball = new Ball((int)(minX + boxW*0.15), (int)(minY + boxH*0.50));
            hole = new Hole((int)(minX + boxW*0.85), (int)(minY + boxH*0.50));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.40, minY + boxH*0.42, boxW*0.20, boxH*0.16), Color.WHITE));
            spinners.add(new Spinner(minX + boxW*0.50, minY + boxH*0.25, Math.min(boxW,boxH)*0.05, 3, 0.05));
            spinners.add(new Spinner(minX + boxW*0.50, minY + boxH*0.75, Math.min(boxW,boxH)*0.05, 3, 0.05));
        }
        case 6 -> {
            // Dual lanes: left to right via either lane
            ball = new Ball((int)(minX + boxW*0.15), (int)(minY + boxH*0.20));
            hole = new Hole((int)(minX + boxW*0.75), (int)(minY + boxH*0.80));
            spinners.add(new Spinner(minX + boxW*0.25, minY + boxH*0.50, Math.min(boxW,boxH)*0.05, 3, 0.045));
            spinners.add(new Spinner(minX + boxW*0.75, minY + boxH*0.50, Math.min(boxW,boxH)*0.05, 3, 0.045));
        }
        case 7 -> {
            // S-curve
            ball = new Ball((int)(minX + boxW*0.90), (int)(minY + boxH*0.15));
            hole = new Hole((int)(minX + boxW*0.80), (int)(minY + boxH*0.90));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.40, minY + boxH*0.08, boxW*0.20, boxH*0.15), Color.WHITE));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.40, minY + boxH*0.72, boxW*0.20, boxH*0.15), Color.WHITE));
            spinners.add(new Spinner(minX + boxW*0.06, minY + boxH*0.50, Math.min(boxW,boxH)*0.05, 3, 0.05));
        }
        case 8 -> {
            // Complex puzzle
            ball = new Ball((int)(minX + boxW*0.9), (int)(minY + boxH*0.35));
            hole = new Hole((int)(minX + boxW*0.9), (int)(minY + boxH*0.9));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.35, minY + boxH*0.28, boxW*0.15, boxH*0.15), Color.WHITE));
            obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.20, minY + boxH*0.60, boxW*0.10, boxH*0.20), Color.WHITE));
            spinners.add(new Spinner(minX + boxW*0.75, minY + boxH*0.35, Math.min(boxW,boxH)*0.04, 3, 0.04));
            spinners.add(new Spinner(minX + boxW*0.75, minY + boxH*0.9, Math.min(boxW,boxH)*0.04, 3, 0.045));
        }
        default -> {
            ball = new Ball((int)(minX + boxW*0.12), (int)(minY + boxH*0.45));
            hole = new Hole((int)(minX + boxW*0.78), (int)(minY + boxH*0.22));
        }
    }

    par = manualPar[lvl - 1];
}

// Update paintComponent to draw stuff
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (paused) {
        resumeBtn.setBounds(getWidth()/2 - 60, getHeight()/2 + 20, 120, 40);
        levelSelectBtn.setBounds(getWidth()/2 - 60, getHeight()/2 + 80 + 30, 120, 40);
    }
    Graphics2D g2 = (Graphics2D) g;

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

    buildCourseShape();
    placeLevelEntitiesIfNeeded(); //place obstacles, ball and hole

    // Background
    g2.setColor(new Color(210,205,190));
    g2.fillRect(0,0,getWidth(),getHeight());

    // Course
    Color courseGreen = new Color(30,160,40);
    if (courseShape != null) {
        Shape oldClip = g2.getClip();
        g2.setClip(courseShape);
        g2.setColor(courseGreen);
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setClip(oldClip);

        g2.setStroke(new BasicStroke(10f));
        g2.setColor(Color.WHITE);
        g2.draw(courseShape);
        g2.setStroke(new BasicStroke(1f));
    } else {
        g2.setColor(courseGreen);
        g2.fillRect(0,0,getWidth(),getHeight());
    }

    // Draw entities
    for (Obstacle o : obstacles) o.draw(g2);
    for (Spinner s : spinners) s.draw(g2);

    hole.draw(g2);
    ball.draw(g2);
    if (playingConfetti) { 
        for (Confetti p : confetti) { p.draw(g2); } 
    }

    // Aim indicator
    if (dragging && dragCurrent != null) {
        double dx = ball.x - dragCurrent.x;
        double dy = ball.y - dragCurrent.y;
        double dist = Math.hypot(dx, dy);
        double maxLen = 120.0;
        double useDist = Math.min(maxLen, dist);
        double nx = dist == 0 ? 0 : dx / dist;
        double ny = dist == 0 ? 0 : dy / dist;

        int endX = (int) (ball.x + nx * useDist);
        int endY = (int) (ball.y + ny * useDist);

        g2.setColor(new Color(255, 50, 50, 200));
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(ball.x, ball.y, endX, endY);

        double ang = Math.atan2(ny, nx);
        int ah = 10;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(endX, endY);
        arrow.lineTo(endX - ah * Math.cos(ang - Math.PI / 6), endY - ah * Math.sin(ang - Math.PI / 6));
        arrow.lineTo(endX - ah * Math.cos(ang + Math.PI / 6), endY - ah * Math.sin(ang + Math.PI / 6));
        arrow.closePath();
        g2.fill(arrow);

        double powerRadius = Math.min(80, dist / 1.2);
        g2.setColor(new Color(255, 150, 150, 120));
        g2.fillOval((int) (ball.x - powerRadius / 2), (int) (ball.y - powerRadius / 2), (int) powerRadius, (int) powerRadius);
    }

    // UI
    g2.setColor(Color.WHITE);
    g2.drawString("Strokes: " + strokes + " | Par: " + par, 120, 30);

    if (paused) {
        g2.setColor(new Color(0, 0, 0, 120)); 
        g2.fillRect(0, 0, getWidth(), getHeight()); 
        g2.setColor(Color.WHITE); 
        g2.setFont(new Font("SansSerif", Font.BOLD, 40)); 
        g2.drawString("PAUSED", getWidth()/2 - 80, getHeight()/2);
    }
}
    @Override
     public void actionPerformed(ActionEvent e) {
        if (!paused) {
            ball.update(this);
            // advance spinners
            for (Spinner s : spinners) s.update();
            if (hole.contains(ball) && !playingConfetti) { //check for ball into hole and update confetti
                int idx = level - 1; 
                levelCompleted[idx] = true;
                bestScores[idx] = Math.min(bestScores[idx], strokes);
                allScores[level - 1] = strokes;
                ball.sunk = true;
                playingConfetti = true; 
                confettiTimer = 60; 
                confetti.clear(); 
                for (int i = 0; i < 120; i++) { 
                    confetti.add(new Confetti(ball.x, ball.y)); 
                } 
            }
            if (playingConfetti) { 
                confetti.removeIf(p -> !p.update()); 
                confettiTimer--; 
                if (confettiTimer <= 0) {
                    playingConfetti = false;
                    
                    // Check if ALL levels are now completed
                    boolean allLevelsCompleted = true;
                    for (int i = 0; i < 8; i++) {
                        if (!levelCompleted[i]) {
                            allLevelsCompleted = false;
                            break;
                        }
                    }
                    
                    if (allLevelsCompleted) {
                        // All 8 levels completed, show leaderboard
                        app.leaderboard.updateScores(allScores);
                        app.showLeaderboard();
                    } else if (level < MAX_LEVEL) {
                        // Not all levels completed yet
                        // Check if next level was already completed
                        if (levelCompleted[level]) {
                            // Next level already completed, go back to level select
                            app.showLevelSelect();
                        } else {
                            // Automatically move to next level
                            loadLevel(level + 1);
                        }
                    } else {
                        // Level 8 completed but not all levels done, go back to level select
                        app.showLevelSelect();
                    }
                }
 
                repaint(); 
                return;  
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
        if (paused) return;
        if (dragging) {
            dragCurrent = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (paused) return;
        if (dragging && dragCurrent != null) {
            dragging = false;
            double dx = ball.x - dragCurrent.x; // aim opposite to drag
            double dy = ball.y - dragCurrent.y;
            double dist = Math.hypot(dx, dy);
            // smaller minimum and stronger scaling so the ball reliably moves
            if (dist > 2) {
                double maxPower = 18.0;
                double power = Math.min(maxPower, dist * 0.12);
                double nx = dx / dist;
                double ny = dy / dist;
                // enforce a minimum visible power so very small pulls still produce motion
                double minPower = 1.2;
                if (power < minPower) power = minPower;
                strokes++;
                ball.vx = nx * power;
                ball.vy = ny * power;
                // ensure the game timer is running so updates happen
                if (!timer.isRunning()) timer.start();
                // apply one immediate update so the ball visibly moves right away
                ball.update(this);
                repaint();
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
