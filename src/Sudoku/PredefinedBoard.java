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
        {8, 3, 2, 5, 0, 9, 1, 6, 0},
        {7, 1, 5, 0, 6, 0, 3, 9, 0},
        {6, 9, 4, 1, 0, 3, 0, 2, 5},
        {2, 5, 1, 0, 0, 7, 0, 3, 0},
        {3, 4, 8, 0, 0, 0, 0, 5, 0},
        {9, 6, 7, 3, 5, 0, 0, 4, 0},
        {5, 7, 3, 0, 0, 6, 4, 1, 9},
        {4, 2, 9, 7, 1, 5, 6, 8, 3},
        {1, 8, 6, 9, 3, 4, 5, 7, 2}
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

