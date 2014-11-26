package outpost.group4;

import java.util.*;

public class GreedyResourceStrategy implements Strategy {

    HashMap<Location, Location> postTargetMap;
    HashSet<Location> stayPutSet;
    List<GridSquare> squaresWithinThresold;
    GridSquareFilter waterFilter;
    GridSquareFilter landFilter;
    int seasonCount;

    public GreedyResourceStrategy() {
        postTargetMap = new HashMap<Location, Location>();
        stayPutSet = new HashSet<Location>();
        waterFilter = new WaterGridSquareFilter();
        landFilter = new NegationGridSquareFilter(waterFilter);
        seasonCount = 0;
    }

    public int delete(ArrayList<Post> posts, GridSquare[][] board) {
        Post nearestPost = (Post) Player.baseLoc.nearestLocation(posts);
        int del = posts.indexOf(nearestPost);
        if (del < 0) del = Player.random.nextInt(posts.size());

        return del;
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (newSeason) {
            seasonCount += 1;

            if (seasonCount % 4 == 0) {
                stayPutSet = new HashSet<Location>();
            }
        }

        WithinThresholdGridSquareFilter thresholdFilter = new WithinThresholdGridSquareFilter(Player.baseLoc, threshold());
        CombinationGridSquareFilter filter = new CombinationGridSquareFilter(thresholdFilter, landFilter);
        squaresWithinThresold = Player.board.filteredSquares(filter);

        ArrayList<Post> newPosts = new ArrayList<Post>();
        for (Post p : posts) {
            newPosts.add(p.copy());
        }

        for (int i = 0; i < posts.size(); i++) {
            Post p = newPosts.get(i);
            Post originalPost = posts.get(i);
            Location targetLocation = postTargetMap.get(p);

            if (targetLocation != null) {
                //System.out.printf("%s HAS A TARGET %s\n", p, targetLocation);
                if (targetLocation.equals(p)) {
                    postTargetMap.remove(p);
                }
                else {
                    postTargetMap.remove(p);
                    p.updateLocationWithShortestPathToTarget(targetLocation);
                    postTargetMap.put(p, targetLocation);
                }
            }

            if (postTargetMap.get(p) == null && !stayPutSet.contains(p)) {
                int waterWithoutMoving = waterCount(newPosts);
                int bestWaterCount = waterWithoutMoving;
                Post bestNeighbor = null;
                ArrayList<Post> neighbors = p.adjacentPosts();
                for (Post neighbor : neighbors) {
                    p.update(neighbor);
                    int water = waterCount(newPosts);
                    if (water > bestWaterCount) {
                        bestNeighbor = neighbor;
                        bestWaterCount = water;
                    }
                    p.update(originalPost);
                }

                if (bestNeighbor != null) {
                    p.update(bestNeighbor);
                }
                else {
                    Collections.shuffle(squaresWithinThresold);
                    targetLocation = targetForPost(p, newPosts);

                    if (!targetLocation.equals(p)) {
                        //System.out.println("NEW TARGET");
                        p.updateLocationWithShortestPathToTarget(targetLocation);
                        postTargetMap.put(p, targetLocation);
                    }
                    else {
                        stayPutSet.add(p);
                    }
                }
            }
        }

        return newPosts;
    }

    public int threshold() {
        return Player.SIZE / 2 + seasonCount * 2;
    }

    public void updatePostLocationWithItsTarget(Post p) {
        Location targetLocation = postTargetMap.get(p);
        p.updateLocationWithShortestPathToTarget(targetLocation);
    }

    public Location targetForPost(Post p, ArrayList<Post> posts) {
        List<GridSquare> options = squaresWithinThresold;

        int currentScore = combinedScore(posts);
        int bestScore = currentScore;
        Post bestOption = null;
        for (GridSquare option : options) {
            postTargetMap.put(p, option);

            int optionalScore = combinedScore(posts);
            if (optionalScore > bestScore) {
                bestScore = optionalScore;
                bestOption = new Post(option, p.id);
            }
        }

        postTargetMap.remove(p);

        if (bestOption == null) bestOption = p;

        //System.out.printf("target for %s is %s ||| %d %d\n", p, bestOption, currentScore, bestScore);

        return bestOption;
    }

    public int combinedScore(ArrayList<Post> posts) {
        int landScore = targetedLandCount(posts);

        int waterValue = (int) (Player.parameters.landWaterRatio() * 0.5);
        int waterScore = targetedWaterCount(posts) * waterValue;

        return landScore + waterScore;
    }

    public int targetedWaterCount(ArrayList<Post> posts) {
        return targetedSurroundingsCount(posts, waterFilter);
    }

    public int targetedLandCount(ArrayList<Post> posts) {
        return targetedSurroundingsCount(posts, landFilter);
    }

    public int targetedSurroundingsCount(ArrayList<Post> posts, GridSquareFilter filter) {
        HashSet<Location> accountedFor = new HashSet<Location>();

        int count = 0;
        for (Post p : posts) {
            ArrayList<GridSquare> surroundings = targetedSurroundings(p, filter);
            for (GridSquare square : surroundings) {
                if (!accountedFor.contains(square)) {
                    accountedFor.add(square);
                    count += 1;
                }
            }
        }
        return count;
    }

    public int waterCount(ArrayList<Post> posts) {
        return surroundingCount(posts, waterFilter);
    }

    public int landCount(ArrayList<Post> posts) {
        return surroundingCount(posts, landFilter);
    }

    public int surroundingCount(ArrayList<Post> posts, GridSquareFilter filter) {
        HashSet<Location> accountedFor = new HashSet<Location>();

        int count = 0;
        for (Post p : posts) {
            ArrayList<GridSquare> surroundings = surroundings(p, filter);
            for (GridSquare square : surroundings) {
                if (!accountedFor.contains(square)) {
                    accountedFor.add(square);
                    count += 1;
                }
            }
        }
        return count;
    }

    public ArrayList<GridSquare> targetedSurroundingWater(Location location) {
        return targetedSurroundings(location, waterFilter);
    }

    public ArrayList<GridSquare> targetedSurroundingLand(Location location) {
        return targetedSurroundings(location, landFilter);
    }

    public ArrayList<GridSquare> targetedSurroundings(Location location, GridSquareFilter filter) {
        Location target = postTargetMap.get(location);
        if (target != null) {
            return Player.board.filteredSquaresWithinRadius(target, filter);
        }
        else {
            return Player.board.filteredSquaresWithinRadius(location, filter);
        }
    }

    public ArrayList<GridSquare> surroundingWater(Location location) {
        return surroundings(location, waterFilter);
    }

    public ArrayList<GridSquare> surroundingLand(Location location) {
        return surroundings(location, landFilter);
    }

    public ArrayList<GridSquare> surroundings(Location location, GridSquareFilter filter) {
        return Player.board.filteredSquaresWithinRadius(location, filter);
    }
}
