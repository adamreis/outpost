package outpost.group1.common;

import java.util.*;

public class Point {
    int x;
    int y;

    public int getX() { return x; };
    public int getY() { return y; };

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this(p.x, p.y);
    }

    public Point(outpost.sim.Pair p) {
        this(p.x, p.y);
    }
    public Point(outpost.sim.Point p) {
        this(p.x, p.y);
    }

    public int distanceTo(Point other) {
        return Math.abs(other.x - this.x) + Math.abs(other.y - this.y);
    }

    public int hashCode() {
        return x + y * Board.BOARD_SIZE;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Point)) {
            return false;
        } else if (other == this) {
            return true;
        }

        Point o = (Point)other;

        return o.x == this.x && o.y == this.y;
    }

    public List<Point> neighbors() {
        return neighbors(false);
    }

    public List<Point> neighbors(boolean corners_too) {
        List<Point> result = new ArrayList<Point>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                if (Math.abs(i) == 1 && Math.abs(j) == 1 && !corners_too) continue;

                Point p = new Point(this.x + j, this.y + i);
                if (Rectangle.BOARD_RECTANGLE.contains(p)) {
                    result.add(p);
                }
            }
        }

        return result;
    }

    public <T> T closestTo(List<Point> list, ArrayList<T> objs) {
        return objs.get(closestToIndex(list));
    }

    public int closestToIndex(List<Point> list) {
        int best_index = -1;
        int index = -1;
        int best_dist = Integer.MAX_VALUE;

        for (Point p : list) {
            index += 1;
            int dist = this.distanceTo(p);
            if (dist < best_dist) {
                best_dist = dist;
                best_index = index;
            }
        }

        return best_index;
    }

    public Point closestTo(Iterable<Point> list) {
        Point best = null;
        int best_dist = Integer.MAX_VALUE;

        for (Point p : list) {
            int dist = this.distanceTo(p);
            if (dist < best_dist) {
                best_dist = dist;
                best = p;
            }
        }

        return best;
    }


    public Point unit() {
        if (this.x == 0) {
            return new Point(this.x, 1);
        } else if (this.y == 0) {
            return new Point(1, this.y);
        } else {
            return new Point(0, 0);
        }
    }
    public Point add(int dx, int dy) {
        return new Point(this.x + dx, this.y + dy);
    }

    public Point add(Point p) {
        return new Point(this.x + p.x, this.y + p.y);
    }

    public Point sub(int dx, int dy) {
        return new Point(this.x - dx, this.y - dy);
    }

    public Point sub(Point p) {
        return new Point(this.x - p.x, this.y - p.y);
    }

    public String toString() {
        return String.format("<Point (%d, %d)>", x, y);
    }
}
