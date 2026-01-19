import java.awt.*;
import java.awt.geom.*;
//spinning obstacle found in higher levels
class Spinner extends Obstacle {
    double cx, cy;
    double radius;
    int arms;
    double angle = 0;
    double speed;
    Spinner(double cx, double cy, double radius, int arms, double speed) { //spinner parameters defined
        super(new Ellipse2D.Double(cx-radius, cy-radius, radius*2, radius*2), Color.WHITE);
        this.cx = cx; this.cy = cy; this.radius = radius; this.arms = arms; this.speed = speed;
    }
    void update() { angle += speed; }
    @Override
    void draw(Graphics2D g) { //draws the shape of the spinner using a compound of other shapes
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(angle);
        g.setColor(Color.WHITE); //sets colour to white
        for (int i=0;i<arms;i++) {
            g.fillRoundRect(-6, (int)(radius*0.2), 12, (int)(radius*0.7), 6, 6);
            g.rotate(2*Math.PI/arms);
        }
        g.setTransform(old);
        // center hub
        g.setColor(new Color(200,200,200));
        g.fillOval((int)(cx-6),(int)(cy-6),12,12);
        g.setColor(Color.BLACK); //sets colour to black
        g.drawOval((int)(cx-6),(int)(cy-6),12,12); //shapes to make up final obstacle
    }
    Shape getCollisionShape() { //the location where a collision will be detected
        Path2D path = new Path2D.Double(); 
        AffineTransform at = new AffineTransform(); 
        at.translate(cx, cy); //paths where a collision is valid, moves with the spinner's movement
        at.rotate(angle); 
        for (int i = 0; i < arms; i++) { 
            Shape arm = new Rectangle2D.Double(-6, radius*0.2, 12, radius*0.7);
            path.append(at.createTransformedShape(arm), false); 
            at.rotate(2 * Math.PI / arms);  //paths where a collision is valid, moves with the spinner's movement
        } 
        Shape hub = new Ellipse2D.Double(cx - 6, cy - 6, 12, 12); //obstacle trajectory for collision detection
        path.append(hub, false); 
        return path;
    }
}