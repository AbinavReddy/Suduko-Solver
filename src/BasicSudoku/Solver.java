package BasicSudoku;

import java.util.*;

public class Solver
{
    Board board;
    final int boardSize;
    final int boardLengthWidth;
    private HashMap<String, List<Integer>> possibleNumbers = new HashMap<>();
    private int possibleNumbersCount;
    private int[][] valuePossibleCountRows; // [value][row]
    private int[][] valuePossibleCountColumns; // [value][column]
    private int[][] valuePossibleCountSubBoards; // [value][sub-board]


    public Solver(Board board)
    {
        // Danny
        this.board = board;
        boardSize = board.getBoardSize();
        boardLengthWidth = board.getBoardLengthWidth();

        possibleNumbersCount = 0;
        valuePossibleCountRows = new int[boardSize + 1][boardSize];
        valuePossibleCountColumns = new int[boardSize + 1][boardSize];
        valuePossibleCountSubBoards = new int[boardSize + 1][boardSize];
    }

    public boolean possibleValuesInCells() {
        // Abinav, Yahya & Danny
        for (int rows = 0; rows < boardSize; rows++) {
            for (int columns = 0; columns < boardSize; columns++) {
                if (board.getBoard()[rows][columns] == 0) {
                    String currentPosition = rows + "," + columns;
                    List<Integer> listOfPosNumbers = new ArrayList<Integer>();
                    for (int number = 1; number <= boardSize; number++) {
                        if (board.checkPlacementRow(rows, number) && board.checkPlacementColumn(columns, number) && board.checkPlacementSubBoard(rows, columns, number)) {
                            listOfPosNumbers.add(number);
                            updatePossibleCounts(number, null, rows, columns, true);
                        }
                    }
                   if(!listOfPosNumbers.isEmpty())
                   {
                       possibleNumbers.put(currentPosition,listOfPosNumbers);
                   }
                   else
                   {
                       return false;
                   }
                }
            }
        }

        return true;
    }

    public void updatePossibleCounts(int value, List<Integer> valueList, int row, int column, boolean increase)
    {
        // Danny
        if(increase)
        {
            possibleNumbersCount++;
            valuePossibleCountRows[value][row]++;
            valuePossibleCountColumns[value][column]++;
            valuePossibleCountSubBoards[value][board.findSubBoardNumber(row, column)]++;
        }
        else if(valueList == null)
        {
            possibleNumbersCount--;
            valuePossibleCountRows[value][row]--;
            valuePossibleCountColumns[value][column]--;
            valuePossibleCountSubBoards[value][board.findSubBoardNumber(row, column)]--;
        }
        else
        {
            for(Integer valueInList : valueList)
            {
                possibleNumbersCount--;
                valuePossibleCountRows[valueInList][row]--;
                valuePossibleCountColumns[valueInList][column]--;
                valuePossibleCountSubBoards[valueInList][board.findSubBoardNumber(row, column)]--;
            }
        }
    }

    public void printPossibilities() {
        // Abinav & Yahya
        for (int rows = 0; rows < boardSize; rows++) {
            for (int columns = 0; columns < boardSize; columns++) {
                String currentPosition = rows + "," + columns;
                List<Integer> values = possibleNumbers.get(currentPosition);
                if (values != null) {
                    System.out.println("Position: (" + rows + "," + columns + ") Possible Values: " + values);
                }
            }
        }
    }

    public boolean solveWithStrategies()
    {
        // to do
        while(!board.isGameFinished())
        {
            int possibleCountBefore = possibleNumbersCount;

            // solving strategies go here
            nakedSingles();
            intersectionRemoval();
            nakedSingles();
            wingStrategies();
            nakedSingles();

            if(possibleCountBefore == possibleNumbersCount) // board is unsolvable with strategies, try backtracking
            {
                return false;
            }
        }

        return true;
    }

