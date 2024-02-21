package BasicSudoku;

import java.lang.Math;
import java.util.*;
public class Board
{
    private int n = 9;
    private int[][] board;
    private int boardSize = n;
    private int availableCells;
    private int filledCells = 0;

    public Board (){
        board = new int [boardSize][boardSize];

        for(int row = 0; row<= boardSize;row++){
            for(int column = 0; column <= boardSize; column++){
                board[row][column] = 0;
            }
        }
    }

    public int findSubBoardNumber(int row,int column)
    {
        int totalNoOfSubBoards = (int) Math.sqrt(boardSize);
        return (row/totalNoOfSubBoards)*totalNoOfSubBoards + (column/totalNoOfSubBoards)+1;
    }

}
