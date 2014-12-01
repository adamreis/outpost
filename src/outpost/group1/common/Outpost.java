package outpost.group1.common;

public class Outpost {
    public static boolean PRINT_TARGETS = false;

    Point position;
    public Point getPosition() { return position; }
    public void setPosition(Point p) { position = p; }

    Commander parent; 
    public Commander getParent() { return parent; }

    public void setParent(Commander parent) { this.parent = parent; };

    boolean dead = false;

    public boolean isDead() { return dead; };
    void kill() { dead = true; };

    int id;
    private static int NEXT_ID = 0;

    public int move_id = 0;

    public int getMoveId() { return move_id; };

    public void updateMoveId(int new_id) {
        this.move_id = new_id;
    }

    public Outpost(Point position) {
        this.id = NEXT_ID++;
        this.position = position;
    }

    public Outpost(outpost.sim.Pair position) {
        this(new Point(position.x, position.y));
    }

    public int distanceTo(Point p) {
        return p.distanceTo(this.position);
    }

    public int distanceTo(Outpost o) {
        return this.position.distanceTo(o.position);
    }

    public int distanceTo(Tile t) {
        return this.position.distanceTo(t.asPoint());
    }

    public Tile getTarget(Game game) {
        if (nearOpposingCorner(game)) {
            target = closestOpposingCorner(game);
        }
        if (inDanger(game)) {
            if (PRINT_TARGETS) {
                System.out.format("Outpost %s in danger! Running home!\n", this, target);
            }
            target = runHome(game);
        }

        if (target == null || currentTile(game) == target) {
            if (parent.outpostCount() == 1) {
                target = closestSupportiveLandTile(game);
            } else {
                if (onSameSquareAsAnother(game)) {
                    target = getRandomTarget(game);
                } else {
                    target = highestValueCloseLandTile(game);
                }
            }

            if (PRINT_TARGETS) {
                System.out.format("Outpost %s selected target %s\n", this, target);
            }
        }

        return target;
    }
    public static int CORNER_DASH_DISTANCE = 45;
    public Tile closestOpposingCorner(Game game) {
        for (Point p : game.getOpposingCorners()) {
            if (this.distanceTo(p) < CORNER_DASH_DISTANCE) {
                game.getBoard().get(p);
            }
        }
        return null;
    }

    public boolean nearOpposingCorner(Game game) {
        for (Point p : game.getOpposingCorners()) {
            if (this.distanceTo(p) < CORNER_DASH_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    public Tile getRandomTarget(Game game) {
        return CollectionUtils.choice(currentTile(game).getImmediateLandNeighbors());
    }
    public boolean inDanger(Game game) {
        return dangerScore(game, currentTile(game)) > 1.0;
    }

    public double dangerScore(Game game, Tile t) {
        int close_opponents = 0;
        int close_allies = 0;

        for (Outpost o : game.getMyOutposts()) {
            if (t.distanceTo(o) < game.radius * 1.7) {
                close_allies += 1;
            }
        }

        for (Outpost o : game.getOpposingOutposts()) {
            if (t.distanceTo(o) < game.radius * 1.5) {
                close_opponents += 1;
                if (this.distanceTo(o) <= 3 && close_allies < 4) {
                    return 1000;
                }
            }
        }

        return close_opponents / (double)(close_allies);
    }

    public boolean onSameSquareAsAnother(Game game) {
        for (Outpost o : game.getMyOutposts()) {
            if (o == this) continue;
            if (o.getPosition().equals(this.getPosition())) {
                return true;
            }
        }
        return false;
    }

    public Tile runHome(final Game game) {
        final Outpost me = this;
        return CollectionUtils.best_golf(currentTile(game).getLandNeighbors(), new CollectionUtils.Score<Tile>() {
            public double score(Tile t) {
                double closest = Double.POSITIVE_INFINITY;
                for (Outpost o : game.getMyOutposts()) {
                    if (o == me) continue;
                    
                    double distance = o.distanceTo(t);
                    if (distance < closest) {
                        closest = distance;
                    }
                }

                return closest;
            };
        });
    }

    public Tile highestValueCloseLandTile(final Game game) {
         final Outpost me = this;
         return CollectionUtils.best(game.getBoard().getAllLandTiles(), new CollectionUtils.Score<Tile>() {
             public double score(Tile t) {
                 for (Outpost o : game.getMyOutposts()) {
                     if (o == me) continue;

                     if (o.distanceTo(t) < 1.5 * game.radius) {
                         return -500;
                     } else if (game.getTargets().containsKey(o) &&
                         t.asPoint().distanceTo(game.getTargets().get(o).asPoint()) < 1.5 * game.radius) {
                         return -500;
                     }
                 }

                 //return t.waterControlled() * 1000 - me.distanceTo(t);
                 return Math.pow(t.unitsSupported() * 10, 2) - me.distanceTo(t);
             }
         });
    }

    public Tile closestSupportiveLandTile(Game game) {
         final Outpost me = this;
         return CollectionUtils.best(game.getBoard().getAllLandTiles(), new CollectionUtils.Score<Tile>() {
             public double score(Tile t) {
                 if (t.unitsSupported() > 1.0) {
                     return -me.distanceTo(t);
                 } else return Double.NEGATIVE_INFINITY;
             }
         });
    }

    public Tile currentTile(Game g) {
        return g.getBoard().get(this.getPosition());
    }

    Tile target = null;
    Pathfinder pathfinder = new NaivePathfinder();

    Formation formation = null;
    void setFormation(Formation formation) { this.formation = formation; };
    public Move getMove(Game game) {
        if (formation != null) {
            return getMoveTo(formation.getMove(game, this));
        }

        target = getTarget(game);
        game.commitTarget(this, target);
        return getMoveTo(pathfinder.getPath(game, currentTile(game), target.asPoint()));
    }

    @Override
    public String toString() {
        return String.format("<Outpost %d belonging to %s at %s>", id, parent.isMe() ? "me" : "opponent", position);
    }

    public Move getMoveTo(Point destination) {
        if (destination == null) {
            System.out.format("Selected null destination for %s. Doing a no-move.\n", this);
            return new Move(position);
        }
        return new Move(position, destination);
    }
}
