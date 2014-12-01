
package outpost.group4;

import java.util.*;

public class ShellStrategy implements Strategy {

    ArrayList<Location> targetLocations;

    int seasonCount;

    public ShellStrategy() {
        targetLocations = new ArrayList<Location>();
        seasonCount = 0;
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (newSeason) {
            seasonCount += 1;

            // refresh targets completely every ten seasons
            if (seasonCount % 10 == 0) {
                targetLocations = new ArrayList<Location>();
            }
        }

        // add the base protectors
        if (targetLocations.size() < posts.size()) {
            ArrayList<Location> shells = shellCells(posts.size(), Player.baseLoc);
            for (int i = 0; i < posts.size(); i++) {
                Location targetLocation = shells.get(i);
                updateTargetAtIndex(i, targetLocation);
            }
        }

        // copy posts
        ArrayList<Post> newPosts = new ArrayList<Post>();
        for (Post p : posts) {
            newPosts.add(p.copy());
        }

        // update with path finding to targets
        for (int i = 0; i < newPosts.size() && i < targetLocations.size(); i++) {
            Post newPost = newPosts.get(i);
            Location targetLocation = targetLocations.get(i);
            newPost.updateLocationWithShortestPathToTarget(targetLocation);
        }

        return newPosts;
    }

    private void updateTargetAtIndex(int i, Location target) {
        if (i < targetLocations.size() + 1) {
          targetLocations.add(target);
        }
        else {
          targetLocations.set(i, target);
        }
    }

    private ArrayList<Location> shellCells(int count, Location base) {
        ArrayList<Location> shells = new ArrayList<Location>();
        boolean[][] controlledLocations = new boolean[Player.SIZE][Player.SIZE];

        for (int i = 0; i < count; i++) {
            Location currentLocation = base;
            HashSet<Location> visited = new HashSet<Location>();
            while (true) {
                ArrayList<Location> neighbors = currentLocation.adjacentLocations();
                Location bestNeighbor = null;
                int bestScore = 0;
                for (Location neighbor : neighbors) {
                    double distanceToBase = neighbor.distanceTo(base);
                    if (distanceToBase >= Player.parameters.outpostRadius) continue;
                    if (visited.contains(neighbor)) continue;
                    visited.add(neighbor);

                    int score = (int) distanceToBase;

                    for (Location shell : shells) {
                        score += (int) neighbor.distanceTo(shell);
                    }

                    ArrayList<GridSquare> squaresWithinRadius = Player.board.squaresWithinRadius(neighbor);
                    for (GridSquare square : squaresWithinRadius) {
                        if (controlledLocations[square.x][square.y]) continue;
                        score += 1;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestNeighbor = neighbor;
                    }
                }
                if (bestNeighbor != null) {
                    currentLocation = bestNeighbor;
                }
                else {
                    break;
                }
            }

            shells.add(currentLocation);
            controlledLocations[currentLocation.x][currentLocation.y] = true;
            ArrayList<GridSquare> surroundingCells = Player.board.squaresWithinRadius(currentLocation);
            for (GridSquare square : surroundingCells) {
                controlledLocations[square.x][square.y] = true;
            }
        }

        return shells;
    }
}
