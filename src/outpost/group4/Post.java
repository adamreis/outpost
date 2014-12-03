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
		super(p);
		this.id = id;
	}

	public Post(Post post) {
		super(post.x, post.y);
		this.id = post.id;
	}

	public Post copy() {
		return new Post(x, y, id);
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

  public boolean hasPathBack() {
    int size = Player.parameters.size;
    GridSquare[][] gridSquares = Player.board.getGridSquares();
    Location baseLoc = Player.baseLoc;  

		// floodfill from baseLoc
		int[] cx = {0, 0, 1, -1};
		int[] cy = {1, -1, 0, 0};

		boolean[][] vst = new boolean[size][size];
		for (int i = 0; i < size; ++i)
			for (int j = 0; j < size; ++j)
				vst[i][j] = false;
		vst[baseLoc.x][baseLoc.y] = true;

		Queue<Location> q = new LinkedList<Location>();
		q.add(baseLoc);
		while (!q.isEmpty()) {
			Location loc = q.poll();
			for (int i = 0; i < 4; ++i) {
				int x = loc.x + cx[i], y = loc.y + cy[i];
				if (x < 0 || x >= size || y < 0 || y >= size || vst[x][y]) continue;
        GridSquare gs = gridSquares[loc.x][loc.y];
				if (!gs.water && (gs.owners.size() == 0 || (gs.owners.size() == 1 && gs.owners.get(0).x == Player.knownID))) {
					vst[x][y] = true;
					q.add(new Location(x, y));
				}
			}
		}
    
    if (!vst[this.x][this.y])
      return false;   
    else
      return true;
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
		return movesMinimizingDistanceFrom(loc).get(0);
	}

	public ArrayList<Post> movesMinimizingDistanceFrom(Location loc) {
		ArrayList<Post> nearestPosts = new ArrayList<Post>();
		nearestPosts.add(this);

		ArrayList<Post> possibleMoves = adjacentPosts();

		for (Post possiblePost : possibleMoves) {
			double curDist = possiblePost.distanceTo(loc);
			double bestDist = nearestPosts.get(0).distanceTo(loc);
			if (curDist == bestDist) {
				nearestPosts.add(possiblePost);
			} else if (curDist < bestDist) {
				nearestPosts.clear();
				nearestPosts.add(possiblePost);
			}
		}

		return nearestPosts;
	}

	public Post moveMaximizingDistanceFrom(Location loc) {
		Post furthestPost = null;
		double maxDist = distanceTo(loc);
		ArrayList<Post> possibleMoves = adjacentPosts();

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
