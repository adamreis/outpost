package outpost.group4;

import java.util.ArrayList;


public class KamikazePostPair {
	public enum State {
		CONNECTED, OVERLAPPING, DISCONNECTED
	};
	
	public State state;
	public Post p1;
	public Post p2;
	private boolean isStalled;
	
	public KamikazePostPair(Post p1, Post p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.isStalled = false;
	}
	
	public KamikazePostPair(KamikazePostPair kPair) {
		this.p1 = new Post(kPair.p1);
		this.p2 = new Post(kPair.p2);
		this.isStalled = kPair.isStalled;
	}
	
	private Location nearestLocationTowardTargetBase(Post startingPoint, ArrayList<Post> targetPosts, Location targetBase){
		Location nearest = targetBase;
		double distanceToTarget = startingPoint.distanceTo(targetBase);
		double shortestDistance = distanceToTarget;
		
		for (Post p : targetPosts) {
			double sToP = startingPoint.distanceTo(p);
			double pToT = p.distanceTo(targetBase);
			if (sToP < shortestDistance && pToT < distanceToTarget) {
				nearest = p;
				shortestDistance = startingPoint.distanceTo(p);
			}
		}
		
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
	
	public void move(ArrayList<Post> targetPosts, Location targetBase) {
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
				
				moveTowardLocation(targetLoc);
				break;
			
			case OVERLAPPING:
				moveApart(targetLoc);
				break;
		
			case DISCONNECTED:
				moveTogether();
				break;
				
			default: break;
		}
	}
	
	private void moveApart(Location targetLoc) {
		this.p1 = this.p1.moveMinimizingDistanceFrom(targetLoc);
	}
	
	private void moveTogether() {
		this.p2 = this.p2.moveMinimizingDistanceFrom(this.p1);
		if (this.p1.distanceTo(p2) > 1.0) {
			this.p1 = this.p1.moveMinimizingDistanceFrom(this.p2);
		}
	}
	
	private void moveTowardLocation(Location targetLoc) {
		this.p1 = this.p1.moveMinimizingDistanceFrom(targetLoc);
		this.p2 = this.p2.moveMinimizingDistanceFrom(this.p1);
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
