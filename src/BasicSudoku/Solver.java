package BasicSudoku;

import java.util.*;

public class Solver
{
    Board board;
    Board solvableTestBoard;
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
        // Danny & Abinav
        board = solverToCopy.board;
        solvableTestBoard = solverToCopy.solvableTestBoard;
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
        // Danny, Yahya & Abinav
        solvableTestBoard = new Board(board);
        solvableTestBoard.getSolver().board = solvableTestBoard;

        return solvableTestBoard.getSolver().solveWithBacktracking(); // has to be with backtracking to avoid singular strategy boards
    }

    public boolean solveWithStrategies()
    {
        // Danny, Abinav & Yahya
        while(!board.isGameFinished())
        {
            int possibleCountBefore;

            do
            {
                possibleCountBefore = possibleNumbersCount;

                nakedSingles();
            }
            while(possibleCountBefore != possibleNumbersCount); // run nakedSingles till there are no cells of size <= 1

            // solving strategies go here (nakedSingles after each)
            pointingDuplicatesWithBLR(true);
            nakedSingles();
            pointingDuplicatesWithBLR(false);
            nakedSingles();
            xWing(true);
            nakedSingles();
            xWing(false);
            nakedSingles();
            //wXYZWingWithExtension(false);
            //nakedSingles();
            //wXYZWingWithExtension(true);
            //nakedSingles();

            if(possibleCountBefore == possibleNumbersCount && !board.isGameFinished()) // board is unsolvable with strategies, try backtracking
            {
                return solveWithBacktracking();
            }
        }

        return true;
    }

    public boolean solveWithBacktracking()
    {
        // Danny, Abinav & Yahya
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

                            if(solveWithBacktracking())
                            {
                                return true;
                            }
                            else
                            {
                                board.setBoardValue(row, column, 0);
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
        // Abinav, Yahya & Danny

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

    /**
     * @author Danny
     */
    private void pointingDuplicatesWithBLR(boolean processingRows)
    {
        int targetValueCount = boardLengthWidth - 1;
        int valuePossibleCount;
        int valueSubBoardCount;
        int previousSubBoard;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;

        for(int i = 1; i <= boardSize; i++) // value
        {
            for(int j = 0; j < boardSize; j++) // row or column
            {
                valuePossibleCount = processingRows ? valuePossibleCountRows[i][j] : valuePossibleCountColumns[i][j];

                if(valuePossibleCount >= 3) // skip if value already present in row or column (no possibilities)
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
                                int startingRow = (previousSubBoard / boardLengthWidth) * boardLengthWidth;
                                int startingColumn = (previousSubBoard - startingRow) * boardLengthWidth;

                                for(int m = 0; m < boardLengthWidth; m++) // added to starting row or column
                                {
                                    if(processingRows && startingRow + m != j || !processingRows && startingColumn + m != j)
                                    {
                                        for(int n = 0; n < boardLengthWidth; n++) // added to starting row or column
                                        {
                                            substituteA = processingRows ? m : n;
                                            substituteB = processingRows ? n : m;

                                            String key = (startingRow + substituteA) + "," + (startingColumn + substituteB);

                                            if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(i))
                                            {
                                                updatePossibleCounts(i, null, startingRow + substituteA, startingColumn + substituteB,false);

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

    /**
     * @author Danny
     */
    public void xWing(boolean processingRows)
    {
        List<int[]> rowColumnPositions;
        List<List<int[]>> processedForXWings;
        int valuePossibleCount;

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

    /**
     * @author Danny
     */
    public void yWingWithXYZExtension(boolean runWithExtension)
    {
        List<int[]> cellsWithValue;
        List<int[]> hingeCells;
        List<int[]> pincerCandidateCells;
        List<int[]> pincerCells;
        List<int[]> pincersProcessed;

        int pincersNeeded = !runWithExtension ? 2 : 3;

        for(int value = 1; value <= boardSize; value++)
        {
            cellsWithValue = new ArrayList<>();
            hingeCells = new ArrayList<>();
            pincerCandidateCells = new ArrayList<>();

            // Find all positions of cells with value, hinges and pincer candidates
            for(int row = 0; row < boardSize; row++)
            {
                for (int column = 0; column < boardSize; column++)
                {
                    String key = (row + "," + column);

                    if(possibleNumbers.get(key) != null)
                    {
                        if(possibleNumbers.get(key).contains(value))
                        {
                            cellsWithValue.add(new int[]{row, column}); // save keys that contains value regardless of size

                            if(possibleNumbers.get(key).size() == 2) // save keys of size 2 that contains value (pincer candidates)
                            {
                                pincerCandidateCells.add(new int[]{row, column});
                            }
                            else if(runWithExtension && possibleNumbers.get(key).size() == 3) // save keys of size 3 that contains value (hinges)
                            {
                                hingeCells.add(new int[]{row, column});
                            }
                        }
                        else if(!runWithExtension && possibleNumbers.get(key).size() == 2) // save keys of size 2 that doesn't contain value (hinges)
                        {
                            hingeCells.add(new int[] {row, column});
                        }
                    }
                }
            }

            for(int[] hinge : hingeCells)
            {
                pincerCells = new ArrayList<>();

                String hingeKey = hinge[0] + "," + hinge[1];

                int hingeValuesInHinge = possibleNumbers.get(hingeKey).size();
                int hingeValuesInPincers = 0;
                boolean[] hingeValuesPresent = new boolean[hingeValuesInHinge];

                for(int[] pincer : pincerCandidateCells)
                {
                    String pincerKey = pincer[0] + "," + pincer[1];

                    // Make sure the hinge and the pincer has at least one value in common
                    for(int hingeValue = 0; hingeValue < hingeValuesInHinge; hingeValue++)
                    {
                        if(runWithExtension && possibleNumbers.get(hingeKey).get(hingeValue) == value) // value is always observable when it needs to be
                        {
                            if(!hingeValuesPresent[hingeValue])
                            {
                                hingeValuesInPincers++;
                                hingeValuesPresent[hingeValue] = true;
                            }

                            continue;
                        }

                        if(possibleNumbers.get(pincerKey).contains(possibleNumbers.get(hingeKey).get(hingeValue)))
                        {
                            // Find all pincers visible from and not equal to the hinge
                            int hingeRow = hinge[0];
                            int hingeColumn = hinge[1];
                            int pincerCandidateRow = pincer[0];
                            int pincerCandidateColumn = pincer[1];

                            if(hingeRow != pincerCandidateRow || hingeColumn != pincerCandidateColumn) // pincer can't be the same as the hinge
                            {
                                if(hingeRow == pincerCandidateRow || hingeColumn == pincerCandidateColumn || board.findSubBoardNumber(hingeRow, hingeColumn) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from the hinge
                                {
                                    if(runWithExtension && pincerCells.isEmpty()) // in extended yWing (XYZWing) the hinge itself is always a pincer
                                    {
                                        pincerCells.add(hinge);
                                    }

                                    pincerCells.add(pincer);

                                    if(!hingeValuesPresent[hingeValue]) // register hinge value presence in pincers (need to all be there)
                                    {
                                        hingeValuesInPincers++;
                                        hingeValuesPresent[hingeValue] = true;
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }

                // Process every combination of found pincer cells (with or without hinge) for potential value elimination
                if(hingeValuesInHinge == hingeValuesInPincers && pincerCells.size() >= pincersNeeded)
                {
                    pincersProcessed = new ArrayList<>();

                    if(runWithExtension) // possibly add the hinge cell
                    {
                        pincersProcessed.add(pincerCells.get(0));
                    }

                    List<int[]> processedBeforeA; // variables for resetting instead of removing (less intensive and faster)
                    List<int[]> processedBeforeB;

                    for(int pincerA = (!runWithExtension ? 0 : 1); pincerA < pincerCells.size() - 1; pincerA++)
                    {
                        processedBeforeA = new ArrayList<>(pincersProcessed);
                        pincersProcessed.add(pincerCells.get(pincerA));

                        for(int pincerB = pincerA + 1; pincerB < pincerCells.size(); pincerB++)
                        {
                            processedBeforeB = new ArrayList<>(pincersProcessed);
                            pincersProcessed.add(pincerCells.get(pincerB));

                            // Find universally observed non-pincer cells with value and eliminate value from them
                            advancedWingElimination(value, cellsWithValue, pincersProcessed);

                            pincersProcessed = processedBeforeB; // resetting
                        }

                        pincersProcessed = processedBeforeA;
                    }
                }
            }
        }
    }

    /**
     * @author Danny
     */
    public void wXYZWingWithExtension(boolean runWithExtension)
    {
        List<int[]> cellsWithValue;
        List<int[]> hingeCells;
        List<int[]> pincerCandidateCells;
        List<int[]> pincerCells;
        List<int[]> pincersProcessed;
        Set<Integer> unionValues;

        int pincersValuesNeeded = !runWithExtension ? 4 : 5;

        for(int value = 1; value <= boardSize; value++)
        {
            cellsWithValue = new ArrayList<>();
            hingeCells = new ArrayList<>();
            pincerCandidateCells = new ArrayList<>();

            // Find all positions of cells with value, hinges and pincer candidates
            for(int row = 0; row < boardSize; row++)
            {
                for (int column = 0; column < boardSize; column++)
                {
                    String key = (row + "," + column);

                    if(possibleNumbers.get(key) != null)
                    {
                        if(possibleNumbers.get(key).contains(value))
                        {
                            cellsWithValue.add(new int[] {row, column}); // save keys that contains value regardless of size

                            if(possibleNumbers.get(key).size() == 2) // save keys of size 2 that contains value (pincer candidates)
                            {
                                pincerCandidateCells.add(new int[] {row, column});
                            }
                            else if(possibleNumbers.get(key).size() == 3 || possibleNumbers.get(key).size() == 4 || (runWithExtension && possibleNumbers.get(key).size() == 5)) // save keys of size 3-5 that contains value (both)
                            {
                                hingeCells.add(new int[] {row, column});
                                pincerCandidateCells.add(new int[] {row, column});
                            }
                        }
                        else
                        {
                            if(possibleNumbers.get(key).size() == 2 || possibleNumbers.get(key).size() == 3 || (runWithExtension && possibleNumbers.get(key).size() == 4)) // save keys of size 2-4 that doesn't contain value (hinges)
                            {
                                hingeCells.add(new int[] {row, column});
                            }
                        }
                    }
                }
            }

            // Process all hinges (1-2) and pincers (3-4) for value elimination
            for(int firstHinge = 0; firstHinge < hingeCells.size(); firstHinge++)
            {
                for(int secondHinge = firstHinge; secondHinge < hingeCells.size(); secondHinge++)
                {
                    if(hingeCells.get(firstHinge)[0] != hingeCells.get(secondHinge)[0] && hingeCells.get(firstHinge)[1] != hingeCells.get(secondHinge)[1] && board.findSubBoardNumber(hingeCells.get(firstHinge)[0], hingeCells.get(firstHinge)[1]) != board.findSubBoardNumber(hingeCells.get(secondHinge)[0], hingeCells.get(secondHinge)[1]))
                    {
                        continue;
                    }

                    pincerCells = new ArrayList<>();
                    unionValues = new HashSet<>();

                    boolean multipleHinges = firstHinge != secondHinge;
                    String hingeKeyA = hingeCells.get(firstHinge)[0] + "," + hingeCells.get(firstHinge)[1];
                    String hingeKeyB = !multipleHinges ? null : hingeCells.get(secondHinge)[0] + "," + hingeCells.get(secondHinge)[1];

                    for(int[] pincer : pincerCandidateCells)
                    {
                        String pincerKey = pincer[0] + "," + pincer[1];

                        // Make sure the hinges and the pincer have at least one value in common
                        boolean valuesInCommon = false;
                        int hingesInvolved = !multipleHinges ? 1 : 2;

                        nestedLoop: // label, allows breaking out of nested loops easily
                        {
                            for(int hinge = 0; hinge < hingesInvolved; hinge++)
                            {
                                String currentHingeKey = hinge == 0 ? hingeKeyA : hingeKeyB;

                                for(int hingeValue = 0; hingeValue < possibleNumbers.get(currentHingeKey).size(); hingeValue++)
                                {
                                    if(possibleNumbers.get(pincerKey).contains(possibleNumbers.get(currentHingeKey).get(hingeValue)))
                                    {
                                        valuesInCommon = true;
                                    }
                                }

                                if(valuesInCommon)
                                {
                                    if(hinge != hingesInvolved - 1) // pincer doesn't have a value in common with at least one hinge
                                    {
                                        valuesInCommon = false;
                                    }
                                    else
                                    {
                                        break nestedLoop;
                                    }
                                }
                                else if(hinge == hingesInvolved - 1)
                                {
                                    break nestedLoop;
                                }
                            }
                        }

                        // Find all pincers visible from and not equal to hinges
                        if(valuesInCommon)
                        {
                            int hingeRowA = hingeCells.get(firstHinge)[0];
                            int hingeColumnA = hingeCells.get(firstHinge)[1];
                            int hingeRowB = hingeCells.get(secondHinge)[0];
                            int hingeColumnB = hingeCells.get(secondHinge)[1];
                            int pincerCandidateRow = pincer[0];
                            int pincerCandidateColumn = pincer[1];

                            if((hingeRowA != pincerCandidateRow || hingeColumnA != pincerCandidateColumn) && (!multipleHinges || hingeRowB != pincerCandidateRow || hingeColumnB != pincerCandidateColumn)) // pincer can't be the same as the hinges
                            {
                                if(hingeRowA == pincerCandidateRow || hingeColumnA == pincerCandidateColumn || board.findSubBoardNumber(hingeRowA, hingeColumnA) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from first hinge
                                {
                                    if(!multipleHinges || hingeRowB == pincerCandidateRow || hingeColumnB == pincerCandidateColumn || board.findSubBoardNumber(hingeRowB, hingeColumnB) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from second hinge
                                    {
                                        if(pincerCells.isEmpty()) // in wXYZWing the hinges themselves are always pincers
                                        {
                                            pincerCells.add(hingeCells.get(firstHinge));
                                            unionValues.addAll(possibleNumbers.get(hingeKeyA));

                                            if(multipleHinges)
                                            {
                                                pincerCells.add(hingeCells.get(secondHinge));
                                                unionValues.addAll(possibleNumbers.get(hingeKeyB));
                                            }
                                        }

                                        pincerCells.add(pincer);
                                    }
                                }
                            }
                        }
                    }

                    // Process every combination of found pincer cells (with hinges) for potential value elimination
                    if(pincerCells.size() >= pincersValuesNeeded)
                    {
                        pincersProcessed = new ArrayList<>();
                        pincersProcessed.add(pincerCells.get(0)); // always add first hinge cell (pincerA)

                        if(multipleHinges) // possibly add second hinge cell (then pincerB)
                        {
                            pincersProcessed.add(pincerCells.get(1));
                        }

                        List<int[]> processedBeforeB; // variables for resetting instead of removing (less intensive and faster)
                        List<int[]> processedBeforeC;
                        List<int[]> processedBeforeD;
                        List<int[]> processedBeforeE;
                        Set<Integer> unionBeforeB;
                        Set<Integer> unionBeforeC;
                        Set<Integer> unionBeforeD;
                        Set<Integer> unionBeforeE;

                        for(int pincerB = 1; pincerB < (!multipleHinges ? (pincerCells.size() - (!runWithExtension ? 2 : 3)) : 2); pincerB++)
                        {
                            processedBeforeB = !multipleHinges ? new ArrayList<>(pincersProcessed) : null; // resetting for pincerB only necessary when there is one hinge
                            unionBeforeB = !multipleHinges ? new HashSet<>(unionValues) : null;

                            if(!multipleHinges)
                            {
                                pincersProcessed.add(pincerCells.get(pincerB));
                                unionValues.addAll(possibleNumbers.get(pincerCells.get(pincerB)[0] + "," + pincerCells.get(pincerB)[1]));
                            }

                            for(int pincerC = pincerB + 1; pincerC < pincerCells.size() - (!runWithExtension ? 1 : 2); pincerC++)
                            {
                                processedBeforeC = new ArrayList<>(pincersProcessed);
                                pincersProcessed.add(pincerCells.get(pincerC));
                                unionBeforeC = new HashSet<>(unionValues);
                                unionValues.addAll(possibleNumbers.get(pincerCells.get(pincerC)[0] + "," + pincerCells.get(pincerC)[1]));

                                for(int pincerD = pincerC + 1; pincerD < pincerCells.size() - (!runWithExtension ? 0 : 1); pincerD++)
                                {
                                    processedBeforeD = new ArrayList<>(pincersProcessed);
                                    pincersProcessed.add(pincerCells.get(pincerD));
                                    unionBeforeD = new HashSet<>(unionValues);
                                    unionValues.addAll(possibleNumbers.get(pincerCells.get(pincerD)[0] + "," + pincerCells.get(pincerD)[1]));

                                    for(int pincerE = pincerD + 1; pincerE < (!runWithExtension ? pincerCells.size() + 1 : pincerCells.size()); pincerE++)
                                    {
                                        processedBeforeE = !runWithExtension ? null : new ArrayList<>(pincersProcessed);
                                        unionBeforeE = !runWithExtension ? null : new HashSet<>(unionValues);

                                        if(runWithExtension)
                                        {
                                            pincersProcessed.add(pincerCells.get(pincerE));
                                            unionValues.addAll(possibleNumbers.get(pincerCells.get(pincerE)[0] + "," + pincerCells.get(pincerE)[1]));
                                        }

                                        // Perform final checks of the pincer combination
                                        if(unionValues.size() == pincersValuesNeeded)
                                        {
                                            boolean hasNonRestricted = false;
                                            boolean allValuesObservable = true; // has to be true for all, so it is easier to detect a negative

                                            nestedLoops:
                                            {
                                                for(Integer unionValue : unionValues)
                                                {
                                                    // Check if there is at least one non-restricted pincer with value and none with other values
                                                    if(unionValue == value)
                                                    {
                                                        for(int nonRestrictedPincer = !multipleHinges ? 1 : 2; nonRestrictedPincer < (pincersValuesNeeded - 1); nonRestrictedPincer++) // skip hinges because they are always observable
                                                        {
                                                            for(int otherPincer = !multipleHinges ? 1 : 2; otherPincer < pincersProcessed.size(); otherPincer++)
                                                            {
                                                                if(otherPincer == nonRestrictedPincer)
                                                                {
                                                                    continue;
                                                                }

                                                                if(pincersProcessed.get(nonRestrictedPincer)[0] == (pincersProcessed.get(otherPincer)[0]) || pincersProcessed.get(nonRestrictedPincer)[1] == (pincersProcessed.get(otherPincer)[1]) || board.findSubBoardNumber(pincersProcessed.get(nonRestrictedPincer)[0], pincersProcessed.get(nonRestrictedPincer)[1]) == board.findSubBoardNumber(pincersProcessed.get(otherPincer)[0], pincersProcessed.get(otherPincer)[1]))
                                                                {
                                                                    continue;
                                                                }

                                                                if(possibleNumbers.get(pincersProcessed.get(nonRestrictedPincer)[0] + "," + pincersProcessed.get(nonRestrictedPincer)[1]).contains(value))
                                                                {
                                                                    hasNonRestricted = true;
                                                                }
                                                                else // non restricted pincer without value found (not allowed)
                                                                {
                                                                    break nestedLoops;
                                                                }
                                                            }
                                                        }

                                                        if(!hasNonRestricted)
                                                        {
                                                            break nestedLoops;
                                                        }
                                                        else
                                                        {
                                                            continue; // value doesn't need the check below
                                                        }
                                                    }

                                                    // Check if all pincers with same union values (except value) are observable by each other
                                                    List<int[]> unionValuePresent = new ArrayList<>();

                                                    for(int pincer = !multipleHinges ? 1 : 2; pincer < pincersProcessed.size(); pincer++) // skip hinges because they are always observable
                                                    {
                                                        if(possibleNumbers.get(pincersProcessed.get(pincer)[0] + "," + pincersProcessed.get(pincer)[1]).contains(unionValue))
                                                        {
                                                            unionValuePresent.add(pincersProcessed.get(pincer));
                                                        }
                                                    }

                                                    if(unionValuePresent.size() > 1)
                                                    {
                                                        for(int observingPincer = 0; observingPincer < unionValuePresent.size() - 1; observingPincer++)
                                                        {
                                                            for(int observedPincer = 0; observedPincer < unionValuePresent.size(); observedPincer++)
                                                            {
                                                                if(observingPincer == observedPincer)
                                                                {
                                                                    continue;
                                                                }

                                                                if(unionValuePresent.get(observingPincer)[0] != unionValuePresent.get(observedPincer)[0] && unionValuePresent.get(observingPincer)[1] != unionValuePresent.get(observedPincer)[1] && board.findSubBoardNumber(unionValuePresent.get(observingPincer)[0], unionValuePresent.get(observingPincer)[1]) != board.findSubBoardNumber(unionValuePresent.get(observedPincer)[0], unionValuePresent.get(observedPincer)[1]))
                                                                {
                                                                    allValuesObservable = false;

                                                                    break nestedLoops;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Find universally observed non-pincer cells with value and eliminate value from them
                                            if(hasNonRestricted && allValuesObservable)
                                            {
                                                advancedWingElimination(value, cellsWithValue, pincersProcessed);
                                            }
                                        }

                                        if(runWithExtension)
                                        {
                                            // resetting
                                            pincersProcessed = processedBeforeE;
                                            unionValues = unionBeforeE;
                                        }
                                        else // only run once if extension is not running
                                        {
                                            break;
                                        }
                                    }

                                    pincersProcessed = processedBeforeD;
                                    unionValues = unionBeforeD;
                                }

                                pincersProcessed = processedBeforeC;
                                unionValues = unionBeforeC;
                            }

                            if(!multipleHinges)
                            {
                                pincersProcessed = processedBeforeB;
                                unionValues = unionBeforeB;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Danny
     */
    private void advancedWingElimination(int value, List<int[]> cellsWithValue, List<int[]> pincersProcessed)
    {
        // Find collectively and individually observed non-pincer cells with value
        List<String> observedCollectively = new ArrayList<>();
        List<List<String>> observedIndividually = new ArrayList<>();

        int[] pincerAKey = pincersProcessed.get(0);
        int[] pincerBKey = pincersProcessed.get(1);
        int[] pincerCKey = pincersProcessed.size() >= 3 ? pincersProcessed.get(2) : null;
        int[] pincerDKey = pincersProcessed.size() >= 4 ? pincersProcessed.get(3) : null;
        int[] pincerEKey = pincersProcessed.size() == 5 ? pincersProcessed.get(4) : null;

        for(int[] pincerKey : pincersProcessed)
        {
            int pincerRow = pincerKey[0];
            int pincerColumn = pincerKey[1];

            if(!possibleNumbers.get(pincerRow + "," + pincerColumn).contains(value)) // only pincers containing value matters
            {
                continue;
            }

            observedIndividually.add(new ArrayList<>());

            for(int[] cellKey : cellsWithValue)
            {
                if(Arrays.equals(cellKey, pincerAKey) || Arrays.equals(cellKey, pincerBKey) || pincerCKey != null && Arrays.equals(cellKey, pincerCKey) || pincerDKey != null && Arrays.equals(cellKey, pincerDKey) || pincerEKey != null && Arrays.equals(cellKey, pincerEKey)) // skip if pincer cell
                {
                    continue;
                }

                int cellRow = cellKey[0];
                int cellColumn = cellKey[1];

                if(cellRow == pincerRow || cellColumn == pincerColumn || board.findSubBoardNumber(cellRow, cellColumn) == board.findSubBoardNumber(pincerRow, pincerColumn)) // cell with value is observable from pincer
                {
                    String key = (cellRow + "," + cellColumn);

                    if(!observedCollectively.contains(key)) // add to list of collectively observed cells (without duplicates)
                    {
                        observedCollectively.add(key);
                    }

                    observedIndividually.get(observedIndividually.size() - 1).add(key); // add to list of individually observed cells
                }
            }
        }

        // Eliminate value from universally observed non-pincer cells
        for(String collectiveKey : observedCollectively)
        {
            for(List<String> individualKeys : observedIndividually)
            {
                if(!individualKeys.contains(collectiveKey)) // if cell is not visible from all pincers, skip cell
                {
                    break;
                }
                else if(individualKeys.equals(observedIndividually.get(observedIndividually.size() - 1))) // last iteration, we can eliminate value from cell
                {
                    if(possibleNumbers.get(collectiveKey).contains(value)) // only remove if not already removed by another pincer combination
                    {
                        String[] parts = collectiveKey.split(",");

                        updatePossibleCounts(value, null, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), false);
                        possibleNumbers.get(collectiveKey).remove((Integer) value);

                        System.out.println("pos: " + " " + parts[0] + "," + parts[1] + " (" + value + ") " + possibleNumbers.get(collectiveKey));
                    }
                }
            }
        }
    }
}


