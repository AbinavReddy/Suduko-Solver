
package BasicSudoku;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Solver
{
    Board board;

    public Solver(Board board)
    {
        // Danny
        this.board = board;
    }

    public HashMap<String, List<Integer>> possibleNumbers = new HashMap<String, List<Integer>>();

    public void possibleValuesInCells() {

        for (int rows = 0; rows < board.getBoardSize(); rows++) {
            for (int columns = 0; columns < board.getBoardSize(); columns++) {
                if (board.getBoard()[rows][columns] == 0) {
                    String currentPosition = rows + "," + columns;
                    List<Integer> listOfPosNumbers = new ArrayList<Integer>();
                    for (int number = 1; number <= board.getBoardSize(); number++) {
                        if (board.checkPlacementRow(rows, number) && board.checkPlacementColumn(columns, number) && board.checkPlacementSubBoard(rows, columns, number)) {
                            listOfPosNumbers.add(number);
                        }
                    }
                    possibleNumbers.put(currentPosition,listOfPosNumbers);
                }
            }
        }
    }

    public void print1() {
        for (int rows = 0; rows < board.getBoardSize(); rows++) {
            for (int columns = 0; columns < board.getBoardSize(); columns++) {
                String currentPosition = rows + "," + columns;
                List<Integer> values = possibleNumbers.get(currentPosition);
                if (values != null) {
                    System.out.println("Position: (" + rows + "," + columns + ") Possible Values: " + values);
                }
            }
        }
    }

    public void print() {
        for (String i : possibleNumbers.keySet()) {
            System.out.println("key: " + i + " value: " + possibleNumbers.get(i));
        }
    }

    public void nakedSingles() {
        for (String key : possibleNumbers.keySet()) {
            String[] parts = key.split(",");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            List<Integer> values = possibleNumbers.get(key);
            if (values.size() == 1) {
                System.out.println(row+","+column+","+values.get(0));
                board.setBoardValue(row, column, values.get(0));

                System.out.println("reached here");
            }
        }
        possibleValuesInCells();

    }

}

