
package BasicSudoku;

import java.lang.Math;

public class Board
{
    private int[][] board;
    private int boardSize;
    private int boardRowsColumns;
    private int availableCells;
    private int filledCells = 0;

    public Board(int boardSize)
    {
        // Danny
        this.boardSize = boardSize;
        boardRowsColumns = boardSize * 3;
        availableCells = boardRowsColumns * boardRowsColumns;

        board = new int [boardRowsColumns][boardRowsColumns];

        initializeBoard(PredefinedBoard.selectBoardRandomly());
    }

    public void initializeBoard(int[][] predefinedBoard)
    {
        // Abinav
        for(int row = 0; row < boardRowsColumns; row++)
        {
            for(int column = 0; column < boardRowsColumns; column++)
            {
                board[row][column] = predefinedBoard[row][column];
            }
        }
    }

    public int findSubBoardNumber(int row,int column)
    {
        // Abinav
        int totalNoOfSubBoards = (int) Math.sqrt(boardRowsColumns);
        return (row/totalNoOfSubBoards)*totalNoOfSubBoards + (column/totalNoOfSubBoards)+1;
    }

    public boolean checkPlacementColumn(int column, int value)
    {
        // Abinav
        for (int row = 0;  row < boardRowsColumns; row++ )
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
        for (int column = 0;  column < boardRowsColumns; column++ )
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

        filledCells++;
    }

    public int[][] getBoard()
    {
        // Danny
        return board;
    }

    public int getBoardSize()
    {
        // Danny
        return boardSize;
    }

    public int getBoardRowsColumns()
    {
        // Danny
        return boardRowsColumns;
    }
}
