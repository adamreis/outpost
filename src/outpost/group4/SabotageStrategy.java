package outpost.group4;

import java.util.ArrayList;

public class SabotageStrategy implements Strategy{
	ArrayList<Post> posts;
    ArrayList<ArrayList<Post>> otherPlayerPosts;
 
    
	public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
		this.posts = posts;
		this.otherPlayerPosts = otherPlayerPosts;
		
		ArrayList<Post> newPosts = new ArrayList<Post>();
		int opponentId = (Player.parameters.id + 1) % 4;
		
		for (int i = 0; i < posts.size() - 1; i += 2) {
			KamikazePostPair kPair = new KamikazePostPair(posts.get(i), posts.get(i+1), opponentId, otherPlayerPosts);
			kPair.move();
			newPosts.add(kPair.p1);
			newPosts.add(kPair.p2);
		}
		
		if (posts.size() % 2 != 0) {
			newPosts.add(posts.get(posts.size() - 1));
		}
		
		return newPosts;
	}
}

