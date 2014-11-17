package outpost.group1;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class OutpostMath {

    public static double distance(Point a, Point b) {
      return Math.sqrt((a.x-b.x) * (a.x-b.x) + (a.y-b.y) * (a.y-b.y));
    }

}
