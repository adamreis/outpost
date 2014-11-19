package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class GridSquare extends Location {

  boolean water;
  double distance;
  ArrayList<Post> owners;

  public GridSquare(Point p, ArrayList<Post> owners) {
    this(p.x, p.y, p.water, p.distance, owners);
  }

  public GridSquare(Location loc, boolean water, double distance, ArrayList<Post> owners) {
    this(loc.x, loc.y, water, distance, owners);
  }

  public GridSquare(int x, int y, boolean water, double distance, ArrayList<Post> owners) {
    super(x, y);

    this.water = water;
    this.distance = distance;
    this.owners = owners;
  }

}
