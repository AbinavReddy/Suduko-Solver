package Sudoku;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SudokuApp implements Initializable, ActionListener
{
    private static Board board;
    private static List<Node> valueInsertHistory;
    private static List<Node> hintInsertHistory;
    private static List<String> valueInsertHistorySaved;
    private static List<String> hintInsertHistorySaved;
    private static boolean gameSavedLoaded = false;
    private static boolean clickedBack = false;

    private static boolean savingGame = false;
    public static boolean boardAlreadySolved = false;
    private static long savedTimeLoaded ;

    private static long preSaveLoadUserTime;

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
    private ComboBox comboBox; // menu
    @FXML
    private Text timeSolvingField; // puzzle
    private final Timer userSolveTimer = new Timer(100, this); // puzzle
    private  long userSolvingTime; // puzzle
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

    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1.5));

    private boolean gamePaused;

    @FXML
    private Button undoButton; // puzzle
    @FXML
    private Button hintButton; // puzzle
    @FXML
    private Button resetButton; // puzzle, custom
    private static boolean soundMuted = false;
    @FXML
    private ImageView soundButtonImage;
    private final Media clickSound = new Media(Objects.requireNonNull(getClass().getResource("UI/Media/click sound.wav")).toExternalForm());
    private final Media insertSound = new Media(Objects.requireNonNull(getClass().getResource("UI/Media/insert sound.wav")).toExternalForm());
    private final Media removeSound = new Media(Objects.requireNonNull(getClass().getResource("UI/Media/remove sound.wav")).toExternalForm());
    private final Media errorSound = new Media(Objects.requireNonNull(getClass().getResource("UI/Media/error sound.wav")).toExternalForm());
    private final Media winSound = new Media(Objects.requireNonNull(getClass().getResource("UI/Media/win sound.wav")).toExternalForm());
    private final Media loseSound = new Media(Objects.requireNonNull(getClass().getResource("UI/Media/lose sound.wav")).toExternalForm());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    List<String> list = new ArrayList<>(List.of("Slot 1", "Slot 2", "Slot 3", "Slot 4", "Slot 5"));

    private enum boardViewState
    {
        NoBoardShown, NoBoardShownSaveLoad, UnsolvedBoardShown, CustomBoardShown, SolvedBoardShown
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
            if(!board.getSolver().getSolverHasRun() && !gameSavedLoaded) // needed to solve custom boards
            {
                board.solve();
            }

            if (gameSavedLoaded) {
                boardAlreadySolved = true;
            } else {
                valueInsertHistorySaved = new ArrayList<>();
                hintInsertHistorySaved = new ArrayList<>();
            }

            showBoardValues(true);

            boardGrid.requestFocus();

            userSolvingTime = clickedBack ? preSaveLoadUserTime : (gameSavedLoaded ? savedTimeLoaded : 0);
            userSolveTimer.start();

            feedbackField.setText("");

            
            updateSoundIcon();

            if(gameSavedLoaded) {
                undoButton.setDisable(false);
                resetButton.setDisable(false);
            }
            else
            {
                undoButton.setDisable(true);
                resetButton.setDisable(true);
            }

            clickedBack = false;
            gameSavedLoaded = false;

            updateFilledCells();
        }
        else if(boardView == boardViewState.CustomBoardShown) // CustomScene
        {
            showBoardValues(true);

            feedbackField.setText("");

            updateSoundIcon();

            undoButton.setDisable(true);
            resetButton.setDisable(true);

            valueInsertHistory = new ArrayList<>();

            updateFilledCells();
        }
        else if(boardView == boardViewState.SolvedBoardShown) // SolverScene
        {
            if(!board.getSolver().getSolverHasRun() && !boardAlreadySolved) // needed to solve custom boards
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

            feedbackField.setText(createSolverFeedbackMessage());

            if(userSolveTimer.isRunning())
            {
                userSolveTimer.stop();
            }
            boardAlreadySolved = false;
        }
        else if(boardView == boardViewState.NoBoardShownSaveLoad) // SaveLoadScene
        {
            gameSavedLoaded = true;

            updateSoundIcon();

            if(savingGame) {
                backButton.setOnAction((event -> {try {clickedBack = true; goToPuzzleScene(); } catch (IOException e) { throw new RuntimeException(e); }}));

                saveLoadSceneTitle.setText("Save");
                saveLoadSceneSubtitle.setText("Choose a slot to save the game in!");
                saveLoadButton.setText("Save");
            } else {
                backButton.setOnAction((event -> {try { goToMenuScene(); } catch (IOException e) { throw new RuntimeException(e); }}));
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
            gameSavedLoaded = false;

            valueInsertHistory = new ArrayList<>();
            hintInsertHistory = new ArrayList<>();

            updateSoundIcon();

            comboBox.getItems().addAll("Timed Mode", "Death Mode", "Hardcore Mode");

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
        try
        {
            int boardSizeBoxes = Integer.parseInt(boardSizeField.getText()); // number of boxes on each side of the board
            int boxSizeRowsColumns = Integer.parseInt(boxSizeField.getText()); // number of rows or columns on each side of the boxes

            if(boardSizeBoxes != 0 && boxSizeRowsColumns != 0 && ((boardSizeBoxes * boxSizeRowsColumns) <= (boxSizeRowsColumns * boxSizeRowsColumns))) // k*n <= n^2, requirement for being valid
            {
                board = new Board(boardSizeBoxes, boxSizeRowsColumns, false);

                goToPuzzleScene();
            }
            else
            {
                boardSizeField.clear();
                boxSizeField.clear();

                boardSizeValidationField.setText("These are invalid dimensions for Sudoku!");

                playSoundEffect(errorSound, 0.2);
            }
        }
        catch(NumberFormatException exception)
        {
            boardSizeField.clear();
            boxSizeField.clear();

            boardSizeValidationField.setText("These are invalid dimensions for Sudoku!");

            playSoundEffect(errorSound, 0.2);
        }
    }

    /**
     * @author Danny
     */
    public void initializeCustomBoard() throws IOException
    {
        try
        {
            int boardSizeBoxes = Integer.parseInt(boardSizeField.getText()); // number of boxes on each side of the board
            int boxSizeRowsColumns = Integer.parseInt(boxSizeField.getText()); // number of rows or columns on each side of the boxes

            if(boardSizeBoxes != 0 && boxSizeRowsColumns != 0 && ((boardSizeBoxes * boxSizeRowsColumns) <= (boxSizeRowsColumns * boxSizeRowsColumns))) // k*n <= n^2, where k and n > 0, requirement for being valid
            {
                board = new Board(boardSizeBoxes, boxSizeRowsColumns, true);

                goToCustomScene();
            }
            else
            {
                boardSizeField.clear();
                boxSizeField.clear();

                boardSizeValidationField.setText("These are invalid dimensions for Sudoku!");

                playSoundEffect(errorSound, 0.2);
            }
        }
        catch(NumberFormatException exception)
        {
            boardSizeField.clear();
            boxSizeField.clear();

            boardSizeValidationField.setText("These are invalid dimensions for Sudoku!");

            playSoundEffect(errorSound, 0.2);
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
        int cellTextSize = (int) (cellSize / 2);

        for(int row = 0; row < boardSizeRowsColumns; row++)
        {
            for (int column = 0; column < boardSizeRowsColumns; column++)
            {
                // Style the text of the grid cell
                boardGridCells[row][column] = new TextField();
                TextField temp = boardGridCells[row][column];
                temp.setPrefSize(cellSize, cellSize);
                temp.setStyle("-fx-border-width: 1px; " + "-fx-padding: 1px;" + "-fx-border-color: #ffffff; " + "-fx-background-color: #ffffff;" + "-fx-font-size: " + cellTextSize + "px; " + "-fx-font-family: 'Arial'; " + "-fx-control-inner-background:#c0c0c0;" + "-fx-text-fill: #960000;" + "-fx-opacity: 1;");
                temp.setAlignment(Pos.CENTER);
                temp.setFocusTraversable(false);

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

                    if(gameSavedLoaded && valueInsertHistorySaved != null && valueInsertHistorySaved.contains(row+","+column) ){
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

        if(gameSavedLoaded) {
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
     * @author Danny & Abinav
     */
    public void updateActiveTextField(TextField boardGridCell)
    {
        if(activeTextField == null)
        {
            activeTextField = boardGridCell;
        }

        if(activeTextField != boardGridCell)
        {
            userClickedOnNewCell();
        }

        activeTextField = boardGridCell;
    }

    /**
     * @author Danny
     */
    public void userClickedOnNewCell()
    {
        int row = GridPane.getRowIndex(activeTextField);
        int column = GridPane.getColumnIndex(activeTextField);

        if(!activeTextField.getText().isEmpty())
        {
            int value = Integer.parseInt(boardGridCells[row][column].getText());

            insertBoardValue(row, column, value);
        }
        else
        {
            if(board.getBoard()[row][column] != 0)
            {
                board.setBoardValue(row, column, 0);

                valueInsertHistory.remove(activeTextField);
                valueInsertHistorySaved.removeIf(string -> string.equals(row + "," + column));

                if(valueInsertHistorySaved.isEmpty())
                {
                    undoButton.setDisable(true);
                    resetButton.setDisable(true);
                }

                playSoundEffect(removeSound, 0.03);
            }

            updateFilledCells();
        }
    }

    /**
     * @author Danny
     */
    public void userPressedKeyboard(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER) // user pressed Enter and is trying to insert a value
        {
            if(activeTextField == event.getTarget())
            {
                int row = GridPane.getRowIndex(activeTextField);
                int column = GridPane.getColumnIndex(activeTextField);

                if(!activeTextField.getText().isEmpty())
                {
                    int value = Integer.parseInt(boardGridCells[row][column].getText());

                    insertBoardValue(row, column, value);

                    boardGrid.requestFocus(); // un-focus cell
                }
                else if(board.getBoard()[row][column] != 0)
                {
                    board.setBoardValue(row, column, 0);
                    updateFilledCells();

                    valueInsertHistory.remove(activeTextField);
                    valueInsertHistorySaved.removeIf(string -> string.equals(row + "," + column));

                    if(valueInsertHistorySaved.isEmpty())
                    {
                        undoButton.setDisable(true);
                        resetButton.setDisable(true);
                    }

                    playSoundEffect(removeSound, 0.03);
                }
            }
        }
    }

    /**
     * @author Danny
     */
    public void isLastInsertion()
    {
        if((board.getFilledCells() == board.getAvailableCells() - 1) && !activeTextField.getText().isEmpty())
        {
            int row = GridPane.getRowIndex(activeTextField);
            int column = GridPane.getColumnIndex(activeTextField);
            int value = Integer.parseInt(boardGridCells[row][column].getText());

            insertBoardValue(row, column, value);

            boardGrid.requestFocus(); // un-focus cell
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void insertBoardValue(int row, int column, int value)
    {
        if(board.getBoard()[row][column] != value) // if already inserted, skip
        {
            try
            {
                if(board.getBoard()[row][column] != 0)
                {
                    board.setBoardValue(row, column, 0);
                }

                if(board.placeValueInCell(row, column, value))
                {
                    if(!valueInsertHistory.contains(activeTextField) && boardView != boardViewState.CustomBoardShown)
                    {
                        valueInsertHistory.add(activeTextField);
                        valueInsertHistorySaved.add(row+","+column);
                    }

                    feedbackField.setText("");

                    if(undoButton.isDisable())
                    {
                        undoButton.setDisable(false);
                    }

                    if(resetButton.isDisable())
                    {
                        resetButton.setDisable(false);
                    }

                    if(!board.isGameFinished() || boardView == boardViewState.CustomBoardShown)
                    {
                        playSoundEffect(insertSound, 0.43);
                    }
                }
                else
                {
                    boardGridCells[row][column].clear(); // reset cell
                    feedbackField.setText(board.getErrorMessage());
                    playSoundEffect(errorSound, 0.2);
                }

                updateFilledCells();
            }
            catch(NumberFormatException exception)
            {
                feedbackField.setText("Only values from 1-" + board.getMaxPuzzleValue() + " are valid!");

                playSoundEffect(errorSound, 0.2);
            }
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void updateFilledCells()
    {
        filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());

        if(board.isGameFinished() && boardView != boardViewState.CustomBoardShown)
        {
            puzzleHasBeenSolved();
        }
    }

    /**
     * @author Danny
     */
    public void puzzleHasBeenSolved()
    {
        userSolveTimer.stop();

        if(!valueInsertHistory.isEmpty())
        {
            feedbackField.setText("You have solved the Sudoku!");
        }
        else // board was already solved
        {
            feedbackField.setText("The puzzle was already solved!");
        }

        undoButton.setDisable(true);
        hintButton.setDisable(true);
        pauseResumeButton.setDisable(true);

        playSoundEffect(winSound, 0.5);
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
            updateFilledCells();

            boardGridCells[row][column].setDisable(false);
            boardGridCells[row][column].clear();
            boardGridCells[row][column].setPromptText("");

            valueInsertHistory.remove(valueInsertHistory.size() - 1);
            valueInsertHistorySaved.remove(valueInsertHistorySaved.size()-1);

            if(valueInsertHistory.isEmpty())
            {
                undoButton.setDisable(true);
                resetButton.setDisable(true);
            }

            feedbackField.setText("Value insertion undone in cell (" + (row + 1) + ", " + (column + 1) + ")");
        }
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
                    activeTextField.clear();
                    activeTextField.setPromptText(String.valueOf(value));

                    hintInsertHistory.add(activeTextField);
                    hintInsertHistorySaved.add(row+","+column);

                    boardGridCells[row][column].setDisable(true);

                    boardGrid.requestFocus(); // un-focus all cells

                    feedbackField.setText("Solution for cell (" + (row + 1) + "," + (column + 1) + ") revealed!");

                    updateFilledCells();

                    activeTextField = null;

                    if(resetButton.isDisable())
                    {
                        resetButton.setDisable(false);
                    }
                }
            }
            else
            {
                feedbackField.setText("Cannot provide hint due to a wrongly inserted value!");

                playSoundEffect(errorSound, 0.2);
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
    public void undoHintInsertion()
    {
        if(!hintInsertHistory.isEmpty())
        {
            int row = GridPane.getRowIndex(hintInsertHistory.get(hintInsertHistory.size() - 1));
            int column = GridPane.getColumnIndex(hintInsertHistory.get(hintInsertHistory.size() - 1));

            board.setBoardValue(row, column, 0);
            updateFilledCells();

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

        while(hintInsertHistory != null && !hintInsertHistory.isEmpty())
        {
            undoHintInsertion();
        }

        if(boardView == boardViewState.UnsolvedBoardShown)
        {
            userSolvingTime = 0;
            userSolveTimer.start();

            hintButton.setDisable(false);
            pauseResumeButton.setDisable(false);
        }

        feedbackField.setText("The puzzle has been reset!");

        undoButton.setDisable(true);
        resetButton.setDisable(true);

        if(gamePaused)
        {
            pauseResumeGame();
        }
    }

    /**
     * Converts the array into json arry
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
     * Converts the data into json object
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
        jsonNode.set("boardsizeBoxes", objectMapper.convertValue(board.getBoardSizeBoxes(), JsonNode.class));
        jsonNode.set("boxsizeRowsColumn", objectMapper.convertValue(board.getBoxSizeRowsColumns(), JsonNode.class));
        jsonNode.set("board", boardNode);
        jsonNode.set("solvedboard", solvedBoardNode);
        if(valueInsertHistorySaved != null){
            jsonNode.set("userInsertedValues",objectMapper.convertValue(valueInsertHistorySaved, JsonNode.class));
        }else {
            jsonNode.set("userInsertedValues",null);
        }
        if(hintInsertHistorySaved != null){
            jsonNode.set("HintHistory",objectMapper.convertValue(hintInsertHistorySaved, JsonNode.class));
        }else {
            jsonNode.set("HintHistory",null);
        }
        jsonNode.set("filledcells",objectMapper.convertValue(board.getFilledCells(), JsonNode.class));
        jsonNode.put("userTime", userSolvingTime);

        return jsonNode;
    }

    /**
     * Writes the saved gamed state in json file
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
        System.out.println("game saved");
        objectMapper.writeValue(jsonFile, arrayNode);
    }

    /**
     * Shows the boards and date on which they were saved in saveLoad scene
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
                    scrollBar.setStyle("-fx-opacity: 0; -fx-max-width: 0; -fx-pref-width: 0;");

                }
            }
        });
        // styles elements in list
        saveLoadSlotList.setStyle("-fx-border-width: 3px; "
            +"-fx-border-color: ffe767; "
            + "-fx-font-size: 18px; "
            + "-fx-font-family: 'Segoe UI'; "
            + "-fx-control-inner-background:#000000;");
        saveLoadSlotList.setFocusTraversable(false);
    }


    /**
     * @author Abinav
     */
    public void onClickedLoadSave() throws IOException {
        SelectionModel<String> selectionModel = saveLoadSlotList.getSelectionModel();
        int selectedSlot = selectionModel.getSelectedIndex();
        int loadSelectedSlot = selectedSlot + 1;

        if (selectedSlot != -1){
            if (savingGame) {
                saveGame(selectedSlot);
                saveLoadGameSlotView();
                saveLoadSceneSubtitle.setText("Game saved in slot " +loadSelectedSlot);
                pauseTransition.setOnFinished(e ->  saveLoadSceneSubtitle.setText("Choose a slot to save the game in!"));
                pauseTransition.play();
            } else if (!saveLoadSlotList.getItems().get(selectedSlot).equals("Slot " + loadSelectedSlot)) {
                loadGame(selectedSlot);
            }
        }
    }

    /**
     * Retrieves the data stored in json file and sets the current game state to loaded game
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
                    board = new Board(boardSizeBoxesSaved, boxSizeRowsColumnSaved, false);
                    convertJsonArrayIntoArray(boardArraySaved ,true);
                    board.setFilledCells(filledCellsSaved);
                    if(solvedBoardArraySaved != null){
                        convertJsonArrayIntoArray(solvedBoardArraySaved,false);
                        board.getSolver().getSolvedBoard().setFilledCells(board.getAvailableCells());
                    }
                    savedTimeLoaded = userSolvingTimeSaved;
                    gameSavedLoaded = true;
                    goToPuzzleScene();

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the stored json array into 2d array
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
    public void onClickedSave() throws IOException {
        savingGame = true;
        preSaveLoadUserTime = userSolvingTime;
        goToSaveLoadScene();
    }

    /**
     * @author Danny
     */
    public void muteUnmuteSound()
    {
        soundMuted = !soundMuted;

        updateSoundIcon();
    }

    /**
     * @author Danny
     */
    public void updateSoundIcon()
    {
        if(soundMuted)
        {
            soundButtonImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("UI/Media/sound off icon.png")).toExternalForm()));
        }
        else
        {
            soundButtonImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("UI/Media/sound on icon.png")).toExternalForm()));
        }
    }

    /**
     * @author Danny
     */
    public void playSoundEffect(Media soundToPlay, double volume)
    {
        if(!soundMuted)
        {
            MediaPlayer soundPlayer = new MediaPlayer(soundToPlay);
            soundPlayer.setVolume(volume);

            soundPlayer.play();
        }
    }

    /**
     * @author Danny
     */
    public void playButtonClickSound()
    {
        playSoundEffect(clickSound, 0.15);
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void pauseResumeGame()
    {
        feedbackField.setText("");

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
            if(!valueInsertHistory.isEmpty())
            {
                undoButton.setDisable(false);
            }

            hintButton.setDisable(false);
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
     * @author Danny
     */
    public String createSolverFeedbackMessage()
    {
        if(board.getSolver().getSolvedBoard().isGameFinished())
        {
            if(board.getSolver().getSolvedWithHardCoding() && !board.getSolver().getSolvedWithStrategies() && !board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with hard coding!";
            }
            if(!board.getSolver().getSolvedWithHardCoding() && board.getSolver().getSolvedWithStrategies() && !board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with strategies!";
            }
            else if(!board.getSolver().getSolvedWithHardCoding() && !board.getSolver().getSolvedWithStrategies() && board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with backtracking!";
            }
            else if(board.getSolver().getSolvedWithHardCoding() && board.getSolver().getSolvedWithStrategies() && !board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with hard coding and strategies!";
            }
            else if(board.getSolver().getSolvedWithHardCoding() && !board.getSolver().getSolvedWithStrategies() && board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with hard coding and backtracking!";
            }
            else if(!board.getSolver().getSolvedWithHardCoding() && board.getSolver().getSolvedWithStrategies() && board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with strategies and backtracking!";
            }
            else if(board.getSolver().getSolvedWithHardCoding() && board.getSolver().getSolvedWithStrategies() && board.getSolver().getSolvedWithBacktracking())
            {
                return "The puzzle was solved with hard coding, strategies and backtracking!";
            }
            else
            {
                return "The puzzle was already solved!";
            }
        }
        else
        {
            if(!board.getIsSolverCandidate())
            {
                return "The puzzle is too large to determine if it is solvable!";
            }
            else
            {
                return "The puzzle is unsolvable!";
            }
        }
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
     * @author Abinav
     */
    public void goToSaveLoadScene() throws IOException
    {
        boardView = boardViewState.NoBoardShownSaveLoad;
        setActiveScene("SaveLoadScene");
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
    public void goToCustomScene() throws IOException
    {
        boardView = boardViewState.CustomBoardShown;
        setActiveScene("CustomScene");
    }

    /**
     * @author Danny
     */
    public void goToSolverScene() throws IOException
    {
        boardView = boardViewState.SolvedBoardShown;
        setActiveScene("SolverScene");
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
}
