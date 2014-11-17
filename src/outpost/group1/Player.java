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

				ArrayList<movePair> nextlist = new ArrayList<movePair>();

				for (int i = 0; i < gridin.length; i++) {
						grid[i] = new Point(gridin[i]);
				}

				ArrayList<Pair> outpostList = king_outpostlist.get(this.id);

				for (int j = 0; j < outpostList.size() - 1; j++) {
						ArrayList<Pair> positions = surround(outpostList.get(j));

						boolean foundMovePair = false;
						while (!foundMovePair) {
								if (theta[j] < positions.size()) {
										Pair pair = positions.get(theta[j]);
										if (isPairInBounds(pair) && !pairToPoint(pair).water) {
												movePair next = new movePair(j, pair, false);
												nextlist.add(next);
												foundMovePair = true;
												break;
										}
								}

								theta[j] = random.nextInt(positions.size());
						}
				}

				if (outpostList.size() > noutpost) {
						movePair mpr = new movePair(outpostList.size() - 1, new Pair(0,0), true);
						nextlist.add(mpr);
				}
				else {
						ArrayList<Pair> positions = surround(outpostList.get(outpostList.size()-1))

						boolean foundMovePair = false;
						while (!foundMovePair) {
								if (theta[0]<positions.size()) {
										Pair pair = positions.get(theta[0]);
										if (isPairInBounds(pair) && !pairToPoint(pair).water) {
												movePair next = new movePair(outpostList.size() - 1, positions.get(theta[0]), false);
												nextlist.add(next);
												foundMovePair = true;
												break;
										}
								}

								theta[0] = random.nextInt(positions.size());
						}
				}

				return nextlist;
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
