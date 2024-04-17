package BasicSudoku;

import java.util.*;

public class Solver
{
    Board board;
    Board solvedBoard;
    final int boardSize;
    final int boardLengthWidth;
    private HashMap<String, List<Integer>> possibleNumbers = new HashMap<>();
    private Set<String> processedKeys = new HashSet<>();
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
        solvedBoard = solverToCopy.solvedBoard;
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
        solvedBoard = new Board(board);
        solvedBoard.getSolver().board = solvedBoard;

        return solvedBoard.getSolver().solveWithBacktracking();  // has to be with backtracking to avoid singular strategy boards
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

            // solving strategies go here
            nakedPairs();
            hiddenPairs();
            nakedQuads();
            hiddenQuads();
            intersectionRemoval();
            simpleColouring();
            swordFish();
            wingStrategies();

            if(possibleCountBefore == possibleNumbersCount && !board.isGameFinished()) // board is unsolvable with strategies, try backtracking (last resort)
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

                removeNumberFromOtherCandidate(key,values,Collections.emptyList());
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            possibleNumbers.remove(key);
        }

        EliminateEmptyLists();
    }

    /**
     * @author Abinav
     */
    public void EliminateEmptyLists(){
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

    /**
     * @author Abinav
     */
    public void removeNumberFromOtherCandidate(String key,List<Integer> values, List<String> cellsContainingCandidate) {
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
                if(!cellsContainingCandidate.isEmpty()) cellsContainingCandidate.remove(Key2);
            }
        }
    }

    /**
     * @author Abinav
     */
    private void returnCandidatesInColumns(int column) {

        Map<String, List<Integer>> columnCandidates = new HashMap<>();
        for (String key : possibleNumbers.keySet()) {
            String[] parts = key.split(",");
            int keyColumn = Integer.parseInt(parts[1]);
            if (keyColumn == column) {
                columnCandidates.put(key, possibleNumbers.get(key));
            }
        }
            findNakedPairs(columnCandidates);
    }

    /**
     * @author Abinav
     */
    private void returnCandidatesInSubBoard(int subBoard) {
        Map<String, List<Integer>> subBoardCandidates = new HashMap<>();
        for (String key : possibleNumbers.keySet()) {
            String[] parts = key.split(",");
            int keyRow = Integer.parseInt(parts[0]);
            int keyColumn = Integer.parseInt(parts[1]);
            if (board.findSubBoardNumber(keyRow, keyColumn) == subBoard) {
                subBoardCandidates.put(key, possibleNumbers.get(key));
            }
        }

            findNakedPairs(subBoardCandidates);

    }

    /**
     * @author Abinav
     */
    private void returnCandidatesInRows(int row) {
        Map<String, List<Integer>> rowCandidates = new HashMap<>();
        for (String key : possibleNumbers.keySet()) {
            String[] parts = key.split(",");
            int keyRow = Integer.parseInt(parts[0]);
            if (keyRow == row) {
                rowCandidates.put(key, possibleNumbers.get(key));
            }
        }

            findNakedPairs(rowCandidates);
    }

    /**
     * @author Abinav
     */
    public void NP(){
        nakedPairs();
        nakedSingles();
    }

    /**
     * @author Abinav
     */
    private void nakedPairs() {
        for (String key1 : possibleNumbers.keySet()) {
            List<Integer> valuesOfKey1 = possibleNumbers.get(key1);
            String[] parts = key1.split(",");
            int Key1row = Integer.parseInt(parts[0]);
            int Key1column = Integer.parseInt(parts[1]);
            int subBoardNoForKey1 = board.findSubBoardNumber(Key1row, Key1column);
            if (valuesOfKey1.size() != 2) continue;
            for (String key2 : possibleNumbers.keySet()) {
                if (key2.equals(key1) || valuesOfKey1.size() != 2) continue;
                List<Integer> valuesOfKey2 = possibleNumbers.get(key2);
                String[] parts2 = key2.split(",");
                int Key2row = Integer.parseInt(parts2[0]);
                int Key2column = Integer.parseInt(parts2[1]);
                int subBoardNoForKey2 = board.findSubBoardNumber(Key2row, Key2column);
                boolean sameRow = (Key1row == Key2row);
                boolean sameColumn = (Key1column == Key2column);
                boolean sameSubBoard = (subBoardNoForKey1 == subBoardNoForKey2);

                if (sameRow || sameColumn || sameSubBoard) {
                    List<Integer> commonValues = new ArrayList<>();
                    if (sameRow) {
                        returnCandidatesInRows(Key1row);
                    } else if (sameColumn) {
                        returnCandidatesInColumns(Key1column);
                    } else if (sameSubBoard) {
                        returnCandidatesInSubBoard(subBoardNoForKey1);
                    }
                }
            }
        }
        processedKeys.clear();
    }


    /**
     * @author Abinav
     */
    private void findNakedPairs(Map<String, List<Integer>> candidates) {
        List<String> keys = new ArrayList<>(candidates.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key1 = keys.get(i);
            if (processedKeys.contains(key1)) continue;
            List<Integer> values1 = new ArrayList<>(candidates.get(key1));
            for (int j = i ; j < keys.size(); j++) {
                String key2 = keys.get(j);
                if (processedKeys.contains(key2) || key1.equals(key2)) continue;
                List<Integer> values2 = new ArrayList<>(candidates.get(key2));
                if (new HashSet<>(values1).equals(new HashSet<>(values2))) {
                    if (values2.size() == 2 && values1.size() == 2) {
                        String[] keyPart = key1.split(",");
                        int rowOfKey1 = Integer.parseInt(keyPart[0]);
                        int columnOfKey1 = Integer.parseInt(keyPart[1]);
                        int subBoardOfKey1 = board.findSubBoardNumber(rowOfKey1,columnOfKey1);

                        String[] keyPart2 = key2.split(",");
                        int rowOfKey2 = Integer.parseInt(keyPart2[0]);
                        int columnOfKey2 = Integer.parseInt(keyPart2[1]);
                        int subBoardOfKey2 = board.findSubBoardNumber(rowOfKey2,columnOfKey2);
                        int code;

                        if((rowOfKey1 == rowOfKey2 )){
                            code = 0;
                            deleteNPCFromOtherCells(key1, key2, new ArrayList<>(values1),code);
                        } else if (columnOfKey1 == columnOfKey2 ){
                            code = 1;
                            deleteNPCFromOtherCells(key1, key2, new ArrayList<>(values1),code);
                        }
                        else if ((subBoardOfKey1 == subBoardOfKey2)) {
                            code = 2;
                            deleteNPCFromOtherCells(key1, key2, new ArrayList<>(values1),code);
                        }
                        processedKeys.add(key1);
                        processedKeys.add(key2);
                        break;
                    }
                }
            }
        }
    }

    /**
     * @author Abinav
     */
    private void deleteNPCFromOtherCells(String key, String key2, List<Integer> values, int code) {
        String[] keyPart = key.split(",");
        int rowOfKey1 = Integer.parseInt(keyPart[0]);
        int columnOfKey1 = Integer.parseInt(keyPart[1]);
        int subBoardOfKey1 = board.findSubBoardNumber(rowOfKey1,columnOfKey1);

        String[] keyPart2 = key2.split(",");
        int rowOfKey2 = Integer.parseInt(keyPart2[0]);
        int columnOfKey2 = Integer.parseInt(keyPart2[1]);
        int subBoardOfKey2 = board.findSubBoardNumber(rowOfKey2,columnOfKey2);

        for (String originalKey : possibleNumbers.keySet()) {
            if( (originalKey.equals(key) || (originalKey.equals(key2)))) continue;
            String[] parts = originalKey.split(",");
            int rowOfKeys = Integer.parseInt(parts[0]);
            int columnOfKeys = Integer.parseInt(parts[1]);
            int subBoardOfKeys = board.findSubBoardNumber(rowOfKeys,columnOfKeys);
            List<Integer> valuesOfKeys = possibleNumbers.get(originalKey);
            if((rowOfKey1 == rowOfKeys) && (rowOfKey2 == rowOfKeys) && code == 0){
                valuesOfKeys.removeAll(values);
                updatePossibleCounts(5,valuesOfKeys,rowOfKeys,columnOfKeys,false);
            } else if ((columnOfKey1 == columnOfKeys) && (columnOfKey2 == columnOfKeys) && code == 1){
                valuesOfKeys.removeAll(values);
                updatePossibleCounts(5,valuesOfKeys,rowOfKeys,columnOfKeys,false);
            }
            else if ((subBoardOfKey1 == subBoardOfKeys) && (subBoardOfKey2 == subBoardOfKeys) && code == 2) {
                valuesOfKeys.removeAll(values);
                updatePossibleCounts(5,valuesOfKeys,rowOfKeys,columnOfKeys,false);
            }
        }
    }


    /**
     * @author Abinav
     */
    public void hiddenPairs(){
        // hidden quads in rows
        hiddenPairsCRcombo(true);
        nakedSingles();

        // hidden quads in columns
        hiddenPairsCRcombo(false);
        nakedSingles();

        // hidden quads in subboard
        hiddenPairsForSubBoards();
        nakedSingles();
    }

    /**
     * @author Abinav
     */
    private void hiddenPairsForSubBoards(){
        List<String> pairs;
        for(int boardNo = 1; boardNo < boardSize; boardNo++) {
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
            if(cellKeys.size() >= 2) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        pairs = new ArrayList<>();
                        Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                        unionOfValues.addAll(possibleValues.get(j));
                        pairs.add(cellKeys.get(i));
                        pairs.add(cellKeys.get(j));
                        Set<Integer> combos = findHiddenPairs(unionOfValues, cellKeys, pairs);
                        boolean pairsVerified = verifyPairs(pairs, combos);
                        if ( pairsVerified) {
                            for (String position : pairs) {
                                String[] pos = position.split(",");
                                List<Integer> valuesDuplicate = possibleNumbers.get(position);
                                possibleNumbers.get(position).retainAll(combos);
                                valuesDuplicate.removeAll(combos);
                                updatePossibleCounts(5,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Abinav
     */
    private void hiddenPairsCRcombo(boolean processRows) {
        List<String> pairs;

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
            if(cellKeys.size() >= 2) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        pairs = new ArrayList<>();
                        Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                        unionOfValues.addAll(possibleValues.get(j));
                        pairs.add(cellKeys.get(i));
                        pairs.add(cellKeys.get(j));
                        Set<Integer> combos = findHiddenPairs(unionOfValues, cellKeys, pairs);
                        boolean pairsVerified = verifyPairs(pairs, combos);
                        if ( pairsVerified) {
                            for (String position : pairs) {
                                String[] pos = position.split(",");
                                List<Integer> valuesDuplicate = possibleNumbers.get(position);
                                possibleNumbers.get(position).retainAll(combos);
                                valuesDuplicate.removeAll(combos);
                                updatePossibleCounts(5,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Abinav
     */
    private boolean verifyPairs(List<String> pairs, Set<Integer> combos) {
        if(!combos.isEmpty()) {
            int count = 0;
            boolean containsBothNumbers = true;
            for (String position : pairs) {
                List<Integer> numbersInPosition = possibleNumbers.get(position);
                for (Integer number : numbersInPosition) {
                    if (!combos.contains(number)) {
                        count++;
                        break;
                    }
                }
            }

            for (String cell : pairs){
                if(!possibleNumbers.get(cell).containsAll(combos)){
                    containsBothNumbers = false;
                    break;
                }
            }

            return count>=1 && containsBothNumbers;
        }
        return false;
    }

    /**
     * @author Abinav
     */
    private Set<Integer> findHiddenPairs(Set<Integer> unionOfValues, List<String> cellKeys, List<String> pairs) {
        List<Integer> valuesList = new ArrayList<>(unionOfValues);
        if(unionOfValues.size() >= 2) {
            for (int i = 0; i < valuesList.size(); i++) {
                for (int j = i + 1; j < valuesList.size(); j++) {
                    boolean foundHiddenPair = true;
                    Set<Integer> combinations = new HashSet<>();
                    combinations.add(valuesList.get(i));
                    combinations.add(valuesList.get(j));
                    if (combinations.size() == 2) {
                        boolean isValidCombination = true;
                        // Check each cell outside the quads to ensure the combination doesn't appear
                        for (String cellKey : cellKeys) {
                            if (!pairs.contains(cellKey)) { // Exclude cells in quads
                                List<Integer> possibleNumbersForCell = possibleNumbers.get(cellKey);
                                for (Integer number : combinations) {
                                    if (possibleNumbersForCell.contains(number)) {
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

        return new HashSet<>();
    }


    /**
     * @author Abinav
     */
    public void nakedQuads(){
        // finds naked quads in rows
        nakedQuadsCRcombo(true);
        nakedSingles();
        // finds naked quads in columns
        nakedQuadsCRcombo(false);
        nakedSingles();

        nakedQuadForSubBoards();
        nakedSingles();
    }


    /**
     * @author Abinav
     */
    private void nakedQuadsCRcombo(boolean processRows) {
        List<String> quads;

        for (int intial = 0; intial < boardSize; intial++) {
            List<List<Integer>> possibleValues = new ArrayList<>();
            List<String> cellKeys = new ArrayList<>();


            for (int secondary = 0; secondary < boardSize; secondary++) {
                String key = processRows? intial + "," + secondary : secondary + "," + intial;
                List<Integer> cellPossibleValues = possibleNumbers.get(key);
                if (cellPossibleValues != null && cellPossibleValues.size() > 1 && cellPossibleValues.size() <= 4) {
                    possibleValues.add(cellPossibleValues);
                    cellKeys.add(key);
                }
            }
            // Find Naked quads among these values
            for (int i = 0; i < possibleValues.size(); i++) {
                for (int j = i + 1; j < possibleValues.size(); j++) {
                    for (int k = j + 1; k < possibleValues.size(); k++) {
                        for (int l = k + 1; l < possibleValues.size(); l++) {
                            quads = new ArrayList<>();
                            Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                            unionOfValues.addAll(possibleValues.get(j));
                            unionOfValues.addAll(possibleValues.get(k));
                            unionOfValues.addAll(possibleValues.get(l));
                            quads.add(cellKeys.get(i));
                            quads.add(cellKeys.get(j));
                            quads.add(cellKeys.get(k));
                            quads.add(cellKeys.get(l));
                            if (unionOfValues.size() == 4 && possibleValues.size() == 4) {
                                List<Integer> quadValues = new ArrayList<>(unionOfValues);
                                // Remove these numbers from other cells' possible values in the same row
                                for (int other = 0; other < boardSize; other++) {
                                    String position = processRows? intial + "," + other : other + "," + intial;
                                    if(possibleNumbers.get(position)==null || quads.contains(position)) continue;
                                    if ( possibleNumbers.get(position).contains(quadValues.get(0)) || possibleNumbers.get(position).contains(quadValues.get(1)) || possibleNumbers.get(position).contains(quadValues.get(2)) || possibleNumbers.get(position).contains(quadValues.get(3))) {
                                        //system.out.println();
                                        String[] pos = position.split(",");
                                        List<Integer> valuesDuplicate = possibleNumbers.get(position);
                                        possibleNumbers.get(position).removeAll(unionOfValues);
                                        updatePossibleCounts(10,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                                        //system.out.println();
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
     * @author Abinav
     */
    private void nakedQuadForSubBoards(){
        // Abinav
        for (int i =0; i < boardSize; i++){
            for(int j=1; j<= boardSize; j++){
                List<String> quads = new ArrayList<>();
                List<List<Integer>> possibleValues = new ArrayList<>();
                List<String> cellKeys = new ArrayList<>();
                int startingRow = (i / boardLengthWidth) * boardLengthWidth;
                int startingColumn = (i - startingRow) * boardLengthWidth;
                Set<Integer> unionOfValues;

                for(int k = 0; k < boardLengthWidth; k++){
                    for(int l = 0; l < boardLengthWidth; l++){
                        String key = (startingRow + k) + "," + (startingColumn + l);
                        if(possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(j)){
                            List<Integer> cellPossibleValues = possibleNumbers.get(key);
                            if (cellPossibleValues != null && cellPossibleValues.size() > 1 && cellPossibleValues.size() <= 4) {
                                possibleValues.add(cellPossibleValues);
                                cellKeys.add(key);
                                quads.add(key);
                            }
                        }
                    }
                }

                unionOfValues = new HashSet<>();
                for(List<Integer> possible: possibleValues)
                {
                    unionOfValues.addAll(possible);

                }

                if (possibleValues.size() == 4 && unionOfValues.size() == 4) { // A Naked Quad is found
                    List<Integer> quadValues = new ArrayList<>(unionOfValues);
                    // Remove these numbers from other cells' possible values in the same row
                    for(int k = 0; k < boardLengthWidth; k++){// added to rows
                        for(int l = 0; l < boardLengthWidth; l++) { // added to columns
                            String key = (startingRow + k) + "," + (startingColumn + l);
                            if (possibleNumbers.get(key)!= null && !quads.contains(key) ) {
                                if( possibleNumbers.get(key).contains(quadValues.get(0)) || possibleNumbers.get(key).contains(quadValues.get(1)) || possibleNumbers.get(key).contains(quadValues.get(2)) && possibleNumbers.get(key).contains(quadValues.get(3))) {
                                    String[] pos = key.split(",");
                                    List<Integer> valuesDuplicate = possibleNumbers.get(key);
                                    updatePossibleCounts(10,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                                    possibleNumbers.get(key).removeAll(unionOfValues);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * @author Abinav
     */
    public void hiddenQuads(){
        // hidden quads in rows
        hiddenQuadsCRcombo(true);
        nakedSingles();

        // hidden quads in columns
        hiddenQuadsCRcombo(false);
        nakedSingles();

        // hidden quads in subboard
        hiddenQuadForSubBoards();
        nakedSingles();
    }

    /**
     * @author Abinav
     */
    private void hiddenQuadForSubBoards(){
        List<String> quads;
        for(int boardNo = 1; boardNo < boardSize; boardNo++) {
            List<List<Integer>> possibleValues = new ArrayList<>();
            List<String> cellKeys = new ArrayList<>();
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    String key = row + "," + col;
                    List<Integer> cellPossibleValues = possibleNumbers.get(key);
                    boolean verifySubBoardNo = board.findSubBoardNumber(row,col) == boardNo;
                    //system.out.println();
                    if(!verifySubBoardNo) continue;
                    if (cellPossibleValues != null && cellPossibleValues.size() > 1) {
                        possibleValues.add(cellPossibleValues);
                        cellKeys.add(key);
                    }
                }
            }

            if(cellKeys.size() >= 4) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        for (int k = j + 1; k < possibleValues.size(); k++) {
                            for (int l = k + 1; l < possibleValues.size(); l++) {
                                quads = new ArrayList<>();
                                Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                                unionOfValues.addAll(possibleValues.get(j));
                                unionOfValues.addAll(possibleValues.get(k));
                                unionOfValues.addAll(possibleValues.get(l));
                                quads.add(cellKeys.get(i));
                                quads.add(cellKeys.get(j));
                                quads.add(cellKeys.get(k));
                                quads.add(cellKeys.get(l));
                                //system.out.println();
                                Set<Integer> combos = findHiddenQuads(unionOfValues, cellKeys, quads);
                                boolean quadsVerified = verifyQuads(quads, combos);
                                //system.out.println();
                                if ( quadsVerified) {
                                    //system.out.println();
                                    for (String position : quads) {
                                        String[] pos = position.split(",");
                                        List<Integer> valuesDuplicate = possibleNumbers.get(position);
                                        valuesDuplicate.removeAll(combos);
                                        updatePossibleCounts(5,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                                        possibleNumbers.get(position).retainAll(combos);
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
     * @author Abinav
     */
    private void hiddenQuadsCRcombo(boolean processRows) {
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
            if(cellKeys.size() >= 4) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        for (int k = j + 1; k < possibleValues.size(); k++) {
                            for (int l = k + 1; l < possibleValues.size(); l++) {
                                quads = new ArrayList<>();
                                Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                                unionOfValues.addAll(possibleValues.get(j));
                                unionOfValues.addAll(possibleValues.get(k));
                                unionOfValues.addAll(possibleValues.get(l));
                                quads.add(cellKeys.get(i));
                                quads.add(cellKeys.get(j));
                                quads.add(cellKeys.get(k));
                                quads.add(cellKeys.get(l));
                                //system.out.println();
                                Set<Integer> combos = findHiddenQuads(unionOfValues, cellKeys, quads);
                                boolean quadsVerified = verifyQuads(quads, combos);
                                //system.out.println();
                                if ( quadsVerified) {
                                    for (String position : quads) {
                                        String[] pos = position.split(",");
                                        List<Integer> valuesDuplicate = possibleNumbers.get(position);
                                        possibleNumbers.get(position).retainAll(combos);
                                        updatePossibleCounts(5,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                                        //system.out.println();
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
     * @author Abinav
     */
    private boolean verifyQuads(List<String> quads, Set<Integer> combos) {
        if(!combos.isEmpty()) {
            int count = 0;
            for (String position : quads) {
                List<Integer> numbersInPosition = possibleNumbers.get(position);
                for (Integer number : numbersInPosition) {
                    if (!combos.contains(number)) {
                        count++;
                        break;
                    }
                }
                if (count >= 2) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * @author Abinav
     */
    private Set<Integer> findHiddenQuads(Set<Integer> unionOfValues, List<String> cellKeys, List<String> quads) {
        List<Integer> valuesList = new ArrayList<>(unionOfValues);
        if(unionOfValues.size() >= 4) {
            for (int i = 0; i < valuesList.size(); i++) {
                for (int j = i + 1; j < valuesList.size(); j++) {
                    for (int k = j + 1; k < valuesList.size(); k++) {
                        for (int l = k + 1; l < valuesList.size(); l++) {
                            boolean foundHiddenQuads = true;
                            Set<Integer> combinations = new HashSet<>();
                            combinations.add(valuesList.get(i));
                            combinations.add(valuesList.get(j));
                            combinations.add(valuesList.get(k));
                            combinations.add(valuesList.get(l));
                            //system.out.println();
                            if (combinations.size() == 4) {
                                boolean isValidCombination = true;
                                // Check each cell outside the quads to ensure the combination doesn't appear
                                for (String cellKey : cellKeys) {
                                    if (!quads.contains(cellKey)) { // Exclude cells in quads
                                        List<Integer> possibleNumbersForCell = possibleNumbers.get(cellKey);
                                        for (Integer number : combinations) {
                                            if (possibleNumbersForCell.contains(number)) {
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
                                    //system.out.println();
                                    return combinations; // Found a valid combination
                                }
                            }
                        }
                    }
                }
            }
        }

        return new HashSet<>();
    }

    /**
     * @author Abinav
     */
    public void swordFish(){
        // swordfish technique on rows where each cell contains only 2 cells
        findSwordFishCandidates(true,2);
        nakedSingles();

        // swordfish technique on columns
        findSwordFishCandidates(false,2);
        nakedSingles();

        // swordfish technique on rows
        findSwordFishCandidates(true,3);
        nakedSingles();

        // swordfish technique on columns
        findSwordFishCandidates(false,3);
        nakedSingles();

    }


    /**
     * @author Abinav
     */
    private void findSwordFishCandidates(boolean processingRows, int pairOrTriple) {
        int valuePossibleCount;
        List<int[]> rowColumnPositions;
        List<List<int[]>> processForSF;

        int substituteA = 0; // variables used to avoid repetitive code
        int substituteB = 0;


        for (int number = 1; number <= boardSize; number++){ // value
            //System.out.println("Checking number: " + number);
            processForSF = new ArrayList<>();
            for (int j = 0; j < boardSize; j++) { // row or column
                valuePossibleCount = processingRows ? valuePossibleCountRows[number][j] : valuePossibleCountColumns[number][j];
                rowColumnPositions = new ArrayList<>();

                if (valuePossibleCount == 2 || valuePossibleCount == 3 ){ // skip if value already present or possible more than 2 places in row or column

                    for (int k = 0; k < boardSize; k++){ // row or column

                        substituteA = processingRows ? j : k;
                        substituteB = processingRows ? k : j;

                        String key = (substituteA + "," + substituteB);

                        if (possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(number)) {
                            rowColumnPositions.add(new int[]{substituteA, substituteB}); // store position of value
                        }

                        if (k == boardSize - 1) {
                            // System.out.println("Added rowColumnPositions for potential X-Wing/SwordFish");
                            processForSF.add(rowColumnPositions);
                            for (int[] position : rowColumnPositions) {
                                //  System.out.println(Arrays.toString(position));
                            }
                        }
                    }
                }
            }
            //System.out.println(processForSF.size());
            handleSFCandidates( pairOrTriple, substituteA,number, processingRows,  processForSF);

        }
    }

    /**
     * @author Abinav
     */
    private void handleSFCandidates( int pairOrTriple,int substituteA,int number,boolean processingRows, List<List<int[]>> processForSF) {
        // System.out.println("Handling SwordFish candidates for number: " + number + ", processingRows: " + processingRows);
        if (processForSF.size() >= 3) // enough candidates found
        {
            substituteA = processingRows ? 1 : 0; // 0 = row index, 1 = column index

            for (int j = 0; j < processForSF.size() - 2; j++) {
                for (int k = j + 1; k < processForSF.size() - 1; k++) {
                    for (int n = k + 1; n < processForSF.size(); n++) {


                        Set<Integer> uniqueCOR = new HashSet<>();
                        List<Integer> emptyList = new ArrayList<>();
                        List<String> candidates = new ArrayList<>();


                        int windowSize = pairOrTriple; // pairs or triples in rows/columns

                        int minListLength = Math.min(processForSF.get(j).size(),
                                Math.min(processForSF.get(k).size(), processForSF.get(n).size()));

                        for (int i = 0; i < minListLength - 1; i++) {

                            // For list j
                            for (int w = i; w < i + windowSize && w < processForSF.get(j).size(); w++) {
                                int[] coord = processForSF.get(j).get(w);
                                uniqueCOR.add(coord[substituteA]);
                                // System.out.println("Adding from list 1, index " + w + ": " + coord[substituteA]);
                                emptyList.add(coord[substituteA]);
                                String row = Integer.toString(coord[0]);
                                String column = Integer.toString(coord[1]);
                                String key = row + "," + column;
                                candidates.add(key);
                            }

                            // For list k
                            for (int w = i; w < i + windowSize && w < processForSF.get(k).size(); w++) {
                                int[] coord = processForSF.get(k).get(w);
                                uniqueCOR.add(coord[substituteA]);
                                //  System.out.println("Adding from list 2, index " + w + ": " + coord[substituteA]);
                                emptyList.add(coord[substituteA]);
                                String row = Integer.toString(coord[0]);
                                String column = Integer.toString(coord[1]);
                                String key = row + "," + column;
                                candidates.add(key);
                            }

                            // For list n
                            for (int w = i; w < i + windowSize && w < processForSF.get(n).size(); w++) {
                                int[] coord = processForSF.get(n).get(w);
                                uniqueCOR.add(coord[substituteA]);
                                //  System.out.println("Adding from list 3, index " + w + ": " + coord[substituteA]);
                                emptyList.add(coord[substituteA]);
                                String row = Integer.toString(coord[0]);
                                String column = Integer.toString(coord[1]);
                                String key = row + "," + column;
                                candidates.add(key);
                            }
                            boolean validSF = uniqueCOR.size() == 3 && checkOccurenceOfEachElement(emptyList, uniqueCOR);
                            eliminateNonSFC(validSF, processingRows, number, j, k, n, substituteA, processForSF, uniqueCOR, candidates);

                        }

                    }
                }
            }
        }
    }

    /**
     * @author Abinav
     */
    private void eliminateNonSFC(boolean validSF, boolean processingRows, int number,int j, int k, int n,int substituteA, List<List<int[]>> processForSF,Set<Integer> uniqueCOR,List<String> candidates){
        if (validSF) {
            //  System.out.println("passed swordfish criteria");


            for (String key : possibleNumbers.keySet()) {
                if (candidates.contains(key)) {
                    continue;
                }

                String[] keyPart = key.split(",");
                int row = Integer.parseInt(keyPart[0]);
                int column = Integer.parseInt(keyPart[1]);
                if (processingRows) {
                    for (Integer setElement : uniqueCOR) {
                        if (setElement == column) {
                            if (possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(number)) {
                                updatePossibleCounts(number,null, row, column, false);
                                possibleNumbers.get(key).remove((Integer) number);
                            }
                        }
                    }
                } else {
                    for (Integer setElement : uniqueCOR) {
                        if (setElement == row) {
                            if (possibleNumbers.get(key) != null && possibleNumbers.get(key).contains(number)) {
                                updatePossibleCounts(number, null,row, column, false);
                                possibleNumbers.get(key).remove((Integer) number);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Abinav
     */
    private boolean checkOccurenceOfEachElement(List<Integer> list, Set<Integer> set){
        boolean validSFC = true;

        Map<Integer, Integer> frequency = new HashMap<>();

        for(Integer number : set){
            int count = 0;
            for(Integer i : list){
                if(number.equals(i)){
                    count++;
                }
            }
            frequency.put(number,count);
        }
        for(Integer count : frequency.values()){
            if(count !=2) {
                validSFC = false;
                break;
            }
        }

        return validSFC;
    }


    /**
     * @author Abinav
     */
    public void simpleColouring(){
        findSimpleColouringCandidates();
        nakedSingles();
    }

    /**
     * @author Abinav
     */
    private void findSimpleColouringCandidates() {
        int blue = 0;
        int green = 1;
        List<String> cellsContainingCandidate;
        Map<String, Integer> scCandidates;

        for (int number = 8; number <= boardSize; number++) {
            cellsContainingCandidate = new ArrayList<>();
            for (String key : possibleNumbers.keySet()) {
                if (possibleNumbers.get(key).contains(number)) cellsContainingCandidate.add(key);
            }
            for(String position: possibleNumbers.keySet()){
                // System.out.println();
                if(!cellsContainingCandidate.contains(position)){
                    continue;
                }
                scCandidates =new LinkedHashMap<>(); // new HashMap<String,Integer>();
                List<String> relatedKeys = getRelatedKeys(number, position, cellsContainingCandidate,scCandidates);
                // System.out.println();
                if (relatedKeys.isEmpty()) {
                    continue;
                } else {
                    scCandidates.put(position, blue);
                    findAndColorRelatedKeys(number, position, cellsContainingCandidate, scCandidates, green); // Alternate starting with green
                }
                //System.out.println("number:"+number+ " start of link is :"+position);

                //System.out.println();
                if(verifyLink(scCandidates)){
                    // System.out.println(verifyLink(scCandidates));
                    // System.out.println(scCandidates.size()%2);
                    // System.out.println();
                    if(scCandidates.size()>=3){
                        handleTwoColorsSameHouse(scCandidates,number,cellsContainingCandidate);
                    }
                    if ( handleTwoColorsSameHouse(scCandidates, number, cellsContainingCandidate) == false && scCandidates.size() % 2 == 0  ) {
                        //  System.out.println();
                        //  System.out.println("link has size odd length");

                        String startPosition = scCandidates.keySet().iterator().next();
                        String[] linkStart = startPosition.split(",");

                        String lastPosition = null;
                        for (String keys : scCandidates.keySet()){
                            lastPosition = keys;
                        }
                        String[] linkEnd = lastPosition.split(",");

                        boolean sameRow = linkStart[0].equals(linkEnd[0]);
                        boolean sameColumn = linkStart[1].equals(linkEnd[1]);
                        // System.out.println();
                        if(sameRow ){

                            int minCol = Math.min(Integer.parseInt(linkStart[1]),Integer.parseInt(linkEnd[1]));
                            int maxCol = Math.max(Integer.parseInt(linkStart[1]),Integer.parseInt(linkEnd[1]));
                            for(int start = minCol+1; start < maxCol; start++){
                                String nogle = linkStart[0]+","+start;
                                if(possibleNumbers.get(nogle) != null && possibleNumbers.get(nogle).contains(number) && !scCandidates.containsKey(nogle)){
                                    // System.out.println(nogle);
                                    List<Integer> valuesOfKey2 = possibleNumbers.get(nogle);
                                    // System.out.println();
                                    valuesOfKey2.remove(Integer.valueOf(number));
                                    updatePossibleCounts(Integer.valueOf(number),null,Integer.parseInt(linkStart[0]),start,false);
                                    cellsContainingCandidate.remove(nogle);
                                    //  System.out.println();
                                }
                            }
                        }
                        else if(sameColumn){
                            int minRow = Math.min(Integer.parseInt(linkStart[0]),Integer.parseInt(linkEnd[0]));
                            int maxRow = Math.max(Integer.parseInt(linkStart[0]),Integer.parseInt(linkEnd[0]));
                            for(int start = minRow+1; start < maxRow; start++){
                                String nogle = start+","+ linkStart[1];
                                if(possibleNumbers.get(nogle) != null && possibleNumbers.get(nogle).contains(number) && !scCandidates.containsKey(nogle)){
                                    //System.out.println(nogle);
                                    List<Integer> valuesOfKey2 = possibleNumbers.get(nogle);
                                    // System.out.println();
                                    valuesOfKey2.remove(Integer.valueOf(number));
                                    updatePossibleCounts(Integer.valueOf(number),null,start,Integer.parseInt(linkStart[1]),false);
                                    cellsContainingCandidate.remove(nogle);
                                    //System.out.println();

                                }
                            }
                        }
                        else {

                            String position1 = Integer.parseInt(linkStart[0]) + "," + Integer.parseInt(linkEnd[1]);
                            String position2 = Integer.parseInt(linkEnd[0]) + "," + Integer.parseInt(linkStart[1]);

                            if(possibleNumbers.get(position1) != null && possibleNumbers.get(position1).contains(number) && !scCandidates.containsKey(position1)){
                                // System.out.println(position1);
                                List<Integer> valuesOfKey2 = possibleNumbers.get(position1);
                                // System.out.println();
                                valuesOfKey2.remove(Integer.valueOf(number));
                                updatePossibleCounts(Integer.valueOf(number),null,Integer.parseInt(linkStart[0]),Integer.parseInt(linkEnd[1]),false);
                                cellsContainingCandidate.remove(position1);
                                //  System.out.println();

                            }
                            if(possibleNumbers.get(position2) != null && possibleNumbers.get(position2).contains(number) && !scCandidates.containsKey(position2)){
                                // System.out.println(position2);
                                List<Integer> valuesOfKey2 = possibleNumbers.get(position2);
                                // System.out.println();
                                valuesOfKey2.remove(Integer.valueOf(number));
                                updatePossibleCounts(Integer.valueOf(number),null,Integer.parseInt(linkEnd[0]),Integer.parseInt(linkStart[1]),false);
                                cellsContainingCandidate.remove(position2);
                                // System.out.println();
                            }
                        }
                    }
                }
                //System.out.println();
                scCandidates.clear();
            }
            EliminateEmptyLists();
        }
    }

    /**
     * @author Abinav
     */
    private void findAndColorRelatedKeys(int number, String key, List<String> cellsContainingCandidate, Map<String, Integer> scCandidates, int color){
        int blue = 0;
        int green = 1;
        List<String> relatedKeys = getRelatedKeys(number,key, cellsContainingCandidate,scCandidates);
        for(String relatedKey : relatedKeys){
            if(relatedKeys.isEmpty()) continue;
            if ( !scCandidates.containsKey(relatedKey)) {
                scCandidates.put(relatedKey,color);
                int nextColor = (color == blue) ? green : blue;
                findAndColorRelatedKeys(number, relatedKey, cellsContainingCandidate, scCandidates, nextColor);
            }
        }
    }


    /**
     * @author Abinav
     */
    private boolean verifyLink(Map<String, Integer> scCandidates){
        Iterator<String> iterator = scCandidates.keySet().iterator();
        boolean linkValid = true;
        String key = iterator.next();

        while(iterator.hasNext()){

            String[] coord = key.split(",");
            int row = Integer.parseInt(coord[0]);
            int column = Integer.parseInt(coord[1]);
            int subBoardNo = board.findSubBoardNumber(row, column);
            int color = scCandidates.get(key);


            String key2 = iterator.next();
            String[] coord2 = key2.split(",");
            int row2 = Integer.parseInt(coord2[0]);
            int column2 = Integer.parseInt(coord2[1]);
            int subBoardNo2 = board.findSubBoardNumber(row2, column2);
            int color2 = scCandidates.get(key2);

            boolean notInSameGroup = (row!=row2 && column != column2 && subBoardNo != subBoardNo2);
            boolean sameColor = color==color2;
            if(notInSameGroup || sameColor){
                linkValid = false;
                break;
            }
            key = key2;
        }
        return linkValid;
    }


    /**
     * @author Abinav
     */
    private List<String> getRelatedKeys (int number, String key,List<String> cellsContainingCandidate,Map<String, Integer> scCandidates){
        List<String> relatedKeys = new ArrayList<>();
        String[] parts = key.split(",");
        int keyrow = Integer.parseInt(parts[0]);
        int keycolumn = Integer.parseInt(parts[1]);
        int subBoardNoForKey = board.findSubBoardNumber(keyrow, keycolumn);
        int rowOccurenceCount = valuePossibleCountRows[number][keyrow];
        int columnOccurenceCount = valuePossibleCountColumns[number][keycolumn];
        int subboardOccurenceCount = valuePossibleCountSubBoards[number][subBoardNoForKey];

        for (String key1 : cellsContainingCandidate) {
            if (key1.equals(key)) continue;
            String[] coord = key1.split(",");
            int row = Integer.parseInt(coord[0]);
            int column = Integer.parseInt(coord[1]);
            int subBoardNo = board.findSubBoardNumber(row, column);
            if (keyrow == row && possibleNumbers.get(key1).contains(number) && rowOccurenceCount ==2) {
                if (!scCandidates.containsKey(key1))  relatedKeys.add(key1);
            }
            else if (keycolumn == column && possibleNumbers.get(key1).contains(number) && columnOccurenceCount == 2) {
                if (!scCandidates.containsKey(key1))  relatedKeys.add(key1);
            }
            else if (subBoardNoForKey == subBoardNo && possibleNumbers.get(key1).contains(number) && subboardOccurenceCount==2) {
                if (!scCandidates.containsKey(key1))  relatedKeys.add(key1);
            }
        }
        if(!relatedKeys.isEmpty()) {
            // System.out.println("related keys of" + key + "are:" + relatedKeys);
        }
        return relatedKeys;

    }

    /**
     * @author Abinav
     */
    private boolean handleTwoColorsSameHouse(Map<String, Integer> scCandidates, int number,List<String> cellsContainingCandidate) {
        List<String> blueColoredCells = new ArrayList<>();
        List<String> greenColoredCells = new ArrayList<>();
        List<String> keysToRemove = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        numberList.add(number);

        boolean greenCellsSameHouse = false;
        boolean blueCellsSameHouse = false;

        for (String position : scCandidates.keySet()) {
            if (scCandidates.get(position) == 0) {
                blueColoredCells.add(position);
            } else {
                greenColoredCells.add(position);
            }
        }

        for (int i = 0; i < blueColoredCells.size(); i++) {
            String[] coord = blueColoredCells.get(i).split(",");
            int row1 = Integer.parseInt(coord[0]);
            int column1 = Integer.parseInt(coord[1]);
            int subBoardNo1 = board.findSubBoardNumber(row1, column1);

            for (int j = 0; j < blueColoredCells.size(); j++) {
                if (i == j) continue;
                String[] coord2 = blueColoredCells.get(j).split(",");
                int row2 = Integer.parseInt(coord2[0]);
                int column2 = Integer.parseInt(coord2[1]);
                int subBoardNo2 = board.findSubBoardNumber(row2, column2);

                if (row1 == row2 || column1 == column2 || subBoardNo1 == subBoardNo2) {
                    blueCellsSameHouse = true;
                    break;
                }
            }
            if (blueCellsSameHouse) break;
        }


        for (int i = 0; i < greenColoredCells.size(); i++) {
            String[] coord = greenColoredCells.get(i).split(",");
            int row1 = Integer.parseInt(coord[0]);
            int column1 = Integer.parseInt(coord[1]);
            int subBoardNo1 = board.findSubBoardNumber(row1, column1);

            for (int j = 0; j < greenColoredCells.size(); j++) {
                if (i == j) continue;
                String[] coord2 = greenColoredCells.get(j).split(",");
                int row2 = Integer.parseInt(coord2[0]);
                int column2 = Integer.parseInt(coord2[1]);
                int subBoardNo2 = board.findSubBoardNumber(row2, column2);

                if (row1 == row2 || column1 == column2 || subBoardNo1 == subBoardNo2) {
                    greenCellsSameHouse = true;
                    break;
                }
            }
            if (greenCellsSameHouse) break;
        }

        if (blueCellsSameHouse && !greenCellsSameHouse) {
            // System.out.println("blue cells belong to same house");
            // System.out.println();
            for (String cell1 : greenColoredCells) {
                if(!cellsContainingCandidate.contains(cell1)) continue;
                String[] coord1 = cell1.split(",");
                int row = Integer.parseInt(coord1[0]);
                int column = Integer.parseInt(coord1[1]);
                System.out.println();
                board.placeValueInCell(row, column,number);
                updatePossibleCounts(number,possibleNumbers.get(cell1), row, column,false);
                cellsContainingCandidate.remove(cell1);
                removeNumberFromOtherCandidate(cell1,numberList,cellsContainingCandidate);
                keysToRemove.add(cell1);
                //removeKeysHavingEmptyList();
                // System.out.println();
            }

            for (String cell2 : blueColoredCells) {
                if(!cellsContainingCandidate.contains(cell2)) continue;
                String[] coord2 = cell2.split(",");
                // System.out.println();
                int row = Integer.parseInt(coord2[0]);
                int column = Integer.parseInt(coord2[1]);
                possibleNumbers.get(cell2).remove(Integer.valueOf(number));
                cellsContainingCandidate.remove(cell2);
                updatePossibleCounts(number,null,row,column,false);

                // System.out.println();
            }
        }

        else if (greenCellsSameHouse && !blueCellsSameHouse) {
            //System.out.println("green cells belong to same house");
            for (String cell3 : blueColoredCells) {
                if(!cellsContainingCandidate.contains(cell3)) continue;
                String[] coord3 = cell3.split(",");
                // System.out.println();
                int row = Integer.parseInt(coord3[0]);
                int column = Integer.parseInt(coord3[1]);
                board.placeValueInCell(row, column,number);
                cellsContainingCandidate.remove(cell3);
                updatePossibleCounts(number,possibleNumbers.get(cell3), row, column,false);
                removeNumberFromOtherCandidate(cell3,numberList,cellsContainingCandidate);
                keysToRemove.add(cell3);
                //removeKeysHavingEmptyList();
                // System.out.println();
            }

            for (String cell4 : greenColoredCells) {
                if(!cellsContainingCandidate.contains(cell4)) continue;
                String[] coord4 = cell4.split(",");
                // System.out.println();
                int row = Integer.parseInt(coord4[0]);
                int column = Integer.parseInt(coord4[1]);
                possibleNumbers.get(cell4).remove(Integer.valueOf(number));
                cellsContainingCandidate.remove(cell4);
                updatePossibleCounts(number,null,row,column,false);
                // System.out.println();
            }
        }

        if(!keysToRemove.isEmpty()) {
            for (String key : keysToRemove) {
                possibleNumbers.remove(key);
            }
        }

        boolean bool = greenCellsSameHouse || blueCellsSameHouse;
        // System.out.println();
        return bool;
    }

    /**
     * @author Danny
     */
    public void intersectionRemoval()
    {
        pointingDuplicatesWithBLR(true);
        nakedSingles();
        pointingDuplicatesWithBLR(false);
        nakedSingles();
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
                                            updatePossibleCounts(value, null, substituteA, substituteB,false);

                                            possibleNumbers.get(key).remove((Integer) value);
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
                                                updatePossibleCounts(value, null, startingRow + substituteA, startingColumn + substituteB,false);

                                                possibleNumbers.get(key).remove((Integer) value); // remove value from the rest of the sub-board
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
    public void wingStrategies()
    {
        xWing(true);
        nakedSingles();
        xWing(false);
        nakedSingles();
        yWingWithXYZExtension(false);
        nakedSingles();
        yWingWithXYZExtension(true);
        nakedSingles();
        wXYZWingWithExtension(false);
        nakedSingles();
        wXYZWingWithExtension(true);
        nakedSingles();
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

                        if(rowOrColumnB == boardSize - 1) // last iteration
                        {
                            processedForXWings.add(rowColumnPositions); // add positions to x-wing candidates list
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
                                            updatePossibleCounts(value, null, substituteC, substituteD,false);

                                            possibleNumbers.get(key).remove((Integer) value);
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
    private void yWingWithXYZExtension(boolean runWithExtension)
    {
        List<int[]> cellsWithValue;
        List<int[]> hingeCells;
        List<int[]> pincerCandidateCells;
        List<int[]> pincerCells;
        List<int[]> pincersProcessed;
        int pincersValuesNeeded = !runWithExtension ? 2 : 3;

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
                                    if(runWithExtension && pincerCells.isEmpty()) // in extended yWing (XYZWing) the hinge itself is always a pincer
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

                            // Make sure all hinge-values are present in the non-hinge pincers of the combination
                            boolean[] hingeValuesPresent = new boolean[possibleNumbers.get(hingeKey).size()];
                            int hingeValuesInPincers = 0;

                            for(int pincer = (!runWithExtension ? 0 : 1); pincer < pincersProcessed.size(); pincer++)
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
    private void wXYZWingWithExtension(boolean runWithExtension)
    {
        List<int[]> cellsWithValue;
        List<int[]> hingeCells;
        List<int[]> pincerCandidateCells;
        List<int[]> pincerCells;
        List<int[]> pincersProcessed;
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
                    Set<Integer> unionHingeValues = new HashSet<>();

                    boolean multipleHinges = firstHinge != secondHinge;
                    String hingeKeyA = hingeCells.get(firstHinge)[0] + "," + hingeCells.get(firstHinge)[1];
                    String hingeKeyB = !multipleHinges ? null : hingeCells.get(secondHinge)[0] + "," + hingeCells.get(secondHinge)[1];

                    for(int[] pincer : pincerCandidateCells)
                    {
                        String pincerKey = pincer[0] + "," + pincer[1];

                        // Make sure the pincer has at least one value in common with both hinges
                        int hingesInvolved = !multipleHinges ? 1 : 2;
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

                            if((hingeRowA != pincerCandidateRow || hingeColumnA != pincerCandidateColumn) && (!multipleHinges || hingeRowB != pincerCandidateRow || hingeColumnB != pincerCandidateColumn)) // pincer can't be the same as the hinges
                            {
                                if(hingeRowA == pincerCandidateRow || hingeColumnA == pincerCandidateColumn || board.findSubBoardNumber(hingeRowA, hingeColumnA) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from first hinge
                                {
                                    if(!multipleHinges || hingeRowB == pincerCandidateRow || hingeColumnB == pincerCandidateColumn || board.findSubBoardNumber(hingeRowB, hingeColumnB) == board.findSubBoardNumber(pincerCandidateRow, pincerCandidateColumn)) // pincer has to be visible from second hinge
                                    {
                                        if(pincerCells.isEmpty()) // in wXYZWing the hinges themselves are always pincers
                                        {
                                            pincerCells.add(hingeCells.get(firstHinge));
                                            unionHingeValues.addAll(possibleNumbers.get(hingeKeyA));

                                            if(multipleHinges)
                                            {
                                                pincerCells.add(hingeCells.get(secondHinge));
                                                unionHingeValues.addAll(possibleNumbers.get(hingeKeyB));
                                            }
                                        }

                                        pincerCells.add(pincer);
                                    }
                                }
                            }
                        }
                    }

                    // Process every combination of found pincer cells (with hinges always) for potential value elimination
                    if(pincerCells.size() >= pincersValuesNeeded)
                    {
                        pincersProcessed = new ArrayList<>();
                        Set<Integer> unionPincerValues = new HashSet<>(unionHingeValues);
                        List<Integer> unionHingeValueList = unionHingeValues.stream().toList(); // get all unique hinge values on a list

                        pincersProcessed.add(pincerCells.get(0)); // always add first hinge cell

                        if(multipleHinges) // possibly add second hinge cell
                        {
                            pincersProcessed.add(pincerCells.get(1));
                        }

                        List<int[]> processedBeforeA; // variables for resetting instead of removing (less intensive and faster)
                        List<int[]> processedBeforeB;
                        List<int[]> processedBeforeC;
                        List<int[]> processedBeforeD;
                        Set<Integer> unionBeforeA;
                        Set<Integer> unionBeforeB;
                        Set<Integer> unionBeforeC;
                        Set<Integer> unionBeforeD;

                        for(int pincerA = (!multipleHinges ? 1 : 2); pincerA < pincerCells.size() - 1; pincerA++)
                        {
                            processedBeforeA = new ArrayList<>(pincersProcessed);
                            pincersProcessed.add(pincerCells.get(pincerA));
                            unionBeforeA = new HashSet<>(unionPincerValues);
                            unionPincerValues.addAll(possibleNumbers.get(pincerCells.get(pincerA)[0] + "," + pincerCells.get(pincerA)[1]));

                            for(int pincerB = pincerA + 1; pincerB < pincerCells.size(); pincerB++)
                            {
                                processedBeforeB = new ArrayList<>(pincersProcessed);
                                pincersProcessed.add(pincerCells.get(pincerB));
                                unionBeforeB = new HashSet<>(unionPincerValues);
                                unionPincerValues.addAll(possibleNumbers.get(pincerCells.get(pincerB)[0] + "," + pincerCells.get(pincerB)[1]));

                                for(int pincerC = (!(multipleHinges && !runWithExtension) ? pincerB + 1 : pincerB); pincerC < pincerCells.size(); pincerC++)
                                {
                                    processedBeforeC = null;
                                    unionBeforeC = null;

                                    if(!(multipleHinges && !runWithExtension))
                                    {
                                        processedBeforeC = new ArrayList<>(pincersProcessed);
                                        pincersProcessed.add(pincerCells.get(pincerC));
                                        unionBeforeC = new HashSet<>(unionPincerValues);
                                        unionPincerValues.addAll(possibleNumbers.get(pincerCells.get(pincerC)[0] + "," + pincerCells.get(pincerC)[1]));
                                    }

                                    for(int pincerD = (!multipleHinges && runWithExtension ? pincerC + 1 : pincerC); pincerD < pincerCells.size(); pincerD++)
                                    {
                                        processedBeforeD = null;
                                        unionBeforeD = null;

                                        if(!multipleHinges && runWithExtension)
                                        {
                                            processedBeforeD = new ArrayList<>(pincersProcessed);
                                            pincersProcessed.add(pincerCells.get(pincerD));
                                            unionBeforeD = new HashSet<>(unionPincerValues);
                                            unionPincerValues.addAll(possibleNumbers.get(pincerCells.get(pincerD)[0] + "," + pincerCells.get(pincerD)[1]));
                                        }

                                        // Make sure all hinge-values are present in the non-hinge pincers of the combination
                                        boolean[] hingeValuesPresent = new boolean[unionHingeValueList.size()];
                                        int hingeValuesInPincers = 0;

                                        for(int pincer = (!multipleHinges ? 1 : 2); pincer < pincersProcessed.size(); pincer++) // skip hinges
                                        {
                                            for(int hingeValue = 0; hingeValue < unionHingeValueList.size(); hingeValue++)
                                            {
                                                if(unionHingeValueList.get(hingeValue) == value)
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

                                        // Perform final checks of the pincer combination
                                        if(hingeValuesInPincers == pincersValuesNeeded)
                                        {
                                            boolean hasNonRestricted = false;
                                            boolean allValuesObservable = true; // it is simpler to detect a negative here

                                            nestedLoops:
                                            {
                                                for(Integer unionValue : unionPincerValues)
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

                                        if(!multipleHinges && runWithExtension)
                                        {
                                            pincersProcessed = processedBeforeD; // resetting
                                            unionPincerValues = unionBeforeD;
                                        }
                                        else // only run the loop once
                                        {
                                            break;
                                        }
                                    }

                                    if(!(multipleHinges && !runWithExtension))
                                    {
                                        pincersProcessed = processedBeforeC;
                                        unionPincerValues = unionBeforeC;
                                    }
                                    else
                                    {
                                        break;
                                    }
                                }

                                pincersProcessed = processedBeforeB;
                                unionPincerValues = unionBeforeB;
                            }

                            pincersProcessed = processedBeforeA;
                            unionPincerValues = unionBeforeA;
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
        int[] pincerAKey = pincersProcessed.get(0);
        int[] pincerBKey = pincersProcessed.get(1);
        int[] pincerCKey = pincersProcessed.size() >= 3 ? pincersProcessed.get(2) : null;
        int[] pincerDKey = pincersProcessed.size() >= 4 ? pincersProcessed.get(3) : null;
        int[] pincerEKey = pincersProcessed.size() == 5 ? pincersProcessed.get(4) : null;

        // Find collectively and individually observed non-pincer cells with value
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
                    }
                }
            }
        }
    }
}

