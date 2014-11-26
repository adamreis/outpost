package outpost.group4;

import java.util.*;

public abstract class BoardHeuristic {

    public Board board;

    public BoardHeuristic(Board board) {
        this.board = board;
    }

    public abstract double score(GridSquare square);

    public ArrayList<GridSquare> getBestSquares() {
        ArrayList<GridSquare> bestSquares = new ArrayList<GridSquare>();

        GridSquare[][] squares = board.board;

        double maxScore = 0;
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares.length; j++) {
                double curScore = score(squares[i][j]);
                if (curScore > maxScore) {
                    maxScore = curScore;
                    bestSquares.clear();
                    bestSquares.add(squares[i][j]);
                } else if (curScore == maxScore) {
                    bestSquares.add(squares[i][j]);
                }
            }
        }

        return bestSquares;
    }

    public List<GridSquare> getTopSquares(int count) {
        List<GridSquare> topSquares = getSortedSquares().subList(0, count);
        return topSquares;
    }

    public ArrayList<GridSquare> getSortedSquares() {
        ArrayList<GridSquare> squares = board.getGridSquaresList();

        Collections.sort(squares, new Comparator<GridSquare>() {
            public int compare(GridSquare one, GridSquare other) {
                return Double.compare(score(other), score(one)); // do it opposite to get descending
            }
        });

        return squares;
    }

}
