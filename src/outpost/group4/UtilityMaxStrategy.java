package outpost.group4;

import java.util.*;

public class UtilityMaxStrategy implements Strategy {

    ArrayList<Post> posts;
    ArrayList<ArrayList<Post>> otherPlayerPosts;
    ArrayList<GridSquare> bestSquares;
    ArrayList<GridSquare> postTargets;
    int turn = 0;

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (postTargets == null) {
          postTargets = new ArrayList<GridSquare>();
        }
        turn += 1;
        this.posts = posts;
        this.otherPlayerPosts = otherPlayerPosts;


        ArrayList<Post> newPosts = new ArrayList<Post>();
        BoardHeuristic waterHeuristic = new MaxWaterHeuristic(Player.board);
        BoardHeuristic heuristic = new DistanceWeighingHeuristic(waterHeuristic);

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            Post newPost = p;
            int squareIndex = p.id;
            ArrayList<Location> path;

            // if we already have a target:
            if (squareIndex < postTargets.size()) {
              // if we are at our target, move away from our own posts that were there first
              if (p.x == postTargets.get(squareIndex).x && p.y == postTargets.get(squareIndex).y) {
                for (Post neighbor : posts) {
                  double dist = p.distanceTo(neighbor);
                  if (dist < (double) Player.parameters.outpostRadius && p.id > neighbor.id) {
                    // pick a new target
                    ArrayList<GridSquare> bestWaterSquares = Player.board.getBestWaterSquaresForPost(p);
                    postTargets.set(squareIndex, bestWaterSquares.get(2 * squareIndex));
                  }
                }
              }

              // move towards target
              path = p.shortestPathToLocation(postTargets.get(squareIndex));
            }

            // if we do not have a target, grab a new one and move toward it
            else {
              ArrayList<GridSquare> bestWaterSquares = Player.board.getBestWaterSquaresForPost(p);
              GridSquare target = bestWaterSquares.get(squareIndex);
              postTargets.add(target);
              path = p.shortestPathToLocation(target);
            }

            int nextLocationIndex = (path.size() > 1) ? 1 : 0;
            newPost.x = path.get(nextLocationIndex).x;
            newPost.y = path.get(nextLocationIndex).y;
            newPosts.add(newPost);
        }

        return newPosts;
    }

    public Post emergencyMove(Post p) {
        for (ArrayList<Post> posts : otherPlayerPosts) {
            for (Post opponentPost : posts) {
                if (p.isLocationUnderInfluence(opponentPost)) {
                    System.out.printf("EMERGENCY!!!! %s vsvsvsvsvsvs %s\n", p, opponentPost);
                    return p.moveMaximizingDistanceFrom(opponentPost);
                }
            }
        }

        return null;
    }
}
