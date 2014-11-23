package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
		private static final int SIZE = 100;

		static Random random = new Random();

		private Location baseLoc;
    private Strategy strategy;
		protected static GameParameters parameters;
    protected static GridSquare[][] gridSquares;
    private int turn;

		public Player(int id) {
			super(id);

			switch (id) {
				case 0: this.baseLoc = new Location(0,0);
						break;
				case 1: this.baseLoc = new Location(SIZE-1, 0);
						break;
				case 2: this.baseLoc = new Location(SIZE-1, SIZE-1);
						break;
				case 3: this.baseLoc = new Location(0, SIZE-1);
						break;
			}

      turn = 0;
		}

		public void init() { }

		public int delete(ArrayList<ArrayList<Pair>> outpostList, Point[] grid) {
			ArrayList<Post> posts = Conversions.postsFromPairs(outpostList.get(this.id));

			Post nearestPost = (Post) this.baseLoc.nearestLocation(posts);
			int del = posts.indexOf(nearestPost);

			System.out.println("DEL INDEX: " + del);

			if (del < 0) del = random.nextInt(outpostList.get(id).size());

			return del;
		}

		public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> outpostList, Point[] grid, int r, int L, int W, int T) {
			if (this.parameters == null) {
				this.parameters = new GameParameters(r, L, W, T, SIZE);
			}

			// perform conversions to sane classes
			ArrayList<Post> oldPosts = Conversions.postsFromPairs(outpostList.get(this.id));
			gridSquares = Conversions.gridSquaresFromPoints(grid);

      strategy = new Strategy(oldPosts);
      ArrayList<Post> newPosts = strategy.move();


      turn += 1;

      return Conversions.movePairsFromPosts(newPosts);
		}
}
