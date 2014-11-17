package outpost.group1;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
		private static final int SIZE = 100;

		static Random random = new Random();

		private Point[] grid;
		private int[] theta = new int[100];

		private int turn;

		public void init() {
				theta = new int[SIZE];
				grid = new Point[SIZE * SIZE]

				randomizeTheta();

				turn = 0;
		}

		public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> king_outpostlist, int noutpost, Point[] gridin){
				turn += 1;

				if (turn % 10 == 0) {
						randomizeTheta();
				}

				for (int i = 0; i < gridin.length; i++) {
						grid[i] = new Point(gridin[i]);
				}

				ArrayList<movePair> nextList = new ArrayList<movePair>();
				ArrayList<Pair> outpostList = king_outpostlist.get(this.id);

				int boundingJ = (outpostList.size() > noutpost)? outpostList.size() - 1 : outpostList.size();

				for (int j = 0; j < boundingJ; j++) {
						ArrayList<Pair> positions = surround(outpostList.get(j));

						movePair move;
						while (move == null) {
								if (theta[j] < positions.size()) {
										Pair pair = positions.get(theta[j]);
										if (isPairInBounds(pair) && !pairToPoint(pair).water) {
												move = new movePair(j, pair, false);
										}
								}

								theta[j] = random.nextInt(positions.size());
						}
						nextList.add(move);
				}

				if (outpostList.size() > noutpost) {
						nextList.add(new movePair(outpostList.size() - 1, new Pair(0,0), true));
				}

				return nextList;
		}

		private randomizeTheta() {
				for (int i = 0; i < SIZE; i++) {
						theta[i] = random.nextInt(4);
				}
		}

		private boolean isPairInBounds(Pair pair) {
				return pair.x >= 0 &&
							 pair.y >= 0 &&
							 pair.x < SIZE &&
							 pair.y < SIZE;
		}

		static ArrayList<Pair> surround(Pair start) {
				ArrayList<Pair> pairList = new ArrayList<Pair>();

				Pair temp = new Pair(start);
				pairList.add(new Pair(temp.x - 1, temp.y));
				pairList.add(new Pair(temp.x + 1, temp.y));
				pairList.add(new Pair(temp.x, temp.y - 1));
				pairList.add(new Pair(temp.x, temp.y + 1));

				return pairList;
		}

		static Point pairToPoint(Pair pr) {
			return grid[pr.x*SIZE+pr.y];
		}

		static Pair pointToPair(Point pt) {
			return new Pair(pt.x, pt.y);
		}
}
