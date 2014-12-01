package outpost.group1.common;

import java.util.*;

public class Commander {
    List<Outpost> outposts;
    Point corner;

    boolean is_me;
    public boolean isMe() { return is_me; }

    public List<Outpost> getOutposts() { return outposts; };
    public Point getCorner() { return corner; };

    public int outpostCount() { return outposts.size(); };

    public double distanceToMe(Tile t) {
        return corner.distanceTo(t.asPoint());
    }

    public int waterControlled(Game g) {
        return CollectionUtils.count(g.getMyTiles(), Tile.isWaterPredicate);
    }

    public int landControlled(Game g) {
        return CollectionUtils.count(g.getMyTiles(), Tile.isLandPredicate);
    }

    public int unitsSupported(Game g) {
        return 1 + Math.min(landControlled(g) / g.L,
                            waterControlled(g) / g.W);
    }

    public CollectionUtils.Score<Tile> closestToMeScore = new CollectionUtils.Score<Tile>() {
        public double score(Tile t) {
            return distanceToMe(t);
        }
    };

    public Commander(Point corner, List<Outpost> outposts, boolean is_me) {
        this.outposts = outposts;
        for (Outpost o : outposts) {
            o.setParent(this);
        }
        this.corner = corner;
        this.is_me = is_me;
    }

    public void addOutpost(Outpost o) {
        this.outposts.add(o);
    }

    public void removeOutpost(Outpost o) {
        this.outposts.remove(o);
    }
}
