package outpost.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import outpost.group4.Conversions;

public class SabotageStrategy implements Strategy {

    private ArrayList<KamikazePostPair> kamikazes;
    private ArrayList<Location> enemyBaseLocs;
    private int id;
    private Location homeBase;

    public SabotageStrategy(int id) {
    	this.kamikazes = new ArrayList<KamikazePostPair>();
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

    public int delete(ArrayList<Post> posts) {
		return -1;
	}

    private ArrayList<Integer> uncoveredEnemyOutpostIds() {
    	ArrayList<Integer> uncoveredBases = new ArrayList<Integer>();

    	for (int i = 0; i < this.enemyBaseLocs.size(); i++) {
    		boolean foundMatch = false;
    		for (KamikazePostPair kPair : this.kamikazes) {
    			if (kPair.p1.distanceTo(this.enemyBaseLocs.get(i)) == 0 || kPair.p2.distanceTo(this.enemyBaseLocs.get(i)) == 0) {
    				foundMatch = true;
    				break;
    			}
    		}
    		if (foundMatch) {
    			continue;
    		} else {
    			uncoveredBases.add(i);
    		}
    	}

    	return uncoveredBases;
    }

    private int pickNewEnemyId(){
    	ArrayList<Integer> uncoveredBases = uncoveredEnemyOutpostIds();

    	if (uncoveredBases.size() == 0) {
    		//System.out.println("ALL ENEMIES CAPTURED, YO!  WE PROB HAVE SOME EXTRA BASES FOR SABOTAGE");
    		for (int i = 0; i < 3; i++) {
    				uncoveredBases.add(i);
    		}
    	}

    	Random r = new Random();
		int randIndex = r.nextInt(uncoveredBases.size());
		return uncoveredBases.get(randIndex);
    }

	public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
		ArrayList<Post> newPosts = new ArrayList<Post>();
		ArrayList<Post> unmatchedPosts = new ArrayList<Post>();
		ArrayList<KamikazePostPair> matchedPairs = new ArrayList<KamikazePostPair>();
		ArrayList<KamikazePostPair> newKamikazes = new ArrayList<KamikazePostPair>();
		ArrayList<Integer> uncoveredEnemyOutposts = uncoveredEnemyOutpostIds();



		for (int i = 0; i < posts.size(); i++) {
			unmatchedPosts.add(new Post(posts.get(i)));
		}

//		System.out.println("Previous pairs:");
		for (KamikazePostPair kPair : this.kamikazes) {
//			System.out.printf("[%s %s]\n", kPair.p1, kPair.p2);

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

			if (!uncoveredEnemyOutposts.contains(kPair.enemyId) && !kPair.onTopOfEnemyBase()) {
				kPair.enemyId = pickNewEnemyId();
			}

			boolean reachedGoal = kPair.move(otherPlayerPosts.get(kPair.enemyId), this.enemyBaseLocs.get(kPair.enemyId));

			newKamikazes.add(kPair);
			newPosts.add(kPair.p1);
			newPosts.add(kPair.p2);
//			System.out.println("matchedPair after : " + kPair.p1 + " + " + kPair.p2);
		}

		this.kamikazes = newKamikazes;

		return newPosts;
	}

	private ArrayList<KamikazePostPair> matchPosts(ArrayList<Post> unmatchedPosts) {
//		System.out.print("Starting with unmatched posts: ");
//		for (Post p : unmatchedPosts) System.out.print(p + ", ");
//		System.out.println();


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
			//System.out.printf("new kPair! with %s %s\n", farthestFromBase, closestMatch);
			// Remove these two from unmatchedPosts
			unmatchedPosts.remove(farthestFromBase);
			unmatchedPosts.remove(closestMatch);
		}

		return newKPairs;
	}
}
