package outpost.group2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import outpost.sim.Pair;
import outpost.sim.Point;

public class GameBoard {
	private static final int BOARD_SIZE = 100;
	GridCell[][] grid = new GridCell[BOARD_SIZE][BOARD_SIZE];
	// this list is first indexed by the player's id, and then gives their list of outposts.
	ArrayList<ArrayList<Outpost>> outposts;
	boolean ownersUpdated;
	
	public GameBoard(Point[] oneDimensionalGrid, ArrayList<ArrayList<Outpost>> outposts) {
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				grid[i][j] = new GridCell(oneDimensionalGrid[i*BOARD_SIZE+j]);
			}
		}
		this.outposts = outposts;
		ownersUpdated = false;
	}
	
	public void updateSupplyLines(HashMap<Integer, Pair> homespaces) throws OwnersNotUpdatedException {
		if (!ownersUpdated)
			throw new OwnersNotUpdatedException();
		for (int id = 0; id < homespaces.size(); id++) {
			Pair startingSpace = homespaces.get(id);
			LinkedList<Pair> queue = new LinkedList<Pair>();
			queue.add(startingSpace);
			int[] cx = {0, 0, 1, -1};
			int[] cy = {1, -1, 0, 0};
			while (!queue.isEmpty()) {
				startingSpace = queue.pop();
				for (int j = 0; j < cx.length; j++) {
					if (!validCellForAnalysis(startingSpace.x + cx[j], startingSpace.y + cy[j]))
						break;
					GridCell testSpace = grid[startingSpace.x + cx[j]][startingSpace.y + cy[j]];
					// check if it's ours or available, it's land, and we haven't already marked it as a supply line.
					if (validCellForMoving(testSpace) && (testSpace.owner == id || testSpace.owner == -1) && !testSpace.hasSupplyLine[id]) {
						testSpace.hasSupplyLine[id] = true;
						queue.add(testSpace);
					}
				}
			}
		}
	}
	
	// land value is a 0 - 100 value describing how much land would be gained by moving there.
	// water value is a heat map getting closer to water.
	public void calculateResourceValues(ArrayList<Pair> pointsToCheck, int id, int influenceDistance, int waterMultiplier, int landMultiplier) throws OwnersNotUpdatedException {
		if (!ownersUpdated)
			throw new OwnersNotUpdatedException();
		GridCell currentCell;
		GridCell comparisonCell;
		for (Pair pair : pointsToCheck) {
			currentCell = grid[pair.x][pair.y];
			if (currentCell.visited)
				continue;
			if (validCellForMoving(currentCell)) {
				ArrayList<GridCell> influencedCells = getInfluencedCells(currentCell, influenceDistance);
				for (GridCell cell : influencedCells) {
					if (!cell.hasWater && cell.owner != id) {
						currentCell.landValue++;
					}
					if (cell.hasWater && cell.owner != id) {
						currentCell.waterValue++;
					}
				}
				currentCell.landValue = (int) (landMultiplier * 100 * ((double) currentCell.landValue/(double) (4*influenceDistance*influenceDistance)));
				for (int k = 0; k < BOARD_SIZE; k++) {
					for (int l = 0; l < BOARD_SIZE; l++) {
						comparisonCell = grid[k][l];
						if (comparisonCell.owner != id && validCellForAnalysis(comparisonCell) && comparisonCell.hasWater) {
							currentCell.waterValue += waterMultiplier * (1/ (double)manhattanDist(comparisonCell, currentCell));
						}
					}
				}
			}
			currentCell.visited = true;
		}
	}
	
	public void updateCellOwners(int influenceDistance) {
		for (int playerId = 0; playerId < outposts.size(); playerId++) {
			for (Outpost outpost : outposts.get(playerId)) {
				ArrayList<GridCell> cellsUnderInfluence = getInfluencedCells(outpost, influenceDistance);
				for (GridCell cell : cellsUnderInfluence) {
					int dist = manhattanDist(cell, outpost);
					if (dist < cell.distToOutpost) {
						cell.owner = playerId;
						cell.distToOutpost = dist;
					}
					else if (dist == cell.distToOutpost && cell.owner != playerId)
						cell.owner = GridCell.DISPUTED;
				}
			}
		}
		ownersUpdated = true;
	}
	
	// Returns a number between 0 and 100, where 100 is the best defensive score, and 0 is the worst.
	public int calculateDefensiveScore(Outpost movingPost, Pair testPos, int playerId, Pair homespace, int influence) {
		ArrayList<Outpost> playerOutposts = outposts.get(playerId);
		int MAX_DIST = 200;
		int combinedDistances = 0;
		for (Outpost testPost : playerOutposts) {
			// we don't want to count the post we're looking to move
			if (testPost.id == movingPost.id)
				continue;
			int dist = manhattanDist(testPos, testPost);
				combinedDistances += (MAX_DIST - dist);
			// Penalty for being too close
			if (dist < influence/3)
				combinedDistances -= influence/3;
		}
		combinedDistances += MAX_DIST - manhattanDist(homespace, testPos);
		return (int) (combinedDistances/(playerOutposts.size()))/2;
	}
	
	public double distance(Pair a, Pair b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}
	
	public double calculateOffensiveScore(Outpost movingPost, Pair testPos, int playerId, int influenceDist,ArrayList<Pair> intruderList) {
		//return 5*grid[testPos.x][testPos.y].landValue;
		double dis = influenceDist * 2;
		//double inf_range = 3*2*influenceDist;
		double inf_range = 3*influenceDist;
		double value = 0;
		double max_value = 0;
	/*	ArrayList<GridCell> influencedCells = getInfluencedCells(testPos, influenceDist);
		for (GridCell cell : influencedCells) {
			if (cell.owner == GridCell.NO_OWNER) {
				value++;
			}
		}*/
		//System.out.println("size of intruder: " + intruderList.size());
		for(int i = 0; i < intruderList.size(); i++)
		{
			value = 0;
	//		System.out.println("dis: " + distance(testPos, intruderList.get(i)));
			
			if (distance(intruderList.get(i), testPos) < inf_range) {
				//System.out.println("dis in range: " + distance(testPos, intruderList.get(i)));

				//System.out.println("testPos-x: " + testPos.x + ", y: " + testPos.y);
				//System.out.println("intruder x: " + intruderList.get(i).x + ", y: " + intruderList.get(i).y);

				//if (grid[intruderList.get(i).x][ intruderList.get(i).y].owner == GridCell.NO_OWNER) {
				if (grid[testPos.x][testPos.y].owner == GridCell.NO_OWNER) {

					//System.out.println("cell x: " + testPos.x + ", y: " + testPos.y + ", non_owner");
					value = 60 - distance(testPos, intruderList.get(i));
					//System.out.println("way 60");
					//return value;
						//System.out.println("neutral res becomes mine!");
					} 
						//else if (grid[intruderList.get(i).x][ intruderList.get(i).y].owner != playerId) {
				else if (grid[testPos.x][ testPos.y].owner != playerId) {
					//System.out.println("way 120");
							//System.out.println("not owned by our ourself");
							if(playerId == 0 && (intruderList.get(i).x < dis || intruderList.get(i).y < dis ))
							{
								value = 120 - distance(testPos, intruderList.get(i));
								//return value;
							}
							else if(playerId == 1 && (intruderList.get(i).x > BOARD_SIZE-dis || intruderList.get(i).y < dis ))
									{
								//System.out.println(" booundary");

								value = 120 - distance(testPos, intruderList.get(i));
								//return value;
									}
							else if(playerId == 3 && (intruderList.get(i).x < dis || intruderList.get(i).y > BOARD_SIZE-dis ))
									{
								value = 120 - distance(testPos, intruderList.get(i));
								//return value;
									}
							else if(playerId == 2 && (intruderList.get(i).x > BOARD_SIZE -dis || intruderList.get(i).y > BOARD_SIZE-dis ))
									{
										value = 120 - distance(testPos, intruderList.get(i));
										//return value;
									}
							//System.out.println("not boundary");

							else 
								{value = 100 - distance(testPos, intruderList.get(i));
								//System.out.println("way 100");
								}
							//System.out.println("!!!!value: " + value);
							if(value > max_value)
								{
								//System.out.println("large than max value!!!!!!!");
								max_value = value;
								}
								
							//return value;
					}
				}
			}
		
		return max_value;
	}
	
	public Resource getResources(int id) {
		Resource resourcesToReturn = new Resource(0,0);
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (grid[i][j].hasWater && grid[i][j].owner == id) {
					resourcesToReturn.water++;
				}
				else if (grid[i][j].owner == id) {
					resourcesToReturn.land++;
				}
			}
		}
		return resourcesToReturn;
	}
	
	public double getWaterResourceVal(Pair pr) {
		return grid[pr.x][pr.y].waterValue;
	}
	
	private ArrayList<GridCell> getInfluencedCells(Pair center, int influenceDistance) {
		Pair checkCell;
		ArrayList<GridCell> influencedCells = new ArrayList<GridCell>();
		for (int i = -1 * influenceDistance; i <= influenceDistance; i++) {
			for (int j = -1 * (influenceDistance - Math.abs(i)); j <= influenceDistance - Math.abs(i); j++) {
				checkCell = new Pair(center.x + i, center.y + j);
				if (validCellForAnalysis(checkCell)) {
					influencedCells.add(grid[checkCell.x][checkCell.y]);
				}
			}
		}
		return influencedCells;
	}
	
	public int manhattanDist(Pair pr1, Pair pr2) {
		return (Math.abs(pr1.x - pr2.x) + Math.abs(pr1.y - pr2.y));
	}
	
	public boolean validCellForAnalysis(int x, int y) {
		return validCellForAnalysis(new Pair(x,y));
	}
	
	public boolean validCellForAnalysis(Pair pair) {
		if (pair.x < 0 || pair.x >= BOARD_SIZE || pair.y < 0 || pair.y >= BOARD_SIZE)
			return false;
		else return true;
	}
	
	public boolean validCellForMoving(Pair pair) {
		if (!validCellForAnalysis(pair) || grid[pair.x][pair.y].hasWater)
			return false;
		return true;
	}

	public double getLandResourceVal(Pair pr) {
		return grid[pr.x][pr.y].landValue;
	}
}