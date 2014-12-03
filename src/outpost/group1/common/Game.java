package outpost.group1.common;

import java.util.*;

public class Game {
    Board board;
    public Board getBoard() { return board; }
    Commander[] commanders = null;

    public int turn = 1;

    public List<Outpost> getMyOutposts() {
        return commanders[my_id].getOutposts();
    }


    List<Outpost> opponents = null;
    public List<Outpost> getOpposingOutposts() {
        if (opponents == null) {
            opponents = new ArrayList<Outpost>();
            for (int i = 0; i < commanders.length; i++) {
                if (i == my_id) continue;
                opponents.addAll(commanders[i].getOutposts());
            }
        }

        return opponents;
    }

    List<Outpost> all = null;
    public List<Outpost> getAllOutposts() {
        if (all == null) {
            all = new ArrayList<Outpost>();
            for (int i = 0; i < commanders.length; i++) {
                if (i == my_id) continue;
                all.addAll(commanders[i].getOutposts());
            }
        }

        return all;
    }

    Map<Tile, Set<Outpost>> owners_by_point = null;
    Map<Tile, Set<Commander>> commanders_by_point = null;

    public List<Point> getOpposingCorners() {
        List<Point> result = new ArrayList<Point>();
        for (int i = 0; i < 4; i++) {
            if (i == my_id) continue;
            result.add(commanders[i].getCorner());
        }
        return result;
    }

    public Map<Tile, Set<Outpost>> getOwnersByPoint() {
        return getOwnersByPoint(false);
    }

    public Map<Tile, Set<Outpost>> getOwnersByPoint(boolean reset) {
        if (owners_by_point == null || reset) {
            owners_by_point = new HashMap<Tile, Set<Outpost>>();
            commanders_by_point = new HashMap<Tile, Set<Commander>>();

            for (int i = 0; i < 4; i++) {
                List<Outpost> outposts = commanders[i].getOutposts();

                for (Outpost o : outposts) {
                    for (Tile t : o.currentTile(this).getNeighbors()) {
                        if (!owners_by_point.containsKey(t)) {
                            owners_by_point.put(t, new HashSet<Outpost>());
                        }
                        if (!commanders_by_point.containsKey(t)) {
                            commanders_by_point.put(t, new HashSet<Commander>());
                        }
                        commanders_by_point.get(t).add(commanders[i]);
                        owners_by_point.get(t).add(o);
                    }
                }
            }
        }

        return owners_by_point;
    }

    List<Tile> my_tiles = null;
    public List<Tile> getMyTiles() {
        return getMyTiles(false);
    }
    public List<Tile> getMyTiles(boolean reset) {
        if (my_tiles == null || reset) {
            my_tiles = new ArrayList<Tile>();

            for (Tile t : getOwnersByPoint(reset).keySet()) {
                if (isOwnedByMe(t)) {
                    my_tiles.add(t);
                }
            }
        }

        return my_tiles;
    }

    public boolean isOwnedBy(Tile t, Outpost o) {
        return owners_by_point.get(t).contains(o);
    }

    public boolean isOwnedByMe(Tile t) {
        Set<Commander> commanders_that_own_t = commanders_by_point.get(t);
        return commanders_that_own_t.size() == 1 && commanders_that_own_t.iterator().next() == getMe();
    }

    public Commander getMe() { return commanders[my_id]; };

    int my_id;

    Map<Outpost, Tile> targets = null;


    public int radius = 10;
    public int L;
    public int W;
    public Game(int id, outpost.sim.Point[] game_board, int r, int L, int W) {
        my_id = id;
        board = new Board(game_board, r, L, W);
        radius = r;
        this.L = L;
        this.W = W;
    }

    private List<Outpost> outpostsFromPairs(List<outpost.sim.Pair> pairs) {
        List<Outpost> posts = new ArrayList<Outpost>();
        for (outpost.sim.Pair p : pairs) {
            posts.add(new Outpost(p));
        }
        return posts;
    }

    public Map<Outpost, Tile> getTargets() {
        if (targets == null) {
            targets = new HashMap<Outpost, Tile>();
        }
        return targets;
    }
    public void commitTarget(Outpost o, Tile t) {
        this.getTargets().put(o,t);
    }

    Map<Point, Set<Outpost>> my_outposts = null;

    public void loadOutposts(ArrayList<ArrayList<outpost.sim.Pair>> outposts) {
        turn += 1;
        targets = null;
        owners_by_point = null;
        my_tiles = null;
        opponents = null;
        all = null;
        Point[] corners = Rectangle.BOARD_RECTANGLE.getCorners();

        for (Formation f : formations) {
            f.chooseTarget();
        }

        if (my_outposts == null) {
            commanders = new Commander[4];
            my_outposts = new HashMap<Point, Set<Outpost>>();
            commanders[my_id] = new Commander(corners[my_id], outpostsFromPairs(outposts.get(my_id)), true);
        } else {
            reloadOutposts(outposts.get(my_id));
        }

        for (int i = 0; i < 4; i++) {
            if (i == my_id) continue;
            commanders[i] = new Commander(corners[i], 
                                          outpostsFromPairs(outposts.get(i)),
                                          i == my_id);
        }
    }

    boolean constructingFormation() {
        return false;
        //return getMyOutposts().size() > 10 && (getMyOutposts().size() % 10 < 5);
    }

    Formation under_construction = null;
    List<Formation> formations = new ArrayList<Formation>();
    private void constructNewOutpost(Point pt) {
        Outpost new_outpost = new Outpost(pt);
        new_outpost.setParent(getMe());
        getMe().addOutpost(new_outpost);

        if (!my_outposts.containsKey(pt)) {
            my_outposts.put(pt, new HashSet<Outpost>());
        }
        my_outposts.get(pt).add(new_outpost);

        if (constructingFormation() && pt.equals(new Point(0,0))) {
            if (under_construction == null) {
                under_construction = new DeathSquare(this);
            }

            under_construction.join(new_outpost);

            if (under_construction.isFull()) {
               System.out.format("Completed DeathSquare %d.\n", formations.size());
               formations.add(under_construction); 
               under_construction = null;
            }
        }
    }

    private void reloadOutposts(ArrayList<outpost.sim.Pair> new_positions) {
        Set<Point> positions = new HashSet<Point>();

        for (outpost.sim.Pair p: new_positions) {
            Point pt = new Point(p);
            positions.add(pt);

            if (!my_outposts.containsKey(pt) || my_outposts.get(pt).size() == 0) {
                constructNewOutpost(pt);
            }
        }

        for (Point p : my_outposts.keySet()) {
            if (!positions.contains(p)) {
                for (Outpost o : my_outposts.get(p)) {
                    getMe().removeOutpost(o);
                    o.kill();
                }
            }
        }

        for (int i = 0; i < new_positions.size(); i++) {
            Point pt = new Point(new_positions.get(i));
            Outpost o = my_outposts.get(pt).iterator().next();

            o.updateMoveId(i);
        }
    }

    public void updateOutpost(Outpost o, Move m) {
        Point start = m.getStart();
        Point end = m.getEnd();

        if (my_outposts.containsKey(start)) {
            my_outposts.get(start).remove(o);
        }

        if (!my_outposts.containsKey(end)) {
            my_outposts.put(end, new HashSet<Outpost>());
        }

        my_outposts.get(end).add(o);
        o.setPosition(end);
    }
}
