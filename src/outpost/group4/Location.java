package outpost.group4;

import java.util.*;

import outpost.sim.Pair;

public class Location {
	public int x;
	public int y;
	public boolean visited; // only used for BFS

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Location(Location location) {
		this.x = location.x;
		this.y = location.y;
	}

	public Location(Pair p) {
		this.x = p.x;
		this.y = p.y;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o instanceof Location))
			return false;

		Location l = (Location) o;

		return x == l.x && y == l.y;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = hash * 71 + this.x;
		hash = hash * 71 + this.y;
		return hash;
	}

	@Override
	public String toString() {
		return "(" + x + " " + y + ")";
	}

	static public double distance(double x1, double y1, double x2, double y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}

	public double distanceTo(Location comparison) {
		return distanceTo(this, comparison);
	}

	static public double distanceTo(Location l1, Location l2) {
		return distance(l1.x, l1.y, l2.x, l2.y);
	}

	static public boolean equals(Location l1, Location l2) {
		return l1.x == l2.x && l1.y == l2.y;
	}

	static public boolean nearAny(Location target, ArrayList<Location> locations, double d) {
		for (Location location : locations) {
			if (distanceTo(target, location) < d)
				return true;
		}

		return false;
	}



	public Location nearestLocation(ArrayList<? extends Location> locations) {
		Location nearestLocation = null;
		double minDist = Double.POSITIVE_INFINITY;

		for (Location location : locations) {
			double dist = distanceTo(location);
			if (dist < minDist) {
				minDist = dist;
				nearestLocation = location;
			}
		}

		return nearestLocation;
	}

	public ArrayList<Location> adjacentLocations() {
		ArrayList<Location> adj = new ArrayList<Location>();

		Location left = new Location(this.x - 1, this.y);
		Location right = new Location(this.x + 1, this.y);
		Location up = new Location(this.x, this.y - 1);
		Location down = new Location(this.x, this.y + 1);

		if (validateLocation(left)) adj.add(left);
		if (validateLocation(right)) adj.add(right);
		if (validateLocation(up)) adj.add(up);
		if (validateLocation(down)) adj.add(down);

		return adj;
	}

	public boolean validateLocation(Location loc) {
		int size = Player.parameters.size;
		GridSquare[][] gridSquares = Player.board.getGridSquares();

		// check board boundaries
		if (loc.x < 0 || loc.x >= size || loc.y < 0 || loc.y >= size)
			return false;
		// check if post is on a water square
		if (gridSquares[loc.x][loc.y].water)
			return false;

		return true;
	}

	/*
	 * Only returns one shortest path (if one exists), not all possible shortest paths
	 */
	public ArrayList<Location> shortestPathToLocation(Location destination) {
		ArrayList<Location> path = new ArrayList<Location>();
		Set<Location> visited = new HashSet<Location>();
		LinkedList<ArrayList<Location>> q = new LinkedList<ArrayList<Location>>();

		visited.add(this);

		ArrayList<Location> basePath = new ArrayList<Location>();
		basePath.add(this);
		q.add(basePath);

		while (!q.isEmpty()) {
			ArrayList<Location> p = q.removeFirst();
			Location last = p.get(p.size() - 1);

			if (last.equals(destination)) {
				path = p;
				break;
			}

			for (Location loc : last.adjacentLocations()) {
				if (visited.contains(loc)) {
					continue;
				}
				visited.add(loc);
				ArrayList<Location> newP = new ArrayList<Location>(p);
				newP.add(loc);
				q.add(newP);
			}
		}

		return path;
	}

	public void updateLocationWithShortestPathToTarget(Location target) {
		ArrayList<Location> path = this.shortestPathToLocation(target);
		int nextLocationIndex = (path.size() > 1)? 1 : 0;
		this.x = path.get(nextLocationIndex).x;
		this.y = path.get(nextLocationIndex).y;
	}

}
