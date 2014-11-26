package outpost.group4;

import java.util.*;

public class MaxWaterHeuristic extends BoardHeuristic {

    private WaterGridSquareFilter filter;
    public boolean careAboutOwnership;

    public MaxWaterHeuristic(Board board) {
        this(board, true);
    }

    public MaxWaterHeuristic(Board board, boolean careAboutOwnership) {
        super(board);

        filter = new WaterGridSquareFilter();
        this.careAboutOwnership = careAboutOwnership;
    }

    public double score(GridSquare square) {
        return score(square, careAboutOwnership);
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
