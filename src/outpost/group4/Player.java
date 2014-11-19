package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
		private static final int SIZE = 100;

		static Random random = new Random();

		private Location baseLoc;
		protected static GameParameters parameters;

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
		}

		public void init() { }

		public int delete(ArrayList<ArrayList<Pair>> outpostList, Point[] gridin) {
			int del = random.nextInt(outpostList.get(id).size());
			return del;
		}

		public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> outpostList, Point[] grid, int r, int L, int W, int T) {
			if (this.parameters == null) {
				this.parameters = new GameParameters(r, L, W, T, SIZE);
			}

			ArrayList<Post> oldPosts = postsFromPairs(outpostList.get(this.id));
			ArrayList<Post> newPosts = new ArrayList<Post>();

			for (Post p : oldPosts) {
				newPosts.add(p.adjacentCells(SIZE).get(p.id % 2));
			}

			return movePairsFromPosts(newPosts);
		}


		public ArrayList<movePair> movePairsFromPosts(ArrayList<Post> posts) {
			ArrayList<movePair> pairs = new ArrayList<movePair>();
			for (Post post : posts) {
				pairs.add(new movePair(post.id, new Pair(post.x, post.y)));
			}
			return pairs;
		}

		public ArrayList<Post> postsFromPairs(ArrayList<Pair> pairs) {
			ArrayList<Post> posts = new ArrayList<Post>();
			int id = 0;
			for (Pair p : pairs) {
				posts.add(new Post(p, id++));
			}
			return posts;
		}
}
