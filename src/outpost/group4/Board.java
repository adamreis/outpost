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

    public GridSquare[][] getGridSquares() {
        return board;
    }

    public ArrayList<GridSquare> availableTerritory(GridSquare square) {
        ArrayList<GridSquare> availableSquares = new ArrayList<GridSquare>();

        int size = Player.parameters.size;
        int radius = Player.parameters.outpostRadius;
        for (int i = -radius; i <= radius; i++) {
            for (int dist = 0; dist <= radius; dist++) {
                for (int j = -dist; j <= dist; j++) {
                    int x = square.x + i;
                    int y = square.y + j;
                    if (i + j != dist || x < 0 || y < 0 || x >= size || y >= size) continue;

                    GridSquare temp = board[x][y];
                    if (!temp.water && temp.owners.size() == 0) {
                        availableSquares.add(temp);
                    } 
                }
            } 
        }
        return availableSquares;
    }

    public ArrayList<GridSquare> getBestSquares() {
        ArrayList<GridSquare> bestSquares = new ArrayList<GridSquare>();

        int maxTerritory = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                int currTerritory = availableTerritory(board[i][j]).size();
                if (currTerritory > maxTerritory) {
                    maxTerritory = currTerritory;
                    bestSquares.clear();
                    bestSquares.add(board[i][j]);
                } else if (currTerritory == maxTerritory) {
                    bestSquares.add(board[i][j]);
                }
            }
        }

        return bestSquares;
    }

}

