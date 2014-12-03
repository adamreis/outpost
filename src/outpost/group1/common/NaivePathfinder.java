package outpost.group1.common;

import java.util.*;

public class NaivePathfinder implements Pathfinder {
    public Point getPath(Game game, final Tile current, final Point destination) {
        List<Tile> considered = current.getImmediateLandNeighbors();
        considered.add(current);

        return CollectionUtils.best(considered, new CollectionUtils.Score<Tile>() {
            public double score(Tile t) { 
                double distance = destination.distanceTo(t.asPoint());
                return -distance;
            };
        }).asPoint();
    }
};
