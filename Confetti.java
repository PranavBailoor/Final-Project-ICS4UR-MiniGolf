
import java.awt.*;

public class Confetti {
    double x, y;
    double vx, vy;
    Color color;
    int life = 60; // ~1 second at 60 FPS

    Confetti(double x, double y) {
        this.x = x;
        this.y = y;

        double angle = Math.random() * Math.PI * 2;
        double speed = 2 + Math.random() * 3;

        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;

        color = new Color(
            (int)(100 + Math.random()*155),
            (int)(100 + Math.random()*155),
            (int)(100 + Math.random()*155)
        );
    }

    boolean update() {
        x += vx;
        y += vy;
        vy += 0.1; // gravity
        return --life > 0;
    }

    void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int)x, (int)y, 4, 4);
    }
}
