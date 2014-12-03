package outpost.group1.common;

import outpost.sim.movePair;

public class Move {
    Point start;
    Point end;

    public Point getStart() { return start; };
    public Point getEnd() { return end; };

    public Move(Point start) {
        this(start, start);
    }
    public Move(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public String toString() {
        return String.format("<Move from %s to %s>", start, end);
    }

    public movePair toMovePair(int id) {
        if (end == null) {
            throw new RuntimeException("Endpoint of move is null.");
        }
        return new movePair(id, new outpost.sim.Pair(end.getX(), end.getY()));
    }
}
