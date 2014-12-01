package outpost.group7;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
	static int size = 100;
	static Point[] grid = new Point[size*size];
	static Random random = new Random();
	static int counter = 0;

	////////////////////////////////////////////////////////////////////
	static Pair HOME_CELL;
	static int playerId;
	static Direction X_AWAY;
	static Direction X_BACK;
	static Direction Y_AWAY;
	static Direction Y_BACK;
	static Direction STAY;
	static int R;
	static int L;
	static int W;
	static int T;
	static ArrayList<Outpost> myOutposts;
	static Mastermind mastermind;
	static boolean tenthTurn = false;
	////////////////////////////////////////////////////////////////////

	double LACK_WATER;
	double LACK_LAND;
	int[][] water = new int[size][size];
	int[][] land = new int[size][size];


	////////////////////////////////////////////////////////////////////
	enum Strategy {
		PEEL,
		ARMY
	}

	class Mastermind {
		public Strategy strategy;

		public Mastermind(Strategy strategy) {
			this.strategy = strategy;
		}

		public void changeStrategy(Strategy newStrategy) {
			strategy = newStrategy;
		}

		public void dispatch() {
			if (strategy == Strategy.PEEL) {
				System.out.println("Strategy is PEEL");
				for (Outpost outpost : myOutposts) {
					Stack<Direction> moves = new Stack<Direction>();
					int i;
					for (i = 0; i < outpost.id; i++) {
						if (outpost.id % 2 == 0) {
							moves.push(X_AWAY);
						}
						else {
							moves.push(Y_AWAY);
						}
					}
					while (i < 10) {
						if (outpost.id % 2 == 0) {
							moves.push(Y_AWAY);
						}
						else {
							moves.push(X_AWAY);
						}
						i++;
					}
					outpost.assignMoves(moves);
				}
			}
			if (strategy == Strategy.ARMY) {
				// general strategy
				// (1) generate bestPositions according to weights
				computeResources(myOutposts);
				//System.out.printf("LACKWATER = %f, LACKLAND = %f\n", LACK_WATER, LACK_LAND);
				ArrayList<Pair> bestPositions = findBestPositions(myOutposts);

				// (2) assign bestPositions as target positions to outposts
				// currently assigned to the nearest outpost based on manhattan distance
				for (Outpost outpost : myOutposts) {
					outpost.target = null;
				}
				for (Pair position : bestPositions) {
					int minDist = Integer.MAX_VALUE;
					int oid = -1;
					for (int i = 0; i < myOutposts.size(); ++i) {
						Outpost outpost = myOutposts.get(i);
						if (outpost.target != null)
							continue;
						int dist = manhattanDistance(position, outpost.position);
						if (dist < minDist) {
							minDist = dist;
							oid = i;
						}
					}
					myOutposts.get(oid).target = new Pair(position);
				}
				/*for (Outpost outpost : myOutposts) {
					System.out.printf("outpost target: (%d, %d) -- (%d, %d)\n", outpost.position.x, outpost.position.y, outpost.target.x, outpost.target.y);
					System.out.printf("water = %d, land = %d\n", water[outpost.target.x][outpost.target.y], land[outpost.target.x][outpost.target.y]);
				}*/

				// soldier strategy
				for (Outpost outpost : myOutposts) {
					Stack<Direction> moves = new Stack<Direction>();
					if (outpost.target.equals(outpost.position)) {
						moves.push(STAY);
					}
					else {
						// perform BFS, ignoring other players' outposts
						// target cannot be a water cell
						int[] cx = {-1, 0, 1, 0};
						int[] cy = {0, -1, 0, 1};
						Queue<Pair> q = new LinkedList<Pair>();
						q.add(outpost.position);
						boolean[][] vst = new boolean[size][size];
						int[][] parentx = new int[size][size];
						int[][] parenty = new int[size][size];
						for (int i = 0; i < size; ++i) {
							for (int j = 0; j < size; ++j) {
								vst[i][j] = false;
								parentx[i][j] = -1;
								parenty[i][j] = -1;
							}
						}
						vst[outpost.position.x][outpost.position.y] = true;
						while (!q.isEmpty()) {
							Pair p = new Pair(q.peek());
							q.poll();
							for (int i = 0; i < 4; ++i) {
								int x = p.x + cx[i];
								int y = p.y + cy[i];
								if (x < 0 || x >= size || y < 0 || y >= size || vst[x][y] || grid[x * size + y].water)
									continue;
								vst[x][y] = true;
								parentx[x][y] = p.x;
								parenty[x][y] = p.y;
								q.add(new Pair(x, y));
								if (x == outpost.target.x && y == outpost.target.y) {
									while (!(parentx[x][y] == outpost.position.x && parenty[x][y] == outpost.position.y)) {
										int newx = parentx[x][y];
										int newy = parenty[x][y];
										x = newx;
										y = newy;
									}
									moves.push(toDirection(x - outpost.position.x, y - outpost.position.y));
									q.clear();
									break;
								}
							}
						}
					}
					outpost.assignMoves(moves);
				}
			}
		}

		// find best positions based on map and resources
		// weight the cells to get the best
		public ArrayList<Pair> findBestPositions(ArrayList<Outpost> outposts) {
			int n = outposts.size();
			ArrayList<Pair> positions = new ArrayList<Pair>();
			/*
			PriorityQueue<Position> queue = new PriorityQueue<Position>(n, new PositionComparator());
			for (int i = 0; i < size; i += R) {
				for (int j = 0; j < size; j += R) {
					Position pos = new Position(i, j);
					if (isInWater(pos.toPair())) continue;
					if (LACK_WATER < 0) {
						//pos.score = water[i][j];
						if (water[i][j] + LACK_WATER >= -1e-6) pos.score = 1;
					}
					else if (LACK_LAND < 0) {
						//pos.score = land[i][j];
						if (land[i][j] + LACK_LAND >= -1e-6) pos.score = 1;
					}
					else {
						//pos.score = (water[i][j] + land[i][j]);
						pos.score = 1;
					}
					queue.add(pos);
					if (queue.size() > n)
						queue.poll();
				}
			}
			ArrayList<Pair> positions = new ArrayList<Pair>();
			for (Position pos : queue) {
				positions.add(pos.toPair());
			}*/
			if (LACK_WATER < 0) {
				for (Outpost outpost : myOutposts) {
					if (hasWater(outpost.position)) {
						int[] cx = {0, -1, 0, 1, 0};
						int[] cy = {0, 0, 1, 0, -1};
						int w = -1;
						int x = -1;
						int y = -1;
						for (int i = 0; i < 5; ++i) {
							int xx = outpost.position.x + cx[i];
							int yy = outpost.position.y + cy[i];
							if (xx < 0 || xx >= size || yy < 0 || yy >= size)
								continue;
							if (!grid[xx * size + yy].water && water[xx][yy] > w) {
								w = water[xx][yy];
								x = xx;
								y = yy;
							}
						}
						int t;
						for (t = 0; t < positions.size(); ++t) {
							if (overlap(positions.get(t), new Pair(x, y)))
								break;
						}
						if (t < positions.size()) continue;
						positions.add(new Pair(x, y));
					}
				}
				int tmp = positions.size();
				int rem = n - positions.size();
				// greedy select n largest water cell
				for (int k = 0; k < rem; ++k) {
					int w = -1;
					int x = -1;
					int y = -1;
					for (int i = 0; i < size; ++i) {
						for (int j = 0; j < size; ++j) {
							if (grid[i * size + j].water || water[i][j] < w) continue;
							int t;
							for (t = 0; t < tmp + k; ++t) {
								if (overlap(positions.get(t), new Pair(i, j)))
									break;
							}
							if (t < tmp + k) continue;
							if (water[i][j] > w) {
								w = water[i][j];
								x = i;
								y = j;
							}
						}
					}
					positions.add(new Pair(x, y));
				}
			}
			else if (LACK_LAND < 0) {
				for (Outpost outpost : myOutposts) {
					if (land[outpost.position.x][outpost.position.y] > 0) {
						int[] cx = {-1, 0, 1, 0, 0};
						int[] cy = {0, 1, 0, -1, 0};
						int w = -1;
						int x = -1;
						int y = -1;
						for (int i = 0; i < 5; ++i) {
							int xx = outpost.position.x + cx[i];
							int yy = outpost.position.y + cy[i];
							if (xx < 0 || xx >= size || yy < 0 || yy >= size)
								continue;
							if (!grid[xx * size + yy].water && water[xx][yy] > w) {
								w = water[xx][yy];
								x = xx;
								y = yy;
							}
						}
						int t;
						for (t = 0; t < positions.size(); ++t) {
							if (overlap(positions.get(t), new Pair(x, y)))
								break;
						}
						if (t < positions.size()) continue;
						positions.add(new Pair(x, y));
					}
				}
				int tmp = positions.size();
				int rem = n - positions.size();
				// greedy select n largest land cell
				for (int k = 0; k < rem; ++k) {
					int w = -1;
					int x = -1;
					int y = -1;
					for (int i = 0; i < size; ++i) {
						for (int j = 0; j < size; ++j) {
							if (grid[i * size + j].water || land[i][j] < w) continue;
							int t;
							for (t = 0; t < k + tmp; ++t) {
								if (overlap(positions.get(t), new Pair(i, j)))
									break;
							}
							if (t < k + tmp) continue;
							if (land[i][j] > w) {
								w = land[i][j];
								x = i;
								y = j;
							}
						}
					}
					positions.add(new Pair(x, y));
				}
			}
			else {
				for (Outpost outpost : myOutposts) {
					if (hasWater(outpost.position)) {
						int[] cx = {0, -1, 0, 1, 0};
						int[] cy = {0, 0, 1, 0, -1};
						int w = -1;
						int x = -1;
						int y = -1;
						for (int i = 0; i < 5; ++i) {
							int xx = outpost.position.x + cx[i];
							int yy = outpost.position.y + cy[i];
							if (xx < 0 || xx >= size || yy < 0 || yy >= size)
								continue;
							if (!grid[xx * size + yy].water && water[xx][yy] > w) {
								w = water[xx][yy];
								x = xx;
								y = yy;
							}
						}
						int t;
						for (t = 0; t < positions.size(); ++t) {
							if (overlap(positions.get(t), new Pair(x, y)))
								break;
						}
						if (t < positions.size()) continue;
						positions.add(new Pair(x, y));
					}
				}
				int tmp = positions.size();
				int rem = n - positions.size();
				// greedy select n largest (water + land) cell
			int cnt = 0;
			int start = 0;
			while (cnt < rem) {
				int row = 0;
				int col;
				col = start;
				while (col > 0 && cnt < rem) {
					int newXPos, newYPos;
					if( X_AWAY == Direction.RIGHT ) {
						newXPos = R * row;
					} else {
						newXPos = size-1 - R * row;
					}
					if( Y_AWAY == Direction.DOWN ) {
						newYPos = R * col;
					} else {
						newYPos = size-1 - R * col;
					}
					Pair newPair = new Pair(newXPos, newYPos);
					if(!grid[newXPos * size + newYPos].water) {
						//int t;
						//for (t = 0; t < positions.size(); ++t) {
						//	if (overlap(positions.get(t), newPair))
						//		break;
						//}
						//if (t >= positions.size()) {
						++cnt;
						positions.add(newPair);
						//}
					}
					++row;
					--col;
				}
				++start;
			}
				/*
				for (int k = 0; k < rem; ++k) {
					double w = -1;
					int x = -1;
					int y = -1;
					for (int ii = 0; ii < size; ++ii) {
						for (int jj = 0; jj < size; ++jj) {
							int i = ii + size / 2;
							int j = jj + size / 2;
							if (i >= size)
								i -= size;
							if (j >= size)
								j -= size;
							//i = ii;
							//j = jj;
							if (grid[i * size + j].water || (water[i][j] + land[i][j]) < w) continue;
							int t;
							for (t = 0; t < tmp + k; ++t) {
								if (overlap(positions.get(t), new Pair(i, j)))
									break;
							}
							if (t < tmp + k) continue;
							if ((water[i][j] + land[i][j]) > w) {
								w = (water[i][j] + land[i][j]);
								x = i;
								y = j;
							}
						}
					}
					positions.add(new Pair(x, y));
				}
				*/
			}
			Collections.sort(positions, new PairComparator());
			return positions;
		}

		boolean hasWater(Pair a) {
			if (water[a.x][a.y] > 0)
				return true;
			else
				return false;
				/*
			for (int i = a.x - R; i <= a.x + R; ++i) {
				for (int j = a.y - R; j <= a.y + R; ++j) {
					if (manhattanDistance(a, new Pair(i, j)) > R) continue;
					if (i < 0 || i >= size || j < 0 || j >= size) continue;
					if (grid[i * size + j].water)
						return true;
				}
			}
			return false;
			*/
		}

		boolean overlap(Pair a, Pair b) {
			for (int i = a.x - R; i <= a.x + R; ++i) {
				for (int j = a.y - R; j <= a.y + R; ++j) {
					if (manhattanDistance(a, new Pair(i, j)) > R) continue;
					if (manhattanDistance(b, new Pair(i, j)) <= R)
						return true;
				}
			}
			return false;
		}

		// compute owned resources
		public void computeResources(ArrayList<Outpost> outposts) {
			double landNeeded = outposts.size() * L;
			double waterNeeded = outposts.size() * W;
			double waterOwned = 0;
			double landOwned = 0;
			for (int i = 0; i < size; ++i) {
				for (int j = 0; j < size; ++j) {
					Point p = grid[i * size + j];
					for (int f = 0; f < p.ownerlist.size(); ++f) {
						if (p.ownerlist.get(f).x == playerId) {
							if (p.water) {
								waterOwned += 1/p.ownerlist.size(); // as in the simulator
							}
							else {
								landOwned += 1/p.ownerlist.size();
							}
						}
					}
				}
			}
			LACK_WATER = waterOwned - waterNeeded;
			LACK_LAND = landOwned - landNeeded;
		}

		// old version findBestPositions (Grid from home cell)
		public ArrayList<Pair> findBestPositionsGrid(ArrayList<Outpost> outposts) {
			int n = outposts.size();
			ArrayList<Pair> positions = new ArrayList<Pair>();
			int cnt = 0;
			int start = 0;
			while (cnt < n) {
				int row = 0;
				int col;
				col = start;
				while (col > 0 && cnt < n) {
					int newXPos, newYPos;
					if( X_AWAY == Direction.RIGHT ) {
						newXPos = R * row;
					} else {
						newXPos = size-1 - R * row;
					}
					if( Y_AWAY == Direction.DOWN ) {
						newYPos = R * col;
					} else {
						newYPos = size-1 - R * col;
					}
					Pair newPair = new Pair(newXPos, newYPos);
					if( /*! isInWater(newPair)*/ true ) {
						++cnt;

						double distanceFromBase = manhattanDistance(newPair, HOME_CELL);
						int i;
						for (i = 0; i < positions.size(); i++) {
							double otherDistance = manhattanDistance(positions.get(i), HOME_CELL);
							if (distanceFromBase > otherDistance) {
								break;
							}
						}
						positions.add(i, newPair);
					}
					++row;
					--col;
				}
				++start;
			}
			return positions;
		}
	}

	static int manhattanDistance(Pair a, Pair b) {
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	static double euclidDistance(Pair a, Pair b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	public boolean isInWater(Pair p) {
		return grid[p.x * size + p.y ].water;
	}

	Direction toDirection(int x, int y) {
		if (x == -1 && y == 0)
			return Direction.LEFT;
		else if (x == 1 && y == 0)
			return Direction.RIGHT;
		else if (x == 0 && y == -1)
			return Direction.UP;
		else if (x == 0 && y == 1)
			return Direction.DOWN;
		return Direction.STAY;
	}
	////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////
	enum Direction {
		LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1), STAY(0, 0);
		public int dx;
		public int dy;

		Direction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
	}

	class Outpost {
		public int id;
		public Pair position;
		public Pair target;
		public Stack<Direction> moves;
		public boolean deleted;

		public Outpost(int id) {
			this.id = id;
			position = HOME_CELL;
			deleted = false;
		}

		public void updatePosition(Pair newPosition) {
			position = newPosition;
		}

		public void assignMoves(Stack<Direction> moves) {
			this.moves = moves;
		}

		public Direction move() {
			return moves.pop();
		}
	}

	class PairComparator implements Comparator<Pair> {
		@Override
		public int compare (Pair a, Pair b) {
			int d3 = manhattanDistance(new Pair(a.x, a.y), HOME_CELL);
			int d4 = manhattanDistance(new Pair(b.x, b.y), HOME_CELL);
			if (d3 == d4) {
				double d1 = euclidDistance(new Pair(a.x, a.y), new Pair(50, 50));
				double d2 = euclidDistance(new Pair(b.x, b.y), new Pair(50, 50));
				return Double.compare(d2, d1);
			}
			return d4 - d3;
		}
	}

	class Position {
		int x;
		int y;
		int score;

		Position() {
			x = HOME_CELL.x;
			y = HOME_CELL.y;
			score = 0;
		}
		Position(Pair p) {
			x = p.x;
			y = p.y;
			score = 0;
		}
		Position(int x, int y) {
			this.x = x;
			this.y = y;
			score = 0;
		}
		Position(int x, int y, int score) {
			this.x = x;
			this.y = y;
			this.score = score;
		}

		public Pair toPair() {
			return new Pair(x, y);
		}
	}

	class PositionComparator implements Comparator<Position> {
		@Override
		public int compare (Position a, Position b) {
			if (a.score == b.score) {
				return manhattanDistance(new Pair(b.x, b.y), HOME_CELL) - manhattanDistance(new Pair(a.x, a.y), HOME_CELL);
			}
			return a.score - b.score;
		}
	}

	////////////////////////////////////////////////////////////////////

	public Player(int id_in) {
		super(id_in);
	}

	public void init() {
	}

	public int delete(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin) {
		int del = random.nextInt(king_outpostlist.get(id).size());
		myOutposts.get(del).deleted = true;
		return del;
	}

	static void deepCopyGrid(Point[] gridIn) {
		for (int i = 0; i < gridIn.length; i++) {
			grid[i] = new Point(gridIn[i]);
			grid[i].ownerlist.addAll(gridIn[i].ownerlist);
		}
	}

	public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin, int r, int L, int W, int t) {
		// Initialize once
		if (counter == 0) {
			playerId = this.id;
			Pair firstOutpost = king_outpostlist.get(this.id).get(0);
			this.HOME_CELL = new Pair(firstOutpost.x, firstOutpost.y);
			System.out.println("Home cell: (" + HOME_CELL.x + ", " + HOME_CELL.y + ")");
			// Init meaning of directions
			if (HOME_CELL.x == 0 && HOME_CELL.y == 0) {
				X_AWAY = Direction.RIGHT;
				X_BACK = Direction.LEFT;
				Y_AWAY = Direction.DOWN;
				Y_BACK = Direction.UP;
			}
			else if (HOME_CELL.x == 0 && HOME_CELL.y > 0) {
				X_AWAY = Direction.RIGHT;
				X_BACK = Direction.LEFT;
				Y_AWAY = Direction.UP;
				Y_BACK = Direction.DOWN;
			}
			else if (HOME_CELL.x > 0 && HOME_CELL.y == 0) {
				X_AWAY = Direction.LEFT;
				X_BACK = Direction.RIGHT;
				Y_AWAY = Direction.DOWN;
				Y_BACK = Direction.UP;
			}
			else {
				X_AWAY = Direction.LEFT;
				X_BACK = Direction.RIGHT;
				Y_AWAY = Direction.UP;
				Y_BACK = Direction.DOWN;
			}
			STAY = Direction.STAY;

			this.R = r;
			this.L = L;
			this.W = W;
			this.T = t;

			myOutposts = new ArrayList<Outpost>();
			myOutposts.add(new Outpost(0));

			//mastermind = new Mastermind(Strategy.PEEL);
			//mastermind.dispatch();
			mastermind = new Mastermind(Strategy.ARMY);
		}

		// Update internal representation of game
		deepCopyGrid(gridin);

		// compute available resources
		if (counter == 0) {
			for (int i = 0; i < size; ++i) {
				for (int j = 0; j < size; ++j) {
					water[i][j] = 0;
					land[i][j] = 0;
					if (grid[i * size + j].water) continue;
					for (int x = i - R; x <= i + R; ++x) {
						if (x < 0 || x >= size) continue;
						for (int y = j - R; y <= j + R; ++y) {
							if (y < 0 || y >= size) continue;
							if (manhattanDistance(new Pair(x, y), new Pair(i, j)) > R)
								continue;
							if (grid[x * size + y].water)
								water[i][j] += 1;
							else
								land[i][j] += 1;
						}
					}
				}
			}

		}

		ArrayList<Pair> updatedOutposts = king_outpostlist.get(this.id);
		if (updatedOutposts.size() < myOutposts.size()) {
			// We lost outposts :(
			// Update outpost list and outpost ID's stored internally
			int i;
			for (i = 0; i < myOutposts.size(); i++) {
				if (myOutposts.get(i).deleted) {
					myOutposts.remove(i);
					break;
				}
			}
			while (i < myOutposts.size()) {
				myOutposts.get(i).id--;
				i++;
			}
		}
		else {
			int i;
			for (i = 0; i < myOutposts.size(); i++) {
				myOutposts.get(i).updatePosition(updatedOutposts.get(i));
			}
			// Did we get new outposts?
			while (i < updatedOutposts.size()) {
				myOutposts.add(new Outpost(i));
				i++;
			}
		}

		// Update counter
		counter++;
		if (counter % 10 == 0) {
			tenthTurn = true;
		}
		else {
			tenthTurn = false;
		}

		// For now have mastermind re-dispatch everyone on the tenth turn
		mastermind.dispatch();
		//if (tenthTurn) {
		//	mastermind.dispatch();
		//}

		// Get moves from each outpost
		ArrayList<movePair> nextList = new ArrayList<movePair>();
		for (Outpost outpost : myOutposts) {
			Pair currentPosition = outpost.position;
			Direction move = outpost.move();
			Pair newPosition = new Pair(currentPosition.x + move.dx, currentPosition.y + move.dy);
			nextList.add(new movePair(outpost.id, newPosition));
		}

		return nextList;
	}

}
