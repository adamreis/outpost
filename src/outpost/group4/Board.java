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

    public ArrayList<GridSquare> allSquares;
    public ArrayList<GridSquare> quadrantSquares;

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

    public ArrayList<GridSquare> getGridSquaresList(boolean landOnly) {
        if (allSquares != null) return allSquares;

        ArrayList<GridSquare> squares = new ArrayList<GridSquare>();
        quadrantSquares = new ArrayList<GridSquare>();

        for (GridSquare[] rowArray : board) {
            for (GridSquare square : rowArray) {
                if (landOnly && square.water) continue;
                squares.add(square);

                if (Math.abs(square.x - Player.baseLoc.x) <= 50 && Math.abs(square.x - Player.baseLoc.x) <= 50) {
                    quadrantSquares.add(square);
                }
            }
        }

        allSquares = squares;
        return squares;
    }

    public ArrayList<GridSquare> getQuadrantGridSquaresList(boolean landOnly) {
      if (allSquares == null) {
        getGridSquaresList(true);
      }
      return quadrantSquares;
    }

    // given an ArrayList of GridSquares, return only those in our quadrant
    public ArrayList<GridSquare> getSquaresInQuadrant(ArrayList<GridSquare> squares) {
        ArrayList<GridSquare> squaresInQuadrant = new ArrayList<GridSquare>();
        for (GridSquare gs : squares) {
            if (inQuadrant(gs))
                squaresInQuadrant.add(gs);
        }
        return squaresInQuadrant;
    }

    // return true if the GridSquare is in our quadrant, false otherwise
    public boolean inQuadrant(GridSquare square) {
        if (Player.knownID == 0) {
            if (square.x < 50 && square.y < 50)
                return true;
            else
                return false;
        } else if (Player.knownID == 1) {
            if (square.x >= 50 && square.y < 50)
                return true;
            else
                return false;
        } else if (Player.knownID == 2) {
            if (square.x < 50 && square.y >= 50)
                return true;
            else
                return false;
        } else {
            if (square.x >= 50 && square.y >= 50)
                return true;
            else
                return false;
        }
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

    public ArrayList<GridSquare> squaresWithinRadius(Location square) {
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

    /**
     * returns the "water score" for a given post, counting squares within its radius in the following way
     * if a square is land, it is worth 0
     * if a water square is owned by the given post, it is worth 1
     * if a water square is owned by another of our posts, it is worth 0
     * if a water square is unowned by us, it is worth 1
     */
    public double resourceScoreForPost(GridSquare square, ArrayList<GridSquare> postSquares, boolean valueLand) {
        // blast water score cause we really care about water
        double waterValue = 1.0;
        double landValue = valueLand? 0.12 : 0.0;

        double score = 0;

        for (GridSquare gs : squaresWithinRadius(square)) {
            double squareValue = (gs.water)? waterValue : landValue;

            // if the post owns the square, consider it a good thing
            if (postSquares.contains(gs)) {
                score += squareValue;
            }
            // if another of our posts owns the square, discount
            else if (weOwnLocation(gs)) {
                continue;
            }
            // if someone else owns the square, discount
            // else if (square.owners.size() > 0) {
            //     continue;
            // }
            // otherwise, we go for it
            else {
                score += squareValue;
            }
        }
        return score;
    }

    /**
     * returns a sorted list of the best water squares *for* a given post, based
     * on the score returned by the ResourceScoreForPost method.
     * Weighted by distance by subtracting .5 * distance from post to square from
     * the water score. This heuristic was determined via trial and error and is
     * open to being changed.
     */
    public ArrayList<GridSquare> getBestResourceSquaresForPost(Post post, boolean valueLand) {
        ArrayList<GridSquare> squaresForPost;
        if (valueLand) {
          squaresForPost = getQuadrantGridSquaresList(true);
        }
        else {
          squaresForPost = getGridSquaresList(true);
        }

        final ArrayList<GridSquare> postSquares = squaresWithinRadius(post);
        final Post relevantPost = post;
        final boolean shouldValueLand = valueLand;
        final double distanceWeight = valueLand? 0 : -0.5;

        // sort by water score
        Collections.sort(squaresForPost, new Comparator<GridSquare>() {
            public int compare(GridSquare one, GridSquare other) {
                double oneResourceScore   = resourceScoreForPost(one, postSquares, shouldValueLand) + (distanceWeight * relevantPost.distanceTo(one));
                double otherResourceScore = resourceScoreForPost(other, postSquares, shouldValueLand) + (distanceWeight * relevantPost.distanceTo(other));
                if (oneResourceScore < otherResourceScore)
                    return 1;
                else if (oneResourceScore == otherResourceScore)
                    return 0;
                else
                    return -1;
            }
        });

        return squaresForPost;
    }

    public boolean weOwnLocation(Location location) {
        return ownerOfLocation(location) == Player.knownID;
    }

    public ArrayList<Post> ourPosts() {
        return masterPosts.get(Player.knownID);
    }

}
