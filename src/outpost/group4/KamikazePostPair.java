package outpost.group4;

import java.util.ArrayList;


public class KamikazePostPair {
	public enum State {
		CONNECTED, OVERLAPPING, DISCONNECTED
	};
	
	public State state;
	public Post p1;
	public Post p2;
	private ArrayList<Post> targetPosts;
	
	public KamikazePostPair(Post p1, Post p2, ArrayList<Post> enemyPosts) {
		this.p1 = p1;
		this.p2 = p2;
		
		this.targetPosts = enemyPosts;
	}
	
	public void move() {
		updateState();
		Location targetLoc = this.p1.nearestLocation(this.targetPosts);
		
		switch (this.state) {
			case CONNECTED: 
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
