package BasicSudoku;

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


    public static int[][] selectBoardRandomly (){
        int randomNumber = 1; // random.nextInt(3);

        switch (randomNumber){
            case 0:
                return Board0;
            case 1:
                return Board1;
            case 2:
                return Board2;
            default:
                return null;
        }
    };
}

