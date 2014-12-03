package outpost.group6;

import java.util.*;
import outpost.sim.Pair;

public class Post {

	public int id;
	public Pair current;
	public Pair target;
	public int w_value;
	public int l_value;
	public int r_value;

	public boolean targetSet;

	public Post(int id) {
		this.id = id;
		targetSet = false;
	}

	public Post() {
		targetSet = false;
	}

	public String toString() {
		return "Id: " + id + " current: " + current.x + "," + current.y + " target: " + target.x + "," + target.y + " w: " + w_value + " l: " + l_value + " r: " + r_value;
	}
}