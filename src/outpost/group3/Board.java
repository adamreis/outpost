package outpost.group3;

import java.util.*;

import outpost.group3.Consts;
import outpost.group3.Cell;
import outpost.group3.Loc;
import outpost.group3.JPS;

import outpost.sim.Pair;
import outpost.sim.Point;

public class Board {
	static final int dimension = Consts.dimension;
	
	private class PlayerSummary {
		public int totalCells;
		public int landCells;
		public int waterCells;
		
		PlayerSummary() {
			reset();
		}
		
		PlayerSummary(PlayerSummary ps) {
			totalCells = ps.totalCells;
			landCells = ps.landCells;
			waterCells = ps.waterCells;
		}
		
		public void reset() {
			totalCells = 0;
			landCells = 0;
			waterCells = 0;
		}
	}
	
	public int playerId;
	public double r;
	public double L;
	public double W;
	public int T;
	
	private int ticks;
	private int numSupportableOutposts;
	private double avgSupportableOutpostsPerCellWithSupport;
	private Cell[][] cells;
	private boolean landGrid[][];
	private boolean passableGrid[][];
	private ArrayList<ArrayList<Loc>> outposts;
	private ArrayList<PlayerSummary> playerSummaries;
	
	private JPS jps;
	
	/* Transforms a coordinate to/from the simulator and system where our player is always at (0,0) */ 
	public void simFlip(Loc loc) {
		if (playerId == 1 || playerId == 2)
			loc.x = dimension - loc.x - 1;
		
		if (playerId == 3 || playerId == 2)
			loc.y = dimension - loc.y - 1;
	}
	
