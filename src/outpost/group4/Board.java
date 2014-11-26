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
    public HashSet<Location> targetedOwnerships;

    public Board(Point[] points, HashMap<Integer, HashSet<? extends Location>> map) {
        board = Conversions.gridSquaresFromPoints(points);
        ownersMap = map;
    }

    public Board(ArrayList<ArrayList<Pair>> outpostList, Point[] grid) {
        board = Conversions.gridSquaresFromPoints(grid);

        ownersMap = new HashMap<Integer, HashSet<? extends Location>>();
        targetedOwnerships = new HashSet<Location>();

        // perform conversions to custom classes
        masterPosts = new ArrayList<ArrayList<Post>>(outpostList.size());
        otherPlayerPosts = new ArrayList<ArrayList<Post>>(outpostList.size() - 1);
        for (int i = 0; i < outpostList.size(); i++) {
            ArrayList<Post> posts = Conversions.postsFromPairs(outpostList.get(i));
            masterPosts.add(posts);

            if (i != Player.knownID) {
                otherPlayerPosts.add(posts);
            }

            HashSet controlledSet = new HashSet(posts);
            for (Post post : posts) {
                ArrayList<GridSquare> controlledTerritory = squaresWithinRadius(post);
                for (GridSquare square : controlledTerritory) {
                    controlledSet.add(square);
                }
            }

            ownersMap.put(i, controlledSet);
        }
    }

    public GridSquare[][] getGridSquares() {
        return board;
    }

    public GridSquare randomLandSquare() {
        GridSquare square = null;
        while (square == null) {
            int x = Player.random.nextInt(Player.SIZE);
            int y = Player.random.nextInt(Player.SIZE);
            GridSquare temp = board[x][y];
            if (!temp.water) {
                square = temp;
            }
        }
        return square;
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

    public GridSquare gridSquareWithLocation(Location location) {
        return board[location.x][location.y];
    }

    public ArrayList<GridSquare> filteredSquaresWithinRadius(Location location, GridSquareFilter filter) {
        ArrayList<GridSquare> possibleSquares = squaresWithinRadius(location);
        return filteredSquares(possibleSquares, filter);
    }

    public static ArrayList<GridSquare> filteredSquares(ArrayList<GridSquare> possibleSquares, GridSquareFilter filter) {
        ArrayList<GridSquare> filteredSquares = new ArrayList<GridSquare>();
        for (GridSquare gridSquare : possibleSquares) {
            if (filter.squareIsValid(gridSquare)) {
                filteredSquares.add(gridSquare);
            }
        }
        return filteredSquares;
    }

    public ArrayList<GridSquare> filteredSquares(GridSquareFilter filter) {
        return filteredSquares(getGridSquaresList(), filter);
    }

    public ArrayList<GridSquare> squaresWithinRadius(Location square) {
        ArrayList<GridSquare> squares = new ArrayList<GridSquare>();

        int size = Player.parameters.size;
        int radius = Player.parameters.outpostRadius;
        for (int i = -radius; i <= radius; i++) {
            for (int dist = 0; dist <= radius; dist++) {
                for (int j = -dist; j <= dist; j++) {
                    int x = square.x + i;
                    int y = square.y + j;
                    if (Math.abs(square.x - x) + Math.abs(square.y - y) != radius || x < 0 || y < 0 || x >= size || y >= size) continue;
        
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

    public boolean weWillOwnLocation(Location location) {
        if (weOwnLocation(location)) return true;

        if (targetedOwnerships.contains(location)) return true;

        return false;
    }

    public ArrayList<Post> ourPosts() {
        return masterPosts.get(Player.knownID);
    }

    public void addTargetSquareOwnership(Location location) {
        targetedOwnerships.add(location);

        ArrayList<GridSquare> controlledTerritory = squaresWithinRadius(location);
        for (GridSquare square : controlledTerritory) {
            targetedOwnerships.add(square);
        }
    }

}
