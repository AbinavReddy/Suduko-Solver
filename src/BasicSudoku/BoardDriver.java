package BasicSudoku;

public class BoardDriver
{
    public static void main(String[] args)
    {
        Board testBoard = new Board(3);

        printBoard(testBoard);

        /*

        testBoard.placeValueInCell(7, 1, 8);

        // System.out.print("--------------------");

        // testBoard.placeValueInCell(7, 1, 8);

        */
    }

    public static void printBoard(Board boardToPrint)
    {
        int boardRowsColumns = boardToPrint.getBoardRowsColumns();

        for(int row = 0; row < boardRowsColumns; row++)
        {
            if(row % 3 == 0)
            {
                System.out.print("\n");
            }

            for(int column = 0; column < boardRowsColumns; column++)
            {
                if(column % 3 == 0)
                {
                    System.out.print(" ");
                }

                if(column != boardRowsColumns - 1)
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
