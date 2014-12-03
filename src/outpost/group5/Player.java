// TODO: HashMap to cache dst and path, save reduncant path finding

package outpost.group5;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
	static int size = 100;
	static Point[][] grid = new Point[size][size];
	static boolean[][] gridOwnership = new boolean[size][size];
	boolean[][] availableWaterGrid = new boolean[size][size];
	static Random random = new Random();
	static int[] theta = new int[100];
	static int counter = 0;
	boolean initFlag = false;
	ArrayList<Pair> ourOutpostLst = new ArrayList<Pair>();
	Set<Pair> opponentOutpostSet = new HashSet<Pair>();
	ArrayList<Point> opponentBaseLst = new ArrayList<Point>();
	ArrayList<ArrayList<Point>> radianLsts = new ArrayList<ArrayList<Point>>();
	ArrayList<ArrayList<Point>> waterBodies = new ArrayList<ArrayList<Point>>();
	ArrayList<Point> harvesterSpotLst = new ArrayList<Point>();
	ArrayList<Point> targetLocationLst = new ArrayList<Point>();
	boolean firstReachedWater = false;
  HashMap<Point, List<Integer>> pointMap = new HashMap<Point, List<Integer>>();
  ArrayList<Point> cachedTargetLst= new ArrayList<Point>();
	static int numWater = 0, numLand = 0;
	Point ourBasePosition;
	int L, W, r, t;
	int ourLandCount, ourWaterCount;
  int tickCounter = 0;

	public Player(int id_in) {
		super(id_in);
		initFlag = true;
	}

	public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> outpostLsts, Point[] gridin, int r, int L, int W, int t) {
		// cell : Point (int x, int y, boolean water)
		// outpost : Pair (int x, int y)
		// move : movePair (int id, Pair pr)

		if (initFlag) {
			initFlag = false;
			this.L = L;
			this.W = W;
			this.r = r;
			this.t = t;
			parseGrid(gridin);
			updateOptimalHarvesterPositions();
			System.out.println("gridin size = " + gridin.length);
			int totalWater = 0;
			for (int i = 0; i < waterBodies.size(); i++) {
				totalWater += waterBodies.get(i).size();
				System.out.print("size = " + waterBodies.get(i).size() + ", ");
			}
			System.out.println("\nnumWater = " + numWater + ", totalWater = " + totalWater);
		}
    
    pointMap.clear();
		updateGridOwnership();
		updateOpponentOutpost(outpostLsts);
		ourOutpostLst = outpostLsts.get(this.id);
    for(int i = 0; i < ourOutpostLst.size(); ++i){
      Point p = grid[ourOutpostLst.get(i).x][ourOutpostLst.get(i).y];
      if(pointMap.containsKey(p)){
        pointMap.get(p).add(i);
      } else {
        List<Integer> newLst = new ArrayList<Integer>();
        newLst.add(i);
        pointMap.put(p, newLst);
      }
    }

		ArrayList<movePair> returnLst = new ArrayList<movePair>();
		/*
    getTargetLocations();
    if(tickCounter++ % 10 == 0){
      getTargetLocations();
      cachedTargetLst = targetLocationLst;
    } else {
      targetLocationLst = cachedTargetLst;
    }
    returnLst = fillTargetLocations();
    */
		int j = 0;
		for (int i = 0; i < ourOutpostLst.size(); ++i) {
			System.out.println("..");
			if (i >= harvesterSpotLst.size()) {
				break;
			}
			returnLst.add(moveTo(i, harvesterSpotLst.get(i)));
			if (distToBase(new Pair(harvesterSpotLst.get(j).x, harvesterSpotLst.get(j).y)) > size
					&& i + 1 < ourOutpostLst.size()) {
				System.out.println(i + 1 + ", " + harvesterSpotLst.get(j).x + 1 + ", " + harvesterSpotLst.get(j).y + 1);
				returnLst.add(moveTo(i + 1, grid[harvesterSpotLst.get(j).x + 1][harvesterSpotLst.get(j).y + 1]));
				i++;
			}
			j++;
		}
		/*
		 * // returnLst.add(moveTo(0, getOptimalWaterPoint(r))); if
		 * (ourOutpostLst.size() >= 4) { ArrayList<Integer> team = new
		 * ArrayList<Integer>(); team.add(1); team.add(2); // team.add(2); //
		 * team.add(3); returnLst = expendablesMove(team);
		 * returnLst.addAll(radianOutreach(ourOutpostLst, 3));
		 * 
		 * } else { returnLst.addAll(radianOutreach(ourOutpostLst, 1)); }
		 */
    /*
    if(returnLst.size() > 15){
      returnLst = (ArrayList<movePair>) returnLst.subList(0, returnLst.size() - 4);
      ArrayList<movePair> res = new ArrayList<movePair>();
		  int minDst = 2 * size;
		  Pair minPair = null;
		  for (Pair p : opponentOutpostSet) {
		  	if (distToBase(p) < minDst) {
		  		minPair = p;
		  		minDst = distToBase(p);
		  	}
		  }
      if(minPair != null){
        ArrayList<Point> vLst = killerSquadMove(grid[minPair.x][minPair.y]);
        for(int i = returnLst.size() - 5; i < returnLst.size(); ++i){
          res.add(moveTo(i, vLst.get(i - returnLst.size() + 5)));
        }
        returnLst.addAll(res);
      }
    }
    */
		return returnLst;
	}

	ArrayList<Point> floodHelper(boolean[][] visitedMap, int i, int j) {
		ArrayList<Point> res = new ArrayList<Point>();
		if (i < 0 || i > grid.length || j < 0 || j > grid.length || visitedMap[i][j] || !grid[i][j].water) {
			return res;
		}
		res.add(grid[i][j]);
		visitedMap[i][j] = true;
		res.addAll(floodHelper(visitedMap, i - 1, j));
		res.addAll(floodHelper(visitedMap, i + 1, j));
		res.addAll(floodHelper(visitedMap, i, j - 1));
		res.addAll(floodHelper(visitedMap, i, j + 1));
		return res;
	}

	public void getNumberOfWaterLandInQuarter() {
		for (int i = 0; i < size / 2; i++) {
			for (int j = 0; j < size / 2; j++) {
				if (grid[i][j].water == true)
					numWater++;
				else
					numLand++;
			}
		}
	}

	// =========================================================
	//
	// Utility logic
	//
	// =========================================================

	ArrayList<Point> assignTargetLocation() {
		ArrayList<Point> res = new ArrayList<Point>();
		if (ourOutpostLst.size() == 0) {
			System.out.println("our post size = 0");
			return res;
		}
		// assign outpost
		return res;
	}

	public Point realPoint(Pair p) {
		Pair realP = realPair(p);
		return grid[realP.x][realP.y];
	}

	public Pair realPair(Pair p) {
		switch (id) {
		case 0:
			return new Pair(p.x, p.y);
		case 1:
			return new Pair(size - p.x - 1, p.y);
		case 2:
			return new Pair(p.x, size - p.y - 1);
		case 3:
			return new Pair(size - p.x - 1, size - p.y - 1);
		}
		System.out.println("something is wroong in realPair");
		return new Pair(0, 0);
	}

	public void init() {
		// update opponent base position for suffocation muahaha
		System.out.println("size of the list: " + opponentBaseLst.size());
		opponentBaseLst.add(new Point(0, 0, false));
		opponentBaseLst.add(new Point(size - 1, 0, false));
		opponentBaseLst.add(new Point(size - 1, size - 1, false));
		opponentBaseLst.add(new Point(0, size - 1, false));
		// cache our base's location
		ourBasePosition = opponentBaseLst.get(id);

		opponentBaseLst.remove(id);
		System.out.println("-----");
		for (Point p : opponentBaseLst) {
			System.out.println(p.x + ":" + p.y);
		}
		System.out.println("-----");
	}

	void updateOptimalHarvesterPositions() {
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				availableWaterGrid[i][j] = grid[i][j].water;
			}
		}

		while (true) {
			int minDst = size * 2;
			Pair minPair = null;
			for (int i = 0; i < size; ++i) {
				for (int j = 0; j < size; ++j) {
					if (grid[i][j].water) {
						continue;
					}
					int tempWaterCount = getAreaWater(i, j);
					if (tempWaterCount >= W) {
						Pair temp = new Pair(i, j);
						int tmpDst = distToBase(temp) - tempWaterCount;
						if (tmpDst < minDst) {
							minDst = tmpDst;
							minPair = temp;
						}
					}
				}
			}
			if (minPair == null) {
				break;
			} else {
				harvesterSpotLst.add(grid[minPair.x][minPair.y]);
				flipAvailableWater(minPair.x, minPair.y);
			}
		}
		System.out.println("========================================");
		for (Point p : harvesterSpotLst) {
			System.out.println(p.x + ":" + p.y);
		}
	}

	void flipAvailableWater(int i, int j) {
		for (int x = i - r; x <= i + r; ++x) {
			for (int y = j - r; y <= j + r; ++y) {
				if (x < 0 || x >= size || y < 0 || y >= size || distance(grid[x][y], grid[i][j]) > r) {
					continue;
				}
				availableWaterGrid[x][y] = false;
			}
		}
	}

	int getAreaWater(int i, int j) {
		int waterCount = 0;
		for (int x = i - r; x <= i + r; ++x) {
			for (int y = j - r; y <= j + r; ++y) {
				if (x < 0 || x >= size || y < 0 || y >= size || distance(grid[x][y], grid[i][j]) > r
						|| !availableWaterGrid[x][y]) {
					continue;
				}
				waterCount++;
			}
		}
		return waterCount;
	}

	private void updateGridOwnership() {
		ourWaterCount = 0;
		ourLandCount = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				gridOwnership[i][j] = getPointOwnerShip(i, j);
			}
		}
	}

	public boolean getPointOwnerShip(int i, int j) {
		Point point = grid[i][j];
		ArrayList<Pair> point_list = point.ownerlist;
		if (point_list.size() != 1)
			return false;
		else if (ourOutpostLst.contains(point_list.get(0))) {
			if (point.water)
				ourWaterCount++;
			else
				ourLandCount++;
			return true;
		}
		return false;
	}

	static int distance(Point a, Point b) {
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	int distToBase(Pair p) {
		return distance(new Point(p.x, p.y, false), ourBasePosition);
	}

	// logic for remove over populated outpost
	// kill the youngest one
	public int delete(ArrayList<ArrayList<Pair>> outpostLsts, Point[] gridin) {
		return outpostLsts.get(id).size() - 1;
	}

	// convert grid into 2-D plane
	void parseGrid(Point[] gridin) {
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				grid[i][j] = gridin[i * size + j];
			}
		}
		System.out.println("Grid parsing finished");
	}

	// setup radian mesh
	// TODO: supporting line formation
	void populateRadianLsts(int r) {

		int boundary = size / 2;

		for (int i = 0; i < size; i += r / 2) {
			ArrayList<Point> newLst = new ArrayList<Point>();
			for (int j = 0; j <= i; j += r / 2) {
				newLst.add(realPoint(new Pair(boundary - i + j, boundary - j)));
			}
			radianLsts.add(newLst);
		}

	}

	void updateOpponentOutpost(ArrayList<ArrayList<Pair>> outpostLsts) {
		opponentOutpostSet.clear();
		for (int i = 0; i < outpostLsts.size(); ++i) {
			if (i == id)
				continue;
			ArrayList<Pair> tempLst = outpostLsts.get(i);
			for (Pair p : tempLst)
				opponentOutpostSet.add(p);
		}
		System.out.println("OpponentSet updated, total hostile outpost : " + opponentOutpostSet.size());
	}

	// basic path finding logic
	// TODO: implement priority queue to switch from BFS to A*
	movePair moveTo(int index, Point dst) {
		System.out.println("id:" + id);
		Pair src = ourOutpostLst.get(index);

		if (src.x == dst.x && src.y == dst.y) {
			System.out.println("dst reached");
			return new movePair(index, src);
		}
		// BFS for rechability finding
		int[][] visitedMap = new int[size][size];
		Queue<Pair> q = new LinkedList<Pair>();
		q.offer(src);
		visitedMap[src.x][src.y] = 1;
		boolean reachableFlag = false;
		Pair nearestLand = null;
		int nearestLandDst = 2 * size;
		while (q.size() > 0) {
			Pair curP = q.poll();
			ArrayList<Pair> nextHop = nextHopLst(curP);
			for (Pair p : nextHop) {
				if (!grid[p.x][p.y].water && visitedMap[p.x][p.y] == 0) {
					q.offer(p);
					visitedMap[p.x][p.y] = visitedMap[curP.x][curP.y] + 1;
					if (distance(new Point(p.x, p.y, false), dst) < nearestLandDst) {
						nearestLandDst = distance(new Point(p.x, p.y, false), dst);
						nearestLand = p;
					}
				}
				if (p.equals(new Pair(dst.x, dst.y))) {
					reachableFlag = true;
					break;
				}
			}
		}
		// reverse searching for path formation
		if (reachableFlag) {
			System.out.printf("a path is found for src: (%d, %d); dst: (%d, %d)\n", src.x, src.y, dst.x, dst.y);
			Pair prevP = new Pair(dst.x, dst.y);
			while (true) {
				ArrayList<Pair> prevHop = nextHopLst(prevP);
				for (Pair p : prevHop) {
					// System.out.print("(" + prevP.x + "," + prevP.y + ")");
					// reach src
					if (visitedMap[p.x][p.y] == 1) {
						System.out.println("returning from moveto");
						return new movePair(index, prevP);
					}
					if (visitedMap[p.x][p.y] == visitedMap[prevP.x][prevP.y] - 1) {
						prevP = p;
						break;
					}
				}
			}
		} else {
			System.out.printf("no path is found for src: (%d, %d); dst: (%d, %d)\n", src.x, src.y, dst.x, dst.y);
			if (nearestLand != null) {
				Pair prevP = new Pair(nearestLand.x, nearestLand.y);
				System.out.println("nearestLand x : " + nearestLand.x + "nearestland y: " + nearestLand.y);
				while (true) {
					ArrayList<Pair> prevHop = nextHopLst(prevP);
					for (Pair p : prevHop) {
						// reach nearestLand
						if (visitedMap[p.x][p.y] == 1) {
							return new movePair(index, prevP);
						}
						if (visitedMap[p.x][p.y] == visitedMap[prevP.x][prevP.y] - 1) {
							prevP = p;
							break;
						}
					}
				}
			}
		}
		System.out.printf(
				"no path is found for src: (%d, %d); dst: (%d, %d) and there is no nearest land, something is wrong\n",
				src.x, src.y, dst.x, dst.y);
		System.out.println("returning from moveto");
		return new movePair(index, src);
	}

	// get adjacent cells
	ArrayList<Pair> nextHopLst(Pair start) {
		ArrayList<Pair> prLst = new ArrayList<Pair>();
		for (int i = 0; i < 4; ++i) {
			Pair tmp0 = new Pair(start);
			Pair tmp = null;
			switch (i) {
			case 0:
				tmp = new Pair(tmp0.x - 1, tmp0.y);
				break;
			case 1:
				tmp = new Pair(tmp0.x + 1, tmp0.y);
				break;
			case 2:
				tmp = new Pair(tmp0.x, tmp0.y - 1);
				break;
			case 3:
				tmp = new Pair(tmp0.x, tmp0.y + 1);
				break;
			}
			if (tmp.x >= 0 && tmp.x < size && tmp.y >= 0 && tmp.y < size && !grid[tmp.x][tmp.y].water) {
				prLst.add(tmp);
			}
		}
		return prLst;
	}

	static Pair PointtoPair(Point pt) {
		return new Pair(pt.x, pt.y);
	}

	// =========================================================
	//
	// Game Logic
	//
	// =========================================================

	void getTargetLocations() {
		// develop our quandrant
		int quarterSize = harvesterSpotLst.size() / 4;
		if (ourOutpostLst.size() < quarterSize) {
			targetLocationLst = new ArrayList<Point>(harvesterSpotLst.subList(0, ourOutpostLst.size()));
		} else if (ourOutpostLst.size() < quarterSize + 4) {
			targetLocationLst = new ArrayList<Point>(harvesterSpotLst.subList(0, quarterSize));
			targetLocationLst.addAll(killerSquadMove(ourBasePosition));
		} else {
			targetLocationLst = new ArrayList<Point>(harvesterSpotLst.subList(0, quarterSize));
			targetLocationLst.addAll(killerSquadMove(ourBasePosition));
			targetLocationLst.addAll(harvesterSpotLst.subList(quarterSize, ourOutpostLst.size() - 4));
		}
		System.out.println("targetLocationLst size : " + targetLocationLst.size());
    for(Point p : targetLocationLst){
      System.out.println(p.x + ":" + p.y);
    }
	}
  
  ArrayList<movePair> fillTargetLocations(){
    ArrayList<movePair> res = new ArrayList<movePair>();
    ArrayList<Point> ourOutpostPointLst = new ArrayList<Point>();
    for(Pair p : ourOutpostLst){
      ourOutpostPointLst.add(grid[p.x][p.y]);
    }
    Set<Point> removeSet = new HashSet<Point>();
    for(Pair p : ourOutpostLst){
      if(targetLocationLst.contains(grid[p.x][p.y])){
        removeSet.add(grid[p.x][p.y]);
      }
    }
    targetLocationLst.removeAll(removeSet);
    ourOutpostPointLst.removeAll(removeSet);
    
    while(targetLocationLst.size() > 0){
      int minDst = size * 2;
      Point minOutpost = null;
      Point minTarget = null;
      for(Point p : targetLocationLst){
        for(Point q : ourOutpostPointLst){
          if(distance(p, q) < minDst){
            minDst = distance(p, q);
            minOutpost = q;
            minTarget = p;
          }
        }
      }
      if(minOutpost == null){
        break;
      }
      targetLocationLst.remove(minTarget);
      ourOutpostPointLst.remove(minOutpost);

      int idx = 0;
      if(pointMap.get(minOutpost).size() > 1){
        idx = pointMap.get(minOutpost).get(0).intValue();
        pointMap.get(minOutpost).remove(0);
      }
      res.add(moveTo(idx, minTarget));
    }
    return res;
  }

	ArrayList<Point> killerSquadMove(Point target) {
		ArrayList<Point> res = new ArrayList<Point>();
		res.add(grid[adjustPoint(target.x - r / 2)][adjustPoint(target.y)]);
		res.add(grid[adjustPoint(target.x + r / 2)][adjustPoint(target.y)]);
		res.add(grid[adjustPoint(target.x)][adjustPoint(target.y - r / 2)]);
		res.add(grid[adjustPoint(target.x)][adjustPoint(target.y + r / 2)]);
		int counter = ourOutpostLst.size() - harvesterSpotLst.size() / 4;
		counter = counter > 4 ? 4 : counter;
		System.out.println("Counter = " + counter);
		return new ArrayList<Point>(res.subList(0, counter));
	}

	int adjustPoint(int src) {
		if (src < 0)
			return 0;
		if (src >= size)
			return size;
		return src;
	}

	// team consists of 4 members in hope to make a surrounding of the left
	// along outpost from opponent
	// *
	// like this: *X*
	// *
	ArrayList<movePair> expendablesMove(List<Integer> team) {
		ArrayList<movePair> res = new ArrayList<movePair>();
		if (team.size() < 4) {
			return res;
		}
		int minDst = 2 * size;
		Pair minPair = null;
		for (Pair p : opponentOutpostSet) {
			if (distToBase(p) < minDst) {
				minPair = p;
				minDst = distToBase(p);
			}
		}
		if (minPair == null) {
			return res;
		}
		System.out.println("adding first point");
		res.add(moveTo(team.get(0), new Point(minPair.x - 1, minPair.y, false)));
		System.out.println("adding second point");
		res.add(moveTo(team.get(1), new Point(minPair.x + 1, minPair.y, false)));
		System.out.println("adding third point");
		res.add(moveTo(team.get(2), new Point(minPair.x, minPair.y - 1, false)));
		System.out.println("adding fourth point");
		res.add(moveTo(team.get(3), new Point(minPair.x, minPair.y + 1, false)));
		return res;
	}

}
