import java.awt.*;
import java.awt.geom.*;

class Ball {
    int x,y;
    double px,py;
    double vx,vy;
    int r = 8;
    java.util.LinkedList<Point> trail = new java.util.LinkedList<>();

    Ball(int x,int y){ this.x=x; this.y=y; this.px = x; this.py = y; }

    void update(GamePanel g) {
        px += vx;
        py += vy;

        // update integer position for drawing/collision
        x = (int)Math.round(px);
        y = (int)Math.round(py);

        // trail
        trail.addLast(new Point(x,y));
        if (trail.size() > 20) trail.removeFirst();

        double friction = 0.988;
        if (g.slowGrass.contains(px,py)) friction = 0.960;
        if (g.fastGrass.contains(px,py)) friction = 0.995;

        vx *= friction;
        vy *= friction;

        if (Math.hypot(vx,vy) < 0.05) {
            vx = vy = 0;
        }

        // bounce off the course boundary (if there is one)
        if (g.courseShape != null && !g.courseShape.contains(px,py)) {
            // find the nearest point on the course boundary and reflect velocity across the boundary normal
            PathIterator it = g.courseShape.getPathIterator(null);
            double[] coords = new double[6];
            double startX = 0, startY = 0;
            double lastX = 0, lastY = 0;
            boolean hasLast = false;
            double bestDist2 = Double.POSITIVE_INFINITY;
            double bestX = px, bestY = py;
            while (!it.isDone()) {
                int seg = it.currentSegment(coords);
                switch (seg) {
                    case PathIterator.SEG_MOVETO -> {
                        startX = coords[0];
                        startY = coords[1];
                        lastX = startX;
                        lastY = startY;
                        hasLast = true;
                    }
                    case PathIterator.SEG_LINETO ->                         {
                            double x1 = lastX, y1 = lastY;
                            double x2 = coords[0], y2 = coords[1];
                            double dxs = x2 - x1, dys = y2 - y1;
                            double len2 = dxs*dxs + dys*dys;
                            double t = 0;
                            if (len2 > 0) {
                                t = ((px - x1)*dxs + (py - y1)*dys) / len2;
                                if (t < 0) t = 0; else if (t > 1) t = 1;
                            }       double cx = x1 + dxs * t;
                            double cy = y1 + dys * t;
                            double d2 = (px-cx)*(px-cx)+(py-cy)*(py-cy);
                            if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; }
                            lastX = x2;
                            lastY = y2;
                        }
                    case PathIterator.SEG_CLOSE ->                         {
                            double x1 = lastX, y1 = lastY;
                            double x2 = startX, y2 = startY;
                            double dxs = x2 - x1, dys = y2 - y1;
                            double len2 = dxs*dxs + dys*dys;
                            double t = 0;
                            if (len2 > 0) {
                                t = ((px - x1)*dxs + (py - y1)*dys) / len2;
                                if (t < 0) t = 0; else if (t > 1) t = 1;
                            }       double cx = x1 + dxs * t;
                            double cy = y1 + dys * t;
                            double d2 = (px-cx)*(px-cx)+(py-cy)*(py-cy);
                            if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; }
                        }
                    default -> {
                    }
                }
                it.next();
            }

            double nx = px - bestX;
            double ny = py - bestY;
            double nlen = Math.hypot(nx, ny);
            if (nlen == 0) {
                // fallback: try stepping back along velocity until inside then invert with damping
                boolean backPlaced = false;
                for (int i=0;i<12;i++) {
                    px -= Math.signum(vx) * 1.0;
                    py -= Math.signum(vy) * 1.0;
                    if (g.courseShape.contains(px, py)) { backPlaced = true; break; }
                }
                if (!backPlaced) {
                    px -= vx; py -= vy;
                }
                x = (int)Math.round(px); y = (int)Math.round(py);
                vx = -vx * 0.6; vy = -vy * 0.6;
            } else {
                nx /= nlen; ny /= nlen; // unit normal pointing outwards (from boundary to outside)
                // reflect velocity across normal: v' = v - 2*(v·n)*n
                double dot = vx*nx + vy*ny;
                vx = vx - 2*dot*nx;
                vy = vy - 2*dot*ny;
                // damping to simulate energy loss
                vx *= 0.63; vy *= 0.63;
                // nudge ball inside by radius + small epsilon (move *inside* the course along -normal)
                double push = r + 0.5;
                px = bestX - nx * push;
                py = bestY - ny * push;
                x = (int)Math.round(px);
                y = (int)Math.round(py);
                // safety: if still outside, push a little further inward
                if (!g.courseShape.contains(px, py)) {
                    px = bestX - nx * (r + 2.0);
                    py = bestY - ny * (r + 2.0);
                    x = (int)Math.round(px);
                    y = (int)Math.round(py);
                }
            }
        }

        // panel bounds bounce as a last resort
        if (x < r) { x = r; px = r; vx = -vx*0.6; }
        if (x > g.getWidth()-r) { x = g.getWidth()-r; px = g.getWidth()-r; vx = -vx*0.6; }
        if (y < r) { y = r; py = r; vy = -vy*0.6; }
        if (y > g.getHeight()-r) { y = g.getHeight()-r; py = g.getHeight()-r; vy = -vy*0.6; }

        // obstacle collisions
        for (Obstacle o : g.obstacles) {
            if (o.shape.contains(px,py)) {
                Rectangle2D b = o.shape.getBounds2D();
                if (px < b.getMinX() || px > b.getMaxX()) vx = -vx * 0.7;
                if (py < b.getMinY() || py > b.getMaxY()) vy = -vy * 0.7;
                if (o.shape.contains(px,py)) {
                    px += Math.signum(vx)*(r+1);
                    py += Math.signum(vy)*(r+1);
                    x = (int)Math.round(px);
                    y = (int)Math.round(py);
                }
            }
        }
    }

    boolean isMoving() {
        return Math.hypot(vx,vy) > 0.05;
    }

    boolean contains(Point p) {
        return p.distance(x,y) <= r + 6;
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