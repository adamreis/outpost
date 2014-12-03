package outpost.group4;

import java.util.*;

public class UtilityMaxStrategy implements Strategy {

    ArrayList<Post> posts;
    ArrayList<ArrayList<Post>> otherPlayerPosts;
    ArrayList<GridSquare> bestSquares;
    HashMap<Location, ArrayList<GridSquare>> targetMap;
    int turn = 0;
    boolean valueLand;

    public UtilityMaxStrategy(boolean shouldValueLand) {
      valueLand = shouldValueLand;
    }

    public int delete(ArrayList<Post> posts) {
      return -1;
    }

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        if (targetMap == null) {
          targetMap = new HashMap<Location, ArrayList<GridSquare>>();
        }
        turn += 1;
        this.posts = posts;
        this.otherPlayerPosts = otherPlayerPosts;

        ArrayList<Post> newPosts = new ArrayList<Post>();
        HashMap<Location, ArrayList<GridSquare>> newTargetMap = new HashMap<Location, ArrayList<GridSquare>>();

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            Post newPost = new Post(p);
            int squareIndex = 0;
            ArrayList<Location> path = new ArrayList<Location>();
            boolean fresh = false;

            GridSquare target = null;
            ArrayList<GridSquare> possibleTargets = targetMap.get(p);

            // if we already have a target:
            if (possibleTargets != null && possibleTargets.size() > 0) {
              target = possibleTargets.get(0);
              possibleTargets.remove(target);

              // if we are at our target, move away from our own posts that were there first
              if (p.x == target.x && p.y == target.y) {
                for (Post neighbor : posts) {
                  double dist = p.distanceTo(neighbor);
                  if (dist < (double) Player.parameters.outpostRadius / 2 && p.id > neighbor.id) {
                    // pick a new target
                    ArrayList<GridSquare> bestWaterSquares = Player.board.getBestResourceSquaresForPost(p, valueLand);
                    int index = i;
                    GridSquare newTarget = target;
                    while (newTarget.distanceTo(target) < Player.parameters.outpostRadius * 2.5 || path.size() <= 1) {
                      target = bestWaterSquares.get(index);
                      path = p.shortestPathToLocation(target);
                      index++;
                    }
                    System.out.printf("PICKING NEW TARGET %s WHEN AT TARGET %s \n", target, p);
                    break;
                  }
                }
              }
            }

            // if we do not have a target, grab a new one and move toward it
            else {
              ArrayList<GridSquare> bestWaterSquares = Player.board.getBestResourceSquaresForPost(p, valueLand);
              while (path.size() <= 1) {
                target = bestWaterSquares.get(squareIndex);
                path = p.shortestPathToLocation(target);
                squareIndex++;
              }
              System.out.printf("FINDING FIRST TARGET %s WHEN AT LOCATIOn %s \n", target, p);
              fresh = true;
            }

            path = p.shortestPathToLocation(target);
            int nextLocationIndex = (path.size() > 1) ? 1 : 0;
            newPost.x = path.get(nextLocationIndex).x;
            newPost.y = path.get(nextLocationIndex).y;

            if (fresh) {
              System.out.printf("OLD %s NEW %s\n", p, newPost);
            }

            if (newTargetMap.get(newPost) == null) {
              newTargetMap.put(newPost, new ArrayList<GridSquare>());
            }
            newTargetMap.get(newPost).add(target);

            newPosts.add(newPost);
        }

        targetMap = newTargetMap;
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
