package Sudoku;

import java.util.Random;

public class SudokuBoard
{
    private int[][] board;
    private final int boardSizeBoxes; // boxes on each side of the board
    private final int boardSizeRowsColumns; // rows and columns on each side of the board
    private final int boxSizeRowsColumns; // rows and columns on each side of the boxes
    private final int maxPuzzleValue; // the maximum value at play in the puzzle
    private final int availableCells;
    private int filledCells;
    private final boolean isCustomBoard;
    private final boolean isStandardSize;
    private final boolean isSolverCandidate;
    private boolean createSolvableBoard;
    private Solver solver;
    private String errorMessage;

    /**
     * @author Danny, Abinav & Yahya
     */
    public SudokuBoard(int boardSizeBoxes, int boxSizeRowsColumns, boolean isCustomBoard)
    {
        this.boardSizeBoxes = boardSizeBoxes;
        this.boardSizeRowsColumns = boardSizeBoxes * boxSizeRowsColumns;
        this.boxSizeRowsColumns = boxSizeRowsColumns;
        maxPuzzleValue = boxSizeRowsColumns * boxSizeRowsColumns;
        availableCells = boardSizeRowsColumns * boardSizeRowsColumns;
        this.isCustomBoard = isCustomBoard;
        isStandardSize = boardSizeBoxes == 3 && boxSizeRowsColumns == 3;
        isSolverCandidate = (boardSizeRowsColumns * boardSizeRowsColumns) <= 81; // standard has 81 cells, we can only reliably solve this amount or less

        if(!isCustomBoard)
        {
            Random chooseSolvable = new Random();
            createSolvableBoard = boardSizeBoxes == 1 || boxSizeRowsColumns == 1 || isSolverCandidate && (0 < chooseSolvable.nextInt(1, 5));  // 0 = unsolvable (~20% chance), 1-4 = solvable (~80% chance)
        }

        do
        {
            initializeBoard(!isCustomBoard ? (int) (availableCells * 0.38) : 0);

            SudokuBoard boardForSolving = new SudokuBoard(this);
            solver = new Solver(boardForSolving);
        }
        while(!isCustomBoard && ((isSolverCandidate && (createSolvableBoard && !solve() || !createSolvableBoard && solve()))) || !solver.possibleValuesInCells());
    }

    /**
     * @author Danny & Abinav
     */
    public SudokuBoard(SudokuBoard boardToCopy)
    {
        board = new int[boardToCopy.boardSizeRowsColumns][boardToCopy.boardSizeRowsColumns];

        for(int row = 0; row < boardToCopy.boardSizeRowsColumns; row++)
        {
            System.arraycopy(boardToCopy.board[row], 0, board[row], 0, boardToCopy.boardSizeRowsColumns);
        }

        boardSizeBoxes = boardToCopy.boardSizeBoxes;
        boardSizeRowsColumns = boardToCopy.boardSizeRowsColumns;
        boxSizeRowsColumns = boardToCopy.boxSizeRowsColumns;
        maxPuzzleValue = boardToCopy.maxPuzzleValue;
        availableCells = boardToCopy.availableCells;
        filledCells = boardToCopy.filledCells;
        isCustomBoard = boardToCopy.isCustomBoard;
        isStandardSize = boardToCopy.isStandardSize;
        isSolverCandidate = boardToCopy.isSolverCandidate;
        createSolvableBoard = boardToCopy.createSolvableBoard;
        solver = null; // the solving board doesn't need a solver field
        errorMessage = boardToCopy.errorMessage;
    }

    /**
     * @author Danny & Abinav
     */
    private void initializeBoard(int filledFromStart)
    {
        board = new int[boardSizeRowsColumns][boardSizeRowsColumns];
        filledCells = 0;

        Random randomNumber = new Random();
        int value;
        int row;
        int column;

        while(filledCells != filledFromStart) // fill cells randomly until reaching the specified amount of filled cells
        {
            value = randomNumber.nextInt(1, maxPuzzleValue + 1);
            row = randomNumber.nextInt(0, boardSizeRowsColumns);
            column = randomNumber.nextInt(0, boardSizeRowsColumns);

            placeValueInCell(row, column, value);
        }
    }

