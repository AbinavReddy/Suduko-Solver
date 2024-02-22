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

    /**
     * Checks if the sub-board already contains the given value.
     * It works by first finding the [0][0]-point of the sub-board in question and then checks each value within this isolated
     * sub-board for the given value, only going as many rows down and columns out as fits the dimensions of the sub-boards.
     * @return True if value is not found, false otherwise
     */
    private boolean checkPlacementSubBoard(int row, int column, int value)
    {
        // Danny
        int subBoard = findSubBoardNumber(row, column);
        int startingRow = (subBoard / subBoardsEachSide) * subBoardsEachSide;
        int startingColumn = (subBoard - startingRow) * subBoardsEachSide;

        for(int i = 0; i <= subBoardsEachSide - 1; i++) // added to rows
        {
            for(int j = 0; j <= subBoardsEachSide - 1; j++) // added to columns
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
