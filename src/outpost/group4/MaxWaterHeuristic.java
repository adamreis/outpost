package outpost.group4;

import java.util.*;

public class MaxWaterHeuristic extends BoardHeuristic {

    private WaterGridSquareFilter filter;

    public MaxWaterHeuristic(Board board) {
        super(board);

        filter = new WaterGridSquareFilter();
    }

    public double score(GridSquare square) {
        if (square.water || board.weOwnLocation(square)) {
            return 0;
        }

        return (double) board.filteredSquaresWithinRadius(square, filter).size();
    }
}
