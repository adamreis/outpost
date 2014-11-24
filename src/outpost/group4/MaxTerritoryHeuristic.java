package outpost.group4;

import java.util.*;

public class MaxTerritoryHeuristic extends BoardHeuristic {

    public MaxTerritoryHeuristic(Board board) {
        super(board);
    }

    public double score(GridSquare square) {
        return (double) board.availableTerritory(square).size();
    }
}
