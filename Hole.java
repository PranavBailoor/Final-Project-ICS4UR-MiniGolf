import java.awt.*;

class Hole {//hole class
    int x,y;
    int r = 12;

    Hole(int x,int y){ this.x=x; this.y=y; }

    boolean contains(Ball b) {
        return java.awt.geom.Point2D.distance(x,y,b.px,b.py) < r;
    }

    void draw(Graphics2D g) {
        // draw hole and small flag
        g.setColor(Color.BLACK);
        g.fillOval(x-r,y-r,r*2,r*2);
        g.setColor(new Color(120,80,50));
        g.fillRect(x-2,y-r-18,4,18);
        g.setColor(Color.RED);
        int[] xs = {x+2, x+18, x+2};
        int[] ys = {y-r-18, y-r-12, y-r-6};
        g.fillPolygon(xs, ys, 3);
    }
}