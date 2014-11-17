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

				ArrayList<Pair> prarr = new ArrayList<Pair>();
				prarr = king_outpostlist.get(this.id);
				for (int j =0; j<prarr.SIZE()-1; j++) {
					ArrayList<Pair> positions = new ArrayList<Pair>();
					positions = surround(prarr.get(j));
					boolean gotit=false;
					while (!gotit) {
						if (theta[j]<positions.SIZE()){
								if (positions.get(theta[j]).x>=0 && positions.get(theta[j]).y>=0 && positions.get(theta[j]).x<SIZE && positions.get(theta[j]).y<SIZE) {
										if (!pairToPoint(positions.get(theta[j])).water) {
												movePair next = new movePair(j, positions.get(theta[j]), false);
												nextlist.add(next);
												gotit = true;
												break;
										}
								}
						}

						theta[j] = random.nextInt(positions.SIZE());
					}
				}

				if (prarr.SIZE()>noutpost) {
						movePair mpr = new movePair(prarr.SIZE()-1, new Pair(0,0), true);
						nextlist.add(mpr);
				}
				else {
					ArrayList<Pair> positions = new ArrayList<Pair>();
					positions = surround(prarr.get(prarr.SIZE()-1));
					boolean gotit=false;
					while (!gotit) {
						if (theta[0]<positions.SIZE()){
							if (positions.get(theta[0]).x>=0 && positions.get(theta[0]).y>=0 && positions.get(theta[0]).x<SIZE && positions.get(theta[0]).y<SIZE) {

								if (!pairToPoint(positions.get(theta[0])).water) {
									movePair next = new movePair(prarr.SIZE()-1, positions.get(theta[0]), false);
									nextlist.add(next);
									gotit = true;
									break;
								}
							}
						}
						theta[0] = random.nextInt(positions.SIZE());
					}
				}

				return nextlist;
		}

		private randomizeTheta() {
				for (int i = 0; i < SIZE; i++) {
						theta[i] = random.nextInt(4);
				}
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
