package outpost.group4;

import java.util.ArrayList;
import java.util.HashMap;

import outpost.group4.Conversions;

public class SabotageStrategy implements Strategy {
	
    private HashMap<Post, KamikazePostPair> kamikazes;
    private ArrayList<Location> enemyBaseLocs;
    private int id;
    private Location homeBase;
    
    public SabotageStrategy(int id) {
    	this.kamikazes = new HashMap<Post, KamikazePostPair>();
    	this.enemyBaseLocs = new ArrayList<Location>();
    	this.id = id;
    	this.homeBase = Conversions.baseLocationForId(id);
    	
    	for (int i = 0; i < 4; i++) {
    		if (i == id) {
    			continue;
    		}
    		Location baseLoc = Conversions.baseLocationForId(i);
    		this.enemyBaseLocs.add(baseLoc);
    	}
    }
    
	public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
		ArrayList<Post> newPosts = new ArrayList<Post>();
		ArrayList<Post> enemyPosts = otherPlayerPosts.get(1);
		Location enemyBase = this.enemyBaseLocs.get(1);
		
		ArrayList<Post> unmatchedPosts = new ArrayList<Post>();
		HashMap<Post, KamikazePostPair> newKamikazes = new HashMap<Post, KamikazePostPair>();
		
	
		for (int i = 0; i < posts.size(); i++) {
			if (this.kamikazes.containsKey(posts.get(i))) {
				KamikazePostPair kPair = this.kamikazes.get(posts.get(i));
				if (this.kamikazes.get(kPair.p1) == kPair && this.kamikazes.get(kPair.p2) == kPair) {
					KamikazePostPair newPair = new KamikazePostPair(kPair); 
					newPair.move(enemyPosts, enemyBase);
					newKamikazes.put(newPair.p1, newPair);
					newKamikazes.put(newPair.p2, newPair);
					newPosts.add(newPair.p1);
					newPosts.add(newPair.p2);
				} else {
					unmatchedPosts.add(posts.get(i));
				}
			} else {
				unmatchedPosts.add(posts.get(i));
			}
		}
		
		for (int i = 0; i < unmatchedPosts.size() - 1; i += 2) {
			Post p1 = unmatchedPosts.get(i);
			Post p2 = unmatchedPosts.get(i+1);
			KamikazePostPair kPair = new KamikazePostPair(p1, p2);
			kPair.move(enemyPosts, enemyBase);
			newKamikazes.put(kPair.p1, kPair);
			newKamikazes.put(kPair.p2,  kPair);
			newPosts.add(kPair.p1);
			newPosts.add(kPair.p2);
		}
		
		if (unmatchedPosts.size() % 2 != 0) {
			Post lastUnmatched = unmatchedPosts.get(unmatchedPosts.size() -1);
			newPosts.add(lastUnmatched.moveMinimizingDistanceFrom(this.homeBase));
		}
		
		this.kamikazes = newKamikazes;
		
//		if (posts.size() > 0)
//			newPosts.add(posts.get(0).moveMinimizingDistanceFrom(new Location(10,0)));
//		if (posts.size() > 1)
//			newPosts.add(posts.get(1).moveMinimizingDistanceFrom(new Location(0,15)));
			
		
//		for (int i = 0; i < posts.size() - 1; i += 2) {
////			System.err.println("post in: " + posts.get(i));
////			System.err.println("post in: " + posts.get(i+1));
//			KamikazePostPair kPair = new KamikazePostPair(posts.get(i), posts.get(i+1), enemyPosts);
//			kPair.move();
//			newPosts.add(kPair.p1);
//			newPosts.add(kPair.p2);
//		}
		
		
		
		return newPosts;
	}
}

