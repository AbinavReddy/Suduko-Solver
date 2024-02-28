package BasicSudoku;

public class BoardTester
{
    public static void main(String[] args)
    {
        Board testBoard = new Board(3);
        Solver solver = new Solver(testBoard);

        printBoard(testBoard);
        solver.possibleValuesInCells();
        solver.print1();
        solver.nakedSingles();
        printBoard(testBoard);
        solver.print1();


    }

    public static void printBoard(Board boardToPrint)
    {
        // Danny
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
