package outpost.group4;

import java.util.*;

public interface Strategy {

	public ArrayList<Post> move(ArrayList<ArrayList<Post>> masterList, ArrayList<Post> posts, boolean newSeason);

}
