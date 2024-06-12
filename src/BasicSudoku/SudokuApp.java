package BasicSudoku;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import java.awt.*;
import java.awt.event.ActionListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.io.File;
import java.net.URL;
import java.util.*;

import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SudokuApp implements Initializable, ActionListener
{
    private static SudokuBoard board;
    private static List<Node> valueInsertHistory;
    private static List<Node> hintInsertHistory;

    private static List<String> hintInsertHistorySaved;
    private static List<String> valueInsertHistorySaved;
    private static boolean gameLoaded = false;
    private static boolean savingGame = false;
    public static boolean boardAlreadySolved = false;
    private static long savedTimeLoaded ;

    // JavaFX related
    private static Stage appStage;
    private static Scene currentScene;
    @FXML
    private GridPane boardGrid; // puzzle, solver
    private TextField[][] boardGridCells; // puzzle, solver
    private TextField activeTextField; // puzzle
    @FXML
    private TextField boardSizeField; // menu
    @FXML
    private TextField boxSizeField; // menu
    @FXML
    private Text boardSizeValidationField; // menu
    @FXML
    private Text timeSolvingField; // puzzle
    private final Timer userSolveTimer = new Timer(100, this); // puzzle
    private long userSolvingTime; // puzzle
    @FXML
    private Text filledCellsField; // puzzle, solver, custom
    @FXML
    private Text feedbackField; // puzzle, custom, solver
    @FXML
    private Button pauseResumeButton; // puzzle
    @FXML
    private Rectangle gamePausedOverlay; // puzzle
    @FXML
    private Text gamePausedField; // puzzle

    @FXML
    private Text saveLoadSceneSubtitle; // saveload

    @FXML
    private Label saveLoadSceneTitle; // saveload

    @FXML
    private Button backButton; // saveload

    @FXML
    private Button saveLoadButton; // saveload

    @FXML
    private ListView saveLoadSlotList; // saveload

    private boolean gamePaused;

    @FXML
    private Button undoButton; // puzzle
    @FXML
    private Button hintButton; // puzzle
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    List<String> list = new ArrayList<>(List.of("Slot 1", "Slot 2", "Slot 3", "Slot 4", "Slot 5"));

    private enum boardViewState
    {
        NoBoardShown, NoBoardShownSaveLoad, UnsolvedBoardShown, SolvedBoardShown, CustomBoardShown,
    }

    private static boardViewState boardView = boardViewState.NoBoardShown;

    /**
     * @author Danny, Abinav & Yahya
     */
    public void initialize(URL url, ResourceBundle resourceBundle) // initializes game scenes and is also needed to inject some JavaFX fields
    {
        if(boardView == boardViewState.UnsolvedBoardShown) // PuzzleScene
        {
            savingGame = false;
            if(board.getIsCustomBoard() && board.getIsSolverCandidate() && !gameLoaded)
            {
                board.solve();
            }

            if (gameLoaded) {
                boardAlreadySolved = true;
            } else {
                valueInsertHistorySaved = new ArrayList<>();
                hintInsertHistorySaved = new ArrayList<>();
            }
            valueInsertHistory = new ArrayList<>();
            hintInsertHistory = new ArrayList<>();


            showBoardValues(true);

            userSolvingTime = gameLoaded? savedTimeLoaded : 0;
            userSolveTimer.start();
            filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());
            feedbackField.setText("");
            gameLoaded = false;
        }
        else if(boardView == boardViewState.CustomBoardShown) // CustomScene
        {
            showBoardValues(true);

            filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());
            feedbackField.setText("");

            valueInsertHistory = new ArrayList<>();
        }
        else if(boardView == boardViewState.SolvedBoardShown) // SolverScene
        {
            if(board.getIsCustomBoard() && board.getIsSolverCandidate() && !board.getSolver().getSolvedBoard().isGameFinished() && !boardAlreadySolved) // second last condition is to not solve twice
            {
                board.solve();
            }
            showBoardValues(false);

            boardGrid.setDisable(true); // if there are empty cells, the user should not be able to edit them

            // Display the time used by the Solver to solve the puzzle
            long solvingTime = board.getSolver().getSolvingTime();
            int totalSeconds = (int) solvingTime / 1000;
            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            int hours = ((totalSeconds / 60) / 60) % 60;
            String secondsAsText = (seconds >= 10 ? String.valueOf(seconds) : "0" + seconds) + "." + String.valueOf((long) (((solvingTime / 1000.0) - Math.floor(solvingTime / 1000.0)) * 1000)).charAt(0);
            String minutesAsText = minutes >= 10 ? String.valueOf(minutes) : "0" + minutes;
            String hoursAsText = hours >= 10 ? String.valueOf(hours) : "0" + hours;
            timeSolvingField.setText("Time: " + hoursAsText + ":" + minutesAsText + ":" + secondsAsText);
            filledCellsField.setText("Filled: " + board.getSolver().getSolvedBoard().getFilledCells() + "/" + board.getSolver().getSolvedBoard().getAvailableCells());



            if(board.getSolver().getSolvedBoard().isGameFinished() && !boardAlreadySolved)
            {
                if(board.getSolver().getSolvedWithStrategies() && !board.getSolver().getSolvedWithBacktracking())
                {
                    feedbackField.setText("The puzzle was solved with strategies!");
                }
                else if(board.getSolver().getSolvedWithStrategies() && board.getSolver().getSolvedWithBacktracking())
                {
                    feedbackField.setText("The puzzle was solved with strategies and backtracking!");
                }
                else
                {
                    feedbackField.setText("The puzzle was solved with backtracking!");
                }
            }
            else
            {
                if(!board.getIsSolverCandidate())
                {
                    feedbackField.setText("The puzzle is too large to determine if it is solvable!");
                }
                else if (boardAlreadySolved) {
                    feedbackField.setText("This is the solution for saved puzzle!");
                }
                else {
                    feedbackField.setText("The puzzle is unsolvable!");
                }
            }

            if(userSolveTimer.isRunning())
            {
                userSolveTimer.stop();
            }
            boardAlreadySolved = false;
        }
        else if(boardView == boardViewState.NoBoardShownSaveLoad) // SaveLoadScene
        {

            if(savingGame) {
                saveLoadSceneTitle.setText("Save");
                saveLoadSceneSubtitle.setText("Choose a slot to save the game in!");
                saveLoadButton.setText("Save");
                backButton.setOpacity(1);
                backButton.setDisable(false);
            } else {
                backButton.setOpacity(0);
                backButton.setDisable(true);
            }

            saveLoadSlotList.getItems().addAll(list);
            try {
                saveLoadGameSlotView();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else // MenuScene
        {
            savingGame = false;
            if(userSolveTimer.isRunning())
            {
                userSolveTimer.stop();
            }
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void initializeRandomBoard() throws IOException
    {
        int boardSizeBoxes = Integer.parseInt(boardSizeField.getText()); // number of boxes on each side of the board
        int boxSizeRowsColumns = Integer.parseInt(boxSizeField.getText()); // number of rows or columns on each side of the boxes

        if((boardSizeBoxes * boxSizeRowsColumns) <= (boxSizeRowsColumns * boxSizeRowsColumns)) // k*n <= n^2, requirement for being valid
        {
            board = new SudokuBoard(boardSizeBoxes, boxSizeRowsColumns, false);

            goToPuzzleScene();
        }
        else
        {
            boardSizeValidationField.setText("These are invalid dimensions for Sudoku!");
        }
    }

    /**
     * @author Danny
     */
    public void initializeCustomBoard() throws IOException
    {
        int boardSizeBoxes = Integer.parseInt(boardSizeField.getText()); // number of boxes on each side of the board
        int boxSizeRowsColumns = Integer.parseInt(boxSizeField.getText()); // number of rows or columns on each side of the boxes

        if((boardSizeBoxes * boxSizeRowsColumns) <= (boxSizeRowsColumns * boxSizeRowsColumns)) // k*n <= n^2, requirement for being valid
        {
            board = new SudokuBoard(boardSizeBoxes, boxSizeRowsColumns, true);

            goToCustomScene();
        }
        else
        {
            boardSizeValidationField.setText("These are invalid dimensions for Sudoku!");
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void showBoardValues(boolean unsolved)
    {
        int[][] boardToShow = unsolved ? board.getBoard() : board.getSolver().getSolvedBoard().getBoard();
        int boardSizeRowsColumns = board.getBoardSizeRowsColumns();
        double cellSize = Math.ceil(850.0 / boardSizeRowsColumns); // 850 = length and width the UI board (in pixels)

        boardGridCells = new TextField[boardSizeRowsColumns][boardSizeRowsColumns];
        int cellTextSize = 40 - ((board.getBoardSizeBoxes() - 3) * 10);

        for(int row = 0; row < boardSizeRowsColumns; row++)
        {
            for (int column = 0; column < boardSizeRowsColumns; column++) {
                // Style the text of the grid cell
                boardGridCells[row][column] = new TextField();
                TextField temp = boardGridCells[row][column];
                temp.setPrefSize(cellSize, cellSize);
                temp.setStyle("-fx-border-width: 0px; " + "-fx-padding: 1px;" + "-fx-border-color: #000000; " + "-fx-background-color: #ffffff;" + "-fx-font-size: " + cellTextSize + "px; " + "-fx-font-family: 'Arial'; " + "-fx-control-inner-background:#c0c0c0;" + "-fx-text-fill: #960000;" + "-fx-opacity: 1;");
                temp.setAlignment(Pos.CENTER);

                temp.setOnMouseClicked(event -> updateActiveTextField(temp)); // needed to know the currently active text field
                temp.setOnMouseDragged(event -> updateActiveTextField(temp));

               // Fill cell
                if(boardToShow[row][column] == 0)
                {
                    temp.setPromptText("");
                    temp.setEditable(true);
                }
                else
                {
                    temp.setPromptText(String.valueOf(boardToShow[row][column]));
                    if(gameLoaded && valueInsertHistorySaved != null && valueInsertHistorySaved.contains(row+","+column)){
                        temp.setDisable(false);
                        temp.setEditable(true);
                        temp.setStyle("-fx-border-width: 0px; "
                                        + "-fx-padding: 1px;"
                                        + "-fx-border-color: #000000; "
                                        + "-fx-background-color: #ffffff;"
                                        + "-fx-font-size: " + cellTextSize + "px; "
                                        + "-fx-font-family: 'Arial'; "
                                        + "-fx-control-inner-background:#FF0000;"
                                        + "-fx-text-fill: #960000;"
                                        + "-fx-opacity: 1;");

                    } else {
                        temp.setDisable(true);
                    }
                }

                boardGrid.add(temp, column, row); // fill the grid with created cells
            }

        }

        if(gameLoaded) {
            if (valueInsertHistorySaved != null) {
                for (String cell : valueInsertHistorySaved) {
                    String[] cellRowColumn = cell.split(",");
                    int cellsRow = Integer.parseInt(cellRowColumn[0]);
                    int cellsColumn = Integer.parseInt(cellRowColumn[1]);
                    valueInsertHistory.add((Node) boardGridCells[cellsRow][cellsColumn]);
                }
            }  if (hintInsertHistorySaved != null) {
                for (String cell : hintInsertHistorySaved) {
                    String[] cellRowColumn = cell.split(",");
                    int cellsRow = Integer.parseInt(cellRowColumn[0]);
                    int cellsColumn = Integer.parseInt(cellRowColumn[1]);
                    hintInsertHistory.add((Node) boardGridCells[cellsRow][cellsColumn]);
                }
            }
        }

        drawBoardLines(true);
        drawBoardLines(false);
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void drawBoardLines(boolean processingRows) // borders are done in SceneBuilder
    {
        int boardSizeRowsColumns = board.getBoardSizeRowsColumns();
        int boxSizeRowsColumns = board.getBoxSizeRowsColumns();
        double cellSize = Math.ceil(850.0 / boardSizeRowsColumns); // 850 = length and width the UI board (in pixels)
        double lengthOfLine;
        double widthOfLine;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;

        for(int rowOrColumnA = 0; rowOrColumnA < boardSizeRowsColumns; rowOrColumnA++)
        {
            for(int rowOrColumnB = 0; rowOrColumnB < boardSizeRowsColumns; rowOrColumnB++)
            {
                substituteA = processingRows ? rowOrColumnA : rowOrColumnB;
                substituteB = processingRows ? rowOrColumnB : rowOrColumnA;

                if(rowOrColumnB % boxSizeRowsColumns != 0) // borders of the cells
                {
                    lengthOfLine = processingRows ? 1 : cellSize;
                    widthOfLine = processingRows ? cellSize: 1;
                }
                else if(rowOrColumnB != 0 && rowOrColumnB != boardSizeRowsColumns - 1) // borders of the boxes
                {
                    lengthOfLine = processingRows ? 3 : cellSize;
                    widthOfLine = processingRows ? cellSize : 3;
                }
                else
                {
                    continue;
                }

                Rectangle line = new Rectangle();
                line.setHeight(lengthOfLine);
                line.setWidth(widthOfLine);
                line.setFill(Color.BLACK);

                boardGrid.add(line, substituteA, substituteB);

                if(processingRows)
                {
                    GridPane.setValignment(line, VPos.TOP); // correct alignment in grid cell
                }
                else
                {
                    GridPane.setHalignment(line, HPos.LEFT);
                }
            }
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void pauseResumeGame()
    {
        if(!gamePaused)
        {
            userSolveTimer.stop();

            boardGrid.requestFocus(); // un-focus all cells

            gamePausedOverlay.setOpacity(0.8);
            gamePausedField.setOpacity(1.0);

            gamePaused = true;

            pauseResumeButton.setText("Resume");

            // Disable buttons
            undoButton.setDisable(true);
            hintButton.setDisable(true);
        }
        else
        {
            userSolveTimer.start();

            gamePausedOverlay.setOpacity(0);
            gamePausedField.setOpacity(0);

            gamePaused = false;

            pauseResumeButton.setText("Pause");

            // Enable buttons
            undoButton.setDisable(false);
            hintButton.setDisable(false);
        }
    }

    /**
     * @author Danny
     */
    public void userPressedKeyboard(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER) // user pressed Enter and is trying to insert a value
        {
            insertBoardValue((Node) event.getTarget());
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void insertBoardValue(Node boardGridCell)
    {
        int row = GridPane.getRowIndex(boardGridCell);
        int column = GridPane.getColumnIndex(boardGridCell);
        int value = Integer.parseInt(boardGridCells[row][column].getText());

        if(board.getBoard()[row][column] != 0)
        {
            board.setBoardValue(row, column, 0);
        }

        if(board.placeValueInCell(row, column, value))
        {
            boardGrid.requestFocus(); // un-focus all cells

            if(!valueInsertHistory.contains(boardGridCell))
            {
                valueInsertHistory.add(boardGridCell);
                valueInsertHistorySaved.add(row+","+column);
            }

            updateFilledCellsUI();

            feedbackField.setText("");
        }
        else
        {
            valueInsertHistory.remove(boardGridCell);
            valueInsertHistorySaved.removeIf(position -> position.equals(row+","+column));;

            updateFilledCellsUI();

            boardGridCells[row][column].clear(); // reset cell

            feedbackField.setText(board.getErrorMessage());
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void updateFilledCellsUI()
    {
        filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());

        if(board.getFilledCells() == board.getAvailableCells())
        {
            userSolveTimer.stop();

            feedbackField.setText("You have solved the Sudoku!");
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void updateActiveTextField(TextField boardGridCell)
    {
        if(activeTextField == null)
        {
            activeTextField = boardGridCell;
        }

        int row = GridPane.getRowIndex(activeTextField);
        int column = GridPane.getColumnIndex(activeTextField);

        if(!activeTextField.equals(boardGridCell) && board.getBoard()[row][column] == 0)
        {
            activeTextField.clear();
        }

        activeTextField = boardGridCell;
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void showHint()
    {
        if(activeTextField != null)
        {
            int row = GridPane.getRowIndex(activeTextField);
            int column = GridPane.getColumnIndex(activeTextField);
            int value = board.getSolver().getSolvedBoard().getBoard()[row][column];

            if(board.placeValueInCell(row, column, value))
            {
                if(!boardGridCells[row][column].isDisable())
                {
                    updateFilledCellsUI();

                    activeTextField.clear();
                    activeTextField.setPromptText(String.valueOf(value));

                    hintInsertHistory.add(activeTextField);
                    hintInsertHistorySaved.add(row+","+column);

                    boardGridCells[row][column].setDisable(true);

                    boardGrid.requestFocus(); // un-focus all cells

                    feedbackField.setText("Solution for cell (" + (row + 1) + "," + (column + 1) + ") revealed!");

                    activeTextField = null;
                }
            }
            else
            {
                feedbackField.setText("Cannot provide hint due to a wrongly inserted value!");
            }
        }
        else
        {
            feedbackField.setText("Select a cell to show hint for!");
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void undoValueInsertion()
    {
        if(!valueInsertHistory.isEmpty())
        {
            int row = GridPane.getRowIndex(valueInsertHistory.get(valueInsertHistory.size() - 1));
            int column = GridPane.getColumnIndex(valueInsertHistory.get(valueInsertHistory.size() - 1));

            board.setBoardValue(row, column, 0);
            updateFilledCellsUI();

            boardGridCells[row][column].setDisable(false);
            boardGridCells[row][column].clear();
            boardGridCells[row][column].setPromptText("");

            valueInsertHistory.remove(valueInsertHistory.size() - 1);
            valueInsertHistorySaved.remove(valueInsertHistorySaved.size()-1);

            feedbackField.setText("Value insertion undone in cell (" + (row + 1) + ", " + (column + 1) + ")");
        }
        else
        {
            feedbackField.setText("There are no insertions to undo!");
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void undoHintInsertion()
    {
        if(!hintInsertHistory.isEmpty())
        {
            int row = GridPane.getRowIndex(hintInsertHistory.get(hintInsertHistory.size() - 1));
            int column = GridPane.getColumnIndex(hintInsertHistory.get(hintInsertHistory.size() - 1));

            board.setBoardValue(row, column, 0);
            updateFilledCellsUI();

            boardGridCells[row][column].setDisable(false);
            boardGridCells[row][column].clear();
            boardGridCells[row][column].setPromptText("");

            hintInsertHistory.remove(hintInsertHistory.size() - 1);
            hintInsertHistorySaved.remove(hintInsertHistorySaved.size()-1);
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void resetBoard()
    {
        while(!valueInsertHistory.isEmpty())
        {
            undoValueInsertion();
        }

        while(!hintInsertHistory.isEmpty() )
        {
            undoHintInsertion();
        }

        userSolvingTime = 0;
        userSolveTimer.start();

        feedbackField.setText("The puzzle has been reset!");

        if(gamePaused)
        {
            pauseResumeGame();
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) // updates solving timer every second
    {
        userSolvingTime += 100;

        // Display the time used by the Solver to solve the puzzle
        int totalSeconds = (int) (userSolvingTime / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = ((totalSeconds / 60) / 60) % 60;
        String secondsAsText = (seconds >= 10 ? String.valueOf(seconds) : "0" + seconds) + "." + (String.valueOf((long) (((userSolvingTime / 1000.0) - Math.floor(userSolvingTime / 1000.0)) * 1000)).charAt(0));
        String minutesAsText = minutes >= 10 ? String.valueOf(minutes) : "0" + minutes;
        String hoursAsText = hours >= 10 ? String.valueOf(hours) : "0" + hours;
        timeSolvingField.setText("Time: " + hoursAsText + ":" + minutesAsText + ":" + secondsAsText);
    }

    /**
     * @author Abinav
     */
    private ArrayNode convertArrayToJsonArray(ObjectMapper objectMapper, int[][] array){
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (int[] row : array) { // formats into json array
            ArrayNode rowNode = objectMapper.createArrayNode();
            for (int value : row) {
                rowNode.add(value);
            }
            arrayNode.add(rowNode);
        }
        return arrayNode;
    }

    /**
     * @author Abinav
     */
    public ObjectNode convertDataIntoJson( ObjectMapper objectMapper) {

            ObjectNode jsonNode = objectMapper.createObjectNode();
            ArrayNode solvedBoardNode = null;
            int[][] solvedBoardArray;

            int[][] boardArray = board.getBoard();
            if(board.getIsSolverCandidate()) {
                solvedBoardArray = board.getSolver().getSolvedBoard().getBoard();
                solvedBoardNode = convertArrayToJsonArray(objectMapper,solvedBoardArray);
            }

            ArrayNode boardNode = convertArrayToJsonArray(objectMapper,boardArray); // Create an ArrayNode to represent the board in JSON format
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime currentTime = LocalDateTime.now();
            // saves following data as json object

            jsonNode.set("savedOnDateAndTime",objectMapper.convertValue(dtf.format(currentTime), JsonNode.class));
            jsonNode.set("board", boardNode);
            jsonNode.set("solvedboard", solvedBoardNode);
            if(!valueInsertHistorySaved.isEmpty()){
                jsonNode.set("userInsertedValues",objectMapper.convertValue(valueInsertHistorySaved, JsonNode.class));
            }else {
                jsonNode.set("userInsertedValues",null);
            }
            if(!hintInsertHistorySaved.isEmpty()){
                jsonNode.set("HintHistory",objectMapper.convertValue(hintInsertHistorySaved, JsonNode.class));
            }else {
                jsonNode.set("HintHistory",null);
            }
            jsonNode.set("filledcells",objectMapper.convertValue(board.getFilledCells(), JsonNode.class));
            jsonNode.put("userTime", userSolvingTime);
            jsonNode.set("boardsizeBoxes", objectMapper.convertValue(board.getBoardSizeBoxes(), JsonNode.class));
            jsonNode.set("boxsizeRowsColumn", objectMapper.convertValue(board.getBoxSizeRowsColumns(), JsonNode.class));


        return jsonNode;
    }

    /**
     * @author Abinav
     */
    public void saveGame(int slotNo) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("saveLoad.json");
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ArrayNode arrayNode;

        if (jsonFile.exists()) {
            JsonNode node = objectMapper.readTree(jsonFile);
            if (node.isArray()) {
                arrayNode = (ArrayNode) node;
            } else {
                arrayNode = factory.arrayNode();
            }
        } else {
            arrayNode = factory.arrayNode();
        }

        // Ensure the ArrayNode has at least 5 elements
        while (arrayNode.size() < 5) {
            arrayNode.add(factory.nullNode());
        }

        ObjectNode gameStateAsJson = convertDataIntoJson(objectMapper);
        if (slotNo >=0 && slotNo < 5)  arrayNode.set(slotNo,gameStateAsJson);

        // saving the json obj in specified slot
        objectMapper.writeValue(jsonFile, arrayNode);
        System.out.println("Data saved to saveLoad.json");
    }

    /**
     * @author Abinav
     */
    public void saveLoadGameSlotView() throws IOException {
        ObservableList items = saveLoadSlotList.getItems();

        executorService.submit(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = new File("saveLoad.json");
            try {
                ArrayNode nodes;
                if (jsonFile.exists()) {
                    nodes = (ArrayNode) objectMapper.readTree(jsonFile);
                    for (int i = 0; i < 5; i++) {
                        JsonNode jsonNode = nodes.get(i);
                        String dateAndTime = jsonNode.get("savedOnDateAndTime").asText();
                        int boardSizeBoxesNode = jsonNode.get("boardsizeBoxes").asInt();
                        int boxSizeRowsColumnNode = jsonNode.get("boxsizeRowsColumn").asInt();
                        final int index = i; // Need to make it effectively final
                        Platform.runLater(() -> items.set(index, boardSizeBoxesNode + "x" + boxSizeRowsColumnNode + " board                             saved on: " + dateAndTime));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        saveLoadSlotList.setFixedCellSize(80.0);
        saveLoadSlotList.prefHeightProperty().bind(Bindings.size(saveLoadSlotList.getItems()).multiply(80));
        // disables scrollbar
        saveLoadSlotList.skinProperty().addListener((observable, oldValue, newValue) -> {
            for (Node node : saveLoadSlotList.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    ScrollBar scrollBar = (ScrollBar) node;
                    scrollBar.setDisable(true);
                    scrollBar.setVisible(false);
                }
            }
        });
        // styles elements in list
        saveLoadSlotList.setStyle("-fx-background-color: #000000; "
                                    + "-fx-border-width: 0px; "
                                    + "-fx-padding: 0 -10px 0px  0; "
                                    + "-fx-font-size: 18px; "
                                    + "-fx-font-family: 'Segoe UI'; "
                                    + "-fx-control-inner-background:#000000;");

    }


    /**
     * @author Abinav
     */
    public void onPressedLoadSave() throws IOException {
        SelectionModel<String> selectionModel = saveLoadSlotList.getSelectionModel();
        int selectedSlot = selectionModel.getSelectedIndex();
        int loadSelectedSlot = selectedSlot+1;
        if(selectedSlot != -1 && savingGame) {
            saveGame(selectedSlot);
            saveLoadGameSlotView();
            savingGame = false;
        } else if (!saveLoadSlotList.getItems().get(selectedSlot).equals("slot " + loadSelectedSlot)) {
            loadGame(selectedSlot);
        }
    }

    /**
     * @author Abinav
     */
    public void loadGame(int slotNo){
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("saveLoad.json");
        try {
            ArrayNode nodes;
            if(jsonFile.exists()) {
                nodes = (ArrayNode) objectMapper.readTree(jsonFile);

                if (slotNo >=0 && slotNo < 5) {
                    JsonNode jsonNode = nodes.get(slotNo);
                    int filledCellsSaved = jsonNode.get("filledcells").asInt();
                    int boardSizeBoxesSaved = jsonNode.get("boardsizeBoxes").asInt();
                    int boxSizeRowsColumnSaved = jsonNode.get("boxsizeRowsColumn").asInt();
                    long userSolvingTimeSaved = jsonNode.get("userTime").asLong();
                    int[][] boardArraySaved = objectMapper.convertValue(jsonNode.get("board"), int[][].class);
                    int[][] solvedBoardArraySaved = objectMapper.convertValue(jsonNode.get("solvedboard"), int[][].class);

                    // setting the saved values
                   if(!jsonNode.get("userInsertedValues").isNull() && jsonNode.get("userInsertedValues").isArray()){
                       List<String> insertedValuesOnBoardSaved = new ArrayList<>();
                       for (JsonNode node : jsonNode.get("userInsertedValues")) {
                           insertedValuesOnBoardSaved.add(node.asText());
                       }
                       valueInsertHistorySaved = insertedValuesOnBoardSaved;
                   }
                    if(!jsonNode.get("HintHistory").isNull() && jsonNode.get("HintHistory").isArray()) {
                        List<String> hintValuesOnBoardSaved = new ArrayList<>();
                        for (JsonNode node : jsonNode.get("HintHistory")) {
                            hintValuesOnBoardSaved.add(node.asText());
                        }
                        hintInsertHistorySaved = hintValuesOnBoardSaved;
                    }
                    board = new SudokuBoard(boardSizeBoxesSaved, boxSizeRowsColumnSaved, false);
                    convertJsonArrayIntoArray(boardArraySaved ,true);
                    board.setFilledCells(filledCellsSaved);
                    if(solvedBoardArraySaved != null){
                        convertJsonArrayIntoArray(solvedBoardArraySaved,false);
                        board.getSolver().getSolvedBoard().setFilledCells(board.getAvailableCells());
                    }
                    savedTimeLoaded = userSolvingTimeSaved;
                    gameLoaded = true;
                    goToPuzzleScene();

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @author Abinav
     */
    private  void  convertJsonArrayIntoArray(int[][] boardArraySaved, boolean unsolvedBoard) {
        for (int row = 0; row < boardArraySaved.length; row++) {
            for (int column = 0; column < boardArraySaved[row].length; column++) {
                if(unsolvedBoard) {
                    board.setBoardValue(row, column, boardArraySaved[row][column]);
                } else {
                    board.getSolver().getSolvedBoard().setBoardValue(row, column, boardArraySaved[row][column]);
                }
            }
        }
    }

    /**
     * @author Abinav
     */
    public void onPressedSave() throws IOException {
        savingGame = true;
        goToSaveLoadScene();
    }


    /**
     * @author Danny
     */
    public void goToMenuScene() throws IOException
    {
        boardView = boardViewState.NoBoardShown;
        setActiveScene("MenuScene");
    }

    /**
     * @author Danny
     */
    public void goToPuzzleScene() throws IOException
    {
        boardView = boardViewState.UnsolvedBoardShown;
        setActiveScene("PuzzleScene");
    }

    /**
     * @author Danny
     */
    public synchronized void goToSolverScene() throws IOException
    {
        boardView = boardViewState.SolvedBoardShown;
        setActiveScene("SolverScene");
    }

    /**
     * @author Danny
     */
    public void goToCustomScene() throws IOException
    {
        boardView = boardViewState.CustomBoardShown;
        setActiveScene("CustomScene");
    }

    /**
     * @author Abinav
     */
    public void goToSaveLoadScene() throws IOException
    {
        boardView = boardViewState.NoBoardShownSaveLoad;
        setActiveScene("SaveLoadScene");
    }

    /**
     * @author Danny & Abinav
     */
    public void setAppStage(Stage stage)
    {
        appStage = stage;

        GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int screenResolutionY = screen.getDisplayMode().getHeight(); // smallest, therefore the one used

        if(screenResolutionY != 1440) // default size
        {
            double adaptedAppSize = 1200 * (screenResolutionY / (double) (1200 + 240));

            appStage = stage;
            appStage.setWidth(adaptedAppSize - 23); // for some reason, stage width has to be -23 to render correctly
            appStage.setHeight(adaptedAppSize);
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void setActiveScene(String sceneName) throws IOException
    {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("UI/" + sceneName + ".fxml")));

        if(currentScene != null)
        {
            currentScene.setRoot(root);
        }
        else
        {
            currentScene = new Scene(root);
        }

        currentScene.widthProperty().addListener((observable) -> scaleScreen()); // used for resizing UI
        currentScene.heightProperty().addListener((observable) -> scaleScreen());

        scaleScreen();

        appStage.setScene(currentScene); // construct scene
        appStage.setTitle("Sudoku (Group 5)"); // window title
        appStage.setResizable(true); // disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("UI/Media/sudoku icon.png")))); // add app icon to stage
        appStage.show(); // show window
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    private static void scaleScreen()
    {
        double width = currentScene.getWidth();
        double height = currentScene.getHeight();

        if(!(Double.isNaN(width) || Double.isNaN(height)))
        {
            double scaleFactor = Math.min(width / 1200.0, height / 1200.0); // standard app resolution is 1200x1200

            if(!Double.isNaN(scaleFactor))
            {
                Scale scale = new Scale(scaleFactor, scaleFactor);
                scale.setPivotX(0);
                scale.setPivotY(0);

                currentScene.getRoot().getTransforms().setAll(scale);
                currentScene.getRoot().setTranslateX(Math.max(0, (width - scaleFactor*(1200)) / 2.0));
                currentScene.getRoot().setTranslateY(Math.max(0, (height - scaleFactor*(1200)) / 2.0));
                currentScene.setFill(Color.BLACK);
                currentScene.getRoot().setStyle("-fx-background-color: #000000;");
            }
        }
    }




}
