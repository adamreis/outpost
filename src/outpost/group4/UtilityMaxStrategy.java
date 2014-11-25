package outpost.group4;

import java.util.*;

public class UtilityMaxStrategy implements Strategy {

    ArrayList<Post> posts;
    ArrayList<ArrayList<Post>> otherPlayerPosts;
    ArrayList<GridSquare> bestSquares;

    public ArrayList<Post> move(ArrayList<ArrayList<Post>> otherPlayerPosts, ArrayList<Post> posts, boolean newSeason) {
        this.posts = posts;
        this.otherPlayerPosts = otherPlayerPosts;

        ArrayList<Post> newPosts = new ArrayList<Post>();
        BoardHeuristic waterHeuristic = new MaxWaterHeuristic(Player.board);
        BoardHeuristic heuristic = new DistanceWeighingHeuristic(waterHeuristic);

        if (newSeason) {
            bestSquares = heuristic.getBestSquares();
            System.out.println(bestSquares.size());

            // sort bestSquares by closeness to baseLoc
            Collections.sort(bestSquares, new Comparator<GridSquare>() {
                public int compare(GridSquare one, GridSquare other) {
                    if (Player.baseLoc.distanceTo(one) > Player.baseLoc.distanceTo(other)) {
                        return 1;
                    } else if (Player.baseLoc.distanceTo(one) == Player.baseLoc.distanceTo(other)) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
        }

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);

            // sort bestSquares by closeness to p
            //Collections.sort(bestSquares, new Comparator<GridSquare>() {
            //    public int compare(GridSquare one, GridSquare other) {
            //        if (p.distanceTo(one) > p.distanceTo(other)) {
            //            return 1;
            //        } else if (p.distanceTo(one) == p.distanceTo(other)) {
            //            return 0;
            //        } else {
            //            return -1;
            //        }
            //    }
            //});
            //ArrayList<Location> path = p.shortestPathToLocation((Location) bestSquares.get(0));

            Post newPost = p;

            Post emergency = emergencyMove(p);
            if (emergency != null) {
                newPost = emergency;
            }
            else {
                int squareIndex = (i * Player.parameters.outpostRadius) % bestSquares.size();
                ArrayList<Location> path = p.shortestPathToLocation(bestSquares.get(squareIndex));

                int nextLocationIndex = (path.size() > 1)? 1 : 0;
                newPost.x = path.get(nextLocationIndex).x;
                newPost.y = path.get(nextLocationIndex).y;
            }

            newPosts.add(newPost);
        }

        return newPosts;
    }

    public Post emergencyMove(Post p) {
        for (ArrayList<Post> posts : otherPlayerPosts) {
            for (Post opponentPost : posts) {
                if (p.isLocationUnderInfluence(opponentPost)) {
                    System.out.printf("EMERGENCY!!!! %s vsvsvsvsvsvs %s\n", p, opponentPost);
                    return p.moveMaximizingDistanceFrom(opponentPost);
                }
            }
        }

        return null;
    }
}
