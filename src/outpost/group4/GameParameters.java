package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class GameParameters {

		public static final int NUM_PLAYERS = 4;

		public int outpostRadius;
		public int requiredLand;
		public int requiredWater;
		public int totalTurns;
		public int size;

		public GameParameters() {
				this(0, 0, 0, 0, 100);
		}

		public GameParameters(int r, int l, int w, int t, int s) {
				this.outpostRadius = r;
				this.requiredLand = l;
				this.requiredWater = w;
				this.totalTurns = t;
				this.size = s;
		}

		public int landNeededForOutposts(int outposts) {
				return Math.max(outposts - 1, 0) * this.requiredLand;
		}

		public int waterNeededForOutposts(int outposts) {
				return Math.max(outposts - 1, 0) * this.requiredWater;
		}

		public double landWaterRatio() {
				return requiredLand / (double) requiredWater;
		}

		public int outpostsSupportedWithResources(int landCells, int waterCells) {
				double ratio = landCells / (double) waterCells;
				boolean waterIsLimiting = ratio >= landWaterRatio();
				int cellsPossible = waterIsLimiting? waterCells / this.requiredWater : landCells / this.requiredLand;
				return cellsPossible + 1; // that (n - 1) formula
		}

}
