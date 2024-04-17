package BasicSudoku;

import java.util.Scanner;

public class SudokuConsoleVersion
{
    public static void main(String[] args)
    {
        Board sudokuBoard = new Board(3, false, 30, false);
        Solver sudokuSolver = sudokuBoard.getSolver();
        boolean solvedBySolver = false;
        int value;
        int row;
        int column;
        int previousValue = 0;
        int previousRow = 0;
        int previousColumn = 0;

        System.out.println();
        System.out.println("WELCOME TO SUDOKU!");
        System.out.println();
        System.out.println("Guide:");
        System.out.println("1. Type coordinates for cells in the form of 'row column' (separated by space) and press enter.");
        System.out.println("2. Type the value you want to place in the given cell and press enter.");
        System.out.println("3. Only use valid rows, columns and values (all from 1-" + sudokuBoard.getBoardSize() + ").");
        System.out.println("4. Type 0 0 and press enter when typing coordinates to revert your latest placement.");
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

            row = console.nextInt() - 1;
            column = console.nextInt() - 1;

            if(row == -1 && column == -1)
            {
                if(previousValue != 0)
                {
                    sudokuBoard.setBoardValue(previousRow, previousColumn, 0);
                    sudokuSolver.possibleValuesInCells();
                    sudokuSolver.updatePossibleCounts(previousValue, null, previousRow, previousColumn, true);

                    previousValue = 0;

                    System.out.println("Previous placement reversed!");
                    System.out.println("------------------------");
                }
                else
                {
                    System.out.println("You haven't made a placement to reverse yet!");
                    System.out.println("------------------------");
                }

                BoardTester.printBoard(sudokuBoard);

                continue;
            }
            else if(row == -2 && column == -2)
            {
                sudokuBoard.solveBoard();

                BoardTester.printBoard(sudokuBoard);

                solvedBySolver = true;

                break;
            }

            System.out.println();
            System.out.println("Value:");

            value = console.nextInt();

            System.out.println();

            if(sudokuBoard.placeValueInCell(row, column, value))
            {
                System.out.println("Value added succesfully!");
                System.out.println("------------------------");
            }
            else
            {
                System.out.println(sudokuBoard.getErrorMessage());

                continue;
            }

            previousValue = value;
            previousRow = row;
            previousColumn = column;

            BoardTester.printBoard(sudokuBoard);
        }

        if(!solvedBySolver)
        {
            System.out.println("You have solved the Sudoku-puzzle!");
        }
        else
        {
            System.out.println("The solver successfully solved the Sudoku-puzzle!");
        }

        console.close();
    }
}
