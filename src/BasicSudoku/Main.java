package BasicSudoku;

import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Board sudokuBoard = new Board(3);

        System.out.println();
        System.out.println("WELCOME TO SUDOKU!");
        System.out.println();
        System.out.println("Guide:");
        System.out.println("1. Type coordinates for cells in the form of 'row column' (separated by space) and press enter.");
        System.out.println("2. Type the value you want to place in the given cell and press enter.");
        System.out.println("3. Only use valid rows, columns and values (1-" + sudokuBoard.getBoardSize() + ").");
        System.out.println();
        System.out.println("Let the solving commence!");
        System.out.print("-----------------------------------------------------------------------------------------------");
        System.out.println();

        BoardTester.printBoard(sudokuBoard);

        Scanner console = new Scanner(System.in);

        while(!sudokuBoard.isGameFinished())
        {
            System.out.println();
            System.out.println("Cell:");

            int row = console.nextInt() - 1;
            int column = console.nextInt() - 1;

            System.out.println();
            System.out.println("Value:");

            int value = console.nextInt();

            System.out.println();

            if(sudokuBoard.placeValueInCell(row, column, value))
            {
                System.out.println("Value added succesfully!");
                System.out.println("------------------------");
            }
            else
            {
                continue;
            }

            BoardTester.printBoard(sudokuBoard);
        }

        System.out.println("The Sudoku-puzzle has been solved!");

        console.close();
    }
}
