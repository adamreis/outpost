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

	public ArrayList<Post> adjacent(int size) {
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

}
