import java.awt.*;
import java.awt.geom.*;

class Spinner extends Obstacle {
    double cx, cy;
    double radius;
    int arms;
    double angle = 0;
    double speed;
    Spinner(double cx, double cy, double radius, int arms, double speed) {
        super(new Ellipse2D.Double(cx-radius, cy-radius, radius*2, radius*2), Color.WHITE);
        this.cx = cx; this.cy = cy; this.radius = radius; this.arms = arms; this.speed = speed;
    }
    void update() { angle += speed; }
    @Override
    void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(angle);
        g.setColor(Color.WHITE);
        for (int i=0;i<arms;i++) {
            g.fillRoundRect(-6, (int)(radius*0.2), 12, (int)(radius*0.7), 6, 6);
            g.rotate(2*Math.PI/arms);
        }
        g.setTransform(old);
        // center hub
        g.setColor(new Color(200,200,200));
        g.fillOval((int)(cx-6),(int)(cy-6),12,12);
        g.setColor(Color.BLACK);
        g.drawOval((int)(cx-6),(int)(cy-6),12,12);
    }
}