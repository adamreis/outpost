package outpost.group1.common;

import java.util.*;

public class GreedyPathFinder implements Pathfinder {
    List<Point> path = null;
    int index = -1;
    public Point getPath(Game game, final Tile current, final Point destination) {
        if (current.asPoint().equals(destination)) {
            return destination;
        }

        if (path == null || path.size() == index || !path.get(index - 1).equals(current.asPoint())){
            path = calculatePath(game, current.asPoint(), destination);

            if (path == null) {
                return current.asPoint();
            }
            index = 0;
        }
        return path.get(index++);
    }

    List<Point> calculatePath(Game game, Point current, Point destination) {
        List<Tile> tiles = new MoveSearchNode(null, game.getBoard().get(current), game).search(game.getBoard().get(destination));
        if (tiles == null) return null;
        else {
            List<Point> result = new ArrayList<Point>();
            for (Tile t : tiles) {
                result.add(t.asPoint());
            }
            return result;
        }
    }

    private static class MoveSearchNode {
        Set<Tile> next;
        MoveSearchNode parent;
        Tile me;

        Game game;
        public MoveSearchNode(MoveSearchNode parent, Tile location, Game game) {
            next = new HashSet<Tile>(location.getImmediateLandNeighbors());
            if (parent != null) {
                next.remove(parent.me);
            }

            this.parent = parent;
            me = location;
            game = game;
        }

        Queue<MoveSearchNode> search_queue = new ArrayDeque<MoveSearchNode>();

        public List<Tile> search(Tile destination) {
            for (Tile t : next) {
                search_queue.add(new MoveSearchNode(this, t, game));
            }


            Set<MoveSearchNode> to_add = new HashSet<MoveSearchNode>();
            int search_count = 0;;
            //System.out.format("Beginning search on %s\n", next);
            Set<Tile> seen = new HashSet<Tile>();
            do {
                to_add.clear();

                MoveSearchNode msn;
                while ((msn = search_queue.poll()) != null) {
                    /*
                    if (msn.parent != null) {
                        System.out.format("Examining %d %s -> %s\n", search_count, msn.parent.me.asPoint(), msn.me.asPoint());
                    } else {
                        System.out.format("Examining %d %s\n", search_count, msn.me.asPoint());
                    }
                    */

                    search_count++;
                    if (msn.isSuccess(destination)) {
                        //System.out.format("Found solution after %d steps.\n", search_count);
                        return msn.toMe();
                    } else if (!msn.isLeaf()) {
                        for (Tile t : msn.next) {
                            if (!seen.contains(t)) {
                                to_add.add(new MoveSearchNode(msn, t, game));
                                seen.add(t);
                            }
                        }
                    }
                }

                search_queue.addAll(to_add);
            } while (search_queue.size() > 0);

            return null;
        }

        public List<Tile> toMe() {
            List<Tile> path = new ArrayList<Tile>();

            MoveSearchNode it = this;
            while (it.parent != null) {
                path.add(it.me);
                it = it.parent;
            }
            Collections.reverse(path);

            return path;
        }

        public boolean isSuccess(Tile destination) {
            return next.contains(destination);
        }

        public boolean isLeaf() {
            return next.size() == 0;
        }
    }
};
