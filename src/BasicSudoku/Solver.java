package BasicSudoku;

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

                            valuePossibleSubBoards[number][board.findSubBoardNumber(rows, columns)]++;
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
                valuePossibleSubBoards[values.get(0)][board.findSubBoardNumber(row, column)]--;

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

    public void pointingDuplicates()
    {
        // Danny
        int boardSize = board.getBoardSize();
        int targetValueCount = board.getBoardLengthWidth() - 1;

        pointingDuplicatesRows(boardSize, targetValueCount);
        nakedSingles(); // remove keys of size <= 1
        pointingDuplicatesColumns(boardSize, targetValueCount);
        nakedSingles();
    }

    private void pointingDuplicatesRows(int boardSize, int targetValueCount)
    {
        // Danny
        int valueCountRow;
        int valueCountSubBoard;
        int previousSubBoard;

        for(int i = 1; i <= boardSize; i++) // value
        {
            for(int j = 0; j < boardSize; j++) // row
            {
                if(!board.getValueInRows()[i][j]) // skip if value already present in row (no possibilities)
                {
                    valueCountRow = 0;
                    valueCountSubBoard = 0;
                    previousSubBoard = board.findSubBoardNumber(j, 0); // initial sub-board

                    for(int k = 0; k < boardSize; k++) // column
                    {
                        if(possibleNumbers.get(j + "," + k) != null && possibleNumbers.get(j + "," + k).contains(i))
                        {
                            if(previousSubBoard != board.findSubBoardNumber(j, k)) // reset and update if sub-board has changed
                            {
                                valueCountSubBoard = 0;
                                previousSubBoard = board.findSubBoardNumber(j, k);
                            }

                            valueCountRow++;
                            valueCountSubBoard++;

                            if(valueCountSubBoard >= targetValueCount && valuePossibleSubBoards[i][board.findSubBoardNumber(j, k)] == valueCountSubBoard) // pointing duplicates found in row, but on multiple sub-boards
                            {
                                for(int l = 0; l < boardSize; l++) // column
                                {
                                    if(possibleNumbers.get(j + "," + l) != null)
                                    {
                                        if(board.findSubBoardNumber(j, l) != previousSubBoard) // remove value from row if on other sub-boards
                                        {
                                            possibleNumbers.get(j + "," + l).remove((Integer) i);

                                            valuePossibleSubBoards[i][board.findSubBoardNumber(j, k)]--;
                                        }
                                    }
                                }
                            }
                        }

                        if(k == boardSize - 1)
                        {
                            if(valueCountSubBoard >= targetValueCount && valueCountRow == valueCountSubBoard) // pointing duplicates found in row, but on a single sub-board
                            {
                                int boardLengthWidth = board.getBoardLengthWidth();
                                int startingRow = (previousSubBoard / boardLengthWidth) * boardLengthWidth;
                                int startingColumn = (previousSubBoard  - startingRow) * boardLengthWidth;

                                for(int m = 0; m < boardLengthWidth; m++) // added to rows
                                {
                                    if(startingRow + m != j)
                                    {
                                        for(int n = 0; n < boardLengthWidth; n++) // added to columns
                                        {
                                            String key = (startingRow + m) + "," + (startingColumn + n);

                                            if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                            {
                                                possibleNumbers.get(key).remove((Integer) i); // remove value from the rest of the sub-board

                                                valuePossibleSubBoards[i][board.findSubBoardNumber(startingRow + m, startingColumn + n)]--;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void pointingDuplicatesColumns(int boardSize, int targetValueCount)
    {
        // Danny
        int valueCountColumn;
        int valueCountSubBoard;
        int previousSubBoard;

        for(int i = 1; i <= boardSize; i++) // value
        {
            for(int j = 0; j < boardSize; j++) // column
            {
                if(!board.getValueInColumns()[i][j]) // skip if value already present in column (no possibilities)
                {
                    valueCountColumn = 0;
                    valueCountSubBoard = 0;
                    previousSubBoard = board.findSubBoardNumber(0, j); // initial sub-board

                    for(int k = 0; k < boardSize; k++) // row
                    {
                        if(possibleNumbers.get(k + "," + j) != null && possibleNumbers.get(k + "," + j).contains(i))
                        {
                            if(previousSubBoard != board.findSubBoardNumber(k, j)) // reset and update if sub-board has changed
                            {
                                valueCountSubBoard = 0;
                                previousSubBoard = board.findSubBoardNumber(k, j);
                            }

                            valueCountColumn++;
                            valueCountSubBoard++;

                            if(valueCountSubBoard >= targetValueCount && valuePossibleSubBoards[i][board.findSubBoardNumber(k, j)] == valueCountSubBoard) // pointing duplicates found in column, but on multiple sub-boards
                            {
                                for(int l = 0; l < boardSize; l++) // row
                                {
                                    if(possibleNumbers.get(l + "," + j) != null)
                                    {
                                        if(board.findSubBoardNumber(l, j) != previousSubBoard) // remove value from column if on other sub-boards
                                        {
                                            possibleNumbers.get(l + "," + j).remove((Integer) i);

                                            valuePossibleSubBoards[i][board.findSubBoardNumber(j, k)]--;
                                        }
                                    }
                                }
                            }
                        }

                        if(k == boardSize - 1)
                        {
                            if(valueCountSubBoard >= targetValueCount && valueCountColumn == valueCountSubBoard) // pointing duplicates found in column, but on a single sub-board
                            {
                                int boardLengthWidth = board.getBoardLengthWidth();
                                int startingRow = (previousSubBoard  / boardLengthWidth) * boardLengthWidth;
                                int startingColumn = (previousSubBoard  - startingRow) * boardLengthWidth;

                                for(int m = 0; m < boardLengthWidth; m++) // added to columns
                                {
                                    if(startingColumn + m != j)
                                    {
                                        for(int n = 0; n < boardLengthWidth; n++) // added to rows
                                        {
                                            String key = (startingRow + n) + "," + (startingColumn + m);

                                            if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                            {
                                                possibleNumbers.get(key).remove((Integer) i); // remove value from the rest of the sub-board

                                                valuePossibleSubBoards[i][board.findSubBoardNumber(startingRow + n, startingColumn + m)]--;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