    public void nakedSingles() {
        // Abinav & Yahya

        List<String> keysToRemove = new ArrayList<>();

        for (String key : possibleNumbers.keySet()) {
            String[] parts = key.split(",");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            List<Integer> values = possibleNumbers.get(key);
            if (values.size() == 1) {
                board.placeValueInCell(row, column, values.get(0));
                updatePossibleCounts(values.get(0), null, row, column,false);

                removeNumberFromOtherCandidate(key,values);
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            possibleNumbers.remove(key);
        }

        EliminateEmptyLists();
    }

    public void EliminateEmptyLists(){
        // Abinav
        List<String> removeKeys = new ArrayList<>();

        for (String key : possibleNumbers.keySet()){
            String[] parts = key.split(",");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            List<Integer> values = possibleNumbers.get(key);
            if(values.isEmpty()) removeKeys.add(key);
        }

        for (String keys : removeKeys) {
            possibleNumbers.remove(keys);
        }

    }



    public void removeNumberFromOtherCandidate(String key,List<Integer> values) {
        // Abinav
        String[] part = key.split(",");
        int row = Integer.parseInt(part[0]);
        int column = Integer.parseInt(part[1]);
        int subBoardNo = board.findSubBoardNumber(row,column);

        for (String Key2 : possibleNumbers.keySet()) {
            if(Key2.equals(key) ) continue;
            String[] parts = Key2.split(",");
            int rowOfKey2 = Integer.parseInt(parts[0]);
            int columnOfKey2 = Integer.parseInt(parts[1]);
            int subBoardNoOfKey2 = board.findSubBoardNumber(rowOfKey2,columnOfKey2);
            List<Integer> valuesOfKey2 = possibleNumbers.get(Key2);
            if((row == rowOfKey2) || (column == columnOfKey2) || (subBoardNo == subBoardNoOfKey2)){
                if(valuesOfKey2.contains(values.get(0)))
                {
                    updatePossibleCounts(values.get(0), null, rowOfKey2, columnOfKey2,false);
                }

                valuesOfKey2.removeAll(values);
            }
        }
    }

    public void intersectionRemoval()
    {
        // Danny
        pointingDuplicatesWithBLR(true);
        pointingDuplicatesWithBLR(false);
    }

    private void pointingDuplicatesWithBLR(boolean processingRows)
    {
        // Danny
        int targetValueCount = boardLengthWidth - 1;
        int valuePossibleCount;
        int valueSubBoardCount;
        int previousSubBoard;
        int startingRowBLR;
        int startingColumnBLR;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;

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
                        substituteA = processingRows ? j : k;
                        substituteB = processingRows ? k : j;

                        if(possibleNumbers.get(substituteA + "," + substituteB) != null && possibleNumbers.get(substituteA + "," + substituteB).contains(i))
                        {
                            if(previousSubBoard != board.findSubBoardNumber(substituteA, substituteB)) // reset and update if sub-board has changed
                            {
                                valueSubBoardCount = 0;
                                previousSubBoard = board.findSubBoardNumber(substituteA, substituteB);
                            }

                            valueSubBoardCount++;

                            if(valueSubBoardCount >= targetValueCount && valuePossibleCountSubBoards[i][board.findSubBoardNumber(substituteA, substituteB)] == valueSubBoardCount) // pointing duplicates found, but value might be present on multiple sub-boards
                            {
                                for(int l = 0; l < boardSize; l++) // row or column
                                {
                                    substituteA = processingRows ? j : l;
                                    substituteB = processingRows ? l : j;

                                    String key = (substituteA + "," + substituteB);

                                    if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                    {
                                        if(board.findSubBoardNumber(substituteA, substituteB) != previousSubBoard) // remove value from row or column if on other sub-boards
                                        {
                                            updatePossibleCounts(i, null, substituteA, substituteB,false);

                                            possibleNumbers.get(key).remove((Integer) i);
                                        }
                                    }
                                }
                            }
                        }

                        if(k == boardSize - 1)
                        {
                            if(valueSubBoardCount >= targetValueCount && valuePossibleCount == valueSubBoardCount) // pointing duplicates found, but value is only present on a single sub-board (perform BLR)
                            {
                                startingRowBLR = (previousSubBoard / boardLengthWidth) * boardLengthWidth;
                                startingColumnBLR = (previousSubBoard - startingRowBLR) * boardLengthWidth;

                                for(int m = 0; m < boardLengthWidth; m++) // added to starting row or column
                                {
                                    if(processingRows && startingRowBLR + m != j || !processingRows && startingColumnBLR + m != j)
                                    {
                                        for(int n = 0; n < boardLengthWidth; n++) // added to starting row or column
                                        {
                                            substituteA = processingRows ? m : n;
                                            substituteB = processingRows ? n : m;

                                            String key = (startingRowBLR + substituteA) + "," + (startingColumnBLR + substituteB);

                                            if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                            {
                                                updatePossibleCounts(i, null, substituteA, substituteB,false);

                                                possibleNumbers.get(key).remove((Integer) i); // remove value from the rest of the sub-board
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

    public void wingStrategies()
    {
        // Danny
        xWing(true);
        xWing(false);
        yWingWithXYZExtension(false);
        yWingWithXYZExtension(true);
    }

    private void xWing(boolean processingRows)
    {
        // Danny
        int valuePossibleCount;
        List<int[]> rowColumnPositions;
        List<List<int[]>> processedForXWings;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;
        int substituteC;
        int substituteD;

        for(int i = 1; i <= boardSize; i++) // value
        {
            processedForXWings = new ArrayList<>();

            for (int j = 0; j < boardSize; j++) // row or column
            {
                valuePossibleCount = processingRows ? valuePossibleCountRows[i][j] : valuePossibleCountColumns[i][j];
                rowColumnPositions = new ArrayList<>();

                if(valuePossibleCount == 2) // skip if value already present or possible more than 2 places in row or column
                {
                    for(int k = 0; k < boardSize; k++) // row or column
                    {
                        substituteA = processingRows ? j : k;
                        substituteB = processingRows ? k : j;

                        String key = (substituteA + "," + substituteB);

                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                        {
                            rowColumnPositions.add(new int[] {substituteA, substituteB}); // store position of value
                        }

                        if(k == boardSize - 1)
                        {
                            processedForXWings.add(rowColumnPositions); // x-wing candidate found
                        }
                    }
                }
            }

            if(processedForXWings.size() >= 2) // enough candidates found
            {
                substituteA = processingRows ? 1 : 0; // 0 = row index, 1 = column index

                for(int j = 0; j < processedForXWings.size() - 1; j++)
                {
                    for(int k = j + 1; k < processedForXWings.size(); k++)
                    {
                        if(processedForXWings.get(j).get(0)[substituteA] == processedForXWings.get(k).get(0)[substituteA] && processedForXWings.get(j).get(1)[substituteA] == processedForXWings.get(k).get(1)[substituteA]) // check if there is an x-wing
                        {
                            substituteB = processingRows ? 0 : 1; // 0 = row index, 1 = column index

                            for(int l = 0; l < boardSize; l++)
                            {
                                if(l != processedForXWings.get(j).get(0)[substituteB] && l != processedForXWings.get(k).get(1)[substituteB]) // don't remove value from x-wing rows or columns
                                {
                                    substituteC = processingRows ? l : processedForXWings.get(j).get(0)[substituteA];
                                    substituteD = processingRows ? processedForXWings.get(j).get(0)[substituteA] : l;

                                    for(int m = 1; m <= 2; m++) // remove value elsewhere in both rows or columns
                                    {
                                        String key = (substituteC + "," + substituteD);

                                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                        {
                                            updatePossibleCounts(i, null, substituteC, substituteD,false);

                                            possibleNumbers.get(key).remove((Integer) i);
                                        }

                                        if(m == 1)
                                        {
                                            substituteC = processingRows ? l : processedForXWings.get(k).get(1)[substituteA];
                                            substituteD = processingRows ? processedForXWings.get(k).get(1)[substituteA] : l;
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

    public void yWingWithXYZExtension(boolean runWithExtension)
    {
        // Danny
        List<int[]> cellsContainingValuePositions;
        List<int[]> hingeValues;
        List<int[]> hingePositions;
        List<int[]> observerCandidateValues;
        List<int[]> observerCandidatePositions;
        List<int[]> observerValues;
        List<int[]> observerPositions;
        List<String> observedByAll;
        List<String> observedByObserverA;
        List<String> observedByObserverB;
        List<String> observedByObserverC;

        int observersNeeded = !runWithExtension ? 2 : 3;
        int currentHingeValue;
        int previousHingeValue;
        int hingeValue1;
        int hingeValue2;
        int observerCandidateValue1;
        int observerCandidateValue2;
        int cellWithValueRow;
        int cellWithValueColumn;
        int hingeRow;
        int hingeColumn;
        int observerCandidateRow;
        int observerCandidateColumn;
        int observerRow;
        int observerColumn;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;
        int substituteC;
        int substituteD;

        for(int i = 1; i <= boardSize; i++) // value
        {
            cellsContainingValuePositions = new ArrayList<>();
            hingeValues = new ArrayList<>();
            hingePositions = new ArrayList<>();
            observerCandidateValues = new ArrayList<>();
            observerCandidatePositions = new ArrayList<>();

            for(int j = 0; j < boardSize; j++) // row
            {
                for (int k = 0; k < boardSize; k++) // column
                {
                    String key = (j + "," + k);

                    if(possibleNumbers.get(key) != null)
                    {
                        if(possibleNumbers.get(key).contains(i)) // save all keys that contains value regardless of size or runWithExtension
                        {
                            cellsContainingValuePositions.add(new int[] {j, k});
                        }

                        if(!runWithExtension)
                        {
                            if(possibleNumbers.get(key).size() == 2)
                            {
                                if(!possibleNumbers.get(key).contains(i)) // save all keys of size 2 that doesn't contain value ("hinges")
                                {
                                    hingeValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1)});
                                    hingePositions.add(new int[] {j, k});
                                }
                                else // save all keys of size 2 that contains value ("observer candidates")
                                {
                                    observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1)});
                                    observerCandidatePositions.add(new int[] {j, k});
                                }
                            }
                        }
                        else if(possibleNumbers.get(key).contains(i)) // runWithExtension is true
                        {
                            if(possibleNumbers.get(key).size() == 3) // save all keys of size 3 that contains value ("hinges"), omitting value
                            {
                                substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;
                                substituteB = !possibleNumbers.get(key).get(substituteA).equals(possibleNumbers.get(key).get(1)) && possibleNumbers.get(key).get(1) != i ? 1 : 2;

                                hingeValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB)});
                                hingePositions.add(new int[] {j, k});
                            }
                            else if(possibleNumbers.get(key).size() == 2) // save all keys of size 2 that contains value ("observer candidates")
                            {
                                observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1)});
                                observerCandidatePositions.add(new int[] {j, k});
                            }
                        }
                    }
                }
            }

            for(int j = 0; j < hingeValues.size(); j++) // hingeValues index
            {
                observerValues = new ArrayList<>();
                observerPositions = new ArrayList<>();

                previousHingeValue = 0;
                hingeValue1 = hingeValues.get(j)[0];
                hingeValue2 = hingeValues.get(j)[1];

                for(int k = 0; k < observerCandidateValues.size(); k++) // observerCandidateValues index
                {
                    observerCandidateValue1 = observerCandidateValues.get(k)[0];
                    observerCandidateValue2 = observerCandidateValues.get(k)[1];

                    if(hingeValue1 == observerCandidateValue1 || hingeValue1 == observerCandidateValue2 || hingeValue2 == observerCandidateValue1 || hingeValue2 == observerCandidateValue2)
                    {
                        hingeRow = hingePositions.get(j)[0];;
                        hingeColumn = hingePositions.get(j)[1];
                        observerCandidateRow = observerCandidatePositions.get(k)[0];
                        observerCandidateColumn = observerCandidatePositions.get(k)[1];

                        if(hingeRow == observerCandidateRow || hingeColumn == observerCandidateColumn || board.findSubBoardNumber(hingeRow, hingeColumn) == board.findSubBoardNumber(observerCandidateRow, observerCandidateColumn)) // save all observers visible from hinge
                        {
                            currentHingeValue = observerCandidateValue1 != i ? observerCandidateValue1 : observerCandidateValue2;

                            if(currentHingeValue != previousHingeValue) // make sure observers contain different hinge values
                            {
                                if(runWithExtension && observerValues.isEmpty()) // in xYZWing the hinge itself is always an observer
                                {
                                    observerValues.add(new int[] {hingeValue1, hingeValue2});
                                    observerPositions.add(new int[] {hingeRow, hingeColumn});
                                }

                                observerValues.add(new int[] {observerCandidateValue1, observerCandidateValue2});
                                observerPositions.add(new int[] {observerCandidateRow, observerCandidateColumn});

                                if(observerValues.size() == observersNeeded)
                                {
                                    break;
                                }

                                previousHingeValue = currentHingeValue;
                            }
                        }
                    }
                }

                if(!runWithExtension && observerValues.size() == 2 || runWithExtension && observerValues.size() == 3) // enough observers found, find all visible cells with value from observers
                {
                    String key;
                    observedByAll = new ArrayList<>();
                    observedByObserverA = new ArrayList<>();
                    observedByObserverB = new ArrayList<>();
                    observedByObserverC = !runWithExtension ? null : new ArrayList<>();

                    for(int k = 0; k < observerValues.size(); k++) // observer index
                    {
                        observerRow = observerPositions.get(k)[0];
                        observerColumn = observerPositions.get(k)[1];

                        substituteA = k == 0 ? observerPositions.get(1)[0] : observerPositions.get(0)[0];
                        substituteB = k == 0 ? observerPositions.get(1)[1] : observerPositions.get(0)[1];
                        substituteC = !runWithExtension ? 0 : substituteA != observerPositions.get(1)[0] && k != 1 ? observerPositions.get(1)[0] : observerPositions.get(2)[0];
                        substituteD = !runWithExtension ? 0 : substituteB != observerPositions.get(1)[1] && k != 1 ? observerPositions.get(1)[1] : observerPositions.get(2)[1];

                        for(int[] positions : cellsContainingValuePositions)
                        {
                            cellWithValueRow = positions[0];
                            cellWithValueColumn = positions[1];

                            if(cellWithValueRow == observerRow && cellWithValueColumn == observerColumn || cellWithValueRow == substituteA && cellWithValueColumn == substituteB || runWithExtension && cellWithValueRow == substituteC && cellWithValueColumn == substituteD) // skip if cell is an observer
                            {
                                continue;
                            }

                            if(observerRow == cellWithValueRow || observerColumn == cellWithValueColumn || board.findSubBoardNumber(observerRow, observerColumn) == board.findSubBoardNumber(cellWithValueRow, cellWithValueColumn)) // add non-observer observable cells with value to list
                            {
                                key = (cellWithValueRow + "," + cellWithValueColumn);

                                if(!observedByAll.contains(key)) // add cells with value to a list of collectively observed cells (no duplicates)
                                {
                                    observedByAll.add(key);
                                }

                                if(k == 0)
                                {
                                    observedByObserverA.add(key);
                                }
                                else if(k == 1)
                                {
                                    observedByObserverB.add(key);
                                }
                                else // runWithExtension is true
                                {
                                    observedByObserverC.add(key);
                                }
                            }
                        }
                    }

                    for(String observedKey : observedByAll) // remove value from cells that are observable by all observers
                    {
                        if(!runWithExtension && observedByObserverA.contains(observedKey) && observedByObserverB.contains(observedKey) || runWithExtension && observedByObserverA.contains(observedKey) && observedByObserverB.contains(observedKey) && observedByObserverC.contains(observedKey))
                        {
                            String[] parts = observedKey.split(",");

                            updatePossibleCounts(i, null, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), false);

                            possibleNumbers.get(observedKey).remove((Integer) i);

                            System.out.println(i);
                        }
                    }
                }
            }
        }
    }

    public void wXYZWing()
    {
        // Danny
        List<int[]> cellsContainingValuePositions;
        List<int[]> hingeValues;
        List<int[]> hingePositions;
        List<int[]> observerCandidateValues;
        List<int[]> observerCandidatePositions;
        List<int[]> observerValues;
        List<int[]> observerPositions;
        List<int[]> nonRestrictedValues;
        List<int[]> nonRestrictedPositions;
        List<String> observedByAll;
        List<String> observedByObserverA;
        List<String> observedByObserverB;
        List<String> observedByObserverC;
        List<String> observedByObserverD;

        int hingeValue1;
        int hingeValue2;
        int hingeValue3;
        int observerCandidateValue1;
        int observerCandidateValue2;
        int cellWithValueRow;
        int cellWithValueColumn;
        int hingeRow;
        int hingeColumn;
        int observerCandidateRow;
        int observerCandidateColumn;
        int observerRow;
        int observerColumn;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;
        int substituteC;
        int substituteD;

        for(int i = 1; i <= boardSize; i++) // value
        {
            cellsContainingValuePositions = new ArrayList<>();
            hingeValues = new ArrayList<>();
            hingePositions = new ArrayList<>();
            observerCandidateValues = new ArrayList<>();
            observerCandidatePositions = new ArrayList<>();

            for(int j = 0; j < boardSize; j++) // row
            {
                for (int k = 0; k < boardSize; k++) // column
                {
                    String key = (j + "," + k);

                    if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                    {
                        cellsContainingValuePositions.add(new int[] {j, k}); // save all keys that contains value regardless of size or runWithExtension

                        if(possibleNumbers.get(key).size() == 4) // save all keys of size 4 that contains value ("hinges"), omitting value
                        {
                            substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;
                            substituteB = !possibleNumbers.get(key).get(substituteA).equals(possibleNumbers.get(key).get(1)) && possibleNumbers.get(key).get(1) != i ? 1 : 2;
                            substituteC = !possibleNumbers.get(key).get(substituteB).equals(possibleNumbers.get(key).get(2)) && possibleNumbers.get(key).get(2) != i ? 2 : 3;

                            hingeValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB), possibleNumbers.get(key).get(substituteC)});
                            hingePositions.add(new int[] {j, k});
                        }
                        else if(possibleNumbers.get(key).size() == 2) // save all keys of size 2 that contains value ("observer candidates")
                        {
                            observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1)});
                            observerCandidatePositions.add(new int[] {j, k});
                        }
                    }
                }
            }

            for(int j = 0; j < hingeValues.size(); j++) // hingeValues index
            {
                observerValues = new ArrayList<>();
                observerPositions = new ArrayList<>();
                nonRestrictedValues = new ArrayList<>();
                nonRestrictedPositions = new ArrayList<>();

                hingeValue1 = hingeValues.get(j)[0];
                hingeValue2 = hingeValues.get(j)[1];
                hingeValue3 = hingeValues.get(j)[2];

                for(int k = 0; k < observerCandidateValues.size(); k++) // observerCandidateValues index
                {
                    observerCandidateValue1 = observerCandidateValues.get(k)[0];
                    observerCandidateValue2 = observerCandidateValues.get(k)[1];

                    if(observerCandidateValue1 == hingeValue1 || observerCandidateValue1 == hingeValue2 || observerCandidateValue1 == hingeValue3 || observerCandidateValue2 == hingeValue1 || observerCandidateValue2 == hingeValue2  || observerCandidateValue2 == hingeValue3)
                    {
                        hingeRow = hingePositions.get(j)[0];;
                        hingeColumn = hingePositions.get(j)[1];
                        observerCandidateRow = observerCandidatePositions.get(k)[0];
                        observerCandidateColumn = observerCandidatePositions.get(k)[1];

                        if(hingeRow == observerCandidateRow || hingeColumn == observerCandidateColumn || board.findSubBoardNumber(hingeRow, hingeColumn) == board.findSubBoardNumber(observerCandidateRow, observerCandidateColumn)) // save all observers visible from hinge
                        {
                            observerValues.add(new int[] {observerCandidateValue1, observerCandidateValue2});
                            observerPositions.add(new int[] {observerCandidateRow, observerCandidateColumn});
                        }
                    }
                }

                if(observerValues.size() == 3) // enough observers found, check if there is a non-restricted observer
                {
                    for(int k = 0; k < observerValues.size(); k++)
                    {
                        substituteA = k == 0 ? 1 : 0;
                        substituteB = substituteA != 1 && k != 1 ? 1 : 2;

                        if(observerPositions.get(k)[0] == observerPositions.get(substituteA)[0] || observerPositions.get(k)[1] == observerPositions.get(substituteA)[1] || board.findSubBoardNumber(observerPositions.get(k)[0], observerPositions.get(k)[1]) == board.findSubBoardNumber(observerPositions.get(substituteA)[0], observerPositions.get(substituteA)[1]))
                        {
                            continue;
                        }
                        else if(observerPositions.get(k)[0] == observerPositions.get(substituteB)[0] || observerPositions.get(k)[1] == observerPositions.get(substituteB)[1] || board.findSubBoardNumber(observerPositions.get(k)[0], observerPositions.get(k)[1]) == board.findSubBoardNumber(observerPositions.get(substituteB)[0], observerPositions.get(substituteB)[1]))
                        {
                            continue;
                        }

                        nonRestrictedValues.add(observerValues.get(k));
                        nonRestrictedPositions.add(observerPositions.get(k));

                        System.out.println();
                    }
                }

                if(!nonRestrictedValues.isEmpty()) // non-restricted observer found, find all visible cells with value from observer
                {

                }
            }
        }
    }
}

