import java.awt.*;

class Obstacle {
    Shape shape; //new shape 
    Color color; //colour parameter
    Obstacle(Shape s, Color c) { shape = s; color = c; }
    void draw(Graphics2D g) { //draws shape with a selected colour
        g.setColor(color);
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.draw(shape);
    }
}