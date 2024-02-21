package BasicSudoku;

public class BoardDriver
{
    public static void main(String[] args)
    {
        Board testBoard = new Board(9);

        printBoard(testBoard);
    }

    public static void printBoard(Board boardToPrint)
    {
        for(int row = 0; row < boardToPrint.getBoardSize(); row++)
        {
            if(row % 3 == 0)
            {
                System.out.print("\n");
            }

            for(int column = 0; column < boardToPrint.getBoardSize(); column++)
            {
                if(column % 3 == 0)
                {
                    System.out.print(" ");
                }

                if(column != boardToPrint.getBoardSize() - 1)
                {
                    System.out.print(boardToPrint.getBoard()[row][column] + " ");
                }
                else
                {
                    System.out.print(boardToPrint.getBoard()[row][column] + "\n");
                }
            }
        }
    }
}
