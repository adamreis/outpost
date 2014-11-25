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

    public ArrayList<GridSquare> getGridSquaresList() {
        ArrayList<GridSquare> squares = new ArrayList<GridSquare>();

        for (GridSquare[] rowArray : board) {
            for (GridSquare square : rowArray) {
                squares.add(square);
            }
        }

        return squares;
    }

    public ArrayList<GridSquare> filteredSquaresWithinRadius(GridSquare square, GridSquareFilter filter) {
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
                    if (filter.squareIsValid(temp)) {
                        availableSquares.add(temp);
                    }
                }
            }
        }

        return availableSquares;
    }

}
