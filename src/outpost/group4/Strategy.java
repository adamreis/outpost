package outpost.group4;

import java.util.*;

public interface Strategy {

	public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason);

	public int delete(ArrayList<Post> posts, GridSquare[][] board);
}
