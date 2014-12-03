package outpost.group1.common;

public class NullPathfinder implements Pathfinder {
    public Point getPath(Game game, final Tile current, final Point destination) {
        return current.asPoint();
    }
};
