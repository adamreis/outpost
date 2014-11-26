
package outpost.group4;

import java.util.*;

public class NegationGridSquareFilter implements GridSquareFilter {

    public GridSquareFilter filter;

    public NegationGridSquareFilter(GridSquareFilter filter) {
        this.filter = filter;
    }

    public boolean squareIsValid(GridSquare square) {
        return !this.filter.squareIsValid(square);
    }

}
