package outpost.group4;

import java.util.*;

public class Location {
    public int x;
    public int y;

    public Location(int x, int y) {
    	this.x = x;
    	this.y = y;
    }

    public Location(Location location) {
    	this.x = location.x;
    	this.y = location.y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Location))
            return false;

        Location l = (Location) o;

    	return x == l.x && y == l.y;
    }

    @Override
    public String toString() {
    	return "(" + Double.toString(x) + " " + Double.toString(y) + ")";
    }

    static public double distanceSquared(double x1, double y1, double x2, double y2) {
    	return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
    }

    static public double distance(double x1, double y1, double x2, double y2) {
    	return Math.sqrt(distanceSquared(x1, y1, x2, y2));
    }

    public double distanceSquared(Location comparison) {
    	return distanceSquaredTo(this, comparison);
    }

    public double distanceTo(Location comparison) {
    	return distanceTo(this, comparison);
    }

    static public double distanceSquaredTo(Location l1, Location l2) {
    	return distanceSquared(l1.x, l1.y, l2.x, l2.y);
    }

    static public double distanceTo(Location l1, Location l2) {
    	return distance(l1.x, l1.y, l2.x, l2.y);
    }

    static public boolean equals(Location l1, Location l2) {
    	return l1.x == l2.x && l1.y == l2.y;
    }

    static public boolean nearAny(Location target, ArrayList<Location> locations, double d) {
    	for (Location location : locations) {
    		if (distanceTo(target, location) < d)
    			return true;
    	}

    	return false;
    }

    public Location nearestLocation(ArrayList<? extends Location> locations) {
      Location nearestLocation = null;
      double minDist = Double.POSITIVE_INFINITY;

      for (Location location : locations) {
        double dist = distanceTo(location);
        if (dist < minDist) {
          minDist = dist;
          nearestLocation = location;
        }
      }

      return nearestLocation;
    }
}
