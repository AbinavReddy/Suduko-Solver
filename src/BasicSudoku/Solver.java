package BasicSudoku;

import javafx.beans.binding.IntegerExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Solver
{
    Board board;
    private HashMap<String, List<Integer>> possibleNumbers = new HashMap<String, List<Integer>>();
    private int[][] valuePossibleSubBoards; // [value][sub-board]
    private  List<String> keysToRemove = new ArrayList<String>();

    public Solver(Board board)
    {
        // Danny
        this.board = board;

        valuePossibleSubBoards = new int[board.getBoardSize() + 1][board.getBoardSize()];
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

                            valuePossibleSubBoards[number][board.findSubBoardNumber(rows, columns)]++; // increase count of value possible in sub-board
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
            List<Integer> values = possibleNumbers.get(key);
            if (values.size() == 1) {
                board.placeValueInCell(row, column, values.get(0));
                valuePossibleSubBoards[values.get(0)][board.findSubBoardNumber(row, column)]--; // decrease count of value possible in sub-board

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
                if(valuesOfKey2.contains(values.get(0)))
                {
                    valuePossibleSubBoards[values.get(0)][subBoardNoOfKey2]--; // decrease count of value possible in sub-board
                }

                valuesOfKey2.removeAll(values);
            }
        }
    }

    public void pointingDuplicates()
    {
        // Danny
        pointingDuplicatesRowsColumns(true);
        nakedSingles(); // remove keys of size <= 1
        pointingDuplicatesRowsColumns(false);
        nakedSingles();
    }

    private void pointingDuplicatesRowsColumns(boolean processingRows)
    {
        // Danny
        int boardSize = board.getBoardSize();
        int targetValueCount = board.getBoardLengthWidth() - 1;
        int valueCount;
        int valueCountSubBoard;
        int previousSubBoard;

        int firstIndex; // variables used to avoid repetitive code
        int secondIndex;
        int thirdIndex;
        int fourthIndex;

        for(int i = 1; i <= boardSize; i++) // value
        {
            for(int j = 0; j < boardSize; j++) // row or column
            {
                if(processingRows && !board.getValueInRows()[i][j] || !processingRows && !board.getValueInColumns()[i][j]) // skip if value already present in row or column (no possibilities)
                {
                    valueCount = 0;
                    valueCountSubBoard = 0;
                    previousSubBoard = processingRows ? board.findSubBoardNumber(j, 0) : board.findSubBoardNumber(0, j); // initial sub-board

                    for(int k = 0; k < boardSize; k++) // row or column
                    {
                        firstIndex = processingRows ? j : k;
                        secondIndex = processingRows ? k : j;

                        if(possibleNumbers.get(firstIndex + "," + secondIndex) != null && possibleNumbers.get(firstIndex + "," + secondIndex).contains(i))
                        {
                            if(previousSubBoard != board.findSubBoardNumber(firstIndex, secondIndex)) // reset and update if sub-board has changed
                            {
                                valueCountSubBoard = 0;
                                previousSubBoard = board.findSubBoardNumber(firstIndex, secondIndex);
                            }

                            valueCount++;
                            valueCountSubBoard++;

                            if(valueCountSubBoard >= targetValueCount && valuePossibleSubBoards[i][board.findSubBoardNumber(firstIndex, secondIndex)] == valueCountSubBoard) // pointing duplicates found, but value might be present on multiple sub-boards
                            {
                                for(int l = 0; l < boardSize; l++) // row or column
                                {
                                    thirdIndex = processingRows ? j : l;
                                    fourthIndex = processingRows ? l : j;

                                    if(possibleNumbers.get(thirdIndex + "," + fourthIndex) != null && possibleNumbers.get(thirdIndex + "," + fourthIndex).contains(i))
                                    {
                                        if(board.findSubBoardNumber(thirdIndex, fourthIndex) != previousSubBoard) // remove value from row or column if on other sub-boards
                                        {
                                            valuePossibleSubBoards[i][board.findSubBoardNumber(thirdIndex, fourthIndex)]--; // decrease count of value possible in sub-board

                                            possibleNumbers.get(thirdIndex + "," + fourthIndex).remove((Integer) i);
                                        }
                                    }
                                }
                            }
                        }

                        if(k == boardSize - 1)
                        {
                            if(valueCountSubBoard >= targetValueCount && valueCount == valueCountSubBoard) // pointing duplicates found, but value is only present on a single sub-board
                            {
                                pointingDuplicatesSubBoards(i, j, previousSubBoard, processingRows);
                            }
                        }
                    }
                }
            }
        }
    }

    private void pointingDuplicatesSubBoards(int value, int rowOrColumn, int previousSubBoard, boolean processingRows)
    {
        // Danny
        int boardLengthWidth = board.getBoardLengthWidth();
        int startingRow = (previousSubBoard / boardLengthWidth) * boardLengthWidth;
        int startingColumn = (previousSubBoard - startingRow) * boardLengthWidth;

        int firstIndex; // variables used to avoid repetitive code
        int secondIndex;

        for(int m = 0; m < boardLengthWidth; m++) // added to starting row or column
        {
            if(processingRows && startingRow + m != rowOrColumn || !processingRows && startingColumn + m != rowOrColumn)
            {
                for(int n = 0; n < boardLengthWidth; n++) // added to starting row or column
                {
                    firstIndex = processingRows ? m : n;
                    secondIndex = processingRows ? n : m;

                    String key = (startingRow + firstIndex) + "," + (startingColumn + secondIndex);

                    if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(value))
                    {
                        valuePossibleSubBoards[value][previousSubBoard]--; // decrease count of value possible in sub-board

                        possibleNumbers.get(key).remove((Integer) value); // remove value from the rest of the sub-board
                    }
                }
            }
        }
    }
}

