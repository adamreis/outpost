package outpost.group4;

import java.util.*;

public class UtilityMaxStrategy implements Strategy {

    ArrayList<Post> posts;
    ArrayList<ArrayList<Post>> otherPlayerPosts;
    List<GridSquare> bestSquares;

    public int seasonCount;

    public UtilityMaxStrategy() {
      seasonCount = 0;
    }

    public int delete(ArrayList<Post> posts, GridSquare[][] board) {
        Post nearestPost = (Post) Player.baseLoc.nearestLocation(posts);
        int del = posts.indexOf(nearestPost);
        if (del < 0) del = Player.random.nextInt(posts.size());

        return del;
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        this.posts = posts;
        this.otherPlayerPosts = otherPlayerPosts;

        ArrayList<Post> newPosts = new ArrayList<Post>();
        MaxWaterHeuristic waterHeuristic = new MaxWaterHeuristic(Player.board);
        DistanceWeighingHeuristic heuristic = new DistanceWeighingHeuristic(waterHeuristic, Player.SIZE / 4 + seasonCount * 3);

        HashSet<Integer> takenSquareIndices = new HashSet<Integer>();

        if (newSeason) {
            seasonCount++;
        }

        bestSquares = heuristic.getTopSquares(posts.size());
        System.out.println(bestSquares);

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);

            Post newPost = p;

            Post emergency = emergencyMove(p);
            if (emergency != null) {
                newPost = emergency;
            }
            else {
                for (int targetIndex = 0; targetIndex < bestSquares.size(); targetIndex++) {
                    if (takenSquareIndices.contains(targetIndex)) continue;

                    GridSquare targetSquare = bestSquares.get(targetIndex);
                    double targetScore = heuristic.weightScore(waterHeuristic.score(targetSquare, true), targetSquare);

                    GridSquare currentSquare = Player.board.gridSquareWithLocation(newPost);
                    double currentScore = heuristic.weightScore(waterHeuristic.score(currentSquare, true), currentSquare);

                    System.out.printf("index: %d |||| current: %s %f |||| target: %s %f\n", targetIndex, currentSquare, currentScore, targetSquare, targetScore);

                    if (targetScore > currentScore) {
                        newPost.target = targetSquare;

                        takenSquareIndices.add(targetIndex);
                        Player.board.addTargetSquareOwnership(newPost.target);
                        ArrayList<Location> path = p.shortestPathToLocation(newPost.target);

                        int nextLocationIndex = (path.size() > 1)? 1 : 0;
                        newPost.x = path.get(nextLocationIndex).x;
                        newPost.y = path.get(nextLocationIndex).y;

                        break;
                    }
                }
            }

            newPosts.add(newPost);
        }

        return newPosts;
    }

    public Post emergencyMove(Post p) {
        for (ArrayList<Post> posts : otherPlayerPosts) {
            for (Post opponentPost : posts) {
                if (p.isLocationUnderInfluence(opponentPost)) {
                    System.out.printf("EMERGENCY!!!! %s vs. %s\n", p, opponentPost);
                    return p.moveMaximizingDistanceFrom(opponentPost);
                }
            }
        }

        return null;
    }
}
