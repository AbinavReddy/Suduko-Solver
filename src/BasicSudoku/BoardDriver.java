package BasicSudoku;

public class BoardDriver
{
    public static void main(String[] args)
    {
        Board testBoard = new Board(3);

        printBoard(testBoard);

        /*

        testBoard.placeValueInCell(0, 3, 2);

        System.out.print("--------------------");

        printBoard(testBoard);

         */
    }

    public static void printBoard(Board boardToPrint)
    {
        int[][] board = boardToPrint.getBoard();
        int boardSize = boardToPrint.getBoardSize();
        int boardLengthWidth = boardToPrint.getBoardLengthWidth();

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
