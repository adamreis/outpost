package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Conversions {

  public static ArrayList<movePair> movePairsFromPosts(ArrayList<Post> posts) {
    ArrayList<movePair> pairs = new ArrayList<movePair>();

    for (Post post : posts) {
      pairs.add(new movePair(post.id, new Pair(post.x, post.y)));
    }
    return pairs;
  }

  public static ArrayList<Post> postsFromPairs(ArrayList<Pair> pairs) {
    ArrayList<Post> posts = new ArrayList<Post>();
    int id = 0;
    for (Pair p : pairs) {
      posts.add(new Post(p, id++));
    }
    return posts;
  }

  public static GridSquare[][] gridSquaresFromPoints(Point[] points) {
    int size = Player.parameters.size;
    GridSquare[][] gridSquares = new GridSquare[size][size];

    int x = 0;
    int y = 0;
    for (int i = 0; i < points.length; i++) {
      Point p = points[i];
      ArrayList<Post> owners = postsFromPairs(p.ownerlist);
      gridSquares[x][y] = new GridSquare(p, owners);
      y += 1;
      if (y % 100 == 0) {
        y = 0;
        x += 1;
      }
    }

    return gridSquares;
  }

}
