
package outpost.group4;

import java.util.*;

public class CombinationGridSquareFilter implements GridSquareFilter {

    public List<GridSquareFilter> filters;

    public CombinationGridSquareFilter(GridSquareFilter... filters) {
        this(Arrays.asList(filters));
    }

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

    public String toString() {
        String s = "(";
        for (GridSquareFilter filter : filters) {
            s += filter.toString() + ", ";
        }
        return s + ")";
    }

}
