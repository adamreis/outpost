package outpost.group8;

import java.util.ArrayList;
import java.util.Random;


// 0, 0 is the top left. x's increase to the right, y's increase down.

import outpost.sim.Pair;
import outpost.sim.Point;

public class Location {
	public double id;
	public int x;
    public int y;
    public boolean water;
    public double distance;
    public Location parent;
    public int value;
    
    //public int owner;
    public ArrayList<Pair> ownerlist = new ArrayList<Pair>();

    public Location() {
    	x = 0;
    	y = 0;
    	water = false;
    	parent = null;
    	id = Math.random();
    }

    public Location(int xx, int yy, boolean wt) {
        x = xx;
        y = yy;
        water = wt;
        parent = null;
    	id = Math.random();
    }

    public Location(Point o) {
        this.x = o.x;
        this.y = o.y;
        this.water = o.water;
        parent = null;
    	id = Math.random();
    }
    public Location(Pair o) {
        this.x = o.x;
        this.y = o.y;
        water = false;
        parent = null;
    	id = Math.random();
    }
    public Location(Location o) {
        this.x = o.x;
        this.y = o.y;
        this.water = o.water;
        parent = null;
    	id = Math.random();
    }
    
    public boolean idEquals(Location o) {
    	return o.id == id;
    }
    public boolean equals(Point o) {
        return o.x == x && o.y == y ;
    }
    public boolean equals(Location o) {
        return o.x == x && o.y == y ;
    }
    public boolean equals(Pair o) {
    	return o.x == x && o.y == y ;
    }
    
}
