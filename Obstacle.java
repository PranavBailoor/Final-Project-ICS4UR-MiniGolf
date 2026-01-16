import java.awt.*;

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