package outpost.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import outpost.group4.Conversions;

public class SabotageStrategy implements Strategy {
	
    private ArrayList<KamikazePostPair> kamikazes;
    private ArrayList<Location> enemyBaseLocs;
    private int id;
    private int currentEnemyId;
    private Location homeBase;
    
    public SabotageStrategy(int id) {
    	this.kamikazes = new ArrayList<KamikazePostPair>();
    	this.enemyBaseLocs = new ArrayList<Location>();
    	this.id = id;
    	this.homeBase = Conversions.baseLocationForId(id);
    	this.currentEnemyId = -1;
    	
    	for (int i = 0; i < 4; i++) {
    		if (i == id) {
    			continue;
    		}
    		Location baseLoc = Conversions.baseLocationForId(i);
    		this.enemyBaseLocs.add(baseLoc);
    	}
    }

    private void pickNewEnemyId(){
    	int newId = -1;
    	double newEnemyDistance = Integer.MAX_VALUE;
    	
    	for (int i = 0; i < this.enemyBaseLocs.size(); i++) {
    		boolean foundMatch = false;
    		for (KamikazePostPair kPair : this.kamikazes) {
    			if (kPair.p1.distanceTo(this.enemyBaseLocs.get(i)) == 0) {
    				foundMatch = true;
    				break;
    			}
    		}
    		if (foundMatch) {
    			continue;
    		}
    		
    		if (this.homeBase.distanceTo(this.enemyBaseLocs.get(i)) < newEnemyDistance) {
    			newId = i;
    			newEnemyDistance = this.homeBase.distanceTo(this.enemyBaseLocs.get(i));
    		}
    	}
    	
    	if (newId == -1) {
    		System.out.println("ALL ENEMIES CAPTURED, YO!  WE PROB HAVE SOME EXTRA BASES FOR SABOTAGE");
    	}
    	
    	this.currentEnemyId = newId;
    }
    
	public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
		ArrayList<Post> newPosts = new ArrayList<Post>();
		ArrayList<Post> unmatchedPosts = new ArrayList<Post>();
		ArrayList<KamikazePostPair> matchedPairs = new ArrayList<KamikazePostPair>();
		ArrayList<KamikazePostPair> newKamikazes = new ArrayList<KamikazePostPair>();
		
		if (this.currentEnemyId == -1) {
			this.pickNewEnemyId();
		}
		
		for (int i = 0; i < posts.size(); i++) {
			unmatchedPosts.add(new Post(posts.get(i)));
		}
		
		for (KamikazePostPair kPair : this.kamikazes) {
			
			if (unmatchedPosts.contains(kPair.p1) && unmatchedPosts.contains(kPair.p2)) {
				matchedPairs.add(kPair);
				unmatchedPosts.remove(kPair.p1);
				unmatchedPosts.remove(kPair.p2);
			}
		}
	
		ArrayList<KamikazePostPair> newKPairs = matchPosts(unmatchedPosts);
		matchedPairs.addAll(newKPairs);
		
		// Add last unmatched, if it exists
		for (Post lastUnmatched : unmatchedPosts) {
			newPosts.add(lastUnmatched.moveMinimizingDistanceFrom(this.homeBase));
		}
		
		// Move all the kamikaze pairs
		for (int i = 0; i < matchedPairs.size(); i++) {
			KamikazePostPair kPair = matchedPairs.get(i);
			
			if (this.currentEnemyId >= 0) {

        // GO AFTER EVERYONE'S POSTS AND BASES
        ArrayList<Post> enemyPosts = new ArrayList<Post>();
        for (int j = 0; j < otherPlayerPosts.size(); j++) {
          for (Post p : otherPlayerPosts.get(j))
            enemyPosts.add(p);
        }
        for (Location loc : this.enemyBaseLocs) {
          // ugly casting location to Post
          Post p = new Post(loc, -1);
          enemyPosts.add(p);
        }

        // GO AFTER EVERYONE'S POSTS
        //ArrayList<Post> enemyPosts = new ArrayList<Post>();
        //for (int j = 0; j < otherPlayerPosts.size(); j++) {
        //  for (Post p : otherPlayerPosts.get(j))
        //    enemyPosts.add(p);
        //}

        // GO AFTER ONLY THE ENEMY's POSTS
				//ArrayList<Post> enemyPosts = otherPlayerPosts.get(this.currentEnemyId);

				Location enemyBase = this.enemyBaseLocs.get(this.currentEnemyId);
				
				boolean reachedGoal = kPair.move(enemyPosts, enemyBase);
				if (reachedGoal) {
					this.currentEnemyId = -1;
				}
				newKamikazes.add(kPair);
			}
			newPosts.add(kPair.p1);
			newPosts.add(kPair.p2);
//			System.out.println("matchedPair after : " + kPair.p1 + " + " + kPair.p2);
		}
		
		this.kamikazes = newKamikazes;
	
		return newPosts;
	}
	
	private ArrayList<KamikazePostPair> matchPosts(ArrayList<Post> unmatchedPosts) {
		// This will remove all unmatched posts as new pairs are created, leaving 1 if there are an odd number
		ArrayList<KamikazePostPair> newKPairs = new ArrayList<KamikazePostPair>();
		
		while (unmatchedPosts.size() > 1) {
			// Find farthest post
			Post farthestFromBase = new Post(0,0,-1);
			double farthestDistFromBase = -1;
			
			for (Post p : unmatchedPosts) {
				double newDist = p.distanceTo(this.homeBase);
				if (newDist > farthestDistFromBase) {
					farthestDistFromBase = newDist;
					farthestFromBase = p;
				}
			}
			
			// Find closest post to this one
			Post closestMatch = new Post(0,0,-1);
			double closestDist = Integer.MAX_VALUE;
			
			for (Post p : unmatchedPosts) {
				if (p == farthestFromBase) {
					continue;
				}
				double newDist = p.distanceTo(farthestFromBase);
				if (newDist < closestDist) {
					closestMatch = p;
					closestDist = newDist;
				}
			}
//			System.out.println("adding new post pair: " + farthestFromBase + " + " + closestMatch);
			// Add these two to a new kPair
			newKPairs.add(new KamikazePostPair(farthestFromBase, closestMatch));
			
			// Remove these two from unmatchedPosts
			unmatchedPosts.remove(farthestFromBase);
			unmatchedPosts.remove(closestMatch);
		}
		
		return newKPairs;
	}
}

