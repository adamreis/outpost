package outpost.group2;

import outpost.sim.Pair;

public class Outpost extends Pair {
	public int id;
	public int waterControlled;
	public int landControlled;
	
	public Outpost(Pair pr, int id) {
		super(pr);
		this.id = id;
		waterControlled = 0;
		landControlled = 0;
	}
}