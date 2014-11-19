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

    public GameParameters() {
        this(0, 0, 0, 0);
    }

    public GameParameters(int r, int l, int w, int t) {
        this.outpostRadius = r;
        this.requiredLand = l;
        this.requiredWater = w;
        this.totalTurns = t;
    }

}
