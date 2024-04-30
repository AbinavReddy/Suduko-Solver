package BasicSudoku;

public class BoardTester
{
    public static void main(String[] args)
    {
        SudokuBoard board = new SudokuBoard(9);
        SudokuBoard solverBoard = board.getSolver().board;
        Solver solver = board.getSolver();

        //solver.emptyCellsDebug();

        printBoard(board);
        solver.printPossibleNumbers(true);
        solver.solveWithStrategies();
        printBoard(solverBoard);
        solver.printPossibleNumbers(false);
    }

    public static void printBoard(SudokuBoard boardToPrint)
    {
        int[][] board = boardToPrint.getBoard();
        int boardLengthWidth = boardToPrint.getBoardLengthWidth();
        int boardSize = boardToPrint.getBoardSize();

        for(int row = 0; row < boardSize; row++)
        {
            if(row % boardLengthWidth == 0)
            {
                System.out.print("\n");
            }

            for(int column = 0; column < boardSize; column++)
            {
                if(column % boardLengthWidth == 0)
                {
                    System.out.print(" ");
                }

                if(column != boardSize - 1)
                {
                    System.out.print(board[row][column] + " ");
                }
                else
                {
                    System.out.print(board[row][column] + "\n");
                }
            }
        }
    }
}
