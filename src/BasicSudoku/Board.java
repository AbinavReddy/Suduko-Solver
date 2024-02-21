
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
        this.boardSize = boardSize;

        board = new int [boardSize][boardSize];

        availableCells = boardSize * boardSize;

        initializeBoard(PredefinedBoard.selectBoardRandomly());
    }

    public void initializeBoard(int[][] predefinedBoard)
    {
        // initialize all elements to 0
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
        int totalNoOfSubBoards = (int) Math.sqrt(boardSize);
        return (row/totalNoOfSubBoards)*totalNoOfSubBoards + (column/totalNoOfSubBoards)+1;
    }

    public boolean checkPlacementColumn(int column, int value)
    {
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
        for (int column = 0;  column < boardSize; column++ )
        {
            if(board[row][column] == value)
            {
                return false;
            }
        }
        return true;
    }

    public boolean checkPlacementSubBoard(int row, int column, int value)
    {
        int sbn = findSubBoardNumber(row, column);

        for(int i = 0; i < boardSize; i++ )
        {
            for (int j = 0;  j < boardSize; j++ )
            {
                if(board[i][j] == value)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public void setBoardValue(int row, int column, int value)
    {
        board[row][column] = value;
    }

    public int getBoardSize()
    {
        return boardSize;
    }

    public int[][] getBoard()
    {
        return board;
    }
}