    /**
     * Checks whether the given parameters are valid/follow the rules of Sudoku, and if that is the case, inserts the value
     * at the provided (row, column)-location.
     * @return True if placing the value in the cell was successful, false otherwise
     * @author Danny & Abinav
     */
    public boolean placeValueInCell(int row, int column, int value)
    {
        if((row < 0 || row > boardSizeRowsColumns - 1) || (column < 0 || column > boardSizeRowsColumns - 1))
        {
            errorMessage = "Only indices from 1-" + boardSizeRowsColumns + " are valid!";

            return false;
        }
        else if(value < 1 || value > maxPuzzleValue)
        {
            errorMessage = "Only values from 1-" + maxPuzzleValue + " are valid!";

            return false;
        }
        else if(board[row][column] != 0)
        {
            errorMessage = "Can't place a value in a filled cell!";

            return false;
        }
        else if(!checkPlacementRow(row, value) || !checkPlacementColumn(column, value) || !checkPlacementSubBoard(row, column, value))
        {
            errorMessage = "Value already in row, column or sub-board!";

            return false;
        }

        setBoardValue(row, column, value);

        return true;
    }

    /**
     * @author Yahya
     */
    public boolean checkPlacementRow(int row, int value)
    {
        for (int column = 0; column < boardSizeRowsColumns; column++ )
        {
            if(board[row][column] == value)
            {

                return false;
            }
        }

        return true;
    }

    /**
     * @author Abinav
     */
    public boolean checkPlacementColumn(int column, int value)
    {
        for (int row = 0; row < boardSizeRowsColumns; row++ )
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
     * @author Danny
     */
    public boolean checkPlacementSubBoard(int row, int column, int value)
    {
        int subBoard = findSubBoardNumber(row, column);
        int startingRow = (subBoard / boardSizeBoxes) * boxSizeRowsColumns;
        int startingColumn = boardSizeRowsColumns - (boxSizeRowsColumns * (boardSizeBoxes - (subBoard - (boardSizeBoxes * (subBoard / boardSizeBoxes)))));

        for(int addedRows = 0; addedRows < boxSizeRowsColumns; addedRows++)
        {
            for(int addedColumns = 0; addedColumns < boxSizeRowsColumns; addedColumns++)
            {
                if(board[startingRow + addedRows][startingColumn + addedColumns] == value)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @author Abinav
     */
    public int findSubBoardNumber(int row,int column)
    {
        return (row / boxSizeRowsColumns) * boardSizeBoxes + (column / boxSizeRowsColumns);
    }

    /**
     * @author Yahya
     */
    public boolean isGameFinished()
    {
        return filledCells == availableCells;
    }

    /**
     * @author Danny
     */
    public boolean solve()
    {
        if(isCustomBoard)
        {
            SudokuBoard boardForSolving = new SudokuBoard(this);
            solver = new Solver(boardForSolving);
        }

        return solver.solveBoard(isStandardSize);
    }

    /**
     * @author Danny
     */
    public void setBoardValue(int row, int column, int value)
    {
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

    public void setFilledCells(int value){
        this.filledCells = value;
    }

    /**
     * @author Danny
     */
    public int[][] getBoard()
    {
        return board;
    }

    /**
     * @author Danny
     */
    public int getBoardSizeBoxes()
    {
        return boardSizeBoxes;
    }

    /**
     * @author Danny
     */
    public int getBoardSizeRowsColumns()
    {
        return boardSizeRowsColumns;
    }

    /**
     * @author Danny
     */
    public int getBoxSizeRowsColumns()
    {
        return boxSizeRowsColumns;
    }

    /**
     * @author Danny
     */
    public int getMaxPuzzleValue()
    {
        return maxPuzzleValue;
    }

    /**
     * @author Danny
     */
    public int getAvailableCells()
    {
        return availableCells;
    }

    /**
     * @author Danny
     */
    public int getFilledCells()
    {
        return filledCells;
    }

    /**
     * @author Danny & Abinav
     */
    public boolean getIsCustomBoard()
    {
        return isCustomBoard;
    }

    /**
     * @author Danny
     */
    public boolean getIsStandardBoard()
    {
        return isStandardSize;
    }

    /**
     * @author Danny
     */
    public boolean getIsSolverCandidate()
    {
        return isSolverCandidate;
    }

    /**
     * @author Danny
     */
    public Solver getSolver()
    {
        return solver;
    }

    /**
     * @author Danny
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }
}
