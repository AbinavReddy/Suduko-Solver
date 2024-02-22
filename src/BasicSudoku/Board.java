
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
                setBoardValue(row, column, predefinedBoard[row][column]);
            }
        }
    }

    public void placeValueInCell(int row, int column, int value)
    {
        // Danny
        if(!checkPlacementRow(row, value) || !checkPlacementColumn(column, value) || !checkPlacementSubBoard(row, column, value))
        {
            System.out.println("Invalid placement!");

            return;
        }

        // abinav

        if(value > 0 && value <= boardSize*boardSize) {
            setBoardValue(row, column, value);
        }
    }

    public boolean checkPlacementRow(int row, int value)
    {
        // Yahya
        for (int column = 0;  column < boardRowsColumns; column++ )
        {
            if(board[row][column] == value)
            {
                return false;
            }
        }
        return true;
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

    public boolean checkPlacementSubBoard(int row, int column, int value)
    {
        // Danny
        int subBoard = findSubBoardNumber(row, column);
        int startingRow = (subBoard / 3) * 3;
        int startingColumn = (subBoard - startingRow) * 3;

        for(int i = 0; i <= boardSize - 1; i++)
        {
            for(int j = 0; j <= boardSize - 1; j++)
            {
                if(board[startingRow + i][startingColumn + j] == value)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public int findSubBoardNumber(int row,int column)
    {
        // Abinav
        int totalNoOfSubBoards = (int) Math.sqrt(boardRowsColumns);
        return (row/totalNoOfSubBoards)*totalNoOfSubBoards + (column/totalNoOfSubBoards);
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
