package outpost.group3;

import java.util.ArrayList;

import outpost.group3.Board;
import outpost.group3.Loc;

public abstract class Strategy {
    public Strategy() {}

    /* Return a list of the target destinations of the outposts */
    public abstract void run(Board board, ArrayList<Outpost> outposts);
}
