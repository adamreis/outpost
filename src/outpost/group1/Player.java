package outpost.group1;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
		static int size =100;
		static Point[] grid = new Point[size*size];
		static Random random = new Random();
		static int[] theta = new int[100];
		static int counter = 0;

		public void init() {
				for (int i=0; i<100; i++) {
						theta[i]=random.nextInt(4);
				}
		}

		public int delete(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin) {
			System.out.printf("haha, we are trying to delete a outpost for player %d\n", this.id);
			int del = random.nextInt(king_outpostlist.get(id).size());
			return del;
		}

		//public movePair move(ArrayList<ArrayList<Pair>> king_outpostlist, int noutpost, Point[] grid) {
		public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> king_outpostlist, int noutpost, Point[] gridin){
			counter = counter+1;
			if (counter % 10 == 0) {
				for (int i=0; i<100; i++) {
					theta[i]=random.nextInt(4);
				}
			}
			ArrayList<movePair> nextlist = new ArrayList<movePair>();
			//System.out.printf("Player %d\n", this.id);
			for (int i=0; i<gridin.length; i++) {
				grid[i]=new Point(gridin[i]);
			}
			ArrayList<Pair> prarr = new ArrayList<Pair>();
			prarr = king_outpostlist.get(this.id);
			for (int j =0; j<prarr.size()-1; j++) {
				ArrayList<Pair> positions = new ArrayList<Pair>();
				positions = surround(prarr.get(j));
				boolean gotit=false;
				while (!gotit) {
					//Random random = new Random();
					//int theta = random.nextInt(positions.size());
					//System.out.println(theta);
					//if (!pairToPoint(positions.get(theta[j])).water && positions.get(theta[j]).x>0 && positions.get(theta[j]).y>0 && positions.get(theta[j]).x<size && positions.get(theta[j]).y<size) {
					if (theta[j]<positions.size()){
						if (positions.get(theta[j]).x>=0 && positions.get(theta[j]).y>=0 && positions.get(theta[j]).x<size && positions.get(theta[j]).y<size) {

							if (!pairToPoint(positions.get(theta[j])).water) {
								movePair next = new movePair(j, positions.get(theta[j]), false);
								nextlist.add(next);
								//next.printmovePair();
								gotit = true;
								break;
							}
						}
					}
					//System.out.println("we need to change the direction???");
					theta[j] = random.nextInt(positions.size());
				}
			}
			if (prarr.size()>noutpost) {
				movePair mpr = new movePair(prarr.size()-1, new Pair(0,0), true);
				nextlist.add(mpr);
				//mpr.printmovePair();
			}
			else {
				ArrayList<Pair> positions = new ArrayList<Pair>();
				positions = surround(prarr.get(prarr.size()-1));
				boolean gotit=false;
				while (!gotit) {
					//Random random = new Random();
					//int theta = random.nextInt(positions.size());
					//System.out.println("we are here!!!");
					if (theta[0]<positions.size()){
						if (positions.get(theta[0]).x>=0 && positions.get(theta[0]).y>=0 && positions.get(theta[0]).x<size && positions.get(theta[0]).y<size) {

							if (!pairToPoint(positions.get(theta[0])).water) {
								movePair next = new movePair(prarr.size()-1, positions.get(theta[0]), false);
								nextlist.add(next);
								//next.printmovePair();
								gotit = true;
								break;
							}
						}
					}
					//System.out.println("outpost 0 need to change the direction???");
					theta[0] = random.nextInt(positions.size());
				}

			}


			return nextlist;
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
			return grid[pr.x*size+pr.y];
		}

		static Pair pointToPair(Point pt) {
			return new Pair(pt.x, pt.y);
		}
}
