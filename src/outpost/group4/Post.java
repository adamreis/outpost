package outpost.group4;

import java.util.ArrayList;

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

	public ArrayList<Post> adjacentCells(int size) {
		ArrayList<Post> adj = new ArrayList<Post>();

		if (this.x > 0) // left
			adj.add(new Post(this.x - 1, this.y, this.id));
		if (this.x < size - 1) // right
			adj.add(new Post(this.x + 1, this.y, this.id));
		if (this.y > 0) // up
			adj.add(new Post(this.x, this.y - 1, this.id));
		if (this.y < size - 1) // down
			adj.add(new Post(this.x, this.y + 1, this.id));

		return adj;
	}

	public ArrayList<Post> postsWithinRadius(ArrayList<Post> posts) {
		ArrayList<Post> postsInRadius = new ArrayList<Post>();

		for (Post post : posts) {
			if (isPostWithinRadius(post)) {
				postsInRadius.add(post);
			}
		}

		return postsInRadius;
	}

	public boolean isPostWithinRadius(Post post) {
		return (this.distanceTo(post) <= Player.parameters.outpostRadius);
	}

	public Post nearestPost(ArrayList<Post> posts) {
		Post nearestPost = null;
		double minDist = Double.POSITIVE_INFINITY;

		for (Post post : posts) {
			double dist = distanceTo(post);
			if (dist < minDist) {
				minDist = dist;
				nearestPost = post;
			}
		}

		return nearestPost;
	}

	public Post moveMaximizingDistanceFrom(Post post) {
		Post furthestPost = null;
		double maxDist = Double.NEGATIVE_INFINITY;

		ArrayList<Post> possibleMoves = adjacentCells(Player.parameters.size);
		for (Post possiblePost : possibleMoves) {
			double dist = distanceTo(post, possiblePost);
			if (dist > maxDist) {
				maxDist = dist;
				furthestPost = possiblePost;
			}
		}

		return furthestPost;
	}

}
