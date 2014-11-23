package outpost.group4;

import java.util.*;

import outpost.sim.Pair;
import outpost.sim.Point;
import outpost.sim.movePair;

public class Board {

    public GridSquare[][] board;
    
    public Board(Point[] points) {
        board = Conversions.gridSquaresFromPoints(points);
    }

    public ArrayList<GridSquare> availableTerritory(GridSquare square) {
        ArrayList<GridSquare> availableSquares = new ArrayList<GridSquare>();

        //int x = square.x;
        //int y = square.y;
        int size = GameParameters.size;
        int radius = GameParameters.outpostRadius;
        //for (int i = x - radius; i <= (x + radius) && i >= 0 && i <= size; i++) {
        //    for (int j = 0; j < radius && (j + y) >= 0 && (j - y) <= size; j++) {
        //        GridSquare tmp = board[i][j];
        //        if (tmp.owners.size() == 0)
        //            availableSquares.add(tmp);
        //    }
        //}

        for (int i = -radius; i <= radius; i++) {
            for (int dist = 0; dist <= radius; dist++) {
                for (int j = -dist; j <= dist; j++) {
                    int x = square.x + i;
                    int y = square.y + j;
                    if (i + j != dist || x < 0 || y < 0 || x >= size || y >= size) continue;

                    GridSquare temp = board[x][y];
                    if (temp.owners.size() == 0) {
                        availableSquares.add(temp);
                    }
                }
            } 
        }
      


    }
}

