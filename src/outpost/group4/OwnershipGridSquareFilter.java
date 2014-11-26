
package outpost.group4;

import java.util.*;

public class OwnershipGridSquareFilter implements GridSquareFilter {

    public boolean squareIsValid(GridSquare square) {
        return Player.board.weWillOwnLocation(square);
    }

}
