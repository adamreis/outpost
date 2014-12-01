package outpost.group8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Strategy {
	
	
	
	public static ArrayList<movePair> initialDefense(
			List<Pair> myOutPosts,
			ArrayList<Location> openShore,
			int r,
			HashMap<Integer, Location> targets,
			HashSet<Integer[]> targetHistory) {
		ArrayList<movePair> returnlist = new ArrayList<movePair>();

		// If this is the first outpost, send it to the closest water.
		if (myOutPosts.size() == 1) {
			return targetWaterResources(myOutPosts,
					targets,
					openShore,
					targetHistory);
		}
		
		for (int i = 0; i < myOutPosts.size(); i++) {
			
			// Set up references
			Pair pOutpost = myOutPosts.get(i);
			Location outpost = new Location(pOutpost);
			Location dest = null;
			
			// Check if target is already locked (for the first 1, 2, or 3 outposts)
			if (targets.containsKey(i)) {
				dest = targets.get(i);
				System.out.println("Target locked. destination: " + dest.x + ", " + dest.y);

			}
			else {
				if (myOutPosts.size() == 2) {
					dest = new Location(0, 2*r, false);
				}
				else if (myOutPosts.size() == 3) {
					dest = new Location(2*r, 0, false);
				}
				else {
					dest = new Location(2*r, 2*r, false);
				}
				targets.put(i, dest);
				openShore.remove(dest);
				targetHistory.add(Player.arrayify(outpost.x, outpost.y));
				
			}
			Location step = generateNextStep(pOutpost, dest, outpost);
			returnlist.add(new movePair(i, new Pair(step.x, step.y)));
		}
		return returnlist;
	}
	
	public static Location generateNextStep(Pair pOutpost, Location dest, Location outpost) {
		try {
			Location step = PlayerUtil.movePairToDFS(pOutpost, new Point(dest.x, dest.y, Global.grid[dest.x][dest.y].water)).get(0);
			System.out.println("next step for " + outpost.x + ", " + outpost.y + ": " + step.x + ", " + step.y);
			return step;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Destination cannot be water");
			e.printStackTrace();
			return null;
		}
	}
	

	public static ArrayList<movePair> targetWaterResources(
			List<Pair> myOutPosts,
			HashMap<Integer, Location> targets,
			ArrayList<Location> openShore,
			HashSet<Integer[]> targetHistory) {
		// TODO Auto-generated method stub
		// Target water resources;
		ArrayList<movePair> returnlist = new ArrayList<movePair>();
		for (int i = 0; i < myOutPosts.size(); i++) {
			Pair pOutpost = myOutPosts.get(i);
		
			// If it's already at its destination, keep it there.
			Location outpost = new Location(pOutpost);
			/*if ()
			if (PlayerUtil.arrayListContainsLocation(openShore, outpost)) {
				targets.put(i, outpost);
				targetHistory.add(arrayify(new Integer(outpost.x), new Integer(outpost.y)));
				returnlist.add(new movePair(i, pOutpost));
			}*/
			// We already have a target locked for this outpost
			if (targets.containsKey(i)) {
				Location dest = targets.get(i);
				System.out.println("Target locked. destination: " + dest.x + ", " + dest.y);
				Location step = null;
				try {
					
						step = PlayerUtil.movePairToDFS(pOutpost, new Point(dest.x, dest.y, Global.grid[dest.x][dest.y].water)).get(0);
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Destination cannot be water");
					e.printStackTrace();
				}
				returnlist.add(new movePair(i, new Pair(step.x, step.y)));
			}
			// This is a new outpost - we need to give it a target piece of shoreline.
			else {
				Location dest = new Location(0, 0, false); // dummy value
				double shortestDist = Double.MAX_VALUE;
				for (Location possDest : openShore) {
					// Look through all pieces of shore that are not occupied currently
					// AND are not the intended destination of an existing outpost.
					// Find the closest one.
                    //
                    if (Player.targetHistoryContains(possDest.x, possDest.y, targetHistory)) continue;
                    if (PlayerUtil.hashMapContainsLocationAsValue(targets, possDest)) continue;
                    if (Global.grid[possDest.x][possDest.y].water == false) {
                        double dist = PlayerUtil.manhattanDistance(possDest, outpost);
                        if (dist < shortestDist && dist > 10) {
                            dest = possDest;
                            shortestDist = dist;
                        }
                    }
				}
				// Choose the closest piece of unoccupied shore.
				System.out.println("destination chosen for outpost at " + outpost.x + ", " + outpost.y + 
						": " + dest.x + ", " + dest.y);
				targets.put(i, dest);
				openShore.remove(dest);
				targetHistory.add(Player.arrayify(outpost.x, outpost.y));
				Location step = null;
				try {
					
					if (i == myOutPosts.size()-1 && i > 5){
						Pair p = PlayerUtil.getStartPair(Global.idCurrent, 100);
						step = PlayerUtil.movePairToDFS(pOutpost, new Point(p.x, p.y, false)).get(0);
					}else {
					
						step = PlayerUtil.movePairToDFS(pOutpost, new Point(dest.x, dest.y, Global.grid[dest.x][dest.y].water)).get(0);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Destination cannot be water");
					e.printStackTrace();
					
				}
				System.out.println("next step for " + outpost.x + ", " + outpost.y + ": " + step.x + ", " + step.y);
				returnlist.add(new movePair(i, new Pair(step.x, step.y)));
			}
		
		}
		return returnlist;
	}

	
	public static ArrayList<movePair> targetWaterResourcesDouble(
			List<Pair> myOutPosts,
			HashMap<Integer, Location> targets,
			ArrayList<Location> openShore,
			HashSet<Integer[]> targetHistory) {
		// TODO Auto-generated method stub
		// Target water resources;
		ArrayList<movePair> returnlist = new ArrayList<movePair>();
		for (int i = 0; i < myOutPosts.size(); i += 2) {
			Pair pOutpost = myOutPosts.get(i);
		
			// If it's already at its destination, keep it there.
			Location outpost = new Location(pOutpost);
			/*if ()
			if (PlayerUtil.arrayListContainsLocation(openShore, outpost)) {
				targets.put(i, outpost);
				targetHistory.add(arrayify(new Integer(outpost.x), new Integer(outpost.y)));
				returnlist.add(new movePair(i, pOutpost));
			}*/
			// We already have a target locked for this outpost
			if (targets.containsKey(i)) {
				Location dest = targets.get(i);
				System.out.println("Target locked. destination: " + dest.x + ", " + dest.y);
				Location step = null;
				try {
					
						step = PlayerUtil.movePairToDFS(pOutpost, new Point(dest.x, dest.y, Global.grid[dest.x][dest.y].water)).get(0);
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Destination cannot be water");
					e.printStackTrace();
				}
				returnlist.add(new movePair(i, new Pair(step.x, step.y)));
				returnlist.add(new movePair(i+1, new Pair(step.x, step.y)));
				
			}
			// This is a new outpost - we need to give it a target piece of shoreline.
			else {
				Location dest = new Location(0, 0, false); // dummy value
				double shortestDist = Double.MAX_VALUE;
				for (Location possDest : openShore) {
					// Look through all pieces of shore that are not occupied currently
					// AND are not the intended destination of an existing outpost.
					// Find the closest one.
                    //
                    if (Player.targetHistoryContains(possDest.x, possDest.y, targetHistory)) continue;
                    if (PlayerUtil.hashMapContainsLocationAsValue(targets, possDest)) continue;
                    if (Global.grid[possDest.x][possDest.y].water == false) {
                        double dist = PlayerUtil.manhattanDistance(possDest, outpost);
                        if (dist < shortestDist && dist > 10) {
                            dest = possDest;
                            shortestDist = dist;
                        }
                    }
				}
				// Choose the closest piece of unoccupied shore.
				System.out.println("destination chosen for outpost at " + outpost.x + ", " + outpost.y + 
						": " + dest.x + ", " + dest.y);
				targets.put(i, dest);
				openShore.remove(dest);
				targetHistory.add(Player.arrayify(outpost.x, outpost.y));
				Location step = null;
				try {
					
					if (i == myOutPosts.size()-1 && i > 5){
						Pair p = PlayerUtil.getStartPair(Global.idCurrent, 100);
						step = PlayerUtil.movePairToDFS(pOutpost, new Point(p.x, p.y, false)).get(0);
					}else {
					
						step = PlayerUtil.movePairToDFS(pOutpost, new Point(dest.x, dest.y, Global.grid[dest.x][dest.y].water)).get(0);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Destination cannot be water");
					e.printStackTrace();
					
				}
				System.out.println("next step for " + outpost.x + ", " + outpost.y + ": " + step.x + ", " + step.y);
				returnlist.add(new movePair(i, new Pair(step.x, step.y)));
				returnlist.add(new movePair(i+1, new Pair(step.x, step.y)));
			}
		
		}
		return returnlist;
	}
	
	
	
	
	public static ArrayList<movePair> attackWater(
			List<Pair> myOutPosts,
			HashSet<Location> shorePoints) {
		// TODO Auto-generated method stub
		ArrayList<movePair> returnlist = new ArrayList<movePair>();
		Location followLocation = null;
		movePair next = null;
		int [][] moves = {{-1, 0}, {1, 0}, {0,1}, {0, -1}};
		for (int i = 0 ; i < myOutPosts.size() ; i++ ) {
			Pair pair = myOutPosts.get(i);
			Location temp = PlayerUtil.getClosestShorePoint(i, myOutPosts, shorePoints);
			try {
				/*
				if (i == myOutPosts.size()-1 && i > 5){
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(0, 0,false)).get(0);
				}else {
					*/
				followLocation = PlayerUtil.movePairToDFS(pair, new Point(temp.x, temp.y, false)).get(0);
				//}
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			pair = new Pair(followLocation.x, followLocation.y); 
			next = new movePair(i, pair);
			returnlist.add(next);
		}
		return returnlist;
	}

	
	
	// We call this if the number of outposts more than 10, last 6 outposts protect the home.
	public static ArrayList<movePair> defenseHome(
			List<Pair> myOutPosts,
			HashSet<Location> shorePoints,
			int id,
			HashMap<Integer, Location> targets,
			ArrayList<Location> openShore,
			HashSet<Integer[]> targetHistory) {
		// TODO Auto-generated method stub
		ArrayList<movePair> returnlist = new ArrayList<movePair>();
		
		Location followLocation = null;
		movePair next = null;
		int [][] moves = {{-1, 0}, {1, 0}, {0,1}, {0, -1}};
		// x = move[0], y = move[1]
		int [] move = PlayerUtil.getPossibleAwayEdgeMoves(id);
		returnlist = targetWaterResources(myOutPosts, targets, openShore, targetHistory);
		Pair homeBase = PlayerUtil.getStartPair(id, 100);
		
		for (int i = 0 ; i < 1 ; i++ ) {
			Pair pair = myOutPosts.get(i);
			//Location temp = PlayerUtil.getClosestShorePoint(i, myOutPosts, shorePoints);
			try {
				/*
				if (i == myOutPosts.size()-1 && i > 5){
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(0, 0,false)).get(0);
				}else {
					*/
				// we need error check to make sure it is not water
				followLocation = PlayerUtil.movePairToDFS(pair, new Point(homeBase.x+move[0], homeBase.y, false)).get(0);
				//}
				pair = new Pair(followLocation.x, followLocation.y); 
				next = new movePair(i, pair);
				returnlist.set(i, next);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pair = myOutPosts.get(i+1);
				/*
				if (i == myOutPosts.size()-1 && i > 5){
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(0, 0,false)).get(0);
				}else {
					*/
				// we need error check to make sure it is not water
				
				followLocation = PlayerUtil.movePairToDFS(pair, new Point(homeBase.x, homeBase.y+move[1], false)).get(0);
				//}
				pair = new Pair(followLocation.x, followLocation.y); 
				next = new movePair(i, pair);
				returnlist.set(i+1, next);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pair = myOutPosts.get(i+2);
				/*
				if (i == myOutPosts.size()-1 && i > 5){
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(0, 0,false)).get(0);
				}else {
					*/
				// we need error check to make sure it is not water
				followLocation = PlayerUtil.movePairToDFS(pair, new Point(homeBase.x+move[0], homeBase.y+move[1], false)).get(0);
				//}
				pair = new Pair(followLocation.x, followLocation.y); 
				next = new movePair(i, pair);
				returnlist.set(i+2, next);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pair = myOutPosts.get(i+3);
				/*
				if (i == myOutPosts.size()-1 && i > 5){
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(0, 0,false)).get(0);
				}else {
					*/
				// we need error check to make sure it is not water
				
				followLocation = PlayerUtil.movePairToDFS(pair, new Point(homeBase.x+move[0]+move[0], homeBase.y+move[1]+move[1], false)).get(0);
				//}
				pair = new Pair(followLocation.x, followLocation.y); 
				next = new movePair(i, pair);
				returnlist.set(i+3, next);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pair = myOutPosts.get(i+4);
				/*
				if (i == myOutPosts.size()-1 && i > 5){
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(0, 0,false)).get(0);
				}else {
					*/
				// we need error check to make sure it is not water
				followLocation = PlayerUtil.movePairToDFS(pair, new Point(homeBase.x+move[0], homeBase.y+move[1]+move[1], false)).get(0);
				//}
				pair = new Pair(followLocation.x, followLocation.y); 
				next = new movePair(i, pair);
				returnlist.set(i+4, next);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		return returnlist;
	}
	
	public static ArrayList<movePair> angularExpansion(
			List<Pair> myOutPosts,
			HashSet<Location> shorePoints,
			int r) {
		// TODO Auto-generated method stub
		ArrayList<movePair> returnlist = new ArrayList<movePair>();
		Location followLocation = null;
		movePair next = null;
		double granularity = r * myOutPosts.size()*10;
		int [] angles = new int [myOutPosts.size()];
		int delta= 90/(myOutPosts.size()+1);
		angles[0] = delta;
		Arrays.fill(angles, 0);
		for (int i = 1 ; i < myOutPosts.size() ; i++ ) {
			angles[i] = angles[i-1] + delta;
		}
		for (int i = 0 ; i < myOutPosts.size() ; i++ ) {
			int deltaX = (int)(Math.cos(Math.toRadians(angles[i])) * granularity);
			int deltaY = (int)(Math.sin(Math.toRadians(angles[i])) * granularity);
			Location temp = null;
			if (!PlayerUtil.isValidBoundry(deltaX, deltaY)) {
				
				for (Location loc : shorePoints) {
					boolean flag = false;
					for (Pair p : myOutPosts) {
						
						if (PlayerUtil.manhattanDistance(p, loc) < 2.5*r){
							flag = true;
							break;
						} 
						
					}
					if (!flag) {
						temp = loc;
						break;
					}
					
				}
				
				
				//returnlist.add(Strategy.targetWaterResources(myOutPosts, targets, openShore, targetHistory).get(i));
				
				
			} else {
				temp = Global.grid[deltaX][deltaY];
				temp = PlayerUtil.getShoreFromWater(temp);
			}
			
			Pair pair = myOutPosts.get(i);
			try {
				if (i == myOutPosts.size()-1 && i > 5){
					
					Pair p = PlayerUtil.getStartPair(Global.idCurrent, 100);
					
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(p.x, p.y,false)).get(0);
				} else {
					followLocation = PlayerUtil.movePairToDFS(pair, new Point(temp.x, temp.y,false)).get(0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Location closeWater = new Location(closestWater);
			pair = new Pair(followLocation.x, followLocation.y); //pair = new Pair(pair.x, pair.y-1);
			next = new movePair(i, pair);
			returnlist.add(next);
			
			
		}
		
		return returnlist;
	}

}
