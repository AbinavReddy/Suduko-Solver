package BasicSudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Solver
{
    Board board;
    private HashMap<String, List<Integer>> possibleNumbers = new HashMap<String, List<Integer>>();
    private  List<String> keysToRemove = new ArrayList<String>();

    public Solver(Board board)
    {
        // Danny
        this.board = board;
    }

    public void possibleValuesInCells() {
        // Abinav & Yahya
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
        // Abinav & Yahya
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

    public void nakedSingles() {
        // Abinav & Yahya
        for (String key : possibleNumbers.keySet()) {
            String[] parts = key.split(",");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            int subBoardNo = board.findSubBoardNumber(row, column);
            List<Integer> values = possibleNumbers.get(key);
            if (values.size() == 1) {
                board.placeValueInCell(row, column, values.get(0));
                removeNumberFromOtherCandidate(key,values);
                keysToRemove.add(key);
            }
        }
        removeKeysHavingEmptyList();
    }

    public void removeKeysHavingEmptyList () {
     // Abinav
     for (String key : keysToRemove) {
         if(!keysToRemove.isEmpty()) {
             possibleNumbers.remove(key);
             }
         }
    }

    public void removeNumberFromOtherCandidate(String key,List<Integer> values) {
        // Abinav

        String[] part = key.split(",");
        int row = Integer.parseInt(part[0]);
        int column = Integer.parseInt(part[1]);
        int subBoardNo = board.findSubBoardNumber(row,column);

        for (String Key2 : possibleNumbers.keySet()) {
            if(Key2 == key) continue;
            String[] parts = Key2.split(",");
            int rowOfKey2 = Integer.parseInt(parts[0]);
            int columnOfKey2 = Integer.parseInt(parts[1]);
            int subBoardNoOfKey2 = board.findSubBoardNumber(rowOfKey2,columnOfKey2);
            List<Integer> valuesOfKey2 = possibleNumbers.get(Key2);
            if((row == rowOfKey2) || (column == columnOfKey2) || (subBoardNo == subBoardNoOfKey2)){
                valuesOfKey2.removeAll(values);
            }
        }
    }

}

