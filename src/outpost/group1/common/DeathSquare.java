package outpost.group1.common;

import java.util.*;

public class DeathSquare implements Formation {
    List<Outpost> outposts = new ArrayList<Outpost>();

    Point center;
    int radius = 1;

    public int id;
    private static int NEXT_ID = 0;

    Game game;
    public DeathSquare(Game game) {
        this.center = new Point(radius, radius);
        this.id = NEXT_ID++;
        this.game = game;

        printVictory();
    }

    public boolean isFull() { 
        for (Outpost o : outposts) {
            if (o.isDead()) {
                return false;
            }
        }
        return outposts.size() == 4; 
    };

    public void join(Outpost o) {
        System.out.format("%s has joined DeathSquare %d!\n", o, id);
        outposts.add(o);
        o.setFormation(this);
        if (isFull()) {
            radius = 0;
        }
    }

    Point target = null;

    Point getTarget() {
        final DeathSquare me = this;
        return CollectionUtils.best_golf(game.getOpposingOutposts(), new CollectionUtils.Score<Outpost>() {
            public double score(Outpost o) { return me.center.distanceTo(o.getPosition()); };
        }).getPosition();
    }

    Pathfinder pf = new NaivePathfinder();

    Point next_move = null;

    public void chooseTarget() {
        if (isFull()) {
            target = getTarget();
            System.out.format("Outpost at %s: prepare to die!\n", target);
            next_move = pf.getPath(game, game.getBoard().get(center), target);

            if (center.distanceTo(target) < game.radius * 3) {
                radius = game.radius / 2;
            }

            center = next_move;
        } else {
            disband();
        }
    }

    public void disband() {
        for (Outpost o : outposts) {
            o.setFormation(null);
        }
    }

    public Point getMove(Game g, Outpost o) {
        int index = outposts.indexOf(o);

        if (index == 3) {
            target = center.add(-radius, -radius);
        } else if (index == 0) {
            target = center.add(radius, -radius);
        } else if (index == 1) {
            target = center.add(radius, radius);
        } else if (index == 2) {
            target = center.add(-radius, radius);
        }
        //System.out.format("DeathSquare %d asks %s to walk to %s\n", id, o, target);

        return pf.getPath(g, o.currentTile(g), target);
    }

    public void printVictory() {
        System.out.println("|  __ \\|  ____|   /\\|__   __| |  | |  / ____|/ __ \\| |  | |  /\\   |  __ \\|  ____| | | |");
        System.out.println("| |  | | |__     /  \\  | |  | |__| | | (___ | |  | | |  | | /  \\  | |__) | |__  | | | |");
        System.out.println("| |  | |  __|   / /\\ \\ | |  |  __  |  \\___ \\| |  | | |  | |/ /\\ \\ |  _  /|  __| | | | |");
        System.out.println("| |__| | |____ / ____ \\| |  | |  | |  ____) | |__| | |__| / ____ \\| | \\ \\| |____|_|_|_|");
        System.out.println("|_____/|______/_/    \\_\\_|  |_|  |_| |_____/ \\___\\_\\\\____/_/    \\_\\_|  \\_\\______(_|_|_)");
    }
}
