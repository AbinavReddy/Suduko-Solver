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

    public Solver(Solver solverToCopy)
    {
        board = solverToCopy.board;
        boardSize = solverToCopy.boardSize;
        boardLengthWidth = solverToCopy.boardLengthWidth;
        possibleNumbers = new HashMap<>(solverToCopy.possibleNumbers);
        possibleNumbersCount = solverToCopy.possibleNumbersCount;
        valuePossibleCountRows = new int[boardSize + 1][boardSize];
        valuePossibleCountColumns = new int[boardSize + 1][boardSize];;
        valuePossibleCountSubBoards = new int[boardSize + 1][boardSize];;

        for(int value = 1; value < boardSize; value++)
        {
            for(int rowColumnSubBoard = 0; rowColumnSubBoard < boardSize; rowColumnSubBoard++)
            {
                valuePossibleCountRows[value][rowColumnSubBoard] = solverToCopy.valuePossibleCountRows[value][rowColumnSubBoard];
                valuePossibleCountColumns[value][rowColumnSubBoard] = solverToCopy.valuePossibleCountColumns[value][rowColumnSubBoard];
                valuePossibleCountSubBoards[value][rowColumnSubBoard] = solverToCopy.valuePossibleCountSubBoards[value][rowColumnSubBoard];
            }
        }
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

    public boolean isBoardSolvable()
    {
        return false;
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
        /*
        while(!board.isGameFinished())
        {
            int possibleCountBefore = possibleNumbersCount;

            do
            {
                possibleCountBefore = possibleNumbersCount;

                nakedSingles();
            }
            while(possibleCountBefore != possibleNumbersCount); // run nakedSingles till there are no cells of size = 1

            // solving strategies go here (nakedSingles surrounding)
            intersectionRemoval();
            nakedSingles();
            wingStrategies();
            nakedSingles();

            if(possibleCountBefore == possibleNumbersCount && !board.isGameFinished()) // board is unsolvable with strategies, try backtracking
            {
                return solveWithBacktracking();
            }
        }
        */

        return solveWithBacktracking();
    }

    public boolean solveWithBacktracking()
    {
        // Danny, Abinav, Yahya
        for(int row = 0; row < boardSize; row++)
        {
            for(int column = 0; column < boardSize; column++)
            {
                if(board.getBoard()[row][column] == 0)
                {
                    for(int valueToTry = 1; valueToTry <= boardSize; valueToTry++)
                    {
                        if(board.checkPlacementRow(row, valueToTry) && board.checkPlacementColumn(column, valueToTry) && board.checkPlacementSubBoard(row, column, valueToTry))
                        {
                            board.setBoardValue(row, column, valueToTry);
                            //updatePossibleCounts(valueToTry, null, row, column, false);

                            if(solveWithBacktracking())
                            {
                                return true;
                            }
                            else
                            {
                                board.setBoardValue(row, column, 0);
                                //updatePossibleCounts(valueToTry, null, row, column, true);
                            }
                        }
                    }

                    return false;
                }
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
        wXYZWingWithExtension(false);
        wXYZWingWithExtension(true);
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
        List<String> observedCollectively;
        List<List<String>> observedIndividually;

        int observersNeeded = !runWithExtension ? 2 : 3;
        int hingeValue1;
        int hingeValue2;
        int observerCandidateValue;
        int observerCandidateRow;
        int observerCandidateColumn;
        int cellWithValueRow;
        int cellWithValueColumn;
        int hingeRow;
        int hingeColumn;
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
                                else // save all keys of size 2 that contains value ("observer candidates"), omitting value
                                {
                                    substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;

                                    observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(substituteA)});
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
                            else if(possibleNumbers.get(key).size() == 2) // save all keys of size 2 that contains value ("observer candidates"), omitting value
                            {
                                substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;

                                observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(substituteA)});
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

                hingeValue1 = hingeValues.get(j)[0];
                hingeValue2 = hingeValues.get(j)[1];

                for(int k = 0; k < observerCandidateValues.size(); k++) // observerCandidateValues index
                {
                    observerCandidateValue = observerCandidateValues.get(k)[0];

                    if(hingeValue1 == observerCandidateValue || hingeValue2 == observerCandidateValue)
                    {
                        hingeRow = hingePositions.get(j)[0];;
                        hingeColumn = hingePositions.get(j)[1];
                        observerCandidateRow = observerCandidatePositions.get(k)[0];
                        observerCandidateColumn = observerCandidatePositions.get(k)[1];

                        if(hingeRow == observerCandidateRow || hingeColumn == observerCandidateColumn || board.findSubBoardNumber(hingeRow, hingeColumn) == board.findSubBoardNumber(observerCandidateRow, observerCandidateColumn)) // save all observers visible from hinge
                        {
                            if(runWithExtension && observerValues.isEmpty()) // in extended xYZWing the hinge itself is always an observer
                            {
                                observerValues.add(new int[] {hingeValue1, hingeValue2});
                                observerPositions.add(new int[] {hingeRow, hingeColumn});
                            }

                            observerValues.add(new int[] {observerCandidateValue});
                            observerPositions.add(new int[] {observerCandidateRow, observerCandidateColumn});

                            if(observerValues.size() == observersNeeded)
                            {
                                break;
                            }
                        }
                    }
                }

                if(!runWithExtension && observerValues.size() == 2 || runWithExtension && observerValues.size() == 3) // enough observers found, find all visible cells with value from observers
                {
                    String key;
                    observedCollectively = new ArrayList<>();
                    observedIndividually = new ArrayList<>();

                    for(int k = 0; k < observerValues.size(); k++) // observer index
                    {
                        observerRow = observerPositions.get(k)[0];
                        observerColumn = observerPositions.get(k)[1];

                        observedIndividually.add(new ArrayList<>());

                        substituteA = k == 0 ? observerPositions.get(1)[0] : observerPositions.get(0)[0];
                        substituteB = k == 0 ? observerPositions.get(1)[1] : observerPositions.get(0)[1];
                        substituteC = !runWithExtension ? 0 : k == 0 || k == 1 ? observerPositions.get(2)[0] : observerPositions.get(1)[0];
                        substituteD = !runWithExtension ? 0 : k == 0 || k == 1 ? observerPositions.get(2)[1] : observerPositions.get(1)[1];

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

                                if(!observedCollectively.contains(key)) // add cells with value to a list of collectively observed cells (no duplicates)
                                {
                                    observedCollectively.add(key);
                                }

                                observedIndividually.get(observedIndividually.size() - 1).add(key);
                            }
                        }
                    }

                    for(String collectiveKey : observedCollectively) // remove value from cells that are observable by all observers
                    {
                        for(List<String> individualKeys : observedIndividually)
                        {
                            if(!individualKeys.contains(collectiveKey))
                            {
                                break;
                            }
                            else if(individualKeys.equals(observedIndividually.get(observedIndividually.size() - 1)))
                            {
                                String[] parts = collectiveKey.split(",");

                                updatePossibleCounts(i, null, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), false);

                                possibleNumbers.get(collectiveKey).remove((Integer) i);
                            }
                        }
                    }
                }
            }
        }
    }

    public void wXYZWingWithExtension(boolean runWithExtension)
    {
        // Danny
        List<int[]> cellsContainingValuePositions;
        List<int[]> hingeValues;
        List<int[]> hingePositions;
        List<int[]> observerCandidateValues;
        List<int[]> observerCandidatePositions;
        List<int[]> observerValues;
        List<int[]> observerPositions;
        Set<Integer> unionCandidateValues;
        List<String> observedCollectively;
        List<List<String>> observedIndividually;

        int observerValuesNeeded = !runWithExtension ? 4 : 5;
        int unionValuesNeeded = !runWithExtension ? 3 : 4;
        int hingeValuesContained;
        int observerValuesContained;
        int hingeValue1;
        int hingeValue2;
        int hingeValue3;
        int hingeValue4;
        int observerCandidateValue1;
        int observerCandidateValue2;
        int observerCandidateValue3;
        int observerCandidateValue4;
        int observerCandidateRow;
        int observerCandidateColumn;
        boolean observersPassedCheck;
        boolean nonRestrictedPresent;
        int cellWithValueRow;
        int cellWithValueColumn;
        int hingeRow;
        int hingeColumn;
        int observerRow;
        int observerColumn;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;
        int substituteC;
        int substituteD;
        int substituteE;
        int substituteF;
        int substituteG;
        int substituteH;

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
                        if(possibleNumbers.get(key).contains(i))
                        {
                            cellsContainingValuePositions.add(new int[] {j, k}); // save positions of all keys that contains value

                            if(possibleNumbers.get(key).size() == 2) // save all keys of size 2 that contains value ("observer candidates"), omitting value
                            {
                                substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;

                                observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(substituteA)});
                                observerCandidatePositions.add(new int[] {j, k});
                            }
                            else if(possibleNumbers.get(key).size() == 3) // save all keys of size 3 that contains value ("hinges"/"observer candidates"), omitting value
                            {
                                substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;
                                substituteB = !possibleNumbers.get(key).get(substituteA).equals(possibleNumbers.get(key).get(1)) && possibleNumbers.get(key).get(1) != i ? 1 : 2;

                                hingeValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB)});
                                hingePositions.add(new int[] {j, k});
                                observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB)});
                                observerCandidatePositions.add(new int[] {j, k});
                            }
                            else if(possibleNumbers.get(key).size() == 4) // save all keys of size 4 that contains value ("hinges"/"observer candidates"), omitting value
                            {
                                substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;
                                substituteB = possibleNumbers.get(key).get(0) == i || possibleNumbers.get(key).get(1) == i ? 2 : 1;
                                substituteC = possibleNumbers.get(key).get(0) == i || possibleNumbers.get(key).get(1) == i || possibleNumbers.get(key).get(2) == i  ? 3 : 2;

                                hingeValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB), possibleNumbers.get(key).get(substituteC)});
                                hingePositions.add(new int[] {j, k});
                                observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB), possibleNumbers.get(key).get(substituteC)});
                                observerCandidatePositions.add(new int[] {j, k});
                            }
                            else if(runWithExtension && possibleNumbers.get(key).size() == 5) // save all keys of size 5 that contains value ("hinges"/"observer candidates"), omitting value
                            {
                                substituteA = possibleNumbers.get(key).get(0) == i ? 1 : 0;
                                substituteB = possibleNumbers.get(key).get(0) == i || possibleNumbers.get(key).get(1) == i ? 2 : 1;
                                substituteC = possibleNumbers.get(key).get(0) == i || possibleNumbers.get(key).get(1) == i || possibleNumbers.get(key).get(2) == i  ? 3 : 2;
                                substituteD = possibleNumbers.get(key).get(0) == i || possibleNumbers.get(key).get(1) == i || possibleNumbers.get(key).get(2) == i || possibleNumbers.get(key).get(3) == i ? 4 : 3;

                                hingeValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB), possibleNumbers.get(key).get(substituteC), possibleNumbers.get(key).get(substituteD)});
                                hingePositions.add(new int[] {j, k});
                                observerCandidateValues.add(new int[] {possibleNumbers.get(key).get(substituteA), possibleNumbers.get(key).get(substituteB), possibleNumbers.get(key).get(substituteC), possibleNumbers.get(key).get(substituteD)});
                                observerCandidatePositions.add(new int[] {j, k});
                            }
                        }
                        else
                        {
                            if(possibleNumbers.get(key).size() == 2) // save all keys of size 2 that doesn't contain value ("hinges")
                            {
                                hingeValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1)});
                                hingePositions.add(new int[] {j, k});
                            }
                            else if(possibleNumbers.get(key).size() == 3) // save all keys of size 3 that contains value ("hinges")
                            {
                                hingeValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1), possibleNumbers.get(key).get(2)});
                                hingePositions.add(new int[] {j, k});
                            }
                            else if(runWithExtension && possibleNumbers.get(key).size() == 4) // save all keys of size 4 that contains value ("hinges")
                            {
                                hingeValues.add(new int[] {possibleNumbers.get(key).get(0), possibleNumbers.get(key).get(1), possibleNumbers.get(key).get(2), possibleNumbers.get(key).get(3)});
                                hingePositions.add(new int[] {j, k});
                            }
                        }
                    }
                }
            }

            for(int j = 0; j < hingeValues.size(); j++) // hingeValues index
            {
                observerValues = new ArrayList<>();
                observerPositions = new ArrayList<>();
                unionCandidateValues = new HashSet<>();

                hingeValuesContained = hingeValues.get(j).length;
                hingeValue1 = hingeValues.get(j)[0];
                hingeValue2 = hingeValues.get(j)[1];
                hingeValue3 = hingeValuesContained == 3 ? hingeValues.get(j)[2] : 0;
                hingeValue4 = hingeValuesContained == 4 ? hingeValues.get(j)[3] : 0;

                for(int k = 0; k < observerCandidateValues.size(); k++) // observerCandidateValues index
                {
                    observerValuesContained = observerCandidateValues.get(k).length;
                    observerCandidateValue1 = observerCandidateValues.get(k)[0];
                    observerCandidateValue2 = observerValuesContained == 2 || observerValuesContained == 3 ? observerCandidateValues.get(k)[1] : 0;
                    observerCandidateValue3 = observerValuesContained == 3 ? observerCandidateValues.get(k)[2] : 0;
                    observerCandidateValue4 = observerValuesContained == 4 ? observerCandidateValues.get(k)[3] : 0;

                    if(hingeValue1 == observerCandidateValue1 || hingeValue1 == observerCandidateValue2  || hingeValue1 == observerCandidateValue3  || hingeValue1 == observerCandidateValue4 || hingeValue2 == observerCandidateValue1 || hingeValue2 == observerCandidateValue2  || hingeValue2 == observerCandidateValue3 || hingeValue2 == observerCandidateValue4 || hingeValue3 == observerCandidateValue1 || hingeValue3 == observerCandidateValue2  || hingeValue3 == observerCandidateValue3 || hingeValue3 == observerCandidateValue4 || hingeValue4 == observerCandidateValue1 || hingeValue4 == observerCandidateValue2  || hingeValue4 == observerCandidateValue3 || hingeValue4 == observerCandidateValue4)
                    {
                        hingeRow = hingePositions.get(j)[0];
                        hingeColumn = hingePositions.get(j)[1];
                        observerCandidateRow = observerCandidatePositions.get(k)[0];
                        observerCandidateColumn = observerCandidatePositions.get(k)[1];

                        if(hingeRow != observerCandidateRow || hingeColumn != observerCandidateColumn)
                        {
                            if(hingeRow == observerCandidateRow || hingeColumn == observerCandidateColumn || board.findSubBoardNumber(hingeRow, hingeColumn) == board.findSubBoardNumber(observerCandidateRow, observerCandidateColumn)) // save all observers visible from hinge
                            {
                                if(observerValues.isEmpty()) // in wXYZWing the hinge itself is always an observer
                                {
                                    if(hingeValuesContained == 2)
                                    {
                                        observerValues.add(new int[] {hingeValue1, hingeValue2});

                                        unionCandidateValues.add(hingeValue1);
                                        unionCandidateValues.add(hingeValue2);
                                    }
                                    else if(hingeValuesContained == 3)
                                    {
                                        observerValues.add(new int[] {hingeValue1, hingeValue2, hingeValue3});

                                        unionCandidateValues.add(hingeValue3);
                                    }
                                    else
                                    {
                                        observerValues.add(new int[] {hingeValue1, hingeValue2, hingeValue3, hingeValue4});

                                        unionCandidateValues.add(hingeValue4);
                                    }

                                    observerPositions.add(new int[] {hingeRow, hingeColumn});
                                }

                                if(observerValuesContained == 1)
                                {
                                    observerValues.add(new int[] {observerCandidateValue1});

                                    unionCandidateValues.add(observerCandidateValue1);
                                }
                                else if(observerValuesContained == 2)
                                {
                                    observerValues.add(new int[] {observerCandidateValue1, observerCandidateValue2});

                                    unionCandidateValues.add(observerCandidateValue1);
                                    unionCandidateValues.add(observerCandidateValue2);
                                }
                                else if(observerValuesContained == 3)
                                {
                                    observerValues.add(new int[] {observerCandidateValue1, observerCandidateValue2, observerCandidateValue3});

                                    unionCandidateValues.add(observerCandidateValue1);
                                    unionCandidateValues.add(observerCandidateValue2);
                                    unionCandidateValues.add(observerCandidateValue3);
                                }
                                else
                                {
                                    observerValues.add(new int[] {observerCandidateValue1, observerCandidateValue2, observerCandidateValue3, observerCandidateValue4});

                                    unionCandidateValues.add(observerCandidateValue1);
                                    unionCandidateValues.add(observerCandidateValue2);
                                    unionCandidateValues.add(observerCandidateValue3);
                                    unionCandidateValues.add(observerCandidateValue4);
                                }

                                observerPositions.add(new int[] {observerCandidateRow, observerCandidateColumn});

                                if(observerValues.size() == observerValuesNeeded)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

                if(observerValues.size() == observerValuesNeeded && unionCandidateValues.size() == unionValuesNeeded) // enough observers and the right amount of union values found
                {
                    observersPassedCheck = true;
                    nonRestrictedPresent = false;

                    for(int unionValue : unionCandidateValues) // find observers with same values
                    {
                        List<int[]> positions = new ArrayList<>();

                        for(int k = 1; k < observerValues.size(); k++) // skip the hinge because it is always observable
                        {
                            for(int l = 0; l < observerValues.get(k).length; l++)
                            {
                                if(observerValues.get(k)[l] == unionValue)
                                {
                                    positions.add(observerPositions.get(k));
                                }
                            }
                        }

                        if(positions.size() > 1) // check if all observers with same values can see each other
                        {
                            for(int k = 0; k < positions.size() - 1; k++)
                            {
                                if(positions.get(k)[0] != positions.get(k + 1)[0] && positions.get(k)[1] != positions.get(k + 1)[1] && board.findSubBoardNumber(positions.get(k)[0], positions.get(k)[1]) != board.findSubBoardNumber(positions.get(k + 1)[0], positions.get(k + 1)[1])) // save all observers visible from hinge
                                {
                                    observersPassedCheck = false;

                                    break;
                                }
                                else if((k == 0 && positions.size() == 3) && positions.get(k)[0] != positions.get(k + 2)[0] && positions.get(k)[1] != positions.get(k + 2)[1] && board.findSubBoardNumber(positions.get(k)[0], positions.get(k)[1]) != board.findSubBoardNumber(positions.get(k + 2)[0], positions.get(k + 2)[1]))
                                {
                                    observersPassedCheck = false;

                                    break;
                                }
                                else if(runWithExtension && k == 0 && positions.size() == 4 && positions.get(k)[0] != positions.get(k + 3)[0] && positions.get(k)[1] != positions.get(k + 3)[1] && board.findSubBoardNumber(positions.get(k)[0], positions.get(k)[1]) != board.findSubBoardNumber(positions.get(k + 3)[0], positions.get(k + 3)[1]))
                                {
                                    observersPassedCheck = false;

                                    break;
                                }
                            }
                        }

                        if(!observersPassedCheck)
                        {
                            break;
                        }
                    }

                    if(observersPassedCheck) // valid observers confirmed, check if there is a non-restricted observer
                    {
                        for(int k = 1; k < observerValues.size(); k++) // skip the hinge because it is always observable
                        {
                            substituteA = k == 1 ? 2 : 1;
                            substituteB = substituteA != 2 && k != 2 ? 2 : 3;
                            substituteC = substituteB != 3 && k != 3 ? 3 : 4;

                            if(observerPositions.get(k)[0] == observerPositions.get(substituteA)[0] || observerPositions.get(k)[1] == observerPositions.get(substituteA)[1] || board.findSubBoardNumber(observerPositions.get(k)[0], observerPositions.get(k)[1]) == board.findSubBoardNumber(observerPositions.get(substituteA)[0], observerPositions.get(substituteA)[1]))
                            {
                                continue;
                            }
                            else if(observerPositions.get(k)[0] == observerPositions.get(substituteB)[0] || observerPositions.get(k)[1] == observerPositions.get(substituteB)[1] || board.findSubBoardNumber(observerPositions.get(k)[0], observerPositions.get(k)[1]) == board.findSubBoardNumber(observerPositions.get(substituteB)[0], observerPositions.get(substituteB)[1]))
                            {
                                continue;
                            }
                            else if(runWithExtension && (observerPositions.get(k)[0] == observerPositions.get(substituteC)[0] || observerPositions.get(k)[1] == observerPositions.get(substituteC)[1] || board.findSubBoardNumber(observerPositions.get(k)[0], observerPositions.get(k)[1]) == board.findSubBoardNumber(observerPositions.get(substituteC)[0], observerPositions.get(substituteC)[1])))
                            {
                                continue;
                            }

                            nonRestrictedPresent = true;
                        }
                    }

                    if(nonRestrictedPresent) // removing value with wXYZWing() is allowed, continue
                    {
                        String key;
                        observedCollectively = new ArrayList<>();
                        observedIndividually = new ArrayList<>();

                        for(int k = 0; k < observerValues.size(); k++) // observer index
                        {
                            observerRow = observerPositions.get(k)[0];
                            observerColumn = observerPositions.get(k)[1];

                            if(!possibleNumbers.get(observerRow + "," + observerColumn).contains(i)) // only observers containing value matters
                            {
                                continue;
                            }

                            observedIndividually.add(new ArrayList<>());

                            substituteA = k == 0 ? observerPositions.get(1)[0] : observerPositions.get(0)[0];
                            substituteB = k == 0 ? observerPositions.get(1)[1] : observerPositions.get(0)[1];
                            substituteC = k == 0 || k == 1 ? observerPositions.get(2)[0] : observerPositions.get(1)[0];
                            substituteD = k == 0 || k == 1 ? observerPositions.get(2)[1] : observerPositions.get(1)[1];
                            substituteE = k == 0 || k == 1 || k == 2 ? observerPositions.get(3)[0] : observerPositions.get(2)[0];
                            substituteF = k == 0 || k == 1 || k == 2 ? observerPositions.get(3)[1] : observerPositions.get(2)[1];
                            substituteG = !runWithExtension ? 0 : k == 0 || k == 1 || k == 2 || k == 3 ? observerPositions.get(4)[0] : observerPositions.get(3)[0];
                            substituteH = !runWithExtension ? 0 : k == 0 || k == 1 || k == 2 || k == 3 ? observerPositions.get(4)[1] : observerPositions.get(3)[1];

                            for(int[] positions : cellsContainingValuePositions)
                            {
                                cellWithValueRow = positions[0];
                                cellWithValueColumn = positions[1];

                                if(cellWithValueRow == observerRow && cellWithValueColumn == observerColumn || cellWithValueRow == substituteA && cellWithValueColumn == substituteB || cellWithValueRow == substituteC && cellWithValueColumn == substituteD || cellWithValueRow == substituteE && cellWithValueColumn == substituteF || cellWithValueRow == substituteG && cellWithValueColumn == substituteH) // skip if cell is an observer
                                {
                                    continue;
                                }

                                if(observerRow == cellWithValueRow || observerColumn == cellWithValueColumn || board.findSubBoardNumber(observerRow, observerColumn) == board.findSubBoardNumber(cellWithValueRow, cellWithValueColumn)) // add non-observer observable cells with value to list
                                {
                                    key = (cellWithValueRow + "," + cellWithValueColumn);

                                    if(!observedCollectively.contains(key)) // add cells with value to a list of collectively observed cells (no duplicates)
                                    {
                                        observedCollectively.add(key);
                                    }

                                    observedIndividually.get(observedIndividually.size() - 1).add(key);
                                }
                            }
                        }

                        for(String collectiveKey : observedCollectively) // remove value from cells that are observable by all observers containing value
                        {
                            for(List<String> individualKeys : observedIndividually)
                            {
                                if(!individualKeys.contains(collectiveKey))
                                {
                                    break;
                                }
                                else if(individualKeys.equals(observedIndividually.get(observedIndividually.size() - 1)))
                                {
                                    String[] parts = collectiveKey.split(",");

                                    updatePossibleCounts(i, null, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), false);

                                    possibleNumbers.get(collectiveKey).remove((Integer) i);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

