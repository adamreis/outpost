
package outpost.group4;

import java.util.*;

public class StagePlanningStrategy implements Strategy {

    private static final int DEFAULT_WATER_COLLECTOR_COUNT = 7;
    private static final int DEFAULT_BASE_PROTECTOR_COUNT = 3;
    private static final int DEFAULT_LAND_COLLECTOR_COUNT = 7;

    boolean[][] currentlyControlledLand;
    ArrayList<Location> targetLocations;

    int waterCollectorCount;
    int baseProtectorCount;
    int landCollectorCount;

    GridSquareFilter waterFilter;
    GridSquareFilter landFilter;
    WithinThresholdGridSquareFilter baseThresholdFilter;
    WithinThresholdGridSquareFilter semiConservativeThresholdFilter;
    GridSquareFilter validQuadrantTerritoriesFilter;

    int seasonCount;

    public StagePlanningStrategy() {
        targetLocations = new ArrayList<Location>();

        waterCollectorCount = DEFAULT_WATER_COLLECTOR_COUNT;
        baseProtectorCount = DEFAULT_BASE_PROTECTOR_COUNT;
        landCollectorCount = DEFAULT_LAND_COLLECTOR_COUNT;

        waterFilter = new WaterGridSquareFilter();
        landFilter = new NegationGridSquareFilter(waterFilter);
        baseThresholdFilter = new WithinThresholdGridSquareFilter(Player.baseLoc, (int) (Player.SIZE * 0.51));
        semiConservativeThresholdFilter = new WithinThresholdGridSquareFilter(Player.baseLoc, (int) (Player.SIZE * 0.76));
        validQuadrantTerritoriesFilter = new CombinationGridSquareFilter(baseThresholdFilter, landFilter);

        seasonCount = 0;
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (newSeason) {
            seasonCount += 1;

            // refresh targets completely every two seasons
            if (seasonCount % 2 == 0) {
                targetLocations = new ArrayList<Location>();

                baseThresholdFilter.threshold += 1;

                waterCollectorCount = (int) Math.max(DEFAULT_WATER_COLLECTOR_COUNT, posts.size() * 0.21);
                landCollectorCount = (int) Math.max(DEFAULT_LAND_COLLECTOR_COUNT, posts.size() * 0.3);
            }
        }

        // update controlled territories
        currentlyControlledLand = new boolean[Player.SIZE][Player.SIZE];
        for (Post p : posts) {
            currentlyControlledLand[p.x][p.y] = true;
            ArrayList<GridSquare> squaresInRadius = Player.board.squaresWithinRadius(p);
            for (GridSquare square : squaresInRadius) {
                if (!square.water) currentlyControlledLand[square.x][square.y] = true;
            }
        }

        int waterAndBaseCount = waterCollectorCount + baseProtectorCount;
        int waterBaseAndLandCount = waterAndBaseCount + landCollectorCount;

        // add the water collectors
        if (targetLocations.size() < waterCollectorCount) {
            ArrayList<GridSquare> squaresInQuadrant = Player.board.filteredSquares(validQuadrantTerritoriesFilter);
            ArrayList<GridSquare> bestWaterCellsInQuadrant = mostValuableCells(waterCollectorCount, squaresInQuadrant, waterFilter, false);
            for (int i = 0; i < bestWaterCellsInQuadrant.size(); i++) {
                GridSquare target = bestWaterCellsInQuadrant.get(i);
                updateTargetAtIndex(i, target);
            }
        }

        // add the base protectors
        if (posts.size() > waterCollectorCount) {
            if (targetLocations.size() < waterAndBaseCount) {
                ArrayList<Location> shells = shellCells(baseProtectorCount, Player.baseLoc);
                for (int i = 0; i < baseProtectorCount; i++) {
                    Location targetLocation = shells.get(i);
                    updateTargetAtIndex(i + waterCollectorCount, targetLocation);
                }
            }
        }

        // add the land collectors
        if (posts.size() > waterAndBaseCount) {
            if (targetLocations.size() < waterBaseAndLandCount) {
                ArrayList<GridSquare> squaresNearQuadrant = Player.board.filteredSquares(semiConservativeThresholdFilter);
                Collections.shuffle(squaresNearQuadrant);
                ArrayList<GridSquare> bestLandCells = mostValuableCells(landCollectorCount, squaresNearQuadrant, landFilter, true);
                for (int i = 0; i < bestLandCells.size(); i++) {
                  GridSquare target = bestLandCells.get(i);
                  updateTargetAtIndex(i + waterAndBaseCount, target);
                }
            }
        }

        // the rest can be aggressors
        if (posts.size() > waterBaseAndLandCount) {
            if (targetLocations.size() < posts.size()) {
                for (int i = waterBaseAndLandCount; i < posts.size(); i++) {
                    updateTargetAtIndex(i, Player.board.randomLandSquare());
                }
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

    public int delete(ArrayList<Post> posts, GridSquare[][] board) {
        int lastTargetIndex = Math.max(targetLocations.size() - 1, 0);
        targetLocations.remove(lastTargetIndex);
        return lastTargetIndex;
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

    private ArrayList<GridSquare> mostValuableCells(int count, ArrayList<GridSquare> possibleCells, GridSquareFilter valueFilter, boolean useCurrentlyControlledLand) {
        ArrayList<GridSquare> mostValuableCells = new ArrayList<GridSquare>(count);
        boolean[][] controlledLocations = new boolean[Player.SIZE][Player.SIZE];

        if (useCurrentlyControlledLand) {
            for(int i = 0; i < currentlyControlledLand.length; i++) {
                controlledLocations[i] = currentlyControlledLand[i].clone();
            }
        }

        for (int i = 0; i < count; i++) {
            int bestScore = 0;
            GridSquare bestSquare = null;
            for (GridSquare square : possibleCells) {
                if (controlledLocations[square.x][square.x]) continue;

                int score = 0;
                ArrayList<GridSquare> surroundingCells = Player.board.filteredSquaresWithinRadius(square, valueFilter);
                for (GridSquare temp : surroundingCells) {
                    if (controlledLocations[temp.x][temp.y]) continue;
                    
                    score += 1;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestSquare = square;
                }
            }

            mostValuableCells.add(bestSquare);
            controlledLocations[bestSquare.x][bestSquare.y] = true;
            ArrayList<GridSquare> surroundingCells = Player.board.filteredSquaresWithinRadius(bestSquare, valueFilter);
            for (GridSquare square : surroundingCells) {
                controlledLocations[square.x][square.y] = true;
            }
        }

        return mostValuableCells;
    }

    private ArrayList<GridSquare> mostValuableCells(int count, ArrayList<GridSquare> possibleCells, GridSquareFilter valueFilter, Post currPost, boolean useCurrentlyControlledLand) {
        ArrayList<GridSquare> mostValuableCells = new ArrayList<GridSquare>(count);
        boolean[][] controlledLocations = new boolean[Player.SIZE][Player.SIZE];

        if (useCurrentlyControlledLand) {
            for(int i = 0; i < currentlyControlledLand.length; i++) {
                controlledLocations[i] = currentlyControlledLand[i].clone();
            }
        }

        for (int i = 0; i < count; i++) {
            int bestScore = 0;
            GridSquare bestSquare = null;
            for (GridSquare square : possibleCells) {
                if (controlledLocations[square.x][square.y]) continue;

                int score = 0;
                ArrayList<GridSquare> surroundingCells = Player.board.filteredSquaresWithinRadius(square, valueFilter);
                for (GridSquare temp : surroundingCells) {
                    if (Player.board.squaresWithinRadius(currPost).contains(temp)) {
                      score += 1;
                    } else if (controlledLocations[temp.x][temp.y]) {
                      continue;
                    } else {
                      score += 1;
                    }
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestSquare = square;
                }
            }

            mostValuableCells.add(bestSquare);
            controlledLocations[bestSquare.x][bestSquare.y] = true;
            ArrayList<GridSquare> surroundingCells = Player.board.filteredSquaresWithinRadius(bestSquare, valueFilter);
            for (GridSquare square : surroundingCells) {
                controlledLocations[square.x][square.y] = true;
            }
        }

        return mostValuableCells;
    }
}
