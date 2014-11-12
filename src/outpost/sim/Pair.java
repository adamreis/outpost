package outpost.sim;

import outpost.sim.Pair;

public class Pair {
    public int x;
    public int y;

    public Pair() { x = 0; y = 0; }

    public Pair(int xx, int yy) {
        x = xx;
        y = yy;
    }

    public Pair(Pair o) {
        this.x = o.x;
        this.y = o.y;
    }

    public boolean equals(Pair o) {
        return o.x == x && o.y == y;
    }
}