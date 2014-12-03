package outpost.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class AdamShellStrategy implements Strategy {
    private Location homeBase;

    public AdamShellStrategy(int id) {
    	this.homeBase = Conversions.baseLocationForId(id);
    }

    public int delete(ArrayList<Post> posts) {
		return -1;
	}
    
    private ArrayList<Post> allEnemyPosts(ArrayList<ArrayList<Post>> otherPlayerPosts) {
    	ArrayList<Post> allEnemyPosts = new ArrayList<Post>();
    	
    	for (ArrayList<Post> p : otherPlayerPosts) {
    		allEnemyPosts.addAll(p);
    	}
    	return allEnemyPosts;
    }
    
	public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
		ArrayList<Post> newPosts = new ArrayList<Post>();
		if (posts.size() < 2) { // not enough for a pair :'(
			return posts;
		}
		// we know we have exactly 2!
		
		ShellPostPair newShellPair = new ShellPostPair(posts.get(0), posts.get(1));
		
		ArrayList<Post> allEnemyPosts = allEnemyPosts(otherPlayerPosts);
		
		if (allEnemyPosts.isEmpty()) {
			return posts;
		}
		
		Post closestPost = allEnemyPosts.get(0);
		double closestDist = this.homeBase.distanceTo(closestPost);
		
		for (Post p : allEnemyPosts) {
			double dist = this.homeBase.distanceTo(p);
			if (dist < closestDist) {
				closestPost = p;
				closestDist = dist;
			}
		}
		
		if (this.homeBase.distanceTo(closestPost) < 10) {
			newShellPair.move(closestPost);
		} else {
			newShellPair.move(this.homeBase);
		}
		
		newPosts.add(newShellPair.p1);
		newPosts.add(newShellPair.p2);
		
//		System.err.printf("my shell pair: [%s, %s]\n", newShellPair.p1, newShellPair.p2);
		
		return newPosts;
		
	}
}
