package outpost.group1.common;

import java.util.*;

public class Board {
    public static final int BOARD_SIZE = 100;
    Tile[][] tiles = new Tile[BOARD_SIZE][BOARD_SIZE];

    public final int radius;
    public final int L;
    public final int W;
    public Board(outpost.sim.Point[] points, int r, int L, int W) {
        for (outpost.sim.Point p : points) {
            if (p.water) {
                Tile new_tile = Tile.Water(p.x, p.y, this);
                tiles[p.y][p.x] = new_tile;
                water.add(new_tile);

            } else {
                Tile new_tile = Tile.Land(p.x, p.y, this);
                tiles[p.y][p.x] = new_tile;
                land.add(new_tile);
            }
        }

        this.radius = r;
        this.lakes = identifyLakes();
        this.L = L;
        this.W = W;
    }


    public List<Tile> getAllLandTiles() {
        return land;
    }

    List<Tile> water = new ArrayList<Tile>();
    List<Tile> land  = new ArrayList<Tile>();

    List<Rectangle> lakes;

    public List<Rectangle> getLakes() { return lakes; };

    List<Rectangle> identifyLakes() {
        // run disjoint sets on water
        // call lakeFromPointSet on each disjoint item
        Set<Point> used = new HashSet<Point>();
        List<Rectangle> lakes = new ArrayList<Rectangle>();
        for (Tile p : water) {
            if (used.contains(p)) continue;

            Set<Point> lake = getLakePointsAdjacentTo(p.asPoint());
            used.addAll(lake);

            lakes.add(lakeFromPointSet(lake));
        }

        return lakes;
    }

    Set<Point> getLakePointsAdjacentTo(Point p) {
        Set<Point> lake = new HashSet<Point>(p.neighbors());
        Set<Point> last_added = new HashSet<Point>(p.neighbors());

        boolean added;

        do {
            added = false;
            Set<Point> recent = new HashSet<Point>(last_added);
            last_added.clear();

            for (Point l : recent) {
                List<Point> neighbors = l.neighbors();

                for (Point neighbor : neighbors) {
                    if (!lake.contains(neighbor) && this.get(neighbor).isWater()) {
                        added = true;

                        lake.add(neighbor);
                        last_added.add(neighbor);
                    }
                }
            }
        } while (added);

        return lake;
    }

    Rectangle lakeFromPointSet(Set<Point> points) {
        int top = 10000;
        int bottom = 0;
        int left = 10000;
        int right = 0;

        for (Point p : points) {
            if (p.getX() < left) {
                left = p.getX();
            }
            if (p.getX() > right) {
                right = p.getX();
            }

            if (p.getY() < top) {
                top = p.getY();
            }
            if (p.getY() > bottom) {
                bottom = p.getY();
            }
        }
        return new Rectangle(top, bottom, left, right);
    }

    public Tile getOffset(Tile t, int dx, int dy) {
        return this.get(t.asPoint().add(dx, dy));
    }
    public Tile get(Point p) {
        return this.get(p.getY(), p.getX());
    }
    public Tile get(int row, int col) {
        if (!Rectangle.BOARD_RECTANGLE.contains(new Point(row, col))) {
            return Tile.Invalid(row, col, this);
        }

        return tiles[row][col];
    }

    public String valueString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int val = (int)Math.floor(this.get(i,j).unitsSupported());
                
                String text;
                if (val == 0) {
                    text = " ";
                } else if (val < 10) {
                    text = String.format("%d", val);
                } else {
                    text = String.format("\033[1;32m%d\033[0m", val);
                }
                sb.append(text);
            }
            sb.append('\n');
        }

        return sb.toString();
    }
};
