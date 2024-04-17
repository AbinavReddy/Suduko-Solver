package BasicSudoku;

import java.util.*;

public class Solver
{
    Board board;
    Board boardBackup; // for resolvability test
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
        // Danny
        board = solverToCopy.board;
        boardBackup = solverToCopy.boardBackup;
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
        // Danny, Abinav & Yahya
        boardBackup = new Board(board);
        boardBackup.getSolver().board = boardBackup;

        return boardBackup.solveBoard();
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

            nakedPairs();
            hiddenPairs();
            nakedQuads();
            hiddenQuads();
            intersectionRemoval();
            nakedSingles();
            simpleColouring();
            swordFish();

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

    public void removeNumberFromOtherCandidate(String key,List<Integer> values, List<String> cellsContainingCandidate) {
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
                if(!cellsContainingCandidate.isEmpty()) cellsContainingCandidate.remove(Key2);
            }
        }
    }

    private void returnCandidatesInColumns(int column) {
        // Abinav

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

    private void returnCandidatesInSubBoard(int subBoard) {
        // Abinav

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

    private void returnCandidatesInRows(int row) {
        // Abinav

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

    public void NP(){
        // Abinav

        nakedPairs();
        nakedSingles();
    }

    private void nakedPairs() {
        // Abinav

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


    private void findNakedPairs(Map<String, List<Integer>> candidates) {
        // Abinav

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
                        //system.out.println("Confirmed naked pair: " + values1 + " in cells " + key1 + " + " + key2 + ".");
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

    private void deleteNPCFromOtherCells(String key, String key2, List<Integer> values, int code) {
        // Abinav

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


    public void hiddenPairs(){

        // Abinav

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

    private void hiddenPairsForSubBoards(){
        // Abinav

        List<String> pairs;
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

            if(cellKeys.size() >= 2) {
                for (int i = 0; i < possibleValues.size(); i++) {
                    for (int j = i + 1; j < possibleValues.size(); j++) {
                        pairs = new ArrayList<>();
                        Set<Integer> unionOfValues = new HashSet<>(possibleValues.get(i));
                        unionOfValues.addAll(possibleValues.get(j));
                        pairs.add(cellKeys.get(i));
                        pairs.add(cellKeys.get(j));
                        //system.out.println();
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

    private void hiddenPairsCRcombo(boolean processRows) {
        // Abinav

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
                        //system.out.println();
                        Set<Integer> combos = findHiddenPairs(unionOfValues, cellKeys, pairs);
                        boolean pairsVerified = verifyPairs(pairs, combos);
                        //system.out.println();
                        if ( pairsVerified) {
                            //system.out.println();
                            for (String position : pairs) {
                                String[] pos = position.split(",");
                                List<Integer> valuesDuplicate = possibleNumbers.get(position);
                                possibleNumbers.get(position).retainAll(combos);
                                valuesDuplicate.removeAll(combos);
                                updatePossibleCounts(5,valuesDuplicate,Integer.parseInt(pos[0]),Integer.parseInt(pos[1]),false);
                                //system.out.println();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean verifyPairs(List<String> pairs, Set<Integer> combos) {
        // Abinav

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

    private Set<Integer> findHiddenPairs(Set<Integer> unionOfValues, List<String> cellKeys, List<String> pairs) {
        // Abinav

        List<Integer> valuesList = new ArrayList<>(unionOfValues);
        if(unionOfValues.size() >= 2) {
            for (int i = 0; i < valuesList.size(); i++) {
                for (int j = i + 1; j < valuesList.size(); j++) {
                    boolean foundHiddenPair = true;
                    Set<Integer> combinations = new HashSet<>();
                    combinations.add(valuesList.get(i));
                    combinations.add(valuesList.get(j));
                    //system.out.println();
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
                            //system.out.println();
                            return combinations; // Found a valid combination
                        }
                    }
                }
            }
        }

        return new HashSet<>();
    }


    public void nakedQuads(){
        // Abinav

        // finds naked quads in rows
        nakedQuadsCRcombo(true);
        nakedSingles();
        // finds naked quads in columns
        nakedQuadsCRcombo(false);
        nakedSingles();

        nakedQuadForSubBoards();
        nakedSingles();
    }


    private void nakedQuadsCRcombo(boolean processRows) {
        // Abinav


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
                                //system.out.println();
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


    public void hiddenQuads(){
        // Abinav

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

    private void hiddenQuadForSubBoards(){
        // Abinav

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

    private void hiddenQuadsCRcombo(boolean processRows) {
        // Abinav

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

    private boolean verifyQuads(List<String> quads, Set<Integer> combos) {
        // Abinav
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

    private Set<Integer> findHiddenQuads(Set<Integer> unionOfValues, List<String> cellKeys, List<String> quads) {
        // Abinav
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


    public void swordFish(){
        // Abinav

        // swordfish technique on rows where each cell contains only 2 cells
        findSwordFishCandidates(true,2);

        // swordfish technique on columns
        findSwordFishCandidates(false,2);

        // swordfish technique on rows
        findSwordFishCandidates(true,3);

        // swordfish technique on columns
        findSwordFishCandidates(false,3);

    }

    private void findSwordFishCandidates(boolean processingRows, int pairOrTriple) {
        // Abinav

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

    private void handleSFCandidates( int pairOrTriple,int substituteA,int number,boolean processingRows, List<List<int[]>> processForSF) {
        // Abinav

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

    private boolean checkOccurenceOfEachElement(List<Integer> list, Set<Integer> set){
        // Abinav
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

    public void simpleColouring(){

        // Abinav

        findSimpleColouringCandidates();
        nakedSingles();
    }

    private void findSimpleColouringCandidates() {
        // Abinav

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



    private void findAndColorRelatedKeys(int number, String key, List<String> cellsContainingCandidate, Map<String, Integer> scCandidates, int color){
        // Abinav

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

    private boolean verifyLink(Map<String, Integer> scCandidates){
        // Abinav

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



    private List<String> getRelatedKeys (int number, String key,List<String> cellsContainingCandidate,Map<String, Integer> scCandidates){

        // Abinav
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

    private boolean handleTwoColorsSameHouse(Map<String, Integer> scCandidates, int number,List<String> cellsContainingCandidate) {

        // Abinav
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

