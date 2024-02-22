package BasicSudoku;

import java.lang.Math;

public class Board
{
    private int boardSize;
    private int[][] board;
    private int availableCells;
    private int filledCells = 0;

    public Board(int boardSize)
    {
        // Danny
        this.boardSize = boardSize;

        board = new int [boardSize][boardSize];

        availableCells = boardSize * boardSize;

        initializeBoard();
    }

    public void initializeBoard()
    {
        // temp method, so no author yet
        for(int row = 0; row < boardSize; row++)
        {
            for(int column = 0; column < boardSize; column++)
            {
                board[row][column] = 0;
            }
        }

        // row 0-2 and column 0-8
        setBoardValue(0, 0, 5);
        setBoardValue(0, 1, 3);
        setBoardValue(0, 4, 7);
        setBoardValue(1, 0, 6);
        setBoardValue(1, 3, 1);
        setBoardValue(1, 4, 9);
        setBoardValue(1, 5, 5);
        setBoardValue(2, 1, 9);
        setBoardValue(2, 2, 8);
        setBoardValue(2, 7, 6);

        // row 3-5 and column 0-8
        setBoardValue(3, 0, 8);
        setBoardValue(3, 4, 6);
        setBoardValue(3, 8, 3);
        setBoardValue(4, 0, 4);
        setBoardValue(4, 3, 8);
        setBoardValue(4, 5, 3);
        setBoardValue(4, 8, 1);
        setBoardValue(5, 0, 7);
        setBoardValue(5, 4, 2);
        setBoardValue(5, 8, 6);

        // row 6-8 and column 0-8
        setBoardValue(6, 1, 6);
        setBoardValue(6, 6, 2);
        setBoardValue(6, 7, 8);
        setBoardValue(7, 3, 4);
        setBoardValue(7, 4, 1);
        setBoardValue(7, 5, 9);
        setBoardValue(7, 8, 5);
        setBoardValue(8, 4, 8);
        setBoardValue(8, 7, 7);
        setBoardValue(8, 8, 9);
    }

    public int findSubBoardNumber(int row,int column)
    {
        // Abinav
        int totalNoOfSubBoards = (int) Math.sqrt(boardSize);
        return (row/totalNoOfSubBoards)*totalNoOfSubBoards + (column/totalNoOfSubBoards)+1;
    }

    public boolean checkPlacementColumn(int column, int value)
    {
        // Abinav
        for (int row = 0;  row < boardSize; row++ )
        {
            if(board[row][column] == value)
            {
                return false;
            }
        }
        return true;
    }
    
    public boolean checkPlacementRow(int row, int value)
    {
    	// Yahya
        for (int column = 0;  column < boardSize; column++ )
        {
            if(board[column][row] == value)
            {
                return false;
            }
        }
        return true;
    }	
    	
   
    public void setBoardValue(int row, int column, int value)
    {
        // Danny
        board[row][column] = value;
    }

    public int getBoardSize()
    {
        // Danny
        return boardSize;
    }

    public int[][] getBoard()
    {
        // Danny
        return board;
    }
}
