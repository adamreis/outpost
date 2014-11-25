package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
	private static final int SIZE = 100;

	static Random random = new Random();

	public static Location baseLoc;
	private Strategy strategy;
	protected static GameParameters parameters;
	protected static Board board;
	private int turn;

	public Player(int id) {
		super(id);

		this.strategy = new UtilityMaxStrategy();

		this.turn = 0;
	}

	public void init() { }

	public int delete(ArrayList<ArrayList<Pair>> outpostList, Point[] grid) {
		ArrayList<Post> posts = Conversions.postsFromPairs(outpostList.get(this.id));

		Post nearestPost = (Post) this.baseLoc.nearestLocation(posts);
		int del = posts.indexOf(nearestPost);

		if (del < 0) del = random.nextInt(outpostList.get(id).size());

		return del;
	}

	public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> outpostList, Point[] grid, int r, int L, int W, int T) {
		// initialize the goods if necessary
		if (Player.parameters == null) {
			baseLoc = new Location(outpostList.get(this.id).get(0));
			Player.parameters = new GameParameters(r, L, W, T, SIZE);
		}

		// perform conversions to custom classes
		ArrayList<ArrayList<Post>> masterPosts = new ArrayList<ArrayList<Post>>(outpostList.size());
		ArrayList<ArrayList<Post>> otherPlayerPosts = new ArrayList<ArrayList<Post>>(outpostList.size() - 1);
		for (int i = 0; i < outpostList.size(); i++) {
				ArrayList<Post> posts = Conversions.postsFromPairs(outpostList.get(i));
				masterPosts.add(posts);

				if (i != this.id) {
						otherPlayerPosts.add(posts);
				}
		}
		ArrayList<Post> oldPosts = masterPosts.get(this.id);
		board = new Board(grid);

		// run the strategy
		boolean newSeason = (turn % 10 == 0);
		ArrayList<Post> newPosts = strategy.move(otherPlayerPosts, oldPosts, newSeason);

		// increment the turn
		turn += 1;

		// and convert back from custom classes
		return Conversions.movePairsFromPosts(newPosts);
	}

}
