package BasicSudoku;

import java.lang.Math;

public class Board
{
    private int[][] board;
    private final int boardSize;
    private final int subBoardsEachSide;
    private final int availableCells;
    private int filledCells = 0;

    public Board(int size)
    {
        // Danny
        boardSize = size * size;
        subBoardsEachSide = size;
        availableCells = boardSize * boardSize;

        board = new int [boardSize][boardSize];

        initializeBoard(PredefinedBoard.selectBoardRandomly());
    }

    private void initializeBoard(int[][] predefinedBoard)
    {
        // Abinav
        for(int row = 0; row < boardSize; row++)
        {
            for(int column = 0; column < boardSize; column++)
            {
                setBoardValue(row, column, predefinedBoard[row][column]);
            }
        }
    }

    public void placeValueInCell(int row, int column, int value)
    {
        // Abinav
        if(value < 1 || value > boardSize)
        {
            System.out.println("ERROR: Only values from 1-" + boardSize + " are valid!");

            return;
        }
        else if(!checkPlacementRow(row, value) || !checkPlacementColumn(column, value) || !checkPlacementSubBoard(row, column, value)) // Danny
        {
            System.out.println("ERROR: Value already in row, column or sub-board!");

            return;
        }

        setBoardValue(row, column, value);
    }

    private boolean checkPlacementRow(int row, int value)
    {
        // Yahya
        for (int column = 0;  column < boardSize; column++ )
        {
            if(board[row][column] == value)
            {
                return false;
            }
        }
        return true;
    }

    private boolean checkPlacementColumn(int column, int value)
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

    private boolean checkPlacementSubBoard(int row, int column, int value)
    {
        // Danny
        int subBoard = findSubBoardNumber(row, column);
        int startingRow = (subBoard / 3) * 3;
        int startingColumn = (subBoard - startingRow) * 3;

        for(int addToRow = 0; addToRow <= subBoardsEachSide - 1; addToRow++)
        {
            for(int addToColumn = 0; addToColumn <= subBoardsEachSide - 1; addToColumn++)
            {
                if(board[startingRow + addToRow][startingColumn + addToColumn] == value)
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
        int totalNoOfSubBoards = (int) Math.sqrt(boardSize);
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

    public int getSubBoardsEachSide()
    {
        // Danny
        return subBoardsEachSide;
    }
}
