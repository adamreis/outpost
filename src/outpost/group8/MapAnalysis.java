package outpost.group8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import outpost.sim.Pair;
import outpost.group8.PlayerUtil;

public class MapAnalysis {
	
	public MapAnalysis() {
		
	}

	public static HashSet<Location> findWater(Location[][] grid) {
		HashSet<Location> waterPoints = new HashSet<Location>();
		for (Location[] line : grid) {
			for (Location l : line) {
				if (l.water) {
					waterPoints.add(l);
				}
			}
		}
		return waterPoints;
	}
	
	public static HashSet<Location> findShore(HashSet<Location> water){
		HashSet<Location> shore = new HashSet<Location>();
		for (Location l : water) {
			// It's ok to initialize this new location as not being water, because the overridden
			// .equals() only compares x and y.
			ArrayList<Location> possibleNeighbors = new ArrayList<Location>();
			possibleNeighbors.add(new Location(l.x, l.y - 1, false)); // north
			possibleNeighbors.add(new Location(l.x, l.y + 1, false)); // south
			possibleNeighbors.add(new Location(l.x + 1, l.y, false)); // east
			possibleNeighbors.add(new Location(l.x - 1, l.y, false)); // west
			possibleNeighbors.add(new Location(l.x + 1, l.y + 1, false)); // southeast
			possibleNeighbors.add(new Location(l.x - 1, l.y - 1, false)); // northwest
			possibleNeighbors.add(new Location(l.x - 1, l.y + 1, false)); // southwest
			possibleNeighbors.add(new Location(l.x + 1, l.y - 1, false)); // northeast
			
			for (Location possible : possibleNeighbors) {
				for (Location w : water) {
					if (possible.x != w.x && possible.y != w.y) {
						shore.add(possible);
					}
				}
			}
		}
		return shore;
	}
	// O(n^2) of findShore.
	public static HashSet<Location> findShores(HashSet<Location> water){
		
		int [][] possibleMove = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
		boolean [][]visited = new boolean[100][100];
		for (int i = 0 ; i < 100 ; i++) {
			Arrays.fill(visited[i], false);
		}
		HashSet<Location> shore = new HashSet<Location>();
		for (Location l : water) {
			ArrayList<Location> possibleNeighbors = new ArrayList<Location>();
			for (int [] po : possibleMove) {
				int row = l.x + po[0];
				int col = l.y + po[1];
				if (!Global.grid[row][col].water && !visited[row][col]) {
					shore.add(Global.grid[row][col]);		
				}
				visited[row][col] = true;
			}
		}
		return shore;
	}
	
	/**
	 * Update the shore points that do not have one of our outposts on them.
	 * @param ourOutposts
	 * @param shorePoints
	 * @return
	 */
	public static ArrayList<Location> updateOpenShore(List<Pair> ourOutposts, HashSet<Location> shorePoints) {
		ArrayList<Location> openShore = new ArrayList<Location>();
		
		for (Location l : shorePoints) {
			for (Pair outpost : ourOutposts) {
				if (l.x == outpost.x && l.y == outpost.y) {
					System.out.println(l.x + ", " + l.y + " is not in openShore");
				} else {
					openShore.add(l);
				}
			}
		}
		
		return openShore;
	}
	
}
