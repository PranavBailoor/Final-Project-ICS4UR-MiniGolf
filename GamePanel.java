import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    MiniGolfGame app;
    javax.swing.Timer timer = new javax.swing.Timer(16, this);

    int level, strokes, par;
    boolean dragging, paused;
    Point dragStart, dragCurrent;

    Ball ball;
    Hole hole;
    List<Obstacle> obstacles = new ArrayList<>();
    List<Spinner> spinners = new ArrayList<>();
    List<Pickup> pickups = new ArrayList<>();
    Path2D courseShape;
    Rectangle2D slowGrass, fastGrass;
    boolean entitiesPlaced = false;

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
        // placeholder ball/hole so the timer can safely run before a level is started
        ball = new Ball(100,100);
        hole = new Hole(700,300);
        timer.start();
    }

    void loadLevel(int lvl) {
        this.level = lvl;
        strokes = 0;
        paused = false;

        obstacles.clear();
        spinners.clear();
        pickups.clear();

        // Defer entity placement until we have a valid component size (done in paintComponent)
        // clear existing entities and mark as not placed
        obstacles.clear();
        spinners.clear();
        pickups.clear();
        slowGrass = new Rectangle2D.Double(0,0,0,0);
        fastGrass = new Rectangle2D.Double(0,0,0,0);
        // placeholder ball/hole until placement
        ball = new Ball(100,100);
        hole = new Hole(700,300);
        entitiesPlaced = false;
        
    }

    // build a normalized, centered course shape based on panel size
    private boolean buildCourseShape() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return false;
        double margin = Math.min(w,h) * 0.06;
        double bx = margin;
        double by = margin;
        double bw = w - margin*2;
        double bh = h - margin*2;

        // normalized polygon points (rough shape similar to screenshot)
        double[][] pts = new double[][] {
            {0.05,0.10},{0.35,0.10},{0.40,0.30},{0.55,0.30},{0.65,0.10},{0.95,0.10},
            {0.95,0.70},{0.62,0.70},{0.62,0.55},{0.47,0.55},{0.47,0.88},{0.28,0.88},{0.28,0.55},{0.05,0.55}
        };

        Path2D p = new Path2D.Double();
        p.moveTo(bx + pts[0][0]*bw, by + pts[0][1]*bh);
        for (int i=1;i<pts.length;i++) p.lineTo(bx + pts[i][0]*bw, by + pts[i][1]*bh);
        p.closePath();
        courseShape = p;
        return true;
    }

    private void placeLevelEntitiesIfNeeded() {
        if (entitiesPlaced) return;
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

        // default grass patches relative to course
        slowGrass = new Rectangle2D.Double(minX + boxW*0.05, minY + boxH*0.05, boxW*0.25, boxH*0.6);
        fastGrass = new Rectangle2D.Double(minX + boxW*0.55, minY + boxH*0.05, boxW*0.35, boxH*0.45);

        obstacles.clear();
        spinners.clear();
        pickups.clear();

        switch (lvl) {
            case 1 -> {
                ball = new Ball((int)(minX + boxW*0.15), (int)(minY + boxH*0.35));
                hole = new Hole((int)(minX + boxW*0.83), (int)(minY + boxH*0.18));
            }
            case 2 -> {
                ball = new Ball((int)(minX + boxW*0.12), (int)(minY + boxH*0.78));
                hole = new Hole((int)(minX + boxW*0.8), (int)(minY + boxH*0.2));
                obstacles.add(new Obstacle(new RoundRectangle2D.Double(minX + boxW*0.28, minY + boxH*0.35, boxW*0.22, boxH*0.08, 20,20), Color.WHITE));
                spinners.add(new Spinner(minX + boxW*0.42, minY + boxH*0.25, Math.min(boxW,boxH)*0.06,3,0.05));
                pickups.add(new Pickup((int)(minX + boxW*0.82),(int)(minY + boxH*0.18)));
            }
            default -> {
                ball = new Ball((int)(minX + boxW*0.12), (int)(minY + boxH*0.45));
                hole = new Hole((int)(minX + boxW*0.78), (int)(minY + boxH*0.22 + (lvl%3-1)*10));
                obstacles.add(new Obstacle(new Rectangle2D.Double(minX + boxW*0.38, minY + boxH*0.12, boxW*(0.08 + lvl*0.01), boxH*0.4), Color.WHITE));
                spinners.add(new Spinner(minX + boxW*0.54, minY + boxH*0.37, Math.min(boxW,boxH)*0.06,3,0.04));
                pickups.add(new Pickup((int)(minX + boxW*0.62),(int)(minY + boxH*0.72)));
            }
        }

        // clamp entities inside bounds
        double pad = Math.min(bounds.getWidth(), bounds.getHeight()) * 0.02;
        for (int i=0;i<obstacles.size();i++) {
            Obstacle o = obstacles.get(i);
            Rectangle2D b = o.shape.getBounds2D();
            double maxW = Math.max(4, bounds.getWidth() - pad*2);
            double maxH = Math.max(4, bounds.getHeight() - pad*2);
            double newW = Math.min(b.getWidth(), maxW);
            double newH = Math.min(b.getHeight(), maxH);
            double newX = Math.max(bounds.getMinX()+pad, Math.min(b.getX(), bounds.getMaxX() - newW - pad));
            double newY = Math.max(bounds.getMinY()+pad, Math.min(b.getY(), bounds.getMaxY() - newH - pad));
            o.shape = new Rectangle2D.Double(newX, newY, newW, newH);
        }
        for (Spinner s : spinners) {
            double r = s.radius;
            double minCX = bounds.getMinX() + pad + r;
            double maxCX = bounds.getMaxX() - pad - r;
            double minCY = bounds.getMinY() + pad + r;
            double maxCY = bounds.getMaxY() - pad - r;
            if (s.cx < minCX) s.cx = minCX;
            if (s.cx > maxCX) s.cx = maxCX;
            if (s.cy < minCY) s.cy = minCY;
            if (s.cy > maxCY) s.cy = maxCY;
            double maxR = Math.min((bounds.getWidth()-pad*2)/4, (bounds.getHeight()-pad*2)/4);
            if (s.radius > maxR) s.radius = maxR;
            s.shape = new Ellipse2D.Double(s.cx - s.radius, s.cy - s.radius, s.radius*2, s.radius*2);
        }
        for (Pickup pk : pickups) {
            if (pk.x < bounds.getMinX() + pad) pk.x = (int)(bounds.getMinX() + pad);
            if (pk.x > bounds.getMaxX() - pad) pk.x = (int)(bounds.getMaxX() - pad);
            if (pk.y < bounds.getMinY() + pad) pk.y = (int)(bounds.getMinY() + pad);
            if (pk.y > bounds.getMaxY() - pad) pk.y = (int)(bounds.getMaxY() - pad);
        }

        double dist = Point.distance(ball.x, ball.y, hole.x, hole.y);
        par = 2 + (int)(dist / 250) + Math.max(0, obstacles.size()/1);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // ensure course shape matches current panel size and place entities once
        buildCourseShape();
        placeLevelEntitiesIfNeeded();

        // Background (beige)
        g2.setColor(new Color(210,205,190));
        g2.fillRect(0,0,getWidth(),getHeight());

        // Course green (single color) with white border
        Color courseGreen = new Color(30,160,40);
        if (courseShape != null) {
            // limit any green painting to inside the course shape
            Shape oldClip = g2.getClip();
            g2.setClip(courseShape);
            g2.setColor(courseGreen);
            g2.fillRect(0,0,getWidth(),getHeight());

            // grass patches (use same green but clipped)
            g2.fill(slowGrass);
            g2.fill(fastGrass);

            g2.setClip(oldClip);

            // white border
            g2.setStroke(new BasicStroke(10f));
            g2.setColor(Color.WHITE);
            g2.draw(courseShape);
            g2.setStroke(new BasicStroke(1f));
        } else {
            g2.setColor(courseGreen);
            g2.fillRect(0,0,getWidth(),getHeight());
        }

        // Obstacles
        for (Obstacle o : obstacles) o.draw(g2);
        for (Spinner s : spinners) s.draw(g2);

        // Pickups
        for (Pickup pk : pickups) pk.draw(g2);

        hole.draw(g2);
        ball.draw(g2);

        // Aim indicator (shows direction ball will travel — opposite the drag)
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

            // arrowhead
            double ang = Math.atan2(ny, nx);
            int ah = 10;
            Path2D arrow = new Path2D.Double();
            arrow.moveTo(endX, endY);
            arrow.lineTo(endX - ah * Math.cos(ang - Math.PI / 6), endY - ah * Math.sin(ang - Math.PI / 6));
            arrow.lineTo(endX - ah * Math.cos(ang + Math.PI / 6), endY - ah * Math.sin(ang + Math.PI / 6));
            arrow.closePath();
            g2.fill(arrow);

            // power circle around ball proportional to pull distance
            double powerRadius = Math.min(80, dist / 1.2);
            g2.setColor(new Color(255, 150, 150, 120));
            g2.fillOval((int) (ball.x - powerRadius / 2), (int) (ball.y - powerRadius / 2), (int) powerRadius, (int) powerRadius);
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
            // advance spinners
            for (Spinner s : spinners) s.update();
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
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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
                ball.vx = nx * power;
                ball.vy = ny * power;
                strokes++;
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