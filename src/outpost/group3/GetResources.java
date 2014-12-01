package outpost.group3;

import java.util.ArrayList;

import outpost.group3.Outpost;

public class GetResources extends outpost.group3.Strategy {
	GetResources() {}
	
	private boolean overlaps(Board board, Loc loc, ArrayList<Loc> targets) {
		for (Loc target : targets)
			if (Loc.mDistance(target, loc) < 2*board.r)
				return true;
		
		return false;
	}
	
	public void run(Board board, ArrayList<Outpost> outposts) {
		ArrayList<Loc> targets = new ArrayList<Loc>();
		int ticksRemainingInSeason = board.getTicksRemainingInSeason();
		
		// The fraction of the board we try to capture resources from is larger if we sense that we are farther ahead in the game (have move outposts)
		// Minimum we look at is our quarter (if we have <= average # of outposts)
		// Maximum we look at is entire board (if we have all outposts)
		double dimensionFactor = Math.max(0.5, Math.sqrt((double) board.numOutpostsFor(board.playerId) / (double) board.numOutpostsTotal()));
		
		for (Outpost outpost : outposts) {
			Loc currentLoc = outpost.getCurrentLoc();
			Cell currentCell = board.getCell(currentLoc);
			Loc bestLoc = null;
			int bestDist;
						
			// First, target the farthest cell (based on path distance) from the home cell that the outpost can reach before the season ends, supports as many or more than the cell it was last targeting, and does not overlap with targets already assigned to other outposts
			bestDist = 0;
			for (int x = 0; x < Board.dimension * dimensionFactor; x++) {
				for (int y = 0; y < Board.dimension * dimensionFactor; y++) {
					Loc loc = new Loc(x, y);
					Cell cell = board.getCell(loc);
					
					if (cell.isWater() ||
						board.numOutpostsSupportableOn(loc) == 0 ||
						(outpost.getTargetLoc() != null && board.numOutpostsSupportableOn(loc) < board.numOutpostsSupportableOn(outpost.getTargetLoc())) ||
						cell.getPathDistanceToHome(board.playerId) <= currentCell.getPathDistanceToHome(board.playerId) ||
						overlaps(board, loc, targets))
						continue;
					
					// Optimization: There is no need to calculate a path if the Manhattan distance is itself too far
					if (Loc.mDistance(currentLoc, loc) > ticksRemainingInSeason)
						continue;
					
					ArrayList<Loc> path = board.findPath(currentLoc, loc);
					
					if (path.size() - 1 > ticksRemainingInSeason)
						continue;
										
					if (cell.getPathDistanceToHome(board.playerId) > bestDist || (cell.getPathDistanceToHome(board.playerId) == bestDist && board.numOutpostsSupportableOn(loc) > board.numOutpostsSupportableOn(bestLoc))) {
						bestDist = cell.getPathDistanceToHome(board.playerId);
						bestLoc = loc;
					}
				}
			}
			
			if (bestLoc != null) {
				targets.add(bestLoc);
				outpost.setTargetLoc(bestLoc);
				continue;
			}
			
			// If no such cell was found and the current one supports some and is not overlapping, stay, put
			if (board.numOutpostsSupportableOn(currentLoc) >= 1 && !overlaps(board, currentLoc, targets)) {
				targets.add(currentLoc);
				outpost.setTargetLoc(currentLoc);
				continue;
			}
			
			// Otherwise, find the nearest one from the current location (based on path distance) that supports at least a full outpost
			bestDist = Integer.MAX_VALUE;
			for (int x = 0; x < Board.dimension * dimensionFactor; x++) {
				for (int y = 0; y < Board.dimension * dimensionFactor; y++) {
					Loc loc = new Loc(x, y);
					Cell cell = board.getCell(loc);
					
					if (cell.isWater() ||
						board.numOutpostsSupportableOn(loc) < 1 ||
						overlaps(board, loc, targets))
						continue;
					
					// Optimization: There is no need to calculate a path if the Manhattan distance is itself too far
					if (Loc.mDistance(currentLoc, loc) > bestDist)
						continue;
					
					ArrayList<Loc> path = board.findPath(currentLoc, loc);
					
					if (path.size() - 1 < bestDist || (path.size() - 1== bestDist && board.numOutpostsSupportableOn(loc) > board.numOutpostsSupportableOn(bestLoc))) {
						bestDist = path.size() - 1;
						bestLoc = loc;
					}
				}
			}
			
			if (bestLoc != null) {
				targets.add(bestLoc);
				outpost.setTargetLoc(bestLoc);
				continue;
			}
			
			// If we had a previous target, use that
			if (outpost.getTargetLoc() != null && !overlaps(board, outpost.getTargetLoc(), targets)) {
				targets.add(outpost.getTargetLoc());
				continue;
			}
			
			// No target was found, and we did not previously have one, so return this outpost to the general
			outpost.setStrategy(null);
		}
	}
}