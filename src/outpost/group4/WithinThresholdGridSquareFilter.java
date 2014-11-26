
package outpost.group4;

import java.util.*;

public class WithinThresholdGridSquareFilter implements GridSquareFilter {

    public Location base;
    public int threshold;

    public WithinThresholdGridSquareFilter(Location base, int threshold) {
        this.base = base;
        this.threshold = threshold;
    }

    public boolean squareIsValid(GridSquare square) {
        return Math.abs(square.x - base.x) <= threshold && Math.abs(square.y - base.y) <= threshold;
    }

}
