import java.awt.*;

class Pickup {
    int x,y;
    Pickup(int x,int y){ this.x=x; this.y=y; }
    void draw(Graphics2D g) {
        g.setColor(new Color(255,200,40));
        g.fillOval(x-12,y-12,24,24);
        g.setColor(new Color(230,170,30));
        for (int i=0;i<5;i++) {
            double a = i * Math.PI*2/5;
            int sx = (int)(x + Math.cos(a)*6);
            int sy = (int)(y + Math.sin(a)*6);
            g.fillOval(sx-3, sy-3, 6, 6);
        }
        g.setColor(Color.BLACK);
        g.drawOval(x-12,y-12,24,24);
    }
}