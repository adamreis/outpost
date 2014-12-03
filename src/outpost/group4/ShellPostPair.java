package outpost.group4;

import java.util.ArrayList;

public class ShellPostPair {
	public enum State {
		CONNECTED, OVERLAPPING, DISCONNECTED
	};

	public State state;
	public Post p1;
	public Post p2;
	public int enemyId;
	private boolean isStalled;

	public ShellPostPair(Post p1, Post p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.isStalled = false;
		this.enemyId = -1;
	}

	public ShellPostPair(ShellPostPair sPair) {
		this.p1 = new Post(sPair.p1);
		this.p2 = new Post(sPair.p2);
		this.isStalled = sPair.isStalled;
		this.enemyId = sPair.enemyId;
	}


	private boolean checkIfAdjacentToTargetLoc(Location targetLoc) {
		if (p1.distanceTo(targetLoc) == 1.0 || p2.distanceTo(targetLoc) == 1.0) {
			return true;
		}
		return false;
	}

	public void move(Location targetLoc) {
		if (this.p1.equals(targetLoc)) {
			return;
		}
		
		updateState();

		switch (this.state) {
		case CONNECTED:
			if (this.isStalled) {
				this.isStalled = false;
			} else if (this.checkIfAdjacentToTargetLoc(targetLoc)) {
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

		default:
			break;
		}

		return;

	}


	private void moveApart(Location targetLoc) {
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

	private void moveTowardLocation(Location targetLoc) {
		// System.out.println("moveTowardLocation called");
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
		int d = (int) this.p1.distanceTo(this.p2);
		if (d == 1) {
			this.state = State.CONNECTED;
		} else if (d == 0) {
			this.state = State.OVERLAPPING;
		} else {
			this.state = State.DISCONNECTED;
		}
	}
}
