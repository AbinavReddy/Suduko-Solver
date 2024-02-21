package BasicSudoku;

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

        initializeBoard();
    }

    public void initializeBoard()
    {
        for(int row = 0; row < boardSize; row++)
        {
            for(int column = 0; column < boardSize; column++)
            {
                board[row][column] = 0;
            }
        }
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