	// Constructor
	Board(int playerId, Point[] simGrid, double r, double L, double W, int T) {
		if (simGrid.length != dimension*dimension)
			System.err.println("Attempting to create board with wrong number of Points");
		
		this.playerId = playerId;
		this.r = r;
		this.L = L;
		this.W = W;
		this.T = T;
		
		ticks = 0;
		cells = new Cell[dimension][dimension];
		landGrid = new boolean[dimension][dimension];
		passableGrid = new boolean[dimension][dimension];
		outposts = new ArrayList<ArrayList<Loc>>();
		playerSummaries = new ArrayList<PlayerSummary>();
		
		jps = new JPS(landGrid, dimension, dimension);
		
		for (int i = 0; i < simGrid.length; i++) {
			Point p = simGrid[i];
			Loc loc = new Loc(p.x, p.y);
			simFlip(loc);
			cells[loc.x][loc.y] = new Cell(loc.x, loc.y, p.water ? Cell.CellType.WATER : Cell.CellType.LAND);
			landGrid[loc.x][loc.y] = !p.water;
		}
	
		for (int id = 0; id < Consts.numPlayers; id++) {
			outposts.add(new ArrayList<Loc>());
			playerSummaries.add(new PlayerSummary());
		}
		
		// Precompute number of land and water cells within r Manhattan distance of each cell
		int numLandAccessible = 0;
		int numWaterAccessible = 0;
		int totalSupportFromCellsWithSupport = 0;
		int numCellsWithSupport = 0;
		
		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				Cell cell = cells[x][y];
				
				cell.setNearestLand(findNearestLand(x, y));
				cell.setNearestWater(findNearestWater(x, y));
				
				int numLandCellsNearby = 0;
				int numWaterCellsNearby = 0;
				ArrayList<Loc> nearbyLocs = getNearbyLocs(x, y);
				for (Loc loc : nearbyLocs) {
					if (cells[loc.x][loc.y].isLand())
						numLandCellsNearby++;
					else
						numWaterCellsNearby++;
				}
				
				cell.setNumLandCellsNearby(numLandCellsNearby);
				cell.setNumWaterCellsNearby(numWaterCellsNearby);
				
				if (numOutpostsSupportableOn(x, y) > 0) {
					totalSupportFromCellsWithSupport += numOutpostsSupportableOn(x, y); 
					numCellsWithSupport++;
				}
				
				if (cell.isLand())
					numLandAccessible++;
				else if (cell.isWater() && Loc.mDistance(cell.x,  cell.y, cell.getNearestLand()) <= r)
					numWaterAccessible++;
			}
		}
		
		numSupportableOutposts = (int) Math.min((double) numLandAccessible / L, (double) numWaterAccessible / W) + Consts.numPlayers;
		avgSupportableOutpostsPerCellWithSupport = (double) totalSupportFromCellsWithSupport / (double) numCellsWithSupport;
		
		// Precompute all shortest paths to home cells by BFS
		for (int id = 0; id < Consts.numPlayers; id++) {
			Loc home = getHomeCell(id);
			LinkedList<Loc> queue = new LinkedList<Loc>();
			boolean[][] visited = new boolean[dimension][dimension];
			
			queue.add(home);
			visited[home.x][home.y] = true;
			cells[home.x][home.y].setPathDistanceToHome(id, 0);
			
			while (!queue.isEmpty()) {
				Loc loc = queue.poll();
				ArrayList<Loc> neighbors = getNearbyLocs(loc.x, loc.y, 1);
				
				for (Loc neighbor : neighbors) {
					if (!visited[neighbor.x][neighbor.y] && cells[neighbor.x][neighbor.y].isLand()) {
						queue.add(neighbor);
						visited[neighbor.x][neighbor.y] = true;
						cells[neighbor.x][neighbor.y].setPathDistanceToHome(id, cells[loc.x][loc.y].getPathDistanceToHome(id) + 1);
					}
				}
			}
		}
	}

	// Copy constructor
	Board(Board board) {
		this.playerId = board.playerId;
		this.r = board.r;
		this.L = board.L;
		this.W = board.W;
		this.T = board.T;
		
		ticks = board.ticks;
		numSupportableOutposts = board.numSupportableOutposts;
		avgSupportableOutpostsPerCellWithSupport = board.avgSupportableOutpostsPerCellWithSupport; 
		cells = new Cell[dimension][dimension];
		landGrid = board.landGrid;
		passableGrid = new boolean[dimension][dimension];
		outposts = new ArrayList<ArrayList<Loc>>();
		playerSummaries = new ArrayList<PlayerSummary>();
		
		jps = new JPS(landGrid, dimension, dimension);
		
		for (int x = 0; x < dimension; x++)
			for (int y = 0; y < dimension; y++)
				cells[x][y] = new Cell(board.cells[x][y]);
		
		for (int id = 0; id < Consts.numPlayers; id++) {
			outposts.add(new ArrayList<Loc>());
			
			for (int j = 0; j < board.outposts.get(id).size(); j++)
				outposts.get(id).add(new Loc(board.outposts.get(id).get(j)));
			
			playerSummaries.add(new PlayerSummary(board.playerSummaries.get(id)));
		}
	}
	
	public void update(ArrayList<ArrayList<Loc>> outpostList) {
		if (outpostList.size() != Consts.numPlayers)
			System.err.println("Attempting to update board with wrong size list of player outposts");
		
		ticks++;
		
		// Update number of outposts on each cell and outpost list per player
		for (int id = 0; id < Consts.numPlayers; id++) {
			for (int x = 0; x < dimension; x++) {
				for (int y = 0; y < dimension; y++) {
					cells[x][y].setNumOutposts(id, 0);
				}
			}

			outposts.get(id).clear();
			playerSummaries.get(id).reset();
			
			for (Loc loc : outpostList.get(id)) {
				cells[loc.x][loc.y].incNumOutposts(id);
				outposts.get(id).add(loc);
			}
		}
		
		// Update state of each cell
		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				Cell cell = cells[x][y];
				cell.setNeutral();
				
				for (int id = 0; id < Consts.numPlayers; id++) {
					for (Loc loc : outposts.get(id)) {
						double d = Loc.mDistance(x, y, loc);
						if (d <= r && d == cell.getOwnerDistance() && id != cell.getOwnerId()) {
							cell.setDisputed();
						} else if (d <= r && d < cell.getOwnerDistance()) {
							cell.setOwned(id, d);
						}
					}
				}
			}
		}
		
		// Calculate scores and other metrics, and perhaps also analyze paths and so forth
		// For each player, want to know: total territory controlled, land controlled, water controlled, supportable outposts
		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				Cell cell = cells[x][y];
				passableGrid[x][y] = false;

				if (cell.isOwned()) {
					int id = cell.getOwnerId();
					playerSummaries.get(id).totalCells += 1;
					if (cell.isLand())
						playerSummaries.get(id).landCells += 1;
					else
						playerSummaries.get(id).waterCells += 1;
				}
				
				if (!cell.isOwned() || (cell.isOwned() && cell.getOwnerId() == playerId))
					passableGrid[x][y] = true;
			}
		}		
	}
	
	public void updateFromSim(ArrayList<ArrayList<Pair>> simOutpostList) {
		if (simOutpostList.size() != Consts.numPlayers)
			System.err.println("Attempting to update board with wrong size list of player outposts");
		
		ArrayList<ArrayList<Loc>> outpostList = new ArrayList<ArrayList<Loc>>();
		
		for (int id = 0; id < Consts.numPlayers; id++) {
			outpostList.add(new ArrayList<Loc>());
			
			for (Pair pair : simOutpostList.get(id)) {
				Loc loc = new Loc(pair.x, pair.y);
				simFlip(loc);
				outpostList.get(id).add(loc);
			}
		}
		
		update(outpostList);
	}
	
	// Returns indexes of outposts that would be disbanded on this board for the given player id
	public ArrayList<Integer> outpostsToDisband(int id) {
		ArrayList<Integer> outpostList = new ArrayList<Integer>();
		
		Loc home = getHomeCell(id);
		LinkedList<Loc> queue = new LinkedList<Loc>();
		boolean[][] visited = new boolean[dimension][dimension];
		
		queue.add(home);
		visited[home.x][home.y] = true;
		
		while (!queue.isEmpty()) {
			Loc loc = queue.poll();
			ArrayList<Loc> neighbors = getNearbyLocs(loc.x, loc.y, 1);
			
			for (Loc neighbor : neighbors) {
				if (!visited[neighbor.x][neighbor.y] && cells[neighbor.x][neighbor.y].isPassableFor(id)) {
					queue.add(neighbor);
					visited[neighbor.x][neighbor.y] = true;
				}
			}
		}
		
		for (int i = 0; i < outposts.get(id).size(); i++) {
			Loc loc = outposts.get(id).get(i);
			
			if (!visited[loc.x][loc.y])
				outpostList.add(new Integer(i));
		}
		
		return outpostList;
	}
	
	public int getTicksRemaining() {
		return T - ticks;
	}
	
	public int getTicksRemainingInSeason() {
		return Consts.ticksPerSeason - (ticks % Consts.ticksPerSeason);
	}
	
	public Loc getHomeCell(int id) {
		Loc loc = null;
		
		if (id == 0)
			loc = new Loc(0, 0);
		else if (id == 1)
			loc = new Loc(dimension - 1, 0);
		else if (id == 2)
			loc = new Loc(dimension - 1, dimension - 1);
		else if (id == 3)
			loc = new Loc(0, dimension - 1);
			
		simFlip(loc);
		return loc;
	}
	
	/* Inefficient algorithm to find nearest land to a cell, but we only call it once per cell and then cache the results */
	private Loc findNearestLand(int xStart, int yStart) {
		Loc nearestLand = null;
		int minDistance = Integer.MAX_VALUE;
		
		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				if (cells[x][y].isLand() && Loc.mDistance(x, y, xStart, yStart) < minDistance) {
					minDistance = Loc.mDistance(x, y, xStart, yStart);
					nearestLand = new Loc(x, y);
				}
			}
		}
		
		return nearestLand;
	}

	/* Inefficient algorithm to find nearest water to a cell, but we only call it per cell and then cache the results */
	private Loc findNearestWater(int xStart, int yStart) {
		Loc nearestWater = null;
		int minDistance = Integer.MAX_VALUE;
		
		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				if (cells[x][y].isWater() && Loc.mDistance(x, y, xStart, yStart) < minDistance) {
					minDistance = Loc.mDistance(x, y, xStart, yStart);
					nearestWater = new Loc(x, y);
				}
			}
		}
		
		return nearestWater;
	}
	
	public Cell getCell(int x, int y) {
		return cells[x][y];
	}
	
	public Cell getCell(Loc loc) {
		return getCell(loc.x, loc.y);
	}
	
	// Returns the diamond of locations centered around (x,y) at given radius (which will typically be r)
	// Includes the central location
	public ArrayList<Loc> getNearbyLocs(int xCenter, int yCenter, double radius) {
		ArrayList<Loc> nearbyLocs = new ArrayList<Loc>();
		
		for (int x = xCenter - (int) radius; x <= xCenter + (int) radius; x++) {
			for (int y = yCenter - (int) radius; y <= yCenter + (int) radius; y++) {
				if (isInside(x, y) && Loc.mDistance(xCenter, yCenter, x, y) <= radius)
					nearbyLocs.add(new Loc(x, y));
			}
		}
				
		return nearbyLocs;
	}

	public ArrayList<Loc> getNearbyLocs(int x, int y) {
		return getNearbyLocs(x, y, r);
	}
	
	public ArrayList<Loc> getNearbyLocs(Loc l) {
		return getNearbyLocs(l.x, l.y, r);
	}
	
	public ArrayList<Loc> getNearbyLocs(Loc l, double radius) {
		return getNearbyLocs(l.x, l.y, radius);
	}
	
	public ArrayList<Loc> findPath(int xStart, int yStart, int xEnd, int yEnd) {
		return findPath(new Loc(xStart, yStart), new Loc(xEnd, yEnd));
	}
	
	public ArrayList<Loc> findPath(Loc start, Loc end) {
		return jps.findPath(start, end);
	}
	
	public ArrayList<Loc> findPathPassable(Loc start, Loc end) {
		JPS jps = new JPS(passableGrid, dimension, dimension);
		return jps.findPath(start, end);
	}
	
	public Loc crop(Loc loc) {
		Loc l = new Loc(loc);
		
		if (l.x > dimension - 1)
			l.x = dimension - 1;
		else if (l.x < 0)
			l.x = 0;
		
		if (l.y > dimension - 1)
			l.y = dimension - 1;
		else if (l.y < 0)
			l.y = 0;
		
		return l;
	}
	
	public Loc nearestLand(Loc loc) {
		Loc l = crop(loc);		
		return cells[l.x][l.y].getNearestLand();
	}

	public Loc nearestWater(Loc loc) {
		Loc l = crop(loc);
		return cells[l.x][l.y].getNearestWater();
	}
	
	public ArrayList<Loc> ourOutposts() {
		return outposts.get(playerId);
	}

	public ArrayList<Loc> theirOutposts(int id) {
		return outposts.get(id);
	}
	
	public ArrayList<ArrayList<Loc>> allOutposts() {
		ArrayList<ArrayList<Loc>> allOutposts = new ArrayList<ArrayList<Loc>>();
		
		for (int id = 0; id < Consts.numPlayers; id++) {
			allOutposts.add(new ArrayList<Loc>());
			
			for (int j = 0; j < outposts.get(id).size(); j++)
				allOutposts.get(id).add(new Loc(outposts.get(id).get(j)));
		}
		
		return allOutposts;
	}
	
	public boolean cellHasOutpost(int x, int y) {
		return cells[x][y].hasOutpost();
	}
	
	public int numOutpostsOnCell(int x, int y) {
		return cells[x][y].getNumOutposts();
	}

	public boolean cellHasOutpostFor(int id, int x, int y) {
		return cells[x][y].hasOutpost(id);
	}
	
	public int numOutpostsOnCellFor(int id, int x, int y) {
		return cells[x][y].getNumOutposts();
	}
	
	public int scoreFor(int id) {
		return playerSummaries.get(id).totalCells;
	}
	
	public int numOutpostsTotal() {
		int sum = 0;
		
		for (int id = 0; id < Consts.numPlayers; id++)
			sum += numOutpostsFor(id);
		
		return sum;
	}
	
	public int numOutpostsFor(int id) {
		return outposts.get(id).size();
	}
	
	public int numOutpostsSupportableTotal() {
		return numSupportableOutposts;
	}
	
	public int numOutpostsSupportableFor(int id) {
		return (int) Math.min((double) playerSummaries.get(id).landCells / L, (double) playerSummaries.get(id).waterCells / W) + 1;
	}
	
	public int numOutpostsSupportableOn(int x, int y) {
		Cell cell = cells[x][y];
		return (int) Math.min((double) cell.getNumLandCellsNearby() / L, (double) cell.getNumWaterCellsNearby() / W);
	}
	
	public int numOutpostsSupportableOn(Loc l) {
		return numOutpostsSupportableOn(l.x, l.y);
	}
	
	public double avgSupportPerCell() {
		return avgSupportableOutpostsPerCellWithSupport;
	}
	
	public static class DumpInfo {
		public static enum DumpType { TYPE, STATE, OWNER, VALUE };
		
		private DumpType dumpType;
		ArrayList<Loc> path;
		
		DumpInfo(DumpType dumpType) {
			this.dumpType = dumpType;
		}
		
		DumpInfo(DumpType dumpType, ArrayList<Loc> path) {
			this.dumpType = dumpType;
			this.path = path;
		}
	}
	
	/* Debug function to print board to console.  Pass 1 for cellType, pass 2 for cellState, pass 3 for cellOwner */
    public void dump(DumpInfo dumpInfo) {
		String s = new String();
    	for (int y = 0; y < dimension; y++) {
			for (int x = 0; x < dimension; x++) {
				if (cells[x][y].hasOutpost()) {
					s = s + "O";
				} else if (dumpInfo.path != null && dumpInfo.path.contains(new Loc(x, y))) {
					s = s + "#";
				} else {
					if (dumpInfo.dumpType == DumpInfo.DumpType.TYPE) {
	 					if (cells[x][y].isLand())
	 						s = s + ".";
	 					else if (cells[x][y].isWater())
	 						s = s + "W";
					} else if (dumpInfo.dumpType == DumpInfo.DumpType.STATE) {
	 					if (cells[x][y].isOwned())
	 						s = s + "+";
	 					else if (cells[x][y].isDisputed())
	 						s = s + "X";
	 					else
	 						s = s + "-";
	 				} else if (dumpInfo.dumpType == DumpInfo.DumpType.OWNER) {
	 					if (cells[x][y].isOwned())
	 						s = s + cells[x][y].getOwnerId();
	 					else
	 						s = s + "-";
	 				} else if (dumpInfo.dumpType == DumpInfo.DumpType.VALUE) {
	 					if (cells[x][y].isLand())
	 						s = s + numOutpostsSupportableOn(x, y);
	 					else
	 						s = s + "W";
	 				}
				}
				s = s + " ";
			}
			
			s = s + "\n";
		}
    	
    	System.out.printf(s);
    }
    
	private boolean isInside(int x, int y) {
		return (x >= 0 && x < dimension) && (y >= 0 && y < dimension);
	}
	
	public int numLandCellsFor(int id) {
		return playerSummaries.get(id).landCells;
	}

	public int numWaterCellsFor(int id) {
		return playerSummaries.get(id).waterCells;
	}
}
