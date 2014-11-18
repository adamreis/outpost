package outpost.sim;

import java.util.ArrayList;

import outpost.sim.Point;

public abstract class Player {
    public  int id; 

    public Pair[] outposts;
    
    public Player(int id_in) {this.id = id_in;}
    
    public abstract void init() ;
    
    public abstract ArrayList<movePair> move(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] grid, int r, int L, int W, int T); // positions of all the outpost, playerid

    public abstract int delete(ArrayList<ArrayList<Pair>> king_outpostlist, Point[] gridin);
}
