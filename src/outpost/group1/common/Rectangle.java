package outpost.group1.common;

import java.util.*;

public class Rectangle {
    Point top_left;
    Point bottom_right;

    Point getTopLeft()     { return top_left; };
    Point getBottomRight() { return bottom_right; };
    Point getTopRight()    { return new Point(this.right(), this.top()); };
    Point getBottomLeft()  { return new Point(this.left(), this.bottom()); };

    public int left()   { return top_left.getX(); };
    public int right()  { return bottom_right.getX(); };

    public int top()    { return top_left.getY(); };
    public int bottom() { return bottom_right.getY(); };

    public Point getCenter() { return new Point(this.left() + (this.right() - this.left()) / 2, 
                                                this.top() + (this.bottom() - this.top()) / 2); };

    public Rectangle(Point top_left, Point bottom_right) {
        this.top_left = top_left;
        this.bottom_right = bottom_right;
    }

    public Rectangle(int top, int bottom, int left, int right) {
        this(new Point(left, top), new Point(right, bottom));
    }

    @Override
    public String toString() {
        return String.format("<Rectangle t:%d b:%d l:%d r:%d>", top(), bottom(), left(), right());
    }

    public int hashCode() {
        return top_left.hashCode() + bottom_right.hashCode();
    }
    public boolean equals(Object other) {
        if (!(other instanceof Rectangle)) {
            return false;
        } else if (other == this) {
            return true;
        }
        Rectangle r = (Rectangle)other;

        return top_left.equals(r.top_left) && bottom_right.equals(r.bottom_right);
    }

    public boolean contains(Point p) {
        final int x = p.getX();
        final int y = p.getY();

        return left() <= x && right() >= x && top() <= y && bottom() >= y;
    }

    public Point[] getCorners() {
        return new Point[] {
            getTopLeft(),
            getTopRight(),
            getBottomRight(),
            getBottomLeft()
        };
    }

    public List<Point> getCornersList() {
        return Arrays.asList(
            getTopLeft(),
            getTopRight(),
            getBottomRight(),
            getBottomLeft()
        );
    }

    public static final Rectangle BOARD_RECTANGLE = 
        new Rectangle(new Point(0,0), 
                      new Point(Board.BOARD_SIZE-1, Board.BOARD_SIZE-1));
}
