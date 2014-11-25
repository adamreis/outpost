
package outpost.group4;

import java.util.*;

public class AvailableTerritoryGridSquareFilter implements GridSquareFilter {

    public boolean squareIsValid(GridSquare square) {
        return (!square.water && square.owners.size() == 0);
    }

}
