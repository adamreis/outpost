package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Player extends outpost.sim.Player {
	protected static final int SIZE = 100;

	static Random random = new Random();

	private Strategy strategy;
	protected int turn;

	protected static Location baseLoc;
	protected static GameParameters parameters;
	protected static Board board;
	protected static int knownID;

	public Player(int id) {
		super(id);

		knownID = id;
		this.turn = 0;
	}

	public void init() { }

	public int delete(ArrayList<ArrayList<Pair>> outpostList, Point[] grid) {
		ArrayList<Post> posts = Conversions.postsFromPairs(outpostList.get(this.id));
		GridSquare[][] board = Conversions.gridSquaresFromPoints(grid);

		return strategy.delete(posts, board);
	}

	public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> outpostList, Point[] grid, int r, int L, int W, int T) {
		// initialize the goods if necessary
		if (Player.parameters == null) {
			baseLoc = new Location(outpostList.get(this.id).get(0));
			Player.parameters = new GameParameters(r, L, W, T, SIZE);

			this.strategy = new StagePlanningStrategy();
		}

		// create the static known board
		board = new Board(outpostList, grid);

		// run the strategy
		boolean newSeason = (turn % 10 == 0);
		ArrayList<Post> newPosts = strategy.move(board.otherPlayerPosts, board.ourPosts(), newSeason);

		// increment the turn
		turn += 1;

		// convert back from custom classes and return
		return Conversions.movePairsFromPosts(newPosts);
	}

}
