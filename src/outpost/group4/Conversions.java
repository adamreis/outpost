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

  public static ArrayList<GridSquare> gridSquaresFromPoints(Point[] points) {
    ArrayList<GridSquare> gridSquares = new ArrayList<GridSquare>();
    for (Point point : points) {
      ArrayList<Post> owners = postsFromPairs(point.ownerlist);
      gridSquares.add(new GridSquare(point, owners));
    }
    return gridSquares;
  }

  public static Point[] pointsFromGridSquares(ArrayList<GridSquare> gridSquares) {
    return null; // fill in later if necessary
  }

}
