import java.awt.*;
import java.awt.geom.*;

class Ball {
    int x,y;
    double px,py;
    double vx,vy;
    boolean sunk = false;
    int r = 8;
    java.util.LinkedList<Point> trail = new java.util.LinkedList<>();

    Ball(int x,int y){ this.x=x; this.y=y; this.px = x; this.py = y; }

    boolean update(GamePanel g) {
        boolean collidedThisFrame = false;
        

        // update integer position for drawing/collision
        x = (int)Math.round(px);
        y = (int)Math.round(py);

        // trail
        trail.addLast(new Point(x,y));
        if (trail.size() > 20) trail.removeFirst();

        double friction = 0.970;  // increased friction to stop faster
        if (g.slowGrass.contains(px,py)) friction = 0.940;
        if (g.fastGrass.contains(px,py)) friction = 0.985;

        vx *= friction;
        vy *= friction;

        if (Math.hypot(vx,vy) < 0.05) {
            vx = vy = 0;
        }
        double moveDist = Math.hypot(vx, vy); 
        if (moveDist > 0.01) { 
            double stepSize = r * 0.5; 
            int steps = (int)(moveDist / stepSize) + 1; 
            double stepVX = vx / steps; 
            double stepVY = vy / steps; 
            for (int i = 0; i < steps; i++) { 
                px += stepVX; py += stepVY; 
                x = (int)Math.round(px); 
                y = (int)Math.round(py); 
        // bounce off the course boundary (if there is one)
        // Check if ball touches course boundary, calculate next position, push if within boundary otherwise invert velocity to bounce
        if (g.courseShape != null) { boolean inside = g.courseShape.contains(px, py); // Find nearest boundary point 
        PathIterator it = g.courseShape.getPathIterator(null); 
        double[] coords = new double[6]; 
        double startX = 0, startY = 0; 
        double lastX = 0, lastY = 0; 
        double bestDist2 = Double.POSITIVE_INFINITY; 
        double bestX = px, bestY = py; 
        while (!it.isDone()) { 
            int seg = it.currentSegment(coords); 
            switch (seg) { 
                case PathIterator.SEG_MOVETO -> { // move to new start point
                    startX = coords[0]; 
                    startY = coords[1]; 
                    lastX = startX; 
                    lastY = startY; 
                } 
                case PathIterator.SEG_LINETO -> { // find closest point on line segment
                    double x1 = lastX, y1 = lastY; 
                    double x2 = coords[0], y2 = coords[1]; 
                    double dx = x2 - x1, dy = y2 - y1; 
                    double len2 = dx*dx + dy*dy; 
                    double t = 0; 
                    if (len2 > 0) { 
                        t = ((px - x1)*dx + (py - y1)*dy) / len2; 
                        t = Math.max(0, Math.min(1, t)); 
                    } 
                    double cx = x1 + dx * t; 
                    double cy = y1 + dy * t; 
                    double d2 = (px - cx)*(px - cx) + (py - cy)*(py - cy); 
                    if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; } 
                    lastX = x2; lastY = y2; 
                } 
                case PathIterator.SEG_CLOSE -> { // check closing segment
                    double x1 = lastX, y1 = lastY; 
                    double x2 = startX, y2 = startY; 
                    double dx = x2 - x1, dy = y2 - y1; 
                    double len2 = dx*dx + dy*dy; 
                    double t = 0; 
                    if (len2 > 0) { 
                        t = ((px - x1)*dx + (py - y1)*dy) / len2; 
                        t = Math.max(0, Math.min(1, t)); 
                    } 
                    double cx = x1 + dx * t; 
                    double cy = y1 + dy * t; 
                    double d2 = (px - cx)*(px - cx) + (py - cy)*(py - cy); 
                    if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; } 
                } 
            } it.next(); 
        } 
        double dist = Math.sqrt(bestDist2); 
        if (dist < r) { //invert velocity if ball is going outside boundary, check case for multiple boundaries
            double nx = px - bestX; 
            double ny = py - bestY; 
            double nlen = Math.hypot(nx, ny); 
            if (nlen == 0) { nx = (vx == 0 && vy == 0) ? 1 : vx; ny = (vx == 0 && vy == 0) ? 0 : vy; 
                nlen = Math.hypot(nx, ny); 
            } 
            nx /= nlen; 
            ny /= nlen; 
            if (!inside) { nx = -nx; ny = -ny; } 
            double dot = vx*nx + vy*ny; 
            if (dot < 0) { 
                vx = vx - 2 * dot * nx; 
                vy = vy - 2 * dot * ny; 
                collidedThisFrame = true; 
            } 
            double safety = 1.0; 
            px = bestX + nx * (r + safety); 
            py = bestY + ny * (r + safety); 
            x = (int)Math.round(px); 
            y = (int)Math.round(py); 
        }

    }
    for (Obstacle o : g.obstacles) { //exact same thing but for obstacles
        PathIterator it = o.shape.getPathIterator(null); 
        double[] coords = new double[6]; 
        double startX = 0, startY = 0; 
        double lastX = 0, lastY = 0; 
        double bestDist2 = Double.POSITIVE_INFINITY; 
        double bestX = px, bestY = py; 
        java.util.List<Point2D.Double> vertices = new java.util.ArrayList<>(); 
        while (!it.isDone()) { 
            int seg = it.currentSegment(coords); 
            switch (seg) { 
                case PathIterator.SEG_MOVETO -> { 
                    startX = coords[0]; 
                    startY = coords[1]; 
                    lastX = startX; 
                    lastY = startY; 
                    vertices.add(new Point2D.Double(startX, startY)); 
                } 
                case PathIterator.SEG_LINETO -> { 
                    double x1 = lastX, y1 = lastY; 
                    double x2 = coords[0], y2 = coords[1]; 
                    double dx = x2 - x1, dy = y2 - y1; 
                    double len2 = dx*dx + dy*dy; 
                    double t = 0; 
                    if (len2 > 0) { 
                        t = ((px - x1)*dx + (py - y1)*dy) / len2; 
                        t = Math.max(0, Math.min(1, t)); 
                    } 
                    double cx = x1 + dx * t; 
                    double cy = y1 + dy * t; 
                    double d2 = (px - cx)*(px - cx) + (py - cy)*(py - cy); 
                    if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; } 
                    lastX = x2; lastY = y2; 
                    vertices.add(new Point2D.Double(x2, y2)); 
                } 
                case PathIterator.SEG_CLOSE -> { 
                    double x1 = lastX, y1 = lastY; 
                    double x2 = startX, y2 = startY; 
                    double dx = x2 - x1, dy = y2 - y1; 
                    double len2 = dx*dx + dy*dy; 
                    double t = 0; 
                    if (len2 > 0) { 
                        t = ((px - x1)*dx + (py - y1)*dy) / len2; 
                        t = Math.max(0, Math.min(1, t)); 
                    } 
                    double cx = x1 + dx * t; 
                    double cy = y1 + dy * t; 
                    double d2 = (px - cx)*(px - cx) + (py - cy)*(py - cy); 
                    if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; } 
                } 
            } it.next(); 
        } 
        for (Point2D.Double v : vertices) { 
            double d2 = (px - v.x)*(px - v.x) + (py - v.y)*(py - v.y); 
            if (d2 < bestDist2) { 
                bestDist2 = d2; 
                bestX = v.x; 
                bestY = v.y; 
            } 
        } 
        double dist = Math.sqrt(bestDist2); 
        if (dist < r) { 
            double nx = px - bestX; 
            double ny = py - bestY; 
            double nlen = Math.hypot(nx, ny); 
            if (nlen == 0) { 
                nx = (vx == 0 && vy == 0) ? 1 : vx; 
                ny = (vx == 0 && vy == 0) ? 0 : vy; 
                nlen = Math.hypot(nx, ny); 
            } 
            nx /= nlen; 
            ny /= nlen; 
            double dot = vx*nx + vy*ny; 
            vx = vx - 2 * dot * nx; 
            vy = vy - 2 * dot * ny; 
            collidedThisFrame = true; 
            double safety = 1.0; 
            px = bestX + nx * (r + safety); 
            py = bestY + ny * (r + safety); 
            x = (int)Math.round(px); 
            y = (int)Math.round(py); 
        } 
    }
    for (Spinner s : g.spinners) { //exact same thing but for spinners
        Shape spinShape = s.getCollisionShape(); 
        PathIterator it = spinShape.getPathIterator(null); 
        double[] coords = new double[6]; 
        double startX = 0, startY = 0; 
        double lastX = 0, lastY = 0; 
        double bestDist2 = Double.POSITIVE_INFINITY; 
        double bestX = px, bestY = py; 
        java.util.List<Point2D.Double> vertices = new java.util.ArrayList<>(); 
        while (!it.isDone()) { 
            int seg = it.currentSegment(coords); 
            switch (seg) { 
                case PathIterator.SEG_MOVETO -> { 
                    startX = coords[0]; 
                    startY = coords[1]; 
                    lastX = startX; 
                    lastY = startY; 
                    vertices.add(new Point2D.Double(startX, startY)); 
                } 
                case PathIterator.SEG_LINETO -> { 
                    double x1 = lastX, y1 = lastY; 
                    double x2 = coords[0], y2 = coords[1]; 
                    double dx = x2 - x1, dy = y2 - y1; 
                    double len2 = dx*dx + dy*dy; 
                    double t = 0; 
                    if (len2 > 0) { 
                        t = ((px - x1)*dx + (py - y1)*dy) / len2; 
                        t = Math.max(0, Math.min(1, t)); 
                    } 
                    double cx = x1 + dx*t; 
                    double cy = y1 + dy*t; 
                    double d2 = (px - cx)*(px - cx) + (py - cy)*(py - cy); 
                    if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; } 
                    lastX = x2; 
                    lastY = y2; 
                    vertices.add(new Point2D.Double(x2, y2)); 
                } 
                case PathIterator.SEG_CLOSE -> { 
                    double x1 = lastX, y1 = lastY; 
                    double x2 = startX, y2 = startY; 
                    double dx = x2 - x1, dy = y2 - y1; 
                    double len2 = dx*dx + dy*dy; 
                    double t = 0; 
                    if (len2 > 0) { 
                        t = ((px - x1)*dx + (py - y1)*dy) / len2; 
                        t = Math.max(0, Math.min(1, t)); 
                    } 
                    double cx = x1 + dx*t; 
                    double cy = y1 + dy*t; 
                    double d2 = (px - cx)*(px - cx) + (py - cy)*(py - cy); 
                    if (d2 < bestDist2) { bestDist2 = d2; bestX = cx; bestY = cy; } 
                } 
            } 
            it.next(); 
        } 
        for (Point2D.Double v : vertices) { 
            double d2 = (px - v.x)*(px - v.x) + (py - v.y)*(py - v.y); 
            if (d2 < bestDist2) { bestDist2 = d2; bestX = v.x; bestY = v.y; } 
        } 
        double dist = Math.sqrt(bestDist2); 
        if (dist < r) { 
            double nx = px - bestX; 
            double ny = py - bestY; 
            double nlen = Math.hypot(nx, ny); 
            if (nlen == 0) { 
                nx = (vx == 0 && vy == 0) ? 1 : vx; 
                ny = (vx == 0 && vy == 0) ? 0 : vy; 
                nlen = Math.hypot(nx, ny); 
            } 
            nx /= nlen; 
            ny /= nlen; 
            double dot = vx*nx + vy*ny; 
            vx = vx - 2 * dot * nx; 
            vy = vy - 2 * dot * ny; 
            collidedThisFrame = true; 
            double safety = 1.0; 
            px = bestX + nx * (r + safety); 
            py = bestY + ny * (r + safety); 
            x = (int)Math.round(px); 
            y = (int)Math.round(py); 
        } 
    }
        } 
    }
    if (collidedThisFrame) { vx *= 0.55; vy *= 0.55; }//check collision
        return false;
}

    boolean isMoving() {
        return Math.hypot(vx,vy) > 0.05;
    }

    boolean contains(Point p) {
        return p.distance(x,y) <= r + 6;
    }

    void draw(Graphics2D g) {
        if (sunk) return;
        // light trail
        float alpha = 0.9f;
        int i=0;
        for (Point pt : trail) {
            float a = Math.max(0.03f, alpha * (i++ / (float)trail.size()));
            g.setColor(new Color(255,255,255, (int)(a*255)));
            g.fillOval(pt.x - r/2, pt.y - r/2, r, r);
        }
        g.setColor(Color.WHITE);
        g.fillOval(x-r,y-r,r*2,r*2);
        g.setColor(Color.GRAY);
        g.drawOval(x-r,y-r,r*2,r*2);
    }
}