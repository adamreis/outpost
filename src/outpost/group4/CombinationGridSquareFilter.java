
package outpost.group4;

import java.util.*;

public class CombinationGridSquareFilter implements GridSquareFilter {

    public List<GridSquareFilter> filters;

    public CombinationGridSquareFilter(List<GridSquareFilter> filters) {
        this.filters = filters;
    }

    public boolean squareIsValid(GridSquare square) {
        for (GridSquareFilter filter : filters) {
            if (!filter.squareIsValid(square)) {
                return false;
            }
        }

        return true;
    }

}
