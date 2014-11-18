package outpost.group1;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class GameParameters {

    public int r;
    public int l;
    public int w;
    public int t;

    public GameParameters() {
        this(0, 0, 0, 0);
    }

    public GameParameters(int r, int l, int w, int t) {
        this.r = r;
        this.l = l;
        this.w = w;
        this.t = t;
    }

}
