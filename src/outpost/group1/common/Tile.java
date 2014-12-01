package outpost.group1.common;

import java.util.*;

public abstract class Tile {
    public final int x;
    public final int y;

    abstract boolean isValid();
    abstract boolean isWater();
    abstract boolean isLand();

    public final Board board;

    protected Tile(Point p, Board b) {
        this(p.getX(), p.getY(), b);
    }

    protected Tile(int x, int y, Board b) {
        this.x = x;
        this.y = y;

        this.board = b;
    }

    public Point asPoint() {
        return new Point(x,y);
    }

    public static Water Water(Point p, Board b) {
        return new Water(p.getX(), p.getY(), b);
    }
    public static Water Water(int x, int y, Board b) {
        return new Water(x,y,b);
    }

    public static Land Land(Point p, Board b) {
        return new Land(p.getX(), p.getY(), b);
    }
    public static Land Land(int x, int y, Board b) {
        return new Land(x,y,b);
    }

    public static Invalid Invalid(Point p, Board b) {
        return new Invalid(p.getX(), p.getY(), b);
    }
    public static Invalid Invalid(int x, int y, Board b) {
        return new Invalid(x,y,b);
    }

    public int waterControlled() { return getWaterNeighbors().size(); }
    public int landControlled() { return getLandNeighbors().size(); }

    public int resourceScore() {
        return waterControlled() * board.W + landControlled() * board.L;
    }

    public double unitsSupported() {
        if (this.isLand()) {
            return Math.min(waterControlled() / (double)board.W, 
                            landControlled() / (double)board.L);
        } else return 0;
    }

    List<Tile> immediate_land_neighbors = null;
    public List<Tile> getImmediateLandNeighbors() {
        if (immediate_land_neighbors == null) {
            immediate_land_neighbors = CollectionUtils.filter(Arrays.asList(
                        this.board.getOffset(this, 1, 0),
                        this.board.getOffset(this, -1, 0),
                        this.board.getOffset(this, 0, 1),
                        this.board.getOffset(this, 0, -1)
                        ), Tile.isValidPredicate);
        }
        return immediate_land_neighbors;
    }

    List<Tile> neighbors = null;
    public List<Tile> getNeighbors() {
        if (neighbors == null) {
            neighbors = new ArrayList<Tile>();

            for (int dy = -board.radius; dy <= 0; dy += 1) {
                for (int dx = -board.radius - dy; dx <= board.radius + dy; dx += 1) {
                    Tile candidate = board.get(this.asPoint().add(dx, dy));
                    if (candidate.isValid()) {
                        neighbors.add(candidate);
                    }
                }
            }
            for (int dy = 1; dy <= board.radius; dy += 1) {
                for (int dx = -board.radius + dy; dx <= board.radius - dy; dx += 1) {
                    Tile candidate = board.get(this.asPoint().add(dx, dy));
                    if (candidate.isValid()) {
                        neighbors.add(candidate);
                    }
                }
            }
        }
        return neighbors;
    }

    List<Tile> land_neighbors = null;
    public List<Tile> getLandNeighbors() {
        if (land_neighbors == null) {
            land_neighbors = CollectionUtils.filter(getNeighbors(), 
                new CollectionUtils.Predicate<Tile>() {
                    public boolean test(Tile t) { return t.isLand(); };
                });
        }

        return land_neighbors;
    }

    List<Tile> water_neighbors = null;
    public List<Tile> getWaterNeighbors() {
        if (water_neighbors == null) {
            water_neighbors = CollectionUtils.filter(getNeighbors(), 
                new CollectionUtils.Predicate<Tile>() {
                    public boolean test(Tile t) { return t.isWater(); };
                });
        }

        return water_neighbors;
    }

    public static class Water extends Tile {
        protected Water(int x, int y, Board b) { super(x, y, b); }

        @Override
        public boolean isWater() { return true; }

        @Override
        public boolean isLand()  { return false; }

        @Override
        public boolean isValid()  { return true; }
    }

    public static class Invalid extends Tile {
        protected Invalid(int x, int y, Board b) { super(x, y, b); }

        @Override
        public boolean isWater() { return false; }

        @Override
        public boolean isLand()  { return false; }

        @Override
        public boolean isValid()  { return false; }

    }
    public static class Land extends Tile {
        protected Land(int x, int y, Board b) { super(x, y, b); }

        @Override
        public boolean isWater() { return false; }

        @Override
        public boolean isLand()  { return true; }

        @Override
        public boolean isValid()  { return true; }
    }

    public static CollectionUtils.Predicate<Tile> isValidPredicate = new CollectionUtils.Predicate<Tile>() {
        public boolean test(Tile t) { return t.isValid(); };
    };

    public static CollectionUtils.Predicate<Tile> isWaterPredicate = new CollectionUtils.Predicate<Tile>() {
        public boolean test(Tile t) { return t.isWater(); };
    };
    public static CollectionUtils.Predicate<Tile> isLandPredicate = new CollectionUtils.Predicate<Tile>() {
        public boolean test(Tile t) { return t.isLand(); };
    };

    public CollectionUtils.Score<Tile> getClosenessScore() {
        return new ClosenessScore(this.asPoint());
    }

    public static class ClosenessScore implements CollectionUtils.Score<Tile> {
        public ClosenessScore(Point current) {
            this.current = current;
        }
        final Point current;
        public double score(Tile t) { return current.distanceTo(t.asPoint()); };
    };

    @Override
    public String toString() {
        return String.format("<%s tile at %s>", this.isWater() ? "Water" : this.isValid() ? "Land" : "Invalid", this.asPoint());
    }

    public double distanceTo(Outpost o) {
        return o.distanceTo(this.asPoint());
    }

    public double distanceTo(Point p) {
        return p.distanceTo(this.asPoint());
    }
}
