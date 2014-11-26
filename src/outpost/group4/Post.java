package outpost.group4;

import java.util.*;

import outpost.group4.Location;
import outpost.sim.Pair;
import outpost.sim.movePair;

public class Post extends Location {
	public int id;
	public Location target;

	public Post(Location loc, int id) {
		super(loc.x, loc.y);
		this.id = id;
	}

	public Post(int x, int y, int id) {
		super(x, y);
		this.id = id;
	}

	public Post(Pair p, int id) {
		super(p);
		this.id = id;
	}

	public Post(Post p) {
			super(p.x, p.y);
			this.id = p.id;
	}

	public Post copy() {
			return new Post(this);
	}

	public void update(Post p) {
			this.x = p.x;
			this.y = p.y;
			this.id = p.id;
	}

	public String toString() {
		return "outpost #" + this.id + ": " + super.toString();
	}

	public ArrayList<Post> adjacentPosts() {
		ArrayList<Location> locations = super.adjacentLocations();
		ArrayList<Post> posts = new ArrayList<Post>();

		for (Location loc : locations) {
			posts.add(new Post(loc, this.id));
		}

		return posts;
	}

	public Post preferredAdjacency() {
		  ArrayList<Post> posts = adjacentPosts();
		  int preferredIndex = this.id % posts.size();
		  return posts.get(preferredIndex);
	}

	public ArrayList<Post> postsUnderInfluence(ArrayList<Post> posts) {
		ArrayList<Post> postsUnderInfluence = new ArrayList<Post>();

		for (Post post : posts) {
			if (post == this) continue;

			if (isLocationUnderInfluence(post)) {
				postsUnderInfluence.add(post);
			}
		}

		return postsUnderInfluence;
	}

	public boolean isLocationUnderInfluence(Location location) {
		return isLocationUnderInfluence(location, 0);
	}

	public boolean isLocationUnderInfluence(Location location, int buffer) {
		return (this.distanceTo(location) <= Player.parameters.outpostRadius + buffer);
	}

	public Post nearestPost(ArrayList<Post> posts) {
		return (Post) nearestLocation(posts);
	}

  public GridSquare furthestWater() {
      GridSquare[][] gridSquares = Player.board.getGridSquares();
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
      GridSquare[][] gridSquares = Player.board.getGridSquares();
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
		ArrayList<Post> possibleMoves = adjacentPosts();

		for (Post possiblePost : possibleMoves) {
			double dist = distance(loc, possiblePost);
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
		ArrayList<Post> possibleMoves = adjacentPosts();

		for (Post possiblePost : possibleMoves) {
			double dist = distance(loc, possiblePost);
			if (dist > maxDist) {
				maxDist = dist;
				furthestPost = possiblePost;
			}
		}

		return furthestPost;
	}

}
