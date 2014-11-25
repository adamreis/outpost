
package outpost.group4;

import java.util.*;

public class DistanceWeighingHeuristic extends BoardHeuristic {

    private static final double DEFAULT_WEIGHTING_FACTOR = 0.75;
    private static final int DEFAULT_NUMBER_OF_BUCKETS = 12;
    private static final int MAX_DIST = 200; // diagonal base

    public BoardHeuristic heuristicToMinimize;
    public double weightingFactor;
    public int numberOfBuckets;
    public boolean verbose;

    public DistanceWeighingHeuristic(BoardHeuristic heuristic) {
        super(heuristic.board);

        heuristicToMinimize = heuristic;
        weightingFactor = DEFAULT_WEIGHTING_FACTOR;
        numberOfBuckets = DEFAULT_NUMBER_OF_BUCKETS;

        verbose = false;
    }

    public double score(GridSquare square) {
        double s = heuristicToMinimize.score(square);

        double dist = Location.distanceTo(square, Player.baseLoc);
        int bucket = (int) ((dist / MAX_DIST) * numberOfBuckets);

        if (bucket <= numberOfBuckets / 4 + 1) { // our quadrant, full score
            return s;
        }
        else { // other quadrent, weight by bucket.
            double factor = 1 - bucket * weightingFactor;
            return s * factor;
        }
    }
}
