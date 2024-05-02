package BasicSudoku;

public class BoardTester
{
    public static void main(String[] args)
    {
        SudokuBoard board = new SudokuBoard(3, 3);
        SudokuBoard solverBoard = board.getSolver().board;
        Solver solver = board.getSolver();

        solver.emptyCellsDebug();

        /*
        printBoard(board);
        solver.printPossibleNumbers(true);
        solver.solveWithStrategies();
        printBoard(solverBoard);
        solver.printPossibleNumbers(false);
        */
    }

    public static void printBoard(SudokuBoard boardToPrint)
    {
        int[][] board = boardToPrint.getBoard();
        int boardBoxes = boardToPrint.getBoardBoxes();
        int boardRowsColumns = boardToPrint.getBoardRowsColumns();

        for(int row = 0; row < boardRowsColumns; row++)
        {
            if(row % boardBoxes == 0)
            {
                System.out.print("\n");
            }

            for(int column = 0; column < boardRowsColumns; column++)
            {
                if(column % boardBoxes == 0)
                {
                    System.out.print(" ");
                }

                if(column != boardRowsColumns - 1)
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
