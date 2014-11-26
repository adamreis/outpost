package outpost.group4;

import java.util.*;

public class MaxWaterHeuristic extends BoardHeuristic {

    private WaterGridSquareFilter filter;

    public MaxWaterHeuristic(Board board) {
        super(board);

        filter = new WaterGridSquareFilter();
    }

    public double score(GridSquare square) {
        return score(square, true);
    }

    public double score(GridSquare square, boolean accountForOwnership) {
        if (square.water) {
            return 0;
        }

        if (accountForOwnership && board.weWillOwnLocation(square)) {
            return 0;
        }

        ArrayList<GridSquare> surroundingWater = board.filteredSquaresWithinRadius(square, filter);
        if (accountForOwnership) {
            NegationGridSquareFilter nonOwningFilter = new NegationGridSquareFilter(new OwnershipGridSquareFilter());
            return (double) board.filteredSquares(surroundingWater, nonOwningFilter).size();
        }
        else {
            return (double) surroundingWater.size();
        }
    }
}
