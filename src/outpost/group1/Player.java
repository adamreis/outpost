package outpost.group1;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.movePair;

import outpost.group1.common.Game;
import outpost.group1.common.Outpost;
import outpost.group1.common.Move;

public class Player extends outpost.sim.Player {
    public Player(int id_in) {
        super(id_in);
    }
    
    public void init() {
        System.out.format("group1 has id %d\n", id);
    }

    public static final boolean PRINT_MOVES = false;

    Game game = null;
    public ArrayList<movePair> move(ArrayList<ArrayList<Pair>> outposts, outpost.sim.Point[] board, int r, int L, int W, int T) {
        if (game == null) {
            loadGame(outposts, board, r, L, W);
            game.loadOutposts(outposts);
        } else {
            game.loadOutposts(outposts);
        }

        ArrayList<movePair> moves = new ArrayList<movePair>();

        printSummary(game);
        for (Outpost o : game.getMyOutposts()) {
            Move m = o.getMove(game);
            moves.add(m.toMovePair(o.getMoveId()));
            if (PRINT_MOVES) {
                System.out.format("%s moves %s\n", o, m);
            }

            game.updateOutpost(o, m);
        }
        return moves;
    }

    private void loadGame(ArrayList<ArrayList<Pair>> outposts, outpost.sim.Point[] board, int r, int L, int W) {
        game = new Game(id, board, r, L, W);
        System.out.println(game.getBoard().valueString());
    }

    public int delete(ArrayList<ArrayList<Pair>> outposts, outpost.sim.Point[] board) {
        // delete the oldest outpost
        return 0;
    }

    public void printSummary(Game game) {
        int water = game.getMe().waterControlled(game);
        System.out.format("[LOG][GROUP1] turn %d - water: \033[1;34m%d\033[0m land: \033[1;32m%d\033[0m outposts: \033[1;36m%d\033[0m cansupport: \033[1m%d\033[0m\n", 
                game.turn,
                water,
                game.getMe().landControlled(game), 
                game.getMyOutposts().size(),
                game.getMe().unitsSupported(game));
    }
}
