package BasicSudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Solver
{
    Board board;
    private HashMap<String, List<Integer>> possibleNumbers = new HashMap<String, List<Integer>>();
    private int[][] valuePossibleCountRows; // [value][row], these are used for simpler and quicker algorithms
    private int[][] valuePossibleCountColumns; // [value][column]
    private int[][] valuePossibleCountSubBoards; // [value][sub-board]
    private  List<String> keysToRemove = new ArrayList<String>();

    public Solver(Board board)
    {
        // Danny
        this.board = board;

        valuePossibleCountRows = new int[board.getBoardSize() + 1][board.getBoardSize()];
        valuePossibleCountColumns = new int[board.getBoardSize() + 1][board.getBoardSize()];
        valuePossibleCountSubBoards = new int[board.getBoardSize() + 1][board.getBoardSize()];
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

                            updatePossibleCounts(number, rows, columns, true);
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

    public void updatePossibleCounts(int value, int row, int column, boolean increase)
    {
        // Danny
        if(increase)
        {
            valuePossibleCountRows[value][row]++;
            valuePossibleCountColumns[value][column]++;
            valuePossibleCountSubBoards[value][board.findSubBoardNumber(row, column)]++;
        }
        else
        {
            valuePossibleCountRows[value][row]--;
            valuePossibleCountColumns[value][column]--;
            valuePossibleCountSubBoards[value][board.findSubBoardNumber(row, column)]--;
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
                updatePossibleCounts(values.get(0), row, column,false);

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
                    updatePossibleCounts(values.get(0), rowOfKey2, columnOfKey2,false);
                }

                valuesOfKey2.removeAll(values);
            }
        }
    }

    public void intersectionRemoval()
    {
        // Danny
        pointingDuplicates(true);
        nakedSingles(); // remove keys of size <= 1
        pointingDuplicates(false);
        nakedSingles();
    }

    private void pointingDuplicates(boolean processingRows)
    {
        // Danny
        int boardSize = board.getBoardSize();
        int targetValueCount = board.getBoardLengthWidth() - 1;
        int valuePossibleCount;
        int valueSubBoardCount;
        int previousSubBoard;

        int firstIndex; // variables used to avoid repetitive code
        int secondIndex;
        int thirdIndex;
        int fourthIndex;

        for(int i = 1; i <= boardSize; i++) // value
        {
            for(int j = 0; j < boardSize; j++) // row or column
            {
                valuePossibleCount = processingRows ? valuePossibleCountRows[i][j] : valuePossibleCountColumns[i][j];

                if(valuePossibleCount >= 2) // skip if value already present in row or column (no possibilities)
                {
                    valueSubBoardCount = 0;
                    previousSubBoard = processingRows ? board.findSubBoardNumber(j, 0) : board.findSubBoardNumber(0, j); // initial sub-board

                    for(int k = 0; k < boardSize; k++) // row or column
                    {
                        firstIndex = processingRows ? j : k;
                        secondIndex = processingRows ? k : j;

                        if(possibleNumbers.get(firstIndex + "," + secondIndex) != null && possibleNumbers.get(firstIndex + "," + secondIndex).contains(i))
                        {
                            if(previousSubBoard != board.findSubBoardNumber(firstIndex, secondIndex)) // reset and update if sub-board has changed
                            {
                                valueSubBoardCount = 0;
                                previousSubBoard = board.findSubBoardNumber(firstIndex, secondIndex);
                            }

                            valueSubBoardCount++;

                            if(valueSubBoardCount >= targetValueCount && valuePossibleCountSubBoards[i][board.findSubBoardNumber(firstIndex, secondIndex)] == valueSubBoardCount) // pointing duplicates found, but value might be present on multiple sub-boards
                            {
                                for(int l = 0; l < boardSize; l++) // row or column
                                {
                                    thirdIndex = processingRows ? j : l;
                                    fourthIndex = processingRows ? l : j;

                                    String key = (thirdIndex) + "," + (fourthIndex);

                                    if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                    {
                                        if(board.findSubBoardNumber(thirdIndex, fourthIndex) != previousSubBoard) // remove value from row or column if on other sub-boards
                                        {
                                            updatePossibleCounts(i, thirdIndex, fourthIndex,false);

                                            possibleNumbers.get(key).remove((Integer) i);
                                        }
                                    }
                                }
                            }
                        }

                        if(k == boardSize - 1)
                        {
                            if(valueSubBoardCount >= targetValueCount && valuePossibleCount == valueSubBoardCount) // pointing duplicates found, but value is only present on a single sub-board
                            {
                                boxLineReduction(i, j, previousSubBoard, processingRows);
                            }
                        }
                    }
                }
            }
        }
    }

    private void boxLineReduction(int value, int rowOrColumn, int previousSubBoard, boolean processingRows)
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
                        updatePossibleCounts(value, firstIndex, secondIndex,false);

                        possibleNumbers.get(key).remove((Integer) value); // remove value from the rest of the sub-board
                    }
                }
            }
        }
    }

    public void wingStrategies()
    {
        // Danny
        xWing(true);
        nakedSingles(); // remove keys of size <= 1
        xWing(false);
        nakedSingles();
        yWing();
        nakedSingles();
    }

    private void xWing(boolean processingRows)
    {
        // Danny
        int boardSize = board.getBoardSize();
        int valuePossibleCount;
        List<int[]> rowColumnPositions;
        List<List<int[]>> toBeProcessed;

        int firstIndex; // variables used to avoid repetitive code
        int secondIndex;
        int thirdIndex;
        int fourthIndex;

        for(int i = 1; i <= boardSize; i++) // value
        {
            toBeProcessed = new ArrayList<>();

            for (int j = 0; j < boardSize; j++) // row or column
            {
                valuePossibleCount = processingRows ? valuePossibleCountRows[i][j] : valuePossibleCountColumns[i][j];
                rowColumnPositions = new ArrayList<>();

                if(valuePossibleCount == 2) // skip if value already present or possible more than 2 places in row or column
                {
                    for(int k = 0; k < boardSize; k++) // row or column
                    {
                        firstIndex = processingRows ? j : k;
                        secondIndex = processingRows ? k : j;

                        String key = (firstIndex + "," + secondIndex);

                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                        {
                            rowColumnPositions.add(new int[] {firstIndex, secondIndex}); // store position of value
                        }

                        if(k == boardSize - 1)
                        {
                            toBeProcessed.add(rowColumnPositions); // x-wing candidate found
                        }
                    }
                }
            }

            if(toBeProcessed.size() >= 2)
            {
                firstIndex = processingRows ? 1 : 0; // 0 = row index, 1 = column index

                for(int j = 0; j < toBeProcessed.size() - 1; j++)
                {
                    for(int k = j + 1; k < toBeProcessed.size(); k++)
                    {
                        if(toBeProcessed.get(j).get(0)[firstIndex] == toBeProcessed.get(k).get(0)[firstIndex] && toBeProcessed.get(j).get(1)[firstIndex] == toBeProcessed.get(k).get(1)[firstIndex]) // check if there is an x-wing
                        {
                            secondIndex = processingRows ? 0 : 1; // 0 = row index, 1 = column index

                            for(int l = 0; l < boardSize; l++)
                            {
                                if(l != toBeProcessed.get(j).get(0)[secondIndex] && l != toBeProcessed.get(k).get(1)[secondIndex]) // don't remove value from x-wing rows or columns
                                {
                                    thirdIndex = processingRows ? l : toBeProcessed.get(j).get(0)[firstIndex];
                                    fourthIndex = processingRows ? toBeProcessed.get(j).get(0)[firstIndex] : l;

                                    for(int m = 1; m <= 2; m++) // remove value elsewhere in both rows or columns
                                    {
                                        String key = (thirdIndex + "," + fourthIndex);

                                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                        {
                                            updatePossibleCounts(i, thirdIndex, fourthIndex,false);

                                            possibleNumbers.get(key).remove((Integer) i);
                                        }

                                        if(m == 1)
                                        {
                                            thirdIndex = processingRows ? l : toBeProcessed.get(k).get(1)[firstIndex];
                                            fourthIndex = processingRows ? toBeProcessed.get(k).get(1)[firstIndex] : l;
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

    public void yWing()
    {
        // Danny
        int boardSize = board.getBoardSize();
        List<int[]> keyCandidateValues = new ArrayList<>();
        List<int[]> keyCandidatePositions = new ArrayList<>();
        List<int[]> observerCandidateValues;
        List<int[]> observerCandidatePositions;
        List<String> observedByObserverA;
        List<String> observedByObserverB;

        int firstIndex; // variables used to avoid repetitive code
        int secondIndex;
        int thirdIndex;
        int fourthIndex;
        int fifthIndex;
        int sixthIndex;
        int seventhIndex;
        int eighthIndex;

        for(int i = 0; i < boardSize; i++) // row
        {
            for(int j = 0; j < boardSize; j++) // column
            {
                String key = (i + "," + j);

                if(possibleNumbers.get(key) != null && possibleNumbers.get(key).size() == 2) // save all keys with value sets of size 2
                {
                    keyCandidateValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1)});
                    keyCandidatePositions.add(new int[] {i, j});
                }
            }
        }

        for(int i = 1; i <= boardSize; i++) // value
        {
            for(int j = 0; j < keyCandidateValues.size(); j++) // first keyCandidateValues index
            {
                observerCandidateValues = new ArrayList<>();
                observerCandidatePositions = new ArrayList<>();

                firstIndex = keyCandidateValues.get(j)[0];
                secondIndex = keyCandidateValues.get(j)[1];

                if(firstIndex != i && secondIndex != i)
                {
                    for(int k = 0; k < keyCandidateValues.size(); k++) // second keyCandidateValues index
                    {
                        if(k == j)
                        {
                            continue;
                        }

                        thirdIndex = keyCandidateValues.get(k)[0];
                        fourthIndex = keyCandidateValues.get(k)[1];

                        if(thirdIndex == i && fourthIndex == firstIndex || thirdIndex == i && fourthIndex == secondIndex || thirdIndex == firstIndex && fourthIndex == i || thirdIndex == secondIndex && fourthIndex == i)
                        {
                            fifthIndex = keyCandidatePositions.get(j)[0]; // row
                            sixthIndex = keyCandidatePositions.get(j)[1]; // column
                            seventhIndex = keyCandidatePositions.get(k)[0]; // row
                            eighthIndex = keyCandidatePositions.get(k)[1]; // column

                            if(fifthIndex == seventhIndex || sixthIndex == eighthIndex || board.findSubBoardNumber(fifthIndex, sixthIndex) == board.findSubBoardNumber(seventhIndex, eighthIndex)) // save all observer cells visible from key cells
                            {
                                observerCandidateValues.add(new int[] {thirdIndex, fourthIndex});
                                observerCandidatePositions.add(new int[] {seventhIndex, eighthIndex});
                            }
                        }
                    }
                }

                if(observerCandidateValues.size() >= 2) // remove value from all cells observed by both observer cells
                {
                    String key;
                    observedByObserverA = new ArrayList<>();
                    observedByObserverB = new ArrayList<>();
                    int boardLengthWidth = board.getBoardLengthWidth();
                    int subBoard;
                    int startingRow;
                    int startingColumn;

                    for(int k = 0; k < 2; k++) // process rows or columns
                    {
                        thirdIndex = k == 0 ? 1 : 0;

                        for(int l = 0; l < 2; l++) // observerCandidateValues index
                        {
                            fourthIndex = l == 0 ? 1 : 0;

                            for(int m = 0; m < boardSize; m++) // row or column
                            {
                                if(m != observerCandidatePositions.get(l)[thirdIndex])
                                {
                                    fifthIndex = k == 0 ? observerCandidatePositions.get(l)[0] : m; // row
                                    sixthIndex = k == 0 ? m : observerCandidatePositions.get(l)[1]; // column
                                    seventhIndex = observerCandidatePositions.get(fourthIndex)[0]; // row
                                    eighthIndex = observerCandidatePositions.get(fourthIndex)[1]; // column

                                    if(fifthIndex != seventhIndex || sixthIndex != eighthIndex)
                                    {
                                        key = (fifthIndex + "," + sixthIndex);

                                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                        {
                                            if(l == 0)
                                            {
                                                observedByObserverA.add(key);
                                            }
                                            else
                                            {
                                                observedByObserverB.add(key);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for(int k = 0; k < 2; k++) // observerCandidateValues index
                    {
                        subBoard = board.findSubBoardNumber(observerCandidatePositions.get(k)[0], observerCandidatePositions.get(k)[1]);
                        startingRow = (subBoard / boardLengthWidth) * boardLengthWidth;
                        startingColumn = (subBoard - startingRow) * boardLengthWidth;

                        thirdIndex = k == 0 ? 1 : 0;

                        for(int l = 0; l < boardLengthWidth; l++) // added to rows
                        {
                            for(int m = 0; m < boardLengthWidth; m++) // added to columns
                            {
                                int row = startingRow + l;
                                int column = startingColumn + m;

                                if((row != observerCandidatePositions.get(k)[0] || column != observerCandidatePositions.get(k)[1]) && (row != observerCandidatePositions.get(thirdIndex)[0] || column != observerCandidatePositions.get(thirdIndex)[1]))
                                {
                                    key = (row + "," + column);

                                    if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                    {
                                        if(k == 0 && !observedByObserverA.contains(key))
                                        {
                                            observedByObserverA.add(key);
                                        }
                                        else if(k == 1 && !observedByObserverB.contains(key))
                                        {
                                            observedByObserverB.add(key);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for(String keyInA : observedByObserverA)
                    {
                        if(observedByObserverB.contains(keyInA))
                        {
                            String[] parts = keyInA.split(",");

                            updatePossibleCounts(i, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), false);

                            possibleNumbers.get(keyInA).remove((Integer) i);

                            System.out.println(keyInA + " (" + i + ")");
                        }
                    }
                }
            }
        }
    }
}

