
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

        initializeBoard(PredefinedBoard.selectBoardRandomly());
    }

    public void initializeBoard(int[][] predefinedBoard)
    {
        // temp method, so no author yet
        for(int row = 0; row < boardSize; row++)
        {
            for(int column = 0; column < boardSize; column++)
            {
                board[row][column] = predefinedBoard[row][column];
            }
        }

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
