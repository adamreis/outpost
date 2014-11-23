package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class GameParameters {

    public int outpostRadius;
    public int requiredLand;
    public int requiredWater;
    public int totalTurns;
    public int size;

    public GameParameters() {
        this(0, 0, 0, 0, 100);
    }

    public GameParameters(int r, int l, int w, int t, int s) {
        this.outpostRadius = r;
        this.requiredLand = l;
        this.requiredWater = w;
        this.totalTurns = t;
        this.size = s;
    }

}
