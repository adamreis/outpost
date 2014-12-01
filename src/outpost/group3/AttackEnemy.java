package outpost.group3;

import java.util.ArrayList;
import outpost.group3.Outpost;
public class AttackEnemy extends outpost.group3.Strategy{

	AttackEnemy() {}
	
	public void run(Board board, ArrayList<Outpost> outposts) {
		for (int i = 0; i < outposts.size(); i++) {
			Outpost outpost = outposts.get(i);
			
			if (outpost.getTargetLoc() == null) {
				if (i % (Consts.numPlayers - 1) == 0)
					outpost.setTargetLoc(new Loc(Board.dimension - 1, Board.dimension - 1));
				else if (i % (Consts.numPlayers - 1) == 1)
					outpost.setTargetLoc(new Loc(Board.dimension - 1, 0));
				else if (i % (Consts.numPlayers - 1) == 2)
					outpost.setTargetLoc(new Loc(0, Board.dimension - 1));
			}
		}	
	}
}