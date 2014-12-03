package outpost.group4;

import java.util.*;

public class DumbQuadrantStrategy implements Strategy {


  int turn = 0;

  public int delete(ArrayList<Post> posts) {
    return -1;
  }

  public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
    turn += 1;

    ArrayList<Post> newPosts = new ArrayList<Post>();

    for (Post p : posts) {
      ArrayList<Post> neighbors = p.adjacentPosts();

      Post bestNeighbor = neighbors.get(Player.random.nextInt(neighbors.size()));

      // sometimes move far away from base on purpose
      if (Math.random() > 0.75) {
        Collections.shuffle(neighbors);

        double furthestDistanceFromBase = 0;
        for (Post n : neighbors) {
          double dist = n.distanceTo(Player.baseLoc);
          if (dist > furthestDistanceFromBase) {
            furthestDistanceFromBase = dist;
            bestNeighbor = n;
          }
        }
      }

      newPosts.add(bestNeighbor);
    }

    return newPosts;
  }

}
