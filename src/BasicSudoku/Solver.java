package BasicSudoku;

import java.util.*;

public class Solver
{
    Board board;
    final int boardSize;
    final int boardLengthWidth;
    private HashMap<String, List<Integer>> possibleNumbers = new HashMap<>();
    private HashMap<String, List<Integer>> possibleNumbersBeginning = new HashMap<>(); // for testing (temp)
    private int possibleValuesCount;
    private int[][] valuePossibleCountRows; // [value][row]
    private int[][] valuePossibleCountColumns; // [value][column]
    private int[][] valuePossibleCountSubBoards; // [value][sub-board]
    private Set<String> processedKeys = new HashSet<>();

    /**
     * @author Danny
     */
    public Solver(Board board)
    {
        this.board = board;
        boardSize = board.getBoardSize();
        boardLengthWidth = board.getBoardLengthWidth();

        possibleValuesCount = 0;
        valuePossibleCountRows = new int[boardSize + 1][boardSize];
        valuePossibleCountColumns = new int[boardSize + 1][boardSize];
        valuePossibleCountSubBoards = new int[boardSize + 1][boardSize];
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public boolean solveWithStrategies()
    {
        // Add all strategies to a list to avoid repetitive code
        List<Runnable> strategies = new ArrayList<>();
        strategies.add(this::nakedSingles); // working
        //strategies.add(this::hiddenSingles); // working
        //strategies.add(this::hiddenTriples); // not working
        //strategies.add(this::pointingDuplicatesWithBLR); // working
        //strategies.add(this::xWing); // working
        //strategies.add(this::yWingWithXYZExtension); // working
        //strategies.add(this::wXYZWingExtended); // working

        boolean possibleValuesChanged;
        int possibleCountBefore;
        int currentStrategy = 0;

        possibleValuesInCells(); // find possibilities of empty cells

        while(!board.isGameFinished())
        {
            possibleValuesChanged = false;

            do
            {
                possibleCountBefore = possibleValuesCount;

                strategies.get(currentStrategy).run();

                if(possibleCountBefore != possibleValuesCount)
                {
                    possibleValuesChanged = true;
                }
            }
            while(possibleCountBefore != possibleValuesCount);

            if(possibleValuesChanged)
            {
                currentStrategy = 0; // effective, reset to the first strategy (nakedSingles)
            }
            else
            {
                if(currentStrategy == strategies.size() - 1 && !board.isGameFinished())  // board is unsolvable with strategies, try backtracking (last resort)
                {
                    return solveWithBacktracking(sortKeysForBacktracking());
                }

                currentStrategy++; // ineffective, go to the next strategy
            }

        }

        return true;
    }

    /**
     * @author Danny
     */
    private List<String> sortKeysForBacktracking()
    {
        // Create a list of keys sorted incrementally by the size of their value-lists
        List<List<Integer>> possibleValuesSorted = new ArrayList<>(possibleNumbers.values()); // needed because we can't sort keys by themselves
        possibleValuesSorted = possibleValuesSorted.stream().sorted(Comparator.comparingInt(List::size)).toList();

        List<String> possibleKeysSorted = new ArrayList<>();

        for(List<Integer> possibleValues : possibleValuesSorted)
        {
            for(String possibleKey : this.possibleNumbers.keySet())
            {
                if(this.possibleNumbers.get(possibleKey) == possibleValues) // has to be == because we are looking for the same objects, not contents
                {
                    possibleKeysSorted.add(possibleKey);
                }
            }
        }

        return possibleKeysSorted;
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    private boolean solveWithBacktracking(List<String> possibleKeysSorted)
    {
        if(possibleKeysSorted.isEmpty())
        {
            // base case
            return true;
        }
        else
        {
            // recursive case
            for(String key : possibleKeysSorted)
            {
                int row = Integer.parseInt(String.valueOf(key.charAt(0)));
                int column = Integer.parseInt(String.valueOf(key.charAt(2)));

                for(Integer value : possibleNumbers.get(key))
                {
                    if(board.checkPlacementRow(row, value) && board.checkPlacementColumn(column, value) && board.checkPlacementSubBoard(row, column, value))
                    {
                        board.setBoardValue(row, column, value);
                        possibleKeysSorted.remove(key);

                        if(solveWithBacktracking(possibleKeysSorted))
                        {
                            return true;
                        }
                        else
                        {
                            board.setBoardValue(row, column, 0);
                            possibleKeysSorted.add(key);
                        }
                    }
                }

                return false;
            }
        }

        return true;
    }

    /**
     * @author Abinav, Yahya & Danny
     */
    public boolean possibleValuesInCells() {
        for (int rows = 0; rows < boardSize; rows++) {
            for (int columns = 0; columns < boardSize; columns++) {
                if (board.getBoard()[rows][columns] == 0) {
                    String currentPosition = rows + "," + columns;
                    List<Integer> listOfPosNumbers = new ArrayList<Integer>();
                    for (int number = 1; number <= boardSize; number++) {
                        if (board.checkPlacementRow(rows, number) && board.checkPlacementColumn(columns, number) && board.checkPlacementSubBoard(rows, columns, number)) {
                            listOfPosNumbers.add(number);
                        }
                    }
                   if(!listOfPosNumbers.isEmpty()) {
                       updatePossibleNumbersAndCounts(currentPosition,null, listOfPosNumbers, true);
                   }
                   else {
                       return false;
                   }
                }
            }
        }
        return true;
    }

    /**
     * @author Abinav & Yahya
     */
    public void printPossibleNumbers(boolean initial) {
        for (int rows = 0; rows < boardSize; rows++) {
            for (int columns = 0; columns < boardSize; columns++) {
                String currentPosition = rows + "," + columns;
                List<Integer> values = initial ? possibleNumbersBeginning.get(currentPosition) : possibleNumbers.get(currentPosition); // for testing (temp)
                if (values != null) {
                    System.out.println("Position: (" + rows + "," + columns + ") Possible Values: " + values);
                }
            }
        }
    }

    /**
     * @author Danny
     */
    private void updatePossibleNumbersAndCounts(String key, Integer valueToUpdate, List<Integer> valuesToUpdate, boolean increase)
    {
        String[] parts = key.split(",");
        int row = Integer.parseInt(parts[0]);
        int column = Integer.parseInt(parts[1]);

        if(increase) // only used in the beginning (intensely)
        {
            possibleNumbers.put(key, valuesToUpdate);
            possibleNumbersBeginning.put(key, valuesToUpdate); // for testing (temp)

            for(Integer value : valuesToUpdate)
            {
                possibleValuesCount++;
                valuePossibleCountRows[value][row]++;
                valuePossibleCountColumns[value][column]++;
                valuePossibleCountSubBoards[value][board.findSubBoardNumber(row, column)]++;
            }
        }
        else
        {
            if(valueToUpdate != null)
            {
                possibleNumbers.get(key).remove(valueToUpdate);

                possibleValuesCount--;
                valuePossibleCountRows[valueToUpdate][row]--;
                valuePossibleCountColumns[valueToUpdate][column]--;
                valuePossibleCountSubBoards[valueToUpdate][board.findSubBoardNumber(row, column)]--;
            }
            else
            {
                possibleNumbers.get(key).removeAll(valuesToUpdate);

                for(Integer value : valuesToUpdate)
                {
                    possibleValuesCount--;
                    valuePossibleCountRows[value][row]--;
                    valuePossibleCountColumns[value][column]--;
                    valuePossibleCountSubBoards[value][board.findSubBoardNumber(row, column)]--;
                }
            }
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    private void nakedSingles()
    {
        HashMap<String, Integer> keysValuesToRemove = new HashMap<>();

        for(String key : possibleNumbers.keySet())
        {
            if(possibleNumbers.get(key).size() == 1)
            {
                keysValuesToRemove.put(key, possibleNumbers.get(key).get(0));
            }
        }

        for(String originKey : keysValuesToRemove.keySet())
        {
            String[] parts = originKey.split(",");
            int originRow = Integer.parseInt(parts[0]);
            int originColumn = Integer.parseInt(parts[1]);
            int value = keysValuesToRemove.get(originKey);

            board.placeValueInCell(originRow, originColumn, value);
            updatePossibleNumbersAndCounts(originKey, value, null, false);
            possibleNumbers.remove(originKey);

            removeValueFromObservableCells(originRow, originColumn, value);
        }

        eliminateEmptyLists();
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    private void removeValueFromObservableCells(int originRow, int originColumn, Integer value)
    {
        for(String candidateKey : possibleNumbers.keySet())
        {
            String[] parts = candidateKey.split(",");
            int candidateRow = Integer.parseInt(parts[0]);
            int candidateColumn = Integer.parseInt(parts[1]);

            if(originRow == candidateRow || originColumn == candidateColumn || board.findSubBoardNumber(originRow, originColumn) == board.findSubBoardNumber(candidateRow, candidateColumn))
            {
                if(possibleNumbers.get(candidateKey).contains(value))
                {
                    updatePossibleNumbersAndCounts(candidateKey, value, null, false);
                }
            }
        }
    }

    /**
     * @author Abinav, Yahya & Danny
     */
    private void eliminateEmptyLists()
    {
        List<String> emptyKeys = new ArrayList<>();

        for(String key : possibleNumbers.keySet())
        {
            if(possibleNumbers.get(key).isEmpty())
            {
                emptyKeys.add(key);
            }
        }

        for(String key : emptyKeys)
        {
            possibleNumbers.remove(key);
        }
    }

    /**
     * @author Yahya
     */
    public void hiddenSingles(){

        // hidden singles in rows
        hiddenSinglesForRowAndCol(true);

        // hidden singles in columns
        hiddenSinglesForRowAndCol(false);

        // hidden singles in subboard
        hiddenSinglesForSubBoard();
    }

    /**
     * @author Yahya
     */
    private void hiddenSinglesForRowAndCol(boolean proccingrows) {

        for (int index = 0; index < boardSize; index++) {
            List<String> cellKeys = new ArrayList<>();
            for (int rows = 0; rows < boardSize; rows++) {
                for (int columns = 0; columns < boardSize; columns++) {
                    String key = rows + "," + columns;
                    int rowcolumn = proccingrows ? rows : columns;
                    if (possibleNumbers.get(key) != null && index == rowcolumn) {
                        cellKeys.add(key);

                    }
                }
            }

            for(int number = 1; number <= boardSize; number++) {
                int count = proccingrows ? valuePossibleCountRows[number][index] : valuePossibleCountColumns[number][index];
                if (count == 1 ) {
                    for(String key : cellKeys) {
                        if(possibleNumbers.get(key).contains(number) && possibleNumbers.get(key).size()>1){
                            List<Integer> keyValues = new ArrayList<>(possibleNumbers.get(key));
                            keyValues.remove((Integer) number);
                            updatePossibleNumbersAndCounts(key, null, keyValues, false);
                        }
                    }
                }
            }
        }

    }

    /**
     * @author Yahya
     */
    private void hiddenSinglesForSubBoard() {

        for (int index = 0; index < boardSize; index++) {
            List<String> cellKeys = new ArrayList<>();
            for (int rows = 0; rows < boardSize; rows++) {
                for (int columns = 0; columns < boardSize; columns++) {
                    String key = rows + "," + columns;
                    int subBoardsNumber = board.findSubBoardNumber(rows,columns);
                    if (possibleNumbers.get(key) != null && index == subBoardsNumber) {
                        cellKeys.add(key);

                    }
                }
            }
            for (int number = 1; number <= boardSize; number++) {

                if (valuePossibleCountSubBoards[number][index] == 1) {
                    for (String key : cellKeys) {
                        if (possibleNumbers.get(key).contains(number) && possibleNumbers.get(key).size() > 1) {
                            List<Integer> keyValues = new ArrayList<>(possibleNumbers.get(key));
                            keyValues.remove((Integer) number);
                            updatePossibleNumbersAndCounts(key, null, keyValues, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Yahya
     */
    public void hiddenTriples(){

        // hidden triples in rows
        hiddenTriplesCRcombo(true);

        // hidden Triples in columns
        hiddenTriplesCRcombo(false);

        // hidden Triples in subboard
        hiddenTriplesForSubBoards();
    }

    /**
     * @author Yahya
     */
    private void hiddenTriplesForSubBoards(){
        List<String> quads;
        for(int boardNo = 0; boardNo < boardSize; boardNo++) {
            List<List<Integer>> possibleValues = new ArrayList<>();
            List<String> cellKeys = new ArrayList<>();
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    String key = row + "," + col;
                    List<Integer> cellPossibleValues = possibleNumbers.get(key);
                    boolean verifySubBoardNo = board.findSubBoardNumber(row,col) == boardNo;
                    if(!verifySubBoardNo) continue;
                    if (cellPossibleValues != null && cellPossibleValues.size() > 1) {
                        possibleValues.add(cellPossibleValues);
                        cellKeys.add(key);
                    }
                }
            }

            if(cellKeys.size() >= 3) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        for (int k = j + 1; k < possibleValues.size(); k++) {

                            quads = new ArrayList<>();
                            Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                            unionOfValues.addAll(possibleValues.get(j));
                            unionOfValues.addAll(possibleValues.get(k));
                            quads.add(cellKeys.get(i));
                            quads.add(cellKeys.get(j));
                            quads.add(cellKeys.get(k));
                            Set<Integer> combos = findHiddenTriples(unionOfValues, cellKeys, quads);
                            boolean quadsVerified = verifyTriples(quads, combos);
                            if ( quadsVerified) {
                                for (String position : quads) {
                                    List<Integer> valuesDuplicate = new ArrayList<>(possibleNumbers.get(position));
                                    valuesDuplicate.removeAll(combos);
                                    updatePossibleNumbersAndCounts(position, null, valuesDuplicate, false);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * @author Yahya
     */
    private void hiddenTriplesCRcombo(boolean processRows) {


        List<String> quads;

        for (int intial = 0; intial < boardSize; intial++) {
            List<List<Integer>> possibleValues = new ArrayList<>();
            List<String> cellKeys = new ArrayList<>();

            // Collect possible values and keys for all cells in the row/column
            for (int secondary = 0; secondary < boardSize; secondary++) {
                String key = processRows? intial + "," + secondary : secondary + "," + intial;
                List<Integer> cellPossibleValues = possibleNumbers.get(key);
                if (cellPossibleValues != null && cellPossibleValues.size() > 1) {
                    possibleValues.add(cellPossibleValues);
                    cellKeys.add(key);
                }
            }
            if(cellKeys.size() >= 3) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        for (int k = j + 1; k < possibleValues.size(); k++) {
                            quads = new ArrayList<>();
                            Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                            unionOfValues.addAll(possibleValues.get(j));
                            unionOfValues.addAll(possibleValues.get(k));
                            quads.add(cellKeys.get(i));
                            quads.add(cellKeys.get(j));
                            quads.add(cellKeys.get(k));

                            Set<Integer> combos = findHiddenTriples(unionOfValues, cellKeys, quads);
                            boolean quadsVerified = verifyTriples(quads, combos);

                            if ( quadsVerified) {


                                for (String position : quads) {
                                    List<Integer> valuesDuplicate = new ArrayList<>(possibleNumbers.get(position));
                                    valuesDuplicate.removeAll(combos);
                                    updatePossibleNumbersAndCounts(position, null, valuesDuplicate, false);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * @author Yahya
     */
    private boolean verifyTriples(List<String> quads, Set<Integer> combos) {

        if(!combos.isEmpty()) {
            for (String position : quads) {
                for (Integer number : possibleNumbers.get(position)) {
                    if (!combos.contains(number)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @author Yahya
     */
    private Set<Integer> findHiddenTriples(Set<Integer> unionOfValues, List<String> cellKeys, List<String> quads) {

        List<Integer> valuesList = new ArrayList<>(unionOfValues);
        if(unionOfValues.size() >= 4) {
            for (int i = 0; i < valuesList.size(); i++) {
                for (int j = i + 1; j < valuesList.size(); j++) {
                    for (int k = j + 1; k < valuesList.size(); k++) {
                        boolean foundHiddenQuads = true;
                        Set<Integer> combinations = new HashSet<>();
                        combinations.add(valuesList.get(i));
                        combinations.add(valuesList.get(j));
                        combinations.add(valuesList.get(k));
                        if (combinations.size() == 3) {
                            boolean isValidCombination = true;
                            // Check each cell outside the quads to ensure the combination doesn't appear
                            for (String cellKey : cellKeys) {
                                if (!quads.contains(cellKey)) { // Exclude cells in quads
                                    for (Integer number : combinations) {
                                        if (possibleNumbers.get(cellKey).contains(number)) {
                                            isValidCombination = false;
                                            break; // Break from checking this combination
                                        }
                                    }
                                    if (!isValidCombination) {
                                        break; // Break from checking cells as one number was found outside quads
                                    }
                                }
                            }

                            if (isValidCombination) {
                                return combinations; // Found a valid combination
                            }
                        }
                    }
                }
            }
        }

        return new HashSet<>();
    }

    /**
     * @author Danny
     */
    private void pointingDuplicatesWithBLR()
    {
        pointingDuplicatesWithBLR(true);
        pointingDuplicatesWithBLR(false);
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

        for(int value = 1; value <= boardSize; value++)
        {
            // Find all pointing duplicates
            for(int rowOrColumnA = 0; rowOrColumnA < boardSize; rowOrColumnA++)
            {
                valuePossibleCount = processingRows ? valuePossibleCountRows[value][rowOrColumnA] : valuePossibleCountColumns[value][rowOrColumnA];

                if(valuePossibleCount >= 2) // to do anything, there has to be at least 3 places where value can be
                {
                    valueSubBoardCount = 0;
                    previousSubBoard = processingRows ? board.findSubBoardNumber(rowOrColumnA, 0) : board.findSubBoardNumber(0, rowOrColumnA); // initial sub-board

                    for(int rowOrColumnB = 0; rowOrColumnB < boardSize; rowOrColumnB++)
                    {
                        substituteA = processingRows ? rowOrColumnA : rowOrColumnB;
                        substituteB = processingRows ? rowOrColumnB : rowOrColumnA;

                        if(possibleNumbers.get(substituteA + "," + substituteB) != null && possibleNumbers.get(substituteA + "," + substituteB).contains(value))
                        {
                            if(previousSubBoard != board.findSubBoardNumber(substituteA, substituteB)) // reset and update if sub-board has changed
                            {
                                valueSubBoardCount = 0;
                                previousSubBoard = board.findSubBoardNumber(substituteA, substituteB);
                            }

                            valueSubBoardCount++;

                            if(valueSubBoardCount >= targetValueCount && valuePossibleCountSubBoards[value][board.findSubBoardNumber(substituteA, substituteB)] == valueSubBoardCount) // pointing duplicate found, but value might be present on multiple sub-boards
                            {
                                // Process pointing duplicates normally for value elimination
                                for(int rowOrColumnC = 0; rowOrColumnC < boardSize; rowOrColumnC++)
                                {
                                    substituteA = processingRows ? rowOrColumnA : rowOrColumnC;
                                    substituteB = processingRows ? rowOrColumnC : rowOrColumnA;

                                    String key = (substituteA + "," + substituteB);

                                    if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(value))
                                    {
                                        if(board.findSubBoardNumber(substituteA, substituteB) != previousSubBoard) // remove value from row or column if on other sub-boards
                                        {
                                            updatePossibleNumbersAndCounts(key, value, null, false);
                                        }
                                    }
                                }
                            }
                        }

                        if(rowOrColumnB == boardSize - 1) // last iteration
                        {
                            if(valueSubBoardCount >= targetValueCount && valuePossibleCount == valueSubBoardCount) // pointing duplicate found, but value is only present on a single sub-board
                            {
                                // Process pointing duplicates with BLR (box/line reduction) for value elimination
                                int startingRow = (previousSubBoard / boardLengthWidth) * boardLengthWidth;
                                int startingColumn = (previousSubBoard - startingRow) * boardLengthWidth;

                                for(int addToRowColumnA = 0; addToRowColumnA < boardLengthWidth; addToRowColumnA++) // added to starting row or column
                                {
                                    if(processingRows && startingRow + addToRowColumnA != rowOrColumnA || !processingRows && startingColumn + addToRowColumnA != rowOrColumnA)
                                    {
                                        for(int addToRowColumnB = 0; addToRowColumnB < boardLengthWidth; addToRowColumnB++)
                                        {
                                            substituteA = processingRows ? addToRowColumnA : addToRowColumnB;
                                            substituteB = processingRows ? addToRowColumnB : addToRowColumnA;

                                            String key = (startingRow + substituteA) + "," + (startingColumn + substituteB);

                                            if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(value))
                                            {
                                                updatePossibleNumbersAndCounts(key, value, null, false);
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
    private void xWing()
    {
        xWing(true);
        xWing(false);
    }

    /**
     * @author Danny
     */
    private void xWing(boolean processingRows)
    {
        List<int[]> rowColumnPositions;
        List<List<int[]>> processedForXWings;
        int valuePossibleCount;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;
        int substituteC;
        int substituteD;

        for(int value = 1; value <= boardSize; value++)
        {
            processedForXWings = new ArrayList<>();

            // Find all x-wing candidates
            for (int rowOrColumnA = 0; rowOrColumnA < boardSize; rowOrColumnA++)
            {
                rowColumnPositions = new ArrayList<>();
                valuePossibleCount = processingRows ? valuePossibleCountRows[value][rowOrColumnA] : valuePossibleCountColumns[value][rowOrColumnA];

                if(valuePossibleCount == 2) // skip if value already present or possible more than 2 places in row or column
                {
                    for(int rowOrColumnB = 0; rowOrColumnB < boardSize; rowOrColumnB++)
                    {
                        substituteA = processingRows ? rowOrColumnA : rowOrColumnB;
                        substituteB = processingRows ? rowOrColumnB : rowOrColumnA;

                        String key = (substituteA + "," + substituteB);

                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(value))
                        {
                            rowColumnPositions.add(new int[] {substituteA, substituteB}); // store position of value
                        }

                        if(rowColumnPositions.size() == 2) // both positions found
                        {
                            processedForXWings.add(rowColumnPositions); // add positions to x-wing candidates list

                            break;
                        }
                    }
                }
            }

            // Check if there is one or more pairs of applicable x-wing candidates
            if(processedForXWings.size() >= 2)
            {
                substituteA = processingRows ? 1 : 0; // 0 = row index, 1 = column index

                for(int xWingPartA = 0; xWingPartA < processedForXWings.size() - 1; xWingPartA++)
                {
                    for(int xWingPartB = xWingPartA + 1; xWingPartB < processedForXWings.size(); xWingPartB++)
                    {
                        if(processedForXWings.get(xWingPartA).get(0)[substituteA] == processedForXWings.get(xWingPartB).get(0)[substituteA] && processedForXWings.get(xWingPartA).get(1)[substituteA] == processedForXWings.get(xWingPartB).get(1)[substituteA]) // check if there is an x-wing
                        {
                            substituteB = processingRows ? 0 : 1; // 0 = row index, 1 = column index

                            // Process x-wing for value elimination
                            for(int rowOrColumn = 0; rowOrColumn < boardSize; rowOrColumn++)
                            {
                                if(rowOrColumn != processedForXWings.get(xWingPartA).get(0)[substituteB] && rowOrColumn != processedForXWings.get(xWingPartB).get(1)[substituteB]) // don't remove value from x-wing rows or columns
                                {
                                    substituteC = processingRows ? rowOrColumn : processedForXWings.get(xWingPartA).get(0)[substituteA];
                                    substituteD = processingRows ? processedForXWings.get(xWingPartA).get(0)[substituteA] : rowOrColumn;

                                    for(int processed = 0; processed < 2; processed++) // remove value elsewhere in both rows or columns
                                    {
                                        String key = (substituteC + "," + substituteD);

                                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(value))
                                        {
                                            updatePossibleNumbersAndCounts(key, value, null, false);
                                        }

                                        if(processed == 0) // switch to process other row or column
                                        {
                                            substituteC = processingRows ? rowOrColumn : processedForXWings.get(xWingPartB).get(1)[substituteA];
                                            substituteD = processingRows ? processedForXWings.get(xWingPartB).get(1)[substituteA] : rowOrColumn;
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
    private void yWingWithXYZExtension()
    {
        yWingWithXYZExtension(false);
        yWingWithXYZExtension(true);
    }

    /**
     * @author Danny
     */
    private void yWingWithXYZExtension(boolean runXYZExtension)
    {
        List<int[]> cellsWithValue;
        List<int[]> hingeCells;
        List<int[]> pincerCandidateCells;
        List<int[]> pincerCells;
        List<int[]> pincersProcessed;
        int pincersValuesNeeded = !runXYZExtension ? 2 : 3;

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
                            else if(runXYZExtension && possibleNumbers.get(key).size() == 3) // save keys of size 3 that contains value (hinges)
                            {
                                hingeCells.add(new int[]{row, column});
                            }
                        }
                        else if(!runXYZExtension && possibleNumbers.get(key).size() == 2) // save keys of size 2 that doesn't contain value (hinges)
                        {
                            hingeCells.add(new int[] {row, column});
                        }
                    }
                }
            }

            // Process all hinges (1) and pincers (2) for value elimination
            for(int[] hinge : hingeCells)
            {
                pincerCells = new ArrayList<>();

                String hingeKey = hinge[0] + "," + hinge[1];

                for(int[] pincer : pincerCandidateCells)
                {
                    String pincerKey = pincer[0] + "," + pincer[1];

                    // Make sure the pincer has at least one value in common with the hinge
                    for(int hingeValue = 0; hingeValue < possibleNumbers.get(hingeKey).size(); hingeValue++)
                    {
                        if(possibleNumbers.get(hingeKey).get(hingeValue) == value) // value is always present in pincers
                        {
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
                                    if(runXYZExtension && pincerCells.isEmpty()) // in extended yWing (XYZWing) the hinge itself is always a pincer
                                    {
                                        pincerCells.add(hinge);
                                    }

                                    pincerCells.add(pincer);

                                    break; // since value is skipped and pincers are always of size = 2, we are done with this pincer
                                }
                            }
                        }
                    }
                }

                // Process every combination of found pincer cells (with or without hinge) for potential value elimination
                if(pincerCells.size() >= pincersValuesNeeded)
                {
                    pincersProcessed = new ArrayList<>();

                    if(runXYZExtension) // possibly add the hinge cell
                    {
                        pincersProcessed.add(pincerCells.get(0));
                    }

                    // Find and process all pincer combinations
                    List<int[]> pincersProcessedStart = new ArrayList<>(pincersProcessed);

                    for(int pincerA = (!runXYZExtension ? 0 : 1); pincerA < pincerCells.size() - 1; pincerA++)
                    {
                        for(int pincerB = pincerA + 1; pincerB < pincerCells.size(); pincerB++)
                        {
                            pincersProcessed.add(pincerCells.get(pincerA)); // always add pincerA and pincerB to combination
                            pincersProcessed.add(pincerCells.get(pincerB));

                            // Make sure all hinge values are present in the non-hinge pincers of the combination
                            boolean[] hingeValuesPresent = new boolean[possibleNumbers.get(hingeKey).size()];
                            int hingeValuesInPincers = 0;

                            for(int pincer = (!runXYZExtension ? 0 : 1); pincer < pincersProcessed.size(); pincer++)
                            {
                                for(int hingeValue = 0; hingeValue < hingeValuesPresent.length; hingeValue++)
                                {
                                    if(possibleNumbers.get(hingeKey).get(hingeValue) == value)  // value is always present in pincers
                                    {
                                        if(!hingeValuesPresent[hingeValue])
                                        {
                                            hingeValuesPresent[hingeValue] = true;
                                            hingeValuesInPincers++;
                                        }

                                        continue;
                                    }

                                    if(possibleNumbers.get(pincersProcessed.get(pincer)[0] + "," + pincersProcessed.get(pincer)[1]).contains(possibleNumbers.get(hingeKey).get(hingeValue)))
                                    {
                                        if(!hingeValuesPresent[hingeValue])
                                        {
                                            hingeValuesPresent[hingeValue] = true;
                                            hingeValuesInPincers++;
                                        }
                                    }
                                }
                            }

                            if(hingeValuesInPincers == pincersValuesNeeded)
                            {
                                // Find universally observed non-pincer cells with value and eliminate value from them
                                advancedWingElimination(value, cellsWithValue, pincersProcessed);
                            }

                            pincersProcessed = new ArrayList<>(pincersProcessedStart); // reset combination
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Danny
     */
    private void wXYZWingExtended()
    {
        wXYZWingExtended(false);
        wXYZWingExtended(true);
    }

    /**
     * @author Danny
     */
    private void wXYZWingExtended(boolean runExtended)
    {
        List<int[]> cellsWithValue;
        List<int[]> hingeCells;
        List<int[]> pincerCandidateCells;
        List<int[]> pincerCells;
        List<int[]> pincersProcessed;
        int pincersValuesNeeded = !runExtended ? 4 : 5;

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
                            else if(possibleNumbers.get(key).size() == 3 || possibleNumbers.get(key).size() == 4 || (runExtended && possibleNumbers.get(key).size() == 5)) // save keys of size 3-5 that contains value (both)
                            {
                                hingeCells.add(new int[] {row, column});
                                pincerCandidateCells.add(new int[] {row, column});
                            }
                        }
                        else
                        {
                            if(possibleNumbers.get(key).size() == 2 || possibleNumbers.get(key).size() == 3 || (runExtended && possibleNumbers.get(key).size() == 4)) // save keys of size 2-4 that doesn't contain value (hinges)
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
                    Set<Integer> hingeValuesUnion = new HashSet<>();

                    int hingesInvolved = firstHinge == secondHinge ? 1 : 2;
                    String hingeKeyA = hingeCells.get(firstHinge)[0] + "," + hingeCells.get(firstHinge)[1];
                    String hingeKeyB = hingesInvolved == 1 ? null : hingeCells.get(secondHinge)[0] + "," + hingeCells.get(secondHinge)[1];

                    for(int[] pincer : pincerCandidateCells)
                    {
                        String pincerKey = pincer[0] + "," + pincer[1];

                        // Make sure the pincer has at least one value in common with the hinges
                        int valuesInCommon = 0;

                        for(int hinge = 0; hinge < hingesInvolved; hinge++)
                        {
                            String currentHingeKey = hinge == 0 ? hingeKeyA : hingeKeyB;

                            for(int hingeValue = 0; hingeValue < possibleNumbers.get(currentHingeKey).size(); hingeValue++)
                            {
                                if(possibleNumbers.get(currentHingeKey).get(hingeValue) == value) // value is always present in pincers
                                {
                                    continue;
                                }

                                if(possibleNumbers.get(pincerKey).contains(possibleNumbers.get(currentHingeKey).get(hingeValue)))
                                {
                                    valuesInCommon++;

                                    break;
                                }
                            }
                        }

                        if(valuesInCommon == hingesInvolved)
                        {
                            // Find all pincers visible from and not equal to hinges
                            int hingeRowA = hingeCells.get(firstHinge)[0];
                            int hingeColumnA = hingeCells.get(firstHinge)[1];
                            int hingeRowB = hingeCells.get(secondHinge)[0];
                            int hingeColumnB = hingeCells.get(secondHinge)[1];
                            int pincerCandidateRow = pincer[0];
                            int pincerCandidateColumn = pincer[1];

                            if((hingeRowA != pincerCandidateRow || hingeColumnA != pincerCandidateColumn) && (hingesInvolved == 1 || hingeRowB != pincerCandidateRow || hingeColumnB != pincerCandidateColumn)) // pincer can't be the same as the hinges
                            {
                                if(hingeRowA == pincerCandidateRow || hingeColumnA == pincerCandidateColumn || board.findSubBoardNumber(hingeRowA, hingeColumnA) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from first hinge
                                {
                                    if(hingesInvolved == 1 || hingeRowB == pincerCandidateRow || hingeColumnB == pincerCandidateColumn || board.findSubBoardNumber(hingeRowB, hingeColumnB) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from second hinge
                                    {
                                        if(pincerCells.isEmpty()) // in wXYZWing the hinges themselves are always pincers
                                        {
                                            pincerCells.add(hingeCells.get(firstHinge));
                                            hingeValuesUnion.addAll(possibleNumbers.get(hingeKeyA));

                                            if(hingesInvolved == 2)
                                            {
                                                pincerCells.add(hingeCells.get(secondHinge));
                                                hingeValuesUnion.addAll(possibleNumbers.get(hingeKeyB));
                                            }
                                        }

                                        pincerCells.add(pincer);
                                    }
                                }
                            }
                        }
                    }

                    // Process every combination of found pincer cells (hinges always present) for potential value elimination
                    if(pincerCells.size() >= pincersValuesNeeded)
                    {
                        pincersProcessed = new ArrayList<>(Collections.singleton(pincerCells.get(0))); // always add the first hinge cell
                        List<Integer> unionHingeValueList = hingeValuesUnion.stream().toList(); // get all unique hinge values on a list
                        Set<Integer> pincerValueUnion = new HashSet<>(hingeValuesUnion);

                        if(hingesInvolved == 2) // add the second hinge
                        {
                            pincersProcessed.add(pincerCells.get(1));
                            pincerValueUnion.addAll(possibleNumbers.get(pincerCells.get(1)[0] + "," + pincerCells.get(1)[1]));
                        }

                        // Find and process all pincer combinations
                        List<int[]> pincersProcessedStart = new ArrayList<>(pincersProcessed);
                        Set<Integer> pincerValueUnionStart = new HashSet<>(pincerValueUnion);

                        for(int pincerA = hingesInvolved; pincerA < pincerCells.size() - 1; pincerA++)
                        {
                            for(int pincerB = pincerA + 1; pincerB < pincerCells.size(); pincerB++)
                            {
                                for(int pincerC = (!runExtended ? (hingesInvolved != 1 ? pincerB : pincerB + 1) : pincerB + 1); pincerC < pincerCells.size(); pincerC++)
                                {
                                    for(int pincerD = (!runExtended ? pincerC : (hingesInvolved != 1 ? pincerC : pincerC + 1)); pincerD < pincerCells.size(); pincerD++)
                                    {
                                        pincersProcessed.add(pincerCells.get(pincerA)); // always add pincerA and pincerB to combination
                                        pincersProcessed.add(pincerCells.get(pincerB));
                                        pincerValueUnion.addAll(possibleNumbers.get(pincerCells.get(pincerA)[0] + "," + pincerCells.get(pincerA)[1]));
                                        pincerValueUnion.addAll(possibleNumbers.get(pincerCells.get(pincerB)[0] + "," + pincerCells.get(pincerB)[1]));

                                        if(hingesInvolved == 1 || runExtended) // add pincerC to combination
                                        {
                                            pincersProcessed.add(pincerCells.get(pincerC));
                                            pincerValueUnion.addAll(possibleNumbers.get(pincerCells.get(pincerC)[0] + "," + pincerCells.get(pincerC)[1]));
                                        }

                                        if(hingesInvolved == 1 && runExtended) // add pincerD to combination
                                        {
                                            pincersProcessed.add(pincerCells.get(pincerD));
                                            pincerValueUnion.addAll(possibleNumbers.get(pincerCells.get(pincerD)[0] + "," + pincerCells.get(pincerD)[1]));
                                        }

                                        // Make sure all hinge values are present in the non-hinge pincers of the combination
                                        boolean[] hingeValuesPresent = new boolean[unionHingeValueList.size()];
                                        int hingeValuesInPincers = 0;

                                        for(int pincer = hingesInvolved; pincer < pincersProcessed.size(); pincer++) // skip hinges
                                        {
                                            for(int hingeValue = 0; hingeValue < unionHingeValueList.size(); hingeValue++)
                                            {
                                                if(unionHingeValueList.get(hingeValue) == value) // value is always present in pincers
                                                {
                                                    if(!hingeValuesPresent[hingeValue])
                                                    {
                                                        hingeValuesPresent[hingeValue] = true;
                                                        hingeValuesInPincers++;
                                                    }

                                                    continue;
                                                }

                                                if(possibleNumbers.get(pincersProcessed.get(pincer)[0] + "," + pincersProcessed.get(pincer)[1]).contains(unionHingeValueList.get(hingeValue)))
                                                {
                                                    if(!hingeValuesPresent[hingeValue])
                                                    {
                                                        hingeValuesPresent[hingeValue] = true;
                                                        hingeValuesInPincers++;
                                                    }
                                                }
                                            }
                                        }

                                        // Perform final checks of the combination
                                        if(pincerValueUnion.size() == pincersValuesNeeded && hingeValuesInPincers == pincersValuesNeeded)
                                        {
                                            boolean hasNonRestricted = false;
                                            boolean allValuesObservable = true; // it is simpler to detect a negative here

                                            nestedLoops:
                                            {
                                                for(Integer unionValue : pincerValueUnion)
                                                {
                                                    // Check if there is at least one non-restricted pincer with value and none with other values
                                                    if(unionValue == value)
                                                    {
                                                        for(int nonRestrictedPincer = hingesInvolved; nonRestrictedPincer < (pincersValuesNeeded - 1); nonRestrictedPincer++) // skip hinges because they are always observable
                                                        {
                                                            for(int otherPincer = hingesInvolved; otherPincer < pincersProcessed.size(); otherPincer++)
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

                                                    for(int pincer = hingesInvolved; pincer < pincersProcessed.size(); pincer++) // skip hinges because they are always observable
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

                                        pincersProcessed = new ArrayList<>(pincersProcessedStart); // reset combination
                                        pincerValueUnion = new HashSet<>(pincerValueUnionStart);

                                        if(!(runExtended && hingesInvolved == 1)) // pincerD not part of combination, so only run loop once
                                        {
                                            break;
                                        }
                                    }

                                    if(!runExtended && hingesInvolved == 2) // pincerC not part of combination, so only run loop once
                                    {
                                        break;
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
    private void advancedWingElimination(int value, List<int[]> cellsWithValue, List<int[]> pincersProcessed)
    {
        List<String> observedCollectively = new ArrayList<>();
        List<List<String>> observedIndividually = new ArrayList<>();
        int[] pincerA = pincersProcessed.get(0);
        int[] pincerB = pincersProcessed.get(1);
        int[] pincerC = pincersProcessed.size() >= 3 ? pincersProcessed.get(2) : null;
        int[] pincerD = pincersProcessed.size() >= 4 ? pincersProcessed.get(3) : null;
        int[] pincerE = pincersProcessed.size() == 5 ? pincersProcessed.get(4) : null;

        // Find collectively and individually observed non-pincer cells with value
        for(int[] pincer : pincersProcessed)
        {
            int pincerRow = pincer[0];
            int pincerColumn = pincer[1];
            String pincerKey = (pincerRow + "," + pincerColumn);

            if(!possibleNumbers.get(pincerKey).contains(value)) // only pincers containing value matters
            {
                continue;
            }

            observedIndividually.add(new ArrayList<>());

            for(int[] cell : cellsWithValue)
            {
                if(Arrays.equals(cell, pincerA) || Arrays.equals(cell, pincerB) || pincerC != null && Arrays.equals(cell, pincerC) || pincerD != null && Arrays.equals(cell, pincerD) || pincerE != null && Arrays.equals(cell, pincerE)) // skip if pincer cell
                {
                    continue;
                }

                int cellRow = cell[0];
                int cellColumn = cell[1];
                String cellKey = (cellRow + "," + cellColumn);

                if(cellRow == pincerRow || cellColumn == pincerColumn || board.findSubBoardNumber(cellRow, cellColumn) == board.findSubBoardNumber(pincerRow, pincerColumn)) // cell with value is observable from pincer
                {
                    if(!observedCollectively.contains(cellKey)) // add to list of collectively observed cells (without duplicates)
                    {
                        observedCollectively.add(cellKey);
                    }

                    observedIndividually.get(observedIndividually.size() - 1).add(cellKey); // add to list of individually observed cells
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
                    if(possibleNumbers.get(collectiveKey) != null && possibleNumbers.get(collectiveKey).contains(value)) // only remove if not already removed by another pincer combination
                    {
                        updatePossibleNumbersAndCounts(collectiveKey, value, null, false);
                    }
                }
            }
        }
    }

    // god tier debugging (it is!)
    public void emptyCellsDebug()
    {
        for(int iterations = 1; iterations <= 1000; iterations++)
        {
            System.out.println("Iteration: " + iterations);

            Board testBoard = new Board(3, false);
            for(int row = 0; row < boardSize; row++)
            {
                for(int column = 0; column < boardSize; column++)
                {
                    if(testBoard.getSolver().board.getBoard()[row][column] == 0)
                    {
                        BoardTester.printBoard(testBoard);
                        testBoard.getSolver().printPossibleNumbers(true);
                        BoardTester.printBoard(testBoard.getSolver().board);
                        testBoard.getSolver().printPossibleNumbers(false);

                        System.out.println("Failed...");
                    }
                    else if(iterations == 1000)
                    {
                        System.out.println("Success!");

                        return;
                    }
                }
            }
        }
    }
}