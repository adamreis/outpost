
package outpost.group4;

import java.util.*;

public class TerritorialGainNearBaseHeuristic extends MaxTerritoryHeuristic {

    private static final double DEFAULT_MINIMIZING_FACTOR = 100;

    public TerritorialGainNearBaseHeuristic(Board board) {
        super(board);
    }

    public double score(GridSquare square) {
        double s = super.score(square);

        double dist = Location.distanceTo(square, Player.baseLoc);
        if (dist > DEFAULT_MINIMIZING_FACTOR) {
            s /= (dist / DEFAULT_MINIMIZING_FACTOR);
        }

        return s;
    }
}
