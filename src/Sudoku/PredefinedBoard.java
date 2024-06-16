package Sudoku;

import java.util.Random;

public class PredefinedBoard {

    private static Random random = new Random();

    public static final int[][] Board0 = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };

    public static final int[][] Board1 = {
            {0, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 6, 0, 3, 0, 0, 0, 4},
            {0, 0, 0, 0, 0, 0, 7, 0, 0},
            {2, 0, 0, 0, 0, 0, 0, 4, 0},
            {0, 7, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 8, 0, 0, 3, 0, 2},
            {0, 0, 4, 0, 0, 0, 8, 0, 0},
            {0, 0, 0, 0, 5, 7, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public static final int[][] Board2 = {
            {0, 3, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 6, 0, 3, 0, 0, 0, 4},
            {0, 0, 0, 0, 0, 0, 7, 0, 0},
            {2, 0, 0, 0, 0, 0, 0, 4, 0},
            {0, 7, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 8, 0, 0, 3, 0, 2},
            {0, 0, 4, 0, 0, 0, 8, 0, 0},
            {0, 0, 0, 0, 5, 7, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public static final int[][] Board3 = {
        {3, 1, 8, 6, 0, 7, 0, 5, 9},
        {0, 0, 0, 0, 8, 9, 1, 0, 0},
        {0, 9, 0, 5, 3, 1, 0, 8, 0},
        {9, 4, 7, 8, 1, 2, 0, 0, 5},
        {6, 0, 0, 7, 9, 4, 8, 2, 1},
        {8, 2, 1, 3, 5, 6, 9, 4, 7},
        {5, 0, 9, 0, 0, 8, 0, 1, 0},
        {0, 0, 0, 0, 6, 5, 0, 0, 8},
        {0, 8, 0, 0, 0, 3, 5, 0, 4}
    };



    public static int[][] selectBoardRandomly (){
        int randomNumber = 3; // random.nextInt(3);

        switch (randomNumber){
            case 0:
                return Board0;
            case 1:
                return Board1;
            case 2:
                return Board2;
            case 3:
                return Board3;
            default:
                return null;
        }
    };
}

