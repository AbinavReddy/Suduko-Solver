package BasicSudoku;

import java.util.Random;

public class SudokuBoard
{
    private int[][] board;
    private final int boardSize;
    private final int boardLengthWidth;
    private final int availableCells;
    private int filledCells;
    private boolean isBoardSolvable;
    private Solver solver;
    private String errorMessage;

    /**
     * @author Danny, Abinav & Yahya
     */
    public SudokuBoard(int boardSize)
    {
        this.boardSize = boardSize;
        boardLengthWidth = (int) Math.sqrt(boardSize);
        availableCells = boardSize * boardSize;

        /*
        board = new int[boardSize][boardSize];

        filledCells = 0;

        initializeBoardTemp(PredefinedBoard.selectBoardRandomly()); // temp

        SudokuBoard boardForSolving = new Board(this);
        solver = new Solver(boardForSolving);

        solver.possibleValuesInCells();
        */

        Random chooseSolvable = new Random();
        isBoardSolvable = boardSize <= 9 && (0 < chooseSolvable.nextInt(1, 5)); // 0 = unsolvable (~20% chance), 1-4 = solvable (~80% chance)

        do
        {
            initializeBoard((int) (availableCells * 0.38));

            SudokuBoard boardForSolving = new SudokuBoard(this);
            solver = new Solver(boardForSolving);
        }
        while((boardSize <= 9 && (isBoardSolvable && !solveBoard() || !isBoardSolvable && solveBoard())) || !solver.possibleValuesInCells());
    }

    /**
     * @author Danny & Abinav
     */
    public SudokuBoard(SudokuBoard boardToCopy)
    {
        board = new int[boardToCopy.boardSize][boardToCopy.boardSize];

        for(int row = 0; row < boardToCopy.boardSize; row++)
        {
            System.arraycopy(boardToCopy.board[row], 0, board[row], 0, boardToCopy.boardSize);
        }

        boardLengthWidth = boardToCopy.boardLengthWidth;
        boardSize = boardToCopy.boardSize;
        availableCells = boardToCopy.availableCells;
        filledCells = boardToCopy.filledCells;
        isBoardSolvable = boardToCopy.isBoardSolvable;
        solver = null; // the solving board doesn't need a solver field
        errorMessage = boardToCopy.errorMessage;
    }

    /**
     * @author Danny & Abinav
     */
    private void initializeBoard(int filledFromStart)
    {
        board = new int[boardSize][boardSize];
        filledCells = 0;

        Random randomNumber = new Random();
        int value;
        int row;
        int column;

        while(filledCells != filledFromStart) // fill cells randomly until reaching the specified amount of filled cells
        {
            value = randomNumber.nextInt(1, boardSize + 1);
            row = randomNumber.nextInt(0, boardSize);
            column = randomNumber.nextInt(0, boardSize);

            placeValueInCell(row, column, value);
        }
    }

    /**
     * @author Abinav & Danny
     */
    private void initializeBoardTemp(int[][] predefinedBoard)
    {
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
     * @author Danny & Abinav
     */
    public boolean placeValueInCell(int row, int column, int value)
    {
        if((row < 0 || row > boardSize - 1) || (column < 0 || column > boardSize - 1))
        {
            errorMessage = "Only indices from 1-" + boardSize + " are valid!";

            return false;
        }
        else if(value < 1 || value > boardSize)
        {
            errorMessage = "Only values from 1-" + boardSize + " are valid!";

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
        for (int column = 0;  column < boardSize; column++ )
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
     * @author Danny
     */
    public boolean checkPlacementSubBoard(int row, int column, int value)
    {
        int subBoard = findSubBoardNumber(row, column);
        int startingRow = (subBoard / boardLengthWidth) * boardLengthWidth;
        int startingColumn = (subBoard - startingRow) * boardLengthWidth;

        for(int addedRows = 0; addedRows < boardLengthWidth; addedRows++)
        {
            for(int addedColumns = 0; addedColumns < boardLengthWidth; addedColumns++)
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

    /**
     * @author Abinav
     */
    public int findSubBoardNumber(int row,int column)
    {
        int totalNoOfSubBoards = (int) Math.sqrt(boardSize);
        return (row/totalNoOfSubBoards)*totalNoOfSubBoards + (column/totalNoOfSubBoards);
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
    public boolean solveBoard()
    {
        return solver.solveWithStrategies();
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
    public int getBoardSize()
    {
        return boardSize;
    }

    /**
     * @author Danny
     */
    public int getBoardLengthWidth()
    {
        return boardLengthWidth;
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
