package outpost.sim;

import outpost.sim.movePair;

public class movePair {

	public int id;
    public Pair pr;
    //boolean delete;

    public movePair() {  }

    public movePair(int id_in, Pair pr_in) {
        this.id = id_in;
        this.pr = pr_in;
      //  this.delete = deletein;
    }
    
    public void printmovePair() {
    	System.out.printf("(%d, (%d, %d))", id, pr.x, pr.y);
    }
}