package outpost.group4;

import java.util.ArrayList;


public class KamikazePostPair {
	public enum State {
		CONNECTED, OVERLAPPING, DISCONNECTED
	};
	
	public State state;
	public Post p1;
	public Post p2;
	public int targetId;
	private ArrayList<Post> targetPosts;
	
	public KamikazePostPair(Post p1, Post p2, int targetId, ArrayList<ArrayList<Post>> enemyPosts) {
		this.p1 = p1;
		this.p2 = p2;
		this.targetId = targetId;
		
		this.targetPosts = new ArrayList<Post>();
		for (ArrayList<Post> posts : enemyPosts) {
			this.targetPosts.addAll(posts);
		}
	}
	
	public void move() {
		updateState();
		Location targetLoc = this.p1.nearestLocation(this.targetPosts);
		
		switch (this.state) {
			case CONNECTED: 
				System.out.println("connected");
				this.p1 = this.p1.moveMinimizingDistanceFrom(targetLoc);
				Post newP2 = this.p2.moveMinimizingDistanceFrom(this.p1);
				newP2.id = this.p2.id;
				this.p2 = newP2;
				break;
			
			case OVERLAPPING:
				System.out.println("overlapping");
				this.p1 = this.p1.moveMinimizingDistanceFrom(targetLoc);
				break;
		
			case DISCONNECTED:
				System.out.println("disconnected");
				this.p2 = this.p2.moveMinimizingDistanceFrom(this.p1);
				if (this.p1.distanceTo(p2) > 1.0) {
					this.p1 = this.p1.moveMinimizingDistanceFrom(this.p2);
				}
				break;
				
			default: break;
		}
	}
	
	private void updateState() {
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
