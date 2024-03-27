package BasicSudoku;

import java.lang.Math;

public class Board
{
    private int[][] board;
    private final int boardLengthWidth;
    private final int boardSize;
    private final int availableCells;
    private int filledCells = 0;

    public Board(int boardLengthWidth)
    {
        // Danny & Abinav
        this.boardLengthWidth = boardLengthWidth;
        boardSize = boardLengthWidth * boardLengthWidth;
        availableCells = boardSize * boardSize;

        board = new int [boardSize][boardSize];

        initializeBoard(PredefinedBoard.selectBoardRandomly());
    }

    private void initializeBoard(int[][] predefinedBoard)
    {
        // Abinav & Danny
        for(int row = 0; row < boardSize; row++)
        {
            for(int column = 0; column < boardSize; column++)
            {
                if(predefinedBoard[row][column] != 0)
                {
                    placeValueInCell(row, column, predefinedBoard[row][column]);
                }
                else
                {
                    board[row][column] = 0;
                }
            }
        }
    }

    /**
     * Checks whether the given parameters are valid/follow the rules of Sudoku, and if that is the case, inserts the value
     * at the provided (row, column)-location.
     * @return True if placing the value in the cell was successful, false otherwise
     */
    public boolean placeValueInCell(int row, int column, int value)
    {
        // Danny & Abinav
        if((row < 0 || row > boardSize - 1) || (column < 0 || column > boardSize - 1))
        {
            System.out.println("ERROR: Only indices from 1-" + boardSize + " are valid!");

            return false;
        }
        else if(value < 1 || value > boardSize)
        {
            System.out.println("ERROR: Only values from 1-" + boardSize + " are valid!");

            return false;
        }
        else if(!checkPlacementRow(row, value) || !checkPlacementColumn(column, value) || !checkPlacementSubBoard(row, column, value))
        {
            System.out.println("ERROR: Value already in row, column or sub-board!");

            return false;
        }

        setBoardValue(row, column, value);

        return true;
    }

    public boolean checkPlacementRow(int row, int value)
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

    /**
     * Checks if the sub-board already contains the given value.
     * It works by first finding the [0][0]-point of the sub-board in question and then checks each cell within this isolated
     * sub-board for the given value, only going as many rows down and columns out as fits the dimensions of the sub-boards.
     * @return True if value is not found, false otherwise
     */
    public boolean checkPlacementSubBoard(int row, int column, int value)
    {
        // Danny
        int subBoard = findSubBoardNumber(row, column);
        int startingRow = (subBoard / boardLengthWidth) * boardLengthWidth;
        int startingColumn = (subBoard - startingRow) * boardLengthWidth;

        for(int i = 0; i < boardLengthWidth; i++) // added to rows
        {
            for(int j = 0; j < boardLengthWidth; j++) // added to columns
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
    
    public boolean isGameFinished() 
    {
        // Yahya
        return filledCells == availableCells;
    }

    private void setBoardValue(int row, int column, int value)
    {
        // Danny
        if(value != 0)
        {
            filledCells++;
        }
        else
        {
            filledCells--;
        }

        board[row][column] = value;
    }

    public int[][] getBoard()
    {
        // Danny
        return board;
    }

    public int getBoardLengthWidth()
    {
        // Danny
        return boardLengthWidth;
    }

    public int getBoardSize()
    {
        // Danny
        return boardSize;
    }
}
