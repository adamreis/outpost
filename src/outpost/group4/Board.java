package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Board {

    public GridSquare[][] board;

    public ArrayList<ArrayList<Post>> masterPosts;
    public ArrayList<ArrayList<Post>> otherPlayerPosts;
    public HashMap<Integer, HashSet<? extends Location>> ownersMap;

    public Board(Point[] points, HashMap<Integer, HashSet<? extends Location>> map) {
        board = Conversions.gridSquaresFromPoints(points);
        ownersMap = map;
    }

    public Board(ArrayList<ArrayList<Pair>> outpostList, Point[] grid) {
        board = Conversions.gridSquaresFromPoints(grid);

        // perform conversions to custom classes
        ownersMap = new HashMap<Integer, HashSet<? extends Location>>();
        masterPosts = new ArrayList<ArrayList<Post>>(outpostList.size());
        otherPlayerPosts = new ArrayList<ArrayList<Post>>(outpostList.size() - 1);
        for (int i = 0; i < outpostList.size(); i++) {
            ArrayList<Post> posts = Conversions.postsFromPairs(outpostList.get(i));
            masterPosts.add(posts);

            if (i != Player.knownID) {
                otherPlayerPosts.add(posts);
            }

            ownersMap.put(i, new HashSet<Post>(posts));
        }
    }

    public GridSquare[][] getGridSquares() {
        return board;
    }

    public ArrayList<GridSquare> getGridSquaresList() {
        ArrayList<GridSquare> squares = new ArrayList<GridSquare>();

        for (GridSquare[] rowArray : board) {
            for (GridSquare square : rowArray) {
                squares.add(square);
            }
        }

        return squares;
    }

    public ArrayList<GridSquare> filteredSquaresWithinRadius(GridSquare square, GridSquareFilter filter) {
        ArrayList<GridSquare> possibleSquares = squaresWithinRadius(square);
        return filteredSquaresWithinRadius(possibleSquares, filter);
    }

    public ArrayList<GridSquare> filteredSquaresWithinRadius(ArrayList<GridSquare> possibleSquares, GridSquareFilter filter) {
        ArrayList<GridSquare> filteredSquares = new ArrayList<GridSquare>();
        for (GridSquare gridSquare : possibleSquares) {
            if (filter.squareIsValid(gridSquare)) {
                filteredSquares.add(gridSquare);
            }
        }
        return filteredSquares;
    }

    public ArrayList<GridSquare> squaresWithinRadius(GridSquare square) {
        ArrayList<GridSquare> squares = new ArrayList<GridSquare>();

        int size = Player.parameters.size;
        int radius = Player.parameters.outpostRadius;
        for (int i = -radius; i <= radius; i++) {
            for (int dist = 0; dist <= radius; dist++) {
                for (int j = -dist; j <= dist; j++) {
                    int x = square.x + i;
                    int y = square.y + j;
                    if (i + j != dist || x < 0 || y < 0 || x >= size || y >= size) continue;

                    squares.add(board[x][y]);
                }
            }
        }

        return squares;
    }

    public int ownerOfLocation(Location location) {
        int owner = -1;

        for (int i = 0; i < GameParameters.NUM_PLAYERS; i++) {
            if (ownersMap.get(i).contains(location)) {
                owner = i;
                break;
            }
        }

        return owner;
    }

    public boolean weOwnLocation(Location location) {
        return ownerOfLocation(location) == Player.knownID;
    }

    public ArrayList<Post> ourPosts() {
        return masterPosts.get(Player.knownID);
    }

}
