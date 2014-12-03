package outpost.group4;

import java.util.ArrayList;


public class KamikazePostPair {
	public enum State {
		CONNECTED, OVERLAPPING, DISCONNECTED
	};

	public State state;
	public Post p1;
	public Post p2;
	public int enemyId;
	public Location enemyBase;
	private boolean isStalled;

	public KamikazePostPair(Post p1, Post p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.isStalled = false;
		this.enemyId = -1;
		this.enemyBase = new Location(999999999,99999999);
	}

	public KamikazePostPair(KamikazePostPair kPair) {
		this.p1 = new Post(kPair.p1);
		this.p2 = new Post(kPair.p2);
		this.isStalled = kPair.isStalled;
		this.enemyId = kPair.enemyId;
		this.enemyBase = kPair.enemyBase;
	}

	public boolean onTopOfEnemyBase() {
		if (this.p1.distanceTo(this.enemyBase) == 0 || this.p2.distanceTo(this.enemyBase) == 0) {
			return true; // because this won't be the first time
		}
		return false;
	}

	private Location nearestLocationTowardTargetBase(Post startingPoint, ArrayList<Post> targetPosts, Location targetBase){
		Location nearest = targetBase;
		double distanceToTarget = startingPoint.distanceTo(targetBase);
		double shortestDistance = distanceToTarget;

		for (Post p : targetPosts) {
			double sToP = startingPoint.distanceTo(p);
			double pToT = p.distanceTo(targetBase);
			if ((sToP + pToT < distanceToTarget + 10) && sToP < shortestDistance) {
				nearest = p;
				shortestDistance = startingPoint.distanceTo(p);
			}
		}

		//System.out.printf("targetBase: %s, nearest: %s\n", targetBase, nearest);

		return nearest;
	}

	private boolean checkIfAdjacentToTargetPost(ArrayList<Post> targetPosts) {
		for (Post p : targetPosts) {
			if (p1.distanceTo(p) == 1.0 || p2.distanceTo(p) == 1.0) {
				return true;
			}
		}
		return false;
	}

	public boolean move(ArrayList<Post> targetPosts, Location targetBase) {
		//System.out.printf("kPair [%s %s] ", this.p1, this.p2);

		this.enemyBase = targetBase;
		// Returns true iff p1 overtakes an enemy base
		if (onTopOfEnemyBase()) {
//			System.out.printf("[%s %s]\n", this.p1, this.p2);
			return true;
		}

		updateState();
		Location targetLoc = nearestLocationTowardTargetBase(this.p1, targetPosts, targetBase);

		switch (this.state) {
			case CONNECTED:
				if (this.isStalled) {
					this.isStalled = false;
				} else if (this.checkIfAdjacentToTargetPost(targetPosts)) {
					this.isStalled = true;
					break;
				}

				moveTowardLocation(targetLoc, targetBase);
				break;

			case OVERLAPPING:
				moveApart(targetLoc, targetBase);
				break;

			case DISCONNECTED:
				moveTogether();
				break;

			default: break;
		}

		if (onTopOfEnemyBase()) {
			//System.out.println("targetBase reached! at " + this.p1);
//			System.out.printf("[%s %s]\n", this.p1, this.p2);
			return true;
		}
//		System.out.printf("[%s %s]\n", this.p1, this.p2);
		return false;

	}

	private void moveApart(Location targetLoc, Location targetBase) {
		ArrayList<Location> sPath = this.p1.shortestPathToLocation(targetLoc);
		Location towardBase;
		if (sPath.size() > 1) {
			 towardBase = sPath.get(1);
		} else {
			towardBase = sPath.get(0);
		}
		this.p1 = new Post(towardBase, this.p1.id);
	}

	private void moveTogether() {
		this.p2 = this.p2.moveMinimizingDistanceFrom(this.p1);
		if (this.p1.distanceTo(p2) > 1.0) {
			this.p1 = this.p1.moveMinimizingDistanceFrom(this.p2);
		}
	}

	private void moveTowardLocation(Location targetLoc, Location targetBase) {
//		System.out.println("moveTowardLocation called");
		ArrayList<Location> sPath = this.p1.shortestPathToLocation(targetLoc);
		Location towardBase;
		if (sPath.size() > 1) {
			 towardBase = sPath.get(1);
		} else {
			towardBase = sPath.get(0);
		}
		this.p1 = new Post(towardBase, this.p1.id);

		if (this.p2.distanceTo(p1) > 1.0) {
			this.p2 = this.p2.moveMinimizingDistanceFrom(this.p1);
		}
	}

	private void updateState() {
		if (this.p1 == null || this.p2 == null) {
			System.out.println("one of my ids is null");
		}
		int d = (int)this.p1.distanceTo(this.p2);
		if (d == 1) {
			this.state = State.CONNECTED;
		} else if (d == 0){
			this.state = State.OVERLAPPING;
		} else {
			this.state = State.DISCONNECTED;
		}
	}
}
