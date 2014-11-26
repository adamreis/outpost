
package outpost.group4;

import java.util.*;

public class DistanceWeighingHeuristic extends BoardHeuristic {

    private static final double DEFAULT_WEIGHTING_FACTOR = 0.75;
    private static final int DEFAULT_NUMBER_OF_BUCKETS = 12;
    private static final int MAX_DIST = 200; // diagonal base

    public BoardHeuristic heuristicToMinimize;
    public double weightingFactor;
    public int numberOfBuckets;
    public int threshold;

    public DistanceWeighingHeuristic(BoardHeuristic heuristic, int threshold) {
        super(heuristic.board);

        this.heuristicToMinimize = heuristic;
        this.weightingFactor = DEFAULT_WEIGHTING_FACTOR;
        this.numberOfBuckets = DEFAULT_NUMBER_OF_BUCKETS;
        this.threshold = threshold;
    }

    public double score(GridSquare square) {
        double s = heuristicToMinimize.score(square);
        return weightScore(s, square);
    }

    public double weightScore(double score, Location location) {
      if (Player.baseLoc.withinThreshold(location, threshold)) { // our quadrant, full score
        return score;
      }
      else { // other quadrent, weight by bucket.
        double dist = Location.distance(location, Player.baseLoc);
        int bucket = (int) ((dist / MAX_DIST) * numberOfBuckets);
        double factor = 1 - bucket * weightingFactor;
        return score * factor;
      }
    }
}
