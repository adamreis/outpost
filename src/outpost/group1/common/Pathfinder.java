package outpost.group1.common;

public interface Pathfinder {
    Point getPath(Game game, final Tile current, final Point destination);
};
