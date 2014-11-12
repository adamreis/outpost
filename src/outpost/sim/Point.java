package outpost.sim;

import java.util.ArrayList;

import outpost.sim.Point;

public class Point {
    public int x;
    public int y;
    public boolean water;
    public double distance;
    //public int owner;
    public ArrayList<Pair> ownerlist = new ArrayList<Pair>();

    public Point() { x = 0; y = 0; water = false; }

    public Point(int xx, int yy, boolean wt) {
        x = xx;
        y = yy;
        water = wt;
       
    }

    public Point(Point o) {
        this.x = o.x;
        this.y = o.y;
        this.water = o.water;
    }

    
    public boolean equals(Point o) {
        return o.x == x && o.y == y ;
    }
}