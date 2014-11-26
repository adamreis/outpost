
package outpost.group4;

import java.util.*;

public class WaterGridSquareFilter implements GridSquareFilter {

    public boolean squareIsValid(GridSquare square) {
        return square.water;
    }

}
