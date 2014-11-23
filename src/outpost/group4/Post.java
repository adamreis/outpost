package outpost.group4;

import java.util.*;

import outpost.group4.Location;
import outpost.sim.Pair;
import outpost.sim.movePair;

public class Post extends Location {
	public int id;

	public Post(Location loc, int id) {
		super(loc.x, loc.y);
		this.id = id;
	}

	public Post(int x, int y, int id) {
		super(x, y);
		this.id = id;
	}

	public Post(Pair p, int id) {
		super(p.x, p.y);
		this.id = id;
	}

	public String toString() {
    return "outpost #" + this.id + ": " + super.toString();
  }

	public ArrayList<Post> adjacentCells() {
		int size = Player.parameters.size;

		ArrayList<Post> adj = new ArrayList<Post>();

    Post left = new Post(this.x - 1, this.y, this.id);
    Post right = new Post(this.x + 1, this.y, this.id);
    Post up = new Post(this.x, this.y - 1, this.id);
    Post down = new Post(this.x, this.y + 1, this.id);

    if (validatePost(left)) adj.add(left);
    if (validatePost(right)) adj.add(right);
    if (validatePost(up)) adj.add(up);
    if (validatePost(down)) adj.add(down);

    //Collections.shuffle(adj);

		return adj;
	}

  public Post preferredAdjacency() {
    ArrayList<Post> cells = adjacentCells();
    int preferredIndex = this.id % cells.size();
    return cells.get(preferredIndex);
  }

  public boolean validatePost(Post post) {
    int size = Player.parameters.size;
    GridSquare[][] gridSquares = Player.gridSquares;

    // check board boundaries
    if (post.x < 0 || post.x >= size || post.y < 0 || post.y >= size)
      return false;
    // check if post is on a water square
    if (gridSquares[post.x][post.y].water)
      return false;

    return true;
  }

	public ArrayList<Post> postsUnderInfluence(ArrayList<Post> posts) {
		ArrayList<Post> postsUnderInfluence = new ArrayList<Post>();

		for (Post post : posts) {
      if (post == this) continue;

			if (isPostUnderInfluence(post)) {
				postsUnderInfluence.add(post);
			}
		}

		return postsUnderInfluence;
	}

	public boolean isPostUnderInfluence(Post post) {
		return (this.distanceTo(post) <= 2 * Player.parameters.outpostRadius);
	}

	public Post nearestPost(ArrayList<Post> posts) {
		return (Post) nearestLocation(posts);
	}

  public GridSquare furthestWater() {
    GridSquare[][] gridSquares = Player.gridSquares;
    GridSquare furthestWater = null;
    double maxDist = Double.NEGATIVE_INFINITY;

    for (int x = 0; x < gridSquares.length; x++) {
      for (int y = 0; y < gridSquares.length; y++) {
        GridSquare square = gridSquares[x][y];
        if (square.water) {
          double dist = distanceTo(square);
          if (dist > maxDist) {
            maxDist = dist;
            furthestWater = square;
          }
        }
      }
    }

    return furthestWater;
  }

  public GridSquare nearestWater() {
    GridSquare[][] gridSquares = Player.gridSquares;
    GridSquare nearestWater = null;
    double minDist = Double.POSITIVE_INFINITY;

    for (int x = 0; x < gridSquares.length; x++) {
      for (int y = 0; y < gridSquares.length; y++) {
        GridSquare square = gridSquares[x][y];
        if (square.water) {
          double dist = distanceTo(square);
          if (dist < minDist) {
            minDist = dist;
            nearestWater = square;
          }
        }
      }
    }

    return nearestWater;
  }

	public Post moveMinimizingDistanceFrom(Location loc) {
		Post nearestPost = null;
		double minDist = distanceTo(loc);
		ArrayList<Post> possibleMoves = adjacentCells();

		for (Post possiblePost : possibleMoves) {
			double dist = distanceTo(loc, possiblePost);
			if (dist < minDist) {
				minDist = dist;
				nearestPost = possiblePost;
			}
		}

		return nearestPost;
	}

	public Post moveMaximizingDistanceFrom(Location loc) {
		Post furthestPost = null;
		double maxDist = distanceTo(loc);
		ArrayList<Post> possibleMoves = adjacentCells();

		for (Post possiblePost : possibleMoves) {
			double dist = distanceTo(loc, possiblePost);
			if (dist > maxDist) {
				maxDist = dist;
				furthestPost = possiblePost;
			}
		}

		return furthestPost;
	}

}
