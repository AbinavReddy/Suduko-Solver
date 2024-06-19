package Sudoku;

import Sudoku.Enums.*;
import Sudoku.Model.*;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.swing.*;

public class Controller implements Initializable, ActionListener
{
    // App
    private static Stage appStage;
    private static Scene currentScene;

    // Game data
    private static final Game gameModel = new Game();
    private TextField[][] boardGridCells; // puzzle, custom, solver
    private TextField activeTextField; // puzzle
    private static final List<String> saveLoadList = new ArrayList<>(List.of("Slot 1", "Slot 2", "Slot 3", "Slot 4", "Slot 5"));

    // JavaFX elements
    @FXML
    private GridPane boardGrid; // puzzle, custom, solver
    @FXML
    private TextField boardSizeField; // menu
    @FXML
    private TextField boxSizeField; // menu
    @FXML
    private Text boardSizeValidationField; // menu
    @FXML
    private Text timeSolvingField; // puzzle
    @FXML
    private Text filledCellsField; // puzzle, solver, custom
    @FXML
    private Text feedbackField; // puzzle, custom, solver
    @FXML
    private Text pausedField; // puzzle
    @FXML
    private Text gameOverField; // puzzle
    @FXML
    private Text livesRemainingField; // puzzle
    @FXML
    private Text saveLoadSceneSubtitle; // save load
    @FXML
    private Label saveLoadSceneTitle; // save load
    @FXML
    private Button undoButton; // puzzle, custom
    @FXML
    private Button saveButton; // puzzle
    @FXML
    private Button hintButton; // puzzle
    @FXML
    private Button pauseResumeButton; // puzzle
    @FXML
    private Button backButton; // save load
    @FXML
    private Button loadMenuButton; // save load
    @FXML
    private Button saveLoadButton; // save load
    @FXML
    private ListView saveLoadSlotList; // save load
    @FXML
    private ComboBox<String> comboBox; // menu
    @FXML
    private CheckBox solvableOnlyCheckBox; // menu
    @FXML
    private CheckBox unlimitedHintsCheckBox; // menu
    @FXML
    private ImageView soundButtonImage; // all
    @FXML
    private Rectangle gameOverPausedOverlay; // puzzle

    // Sound
    private final Media clickSound = new Media(Objects.requireNonNull(getClass().getResource("View/Media/click sound.wav")).toExternalForm());
    private final Media insertSound = new Media(Objects.requireNonNull(getClass().getResource("View/Media/insert sound.wav")).toExternalForm());
    private final Media removeSound = new Media(Objects.requireNonNull(getClass().getResource("View/Media/remove sound.wav")).toExternalForm());
    private final Media errorSound = new Media(Objects.requireNonNull(getClass().getResource("View/Media/error sound.wav")).toExternalForm());
    private final Media winSound = new Media(Objects.requireNonNull(getClass().getResource("View/Media/win sound.wav")).toExternalForm());
    private final Media loseSound = new Media(Objects.requireNonNull(getClass().getResource("View/Media/lose sound.wav")).toExternalForm());

    // Tools
    private static final Timer userSolveTimer = new Timer(100, null);
    private static final PauseTransition pauseTransition = new PauseTransition(Duration.seconds(3));
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * @author Danny, Abinav & Yahya
     */
    public void initialize(URL url, ResourceBundle resourceBundle) // initializes game scenes and injects JavaFX fields
    {
        Board board = gameModel.getBoard();
        boolean gameSavedLoaded = gameModel.getGameSavedLoaded();

        if(gameModel.getGameScene() == GameScenes.PuzzleScene)
        {
            if(!board.getSolver().getSolverHasRun() && !gameSavedLoaded) // needed to solve custom boards
            {
                board.solve();
            }

            showBoardValues(true);

            if(gameSavedLoaded)
            {
                undoButton.setDisable(false);
            }
            else
            {
                gameModel.setValueInsertHistorySaved(new ArrayList<>());
                gameModel.setHintInsertHistorySaved(new ArrayList<>());

                undoButton.setDisable(true);
            }

            gameModel.setGameSavedLoaded(false);
            gameModel.setSavingGame(false);
            gameModel.setClickedBack(false);

            feedbackField.setText("");

            initializeGameModeSettings();

            updateSoundIcon();
            updateFilledCells();
            boardGrid.requestFocus();

            if(userSolveTimer.getActionListeners().length == 1) // timer has an old listener, remove
            {
                userSolveTimer.removeActionListener(userSolveTimer.getActionListeners()[0]);
            }

            userSolveTimer.addActionListener(this);

            userSolveTimer.start();
        }
        else if(gameModel.getGameScene() == GameScenes.CustomScene)
        {
            showBoardValues(true);

            gameModel.setValueInsertHistory(new ArrayList<>());
            gameModel.setHintInsertHistory(new ArrayList<>());

            updateSoundIcon();
            updateFilledCells();
            feedbackField.setText("");
            boardGrid.requestFocus();
        }
        else if(gameModel.getGameScene() == GameScenes.SolverScene)
        {
            if(!board.getSolver().getSolverHasRun() && !gameSavedLoaded) // needed to solve custom boards
            {
                board.solve();
            }

            showBoardValues(false);

            boardGrid.setDisable(true); // if there are empty cells, the user should not be able to edit them

            timeSolvingField.setText(formatTime(board.getSolver().getSolvingTime()));

            updateSoundIcon();
            filledCellsField.setText("Filled: " + board.getSolver().getSolvedBoard().getFilledCells() + "/" + board.getSolver().getSolvedBoard().getAvailableCells());
            feedbackField.setText(createSolverFeedbackMessage());
        }
        else if(gameModel.getGameScene() == GameScenes.SaveLoadScene)
        {
            if(gameModel.getSavingGame())
            {
                backButton.setOnAction((event -> {try {gameModel.setClickedBack(true); goToPuzzleScene(); } catch (IOException e) { throw new RuntimeException(e);}}));

                saveLoadSceneTitle.setText("Save");
                saveLoadSceneSubtitle.setText("Choose a slot to save the game in!");
                saveLoadButton.setText("Save");
            }
            else
            {
                backButton.setOnAction((event -> {try {goToMenuScene(); } catch (IOException e) { throw new RuntimeException(e);}}));
            }

            saveLoadSlotList.getItems().addAll(saveLoadList);
            try {saveLoadGameSlotView(); } catch (IOException e) {throw new RuntimeException(e);}

            gameModel.setGameSavedLoaded(true);

            updateSoundIcon();
        }
        else // MenuScene
        {
            GameModes gameMode = gameModel.getGameMode();

            if(gameMode == GameModes.NormalMode)
            {
                comboBox.setPromptText("Normal Mode");
            }
            else if(gameMode == GameModes.TimedMode)
            {
                comboBox.setPromptText("Timed Mode");
            }
            else if(gameMode == GameModes.DeathMode)
            {
                comboBox.setPromptText("Death Mode");
            }
            else
            {
                comboBox.setPromptText("Hardcore Mode");
            }

            comboBox.getItems().addAll("Normal Mode", "Timed Mode", "Death Mode", "Hardcore Mode");
            comboBox.setOnAction( event -> {try {handleModeSelected(); } catch (IOException e) {throw new RuntimeException(e);}});

            if(gameModel.getSolvableOnly())
            {
                solvableOnlyCheckBox.setSelected(true);
            }

            if(gameModel.getUnlimitedHints())
            {
                unlimitedHintsCheckBox.setSelected(true);
            }

            gameModel.setValueInsertHistory(new ArrayList<>());
            gameModel.setHintInsertHistory(new ArrayList<>());

            gameModel.setGameSavedLoaded(false);
            gameModel.setSavingGame(false);
            gameModel.setClickedBack(false);

            if(userSolveTimer.isRunning())
            {
                userSolveTimer.stop();
            }

            updateSoundIcon();
        }
    }

    /**
     * @author  Abinav & Danny
     */
    private void handleModeSelected() throws IOException {
        String selectedItem = comboBox.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            switch (selectedItem) {
                case "Timed Mode" ->
                {
                    gameModel.setGameMode(GameModes.TimedMode);
                    loadMenuButton.setDisable(true);
                    unlimitedHintsCheckBox.setDisable(true);
                }

                case "Death Mode" ->
                {
                    gameModel.setGameMode(GameModes.DeathMode);
                    loadMenuButton.setDisable(true);
                    unlimitedHintsCheckBox.setDisable(true);
                }


                case "Hardcore Mode" ->
                {
                    gameModel.setGameMode(GameModes.HardcoreMode);
                    loadMenuButton.setDisable(true);
                    unlimitedHintsCheckBox.setDisable(true);
                }

                default ->
                {
                    gameModel.setGameMode(GameModes.NormalMode);
                    loadMenuButton.setDisable(false);
                    unlimitedHintsCheckBox.setDisable(false);
                }
            }
        }
    }

    /**
     * @author  Abinav, Danny & Yahya
     */
    private void initializeGameModeSettings() {
        GameModes gameMode = gameModel.getGameMode();

        if(gameMode == GameModes.HardcoreMode) { // Timer countdown mode + mode allowing limited mistakes based on board size

            hintButton.setDisable(true);
            undoButton.setDisable(true);
            saveButton.setDisable(true);
            gameModel.setLives(calculateLivesBasedOnBoardSize());
            livesRemainingField.setVisible(true);
            livesRemainingField.setText("Lives: " + gameModel.getLives());
            gameModel.setUserSolveTime(calculateUserSolvingTime());
            feedbackField.setText("Welcome to Hardcore Mode!");
            pauseTransition.setOnFinished(e ->  feedbackField.setText("Solve the puzzle before time and/or lives are depleted!"));
            pauseTransition.play();

        } else if(gameMode == GameModes.DeathMode) { // Mode allowing limited mistakes based on board size

            gameModel.setLives(calculateLivesBasedOnBoardSize());
            livesRemainingField.setVisible(true);
            livesRemainingField.setText("Lives: " + gameModel.getLives());
            gameModel.setUserSolveTime(0);
            hintButton.setDisable(true);
            saveButton.setDisable(true);
            feedbackField.setText("Welcome to Death Mode!");
            pauseTransition.setOnFinished(e ->  feedbackField.setText("Solve the puzzle before running out of lives!"));
            pauseTransition.play();

        } else if (gameMode == GameModes.TimedMode) { // Timer countdown mode

            gameModel.setUserSolveTime(calculateUserSolvingTime());
            livesRemainingField.setVisible(false);
            hintButton.setDisable(true);
            saveButton.setDisable(true);
            feedbackField.setText("Welcome to Timed Mode!");
            pauseTransition.setOnFinished(e ->  feedbackField.setText("Solve the puzzle before time runs out!"));
            pauseTransition.play();

        } else {
            gameModel.setUserSolveTime(gameModel.getClickedBack() ? gameModel.getPreSaveLoadUserTime() : (gameModel.getGameSavedLoaded() ? gameModel.getSavedTimeLoaded() : 0));
            gameModel.setHintsAvailable(calculateHintsAvailable());
            livesRemainingField.setVisible(false);
        }
    }

    /**
     * @author  Danny
     */
    private int calculateHintsAvailable()
    {
        Board board = gameModel.getBoard();

        return !gameModel.getUnlimitedHints() ? (int) (Math.ceil((board.getAvailableCells() - board.getFilledCells()) * 0.13)) : 0;
    }

    /**
     * @author  Danny, Abinav & Yahya
     */
    private long calculateUserSolvingTime() {
        Board board = gameModel.getBoard();

        return board.getBoardSizeRowsColumns() != 1 ? (long) ((Math.ceil(((board.getBoardSizeRowsColumns() * board.getBoardSizeRowsColumns()) / 81.0) * 10.0)) * 60000) : 10000;
    }

    /**
     * @author  Danny, Abinav & Yahya
     */
    private int calculateLivesBasedOnBoardSize() {
        Board board = gameModel.getBoard();

        return (int) (Math.ceil(((board.getBoardSizeRowsColumns() / 2.0)) * ((1 - ((double) board.getFilledCells() / board.getAvailableCells())) / 0.63)));
    }

    /**
     * @author Abinav
     */
    public void checkAndUpdateLivesRemaining(){
        int lives = gameModel.getLives();
        GameModes gameMode = gameModel.getGameMode();

        if (lives > 0 && (gameMode == GameModes.DeathMode || gameMode == GameModes.HardcoreMode)) {
            gameModel.setLives(lives - 1);
            livesRemainingField.setText("Lives: " + gameModel.getLives());
            livesRemainingField.setStyle("-fx-fill: red;");
            pauseTransition.setDuration(Duration.seconds(1));
            pauseTransition.setOnFinished(e -> {
                if(gameModel.getLives() > 0) livesRemainingField.setStyle("-fx-fill: white;"); // need to get lives again since lambda needs final variable
            }) ;
            pauseTransition.play();
            pauseTransition.setDuration(Duration.seconds(3)); // resetting to back to original
        }
        if (gameModel.getLives() == 0 && (gameMode == GameModes.DeathMode || gameMode == GameModes.HardcoreMode)) {
            userSolveTimer.stop();
            undoButton.setDisable(true);
            hintButton.setDisable(true);
            pauseResumeButton.setDisable(true);
            livesRemainingField.setText("Lives: " + gameModel.getLives());
            livesRemainingField.setStyle("-fx-fill: red;");
            boardGrid.setDisable(true);
            gameOverState(true);

            if(gameMode == GameModes.DeathMode || gameModel.getUserSolveTime() != 0)
            {
                feedbackField.setText("You have run out of lives!");
            }
            else
            {
                feedbackField.setText("You have run out of time and lives!" );
            }

            playSoundEffect(loseSound, 0.5);
        }
    }

    /**
     * @author Abinav
     */
    private void updateTimeOnInsert() {
        long userSolveTime = gameModel.getUserSolveTime();
        GameModes gameMode = gameModel.getGameMode();

        if(gameMode == GameModes.TimedMode || gameMode == GameModes.HardcoreMode) {
            if(userSolveTime < 15000){
                gameModel.setUserSolveTime(0);
            } else {
                gameModel.setUserSolveTime(userSolveTime - 15000);
                timeSolvingField.setStyle("-fx-fill: red;");
                pauseTransition.setDuration(Duration.seconds(1));
                pauseTransition.setOnFinished(e -> timeSolvingField.setStyle("-fx-fill: white;"));
                pauseTransition.play();
                pauseTransition.setDuration(Duration.seconds(3)); // resetting to back to original
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
                gameModel.setSolvableOnly(solvableOnlyCheckBox.isSelected());
                gameModel.setUnlimitedHints(unlimitedHintsCheckBox.isSelected());

                gameModel.setBoard(new Board(boardSizeBoxes, boxSizeRowsColumns, gameModel.getSolvableOnly(), false));

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
                gameModel.setUnlimitedHints(unlimitedHintsCheckBox.isSelected());

                gameModel.setBoard(new Board(boardSizeBoxes, boxSizeRowsColumns, false, true)); // can't force solvable on custom boards

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
        Board board = gameModel.getBoard();
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
        List<Node> hintInsertHistory = gameModel.getHintInsertHistory();
        List<String> valueInsertHistorySaved = gameModel.getValueInsertHistorySaved();
        List<String> hintInsertHistorySaved = gameModel.getHintInsertHistorySaved();

        int[][] boardToShow = unsolved ? board.getBoard() : board.getSolver().getSolvedBoard().getBoard();
        int boardSizeRowsColumns = board.getBoardSizeRowsColumns();
        double cellSize = Math.ceil(850.0 / boardSizeRowsColumns); // 850 = length and width the View board (in pixels)

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
                temp.setOnMouseMoved(event -> isLastInsertion()); // needed to auto insert last insertion

                // Fill cell
                if(boardToShow[row][column] == 0)
                {
                    temp.setPromptText("");
                    temp.setEditable(true);
                }
                else
                {
                    temp.setPromptText(String.valueOf(boardToShow[row][column]));

                    if(gameModel.getGameSavedLoaded() && valueInsertHistorySaved != null && valueInsertHistorySaved.contains(row+","+column) ){
                        temp.setDisable(false);
                        temp.setEditable(true);
                        temp.setStyle("-fx-border-width: 1px; "
                            + "-fx-padding: 1px;"
                            + "-fx-border-color: #ffffff; "
                            + "-fx-background-color: #ffffff;"
                            + "-fx-font-size: " + cellTextSize + "px; "
                            + "-fx-font-family: 'Arial'; "
                            + "-fx-control-inner-background:#d30202;"
                            + "-fx-text-fill: #960000;"
                            + "-fx-opacity: 1;");

                    } else {
                        temp.setDisable(true);
                    }
                }

                boardGrid.add(temp, column, row); // fill the grid with created cells
            }

        }

        if(gameModel.getGameSavedLoaded()) {
            if (valueInsertHistorySaved != null) {
                for (String cell : valueInsertHistorySaved) {
                    String[] cellRowColumn = cell.split(",");
                    int cellsRow = Integer.parseInt(cellRowColumn[0]);
                    int cellsColumn = Integer.parseInt(cellRowColumn[1]);
                    valueInsertHistory.add(boardGridCells[cellsRow][cellsColumn]);
                }
            }  if (hintInsertHistorySaved != null) {
                for (String cell : hintInsertHistorySaved) {
                    String[] cellRowColumn = cell.split(",");
                    int cellsRow = Integer.parseInt(cellRowColumn[0]);
                    int cellsColumn = Integer.parseInt(cellRowColumn[1]);
                    hintInsertHistory.add(boardGridCells[cellsRow][cellsColumn]);
                }
            }
        }

        drawBoardLines(true);
        drawBoardLines(false);
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void drawBoardLines(boolean processingRows) // board borders are done in SceneBuilder
    {
        Board board = gameModel.getBoard();

        int boardSizeRowsColumns = board.getBoardSizeRowsColumns();
        int boxSizeRowsColumns = board.getBoxSizeRowsColumns();
        double cellSize = Math.ceil(850.0 / boardSizeRowsColumns); // 850 = length and width the View board (in pixels)
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
        Board board = gameModel.getBoard();
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
        List<String> valueInsertHistorySaved = gameModel.getValueInsertHistorySaved();

        int row = GridPane.getRowIndex(activeTextField);
        int column = GridPane.getColumnIndex(activeTextField);

        try
        {
            if(!activeTextField.getText().isEmpty())
            {
                int value = Integer.parseInt(boardGridCells[row][column].getText());

                if(value == 0)
                {
                    throw new IllegalArgumentException();
                }

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
                    }

                    playSoundEffect(removeSound, 0.03);
                }

                updateFilledCells();
            }
        }
        catch(NumberFormatException e)
        {
            boardGridCells[row][column].clear(); // reset cell
            feedbackField.setText("Non-numeric characters are illegal!");
            playSoundEffect(errorSound, 0.2);
        }
        catch(IllegalArgumentException e)
        {
            boardGridCells[row][column].clear(); // reset cell
            feedbackField.setText("Only values from 1-" + gameModel.getBoard().getMaxPuzzleValue() + " are valid!");
            playSoundEffect(errorSound, 0.2);
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
                Board board = gameModel.getBoard();
                List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
                List<String> valueInsertHistorySaved = gameModel.getValueInsertHistorySaved();

                int row = GridPane.getRowIndex(activeTextField);
                int column = GridPane.getColumnIndex(activeTextField);

                try
                {
                    if(!activeTextField.getText().isEmpty())
                    {
                        int value = Integer.parseInt(boardGridCells[row][column].getText());

                        if(value == 0)
                        {
                            throw new IllegalArgumentException();
                        }

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
                        }

                        playSoundEffect(removeSound, 0.03);
                    }
                }
                catch(NumberFormatException e)
                {
                    boardGridCells[row][column].clear(); // reset cell
                    feedbackField.setText("Non-numeric characters are illegal!");
                    playSoundEffect(errorSound, 0.2);
                }
                catch(IllegalArgumentException e)
                {
                    boardGridCells[row][column].clear(); // reset cell
                    feedbackField.setText("Only values from 1-" + gameModel.getBoard().getMaxPuzzleValue() + " are valid!");
                    playSoundEffect(errorSound, 0.2);
                }
            }
        }
    }

    /**
     * @author Danny
     */
    public void isLastInsertion()
    {
        Board board = gameModel.getBoard();

        if((board.getFilledCells() == board.getAvailableCells() - 1) && activeTextField != null && !activeTextField.getText().isEmpty())
        {
            int row = GridPane.getRowIndex(activeTextField);
            int column = GridPane.getColumnIndex(activeTextField);

            try
            {
                int value = Integer.parseInt(boardGridCells[row][column].getText());

                if(value == 0)
                {
                    throw new IllegalArgumentException();
                }

                insertBoardValue(row, column, value);

                boardGrid.requestFocus(); // un-focus cell
            }
            catch(NumberFormatException e)
            {
                boardGridCells[row][column].clear(); // reset cell
                feedbackField.setText("Non-numeric characters are illegal!");
                playSoundEffect(errorSound, 0.2);
            }
            catch(IllegalArgumentException e)
            {
                boardGridCells[row][column].clear(); // reset cell
                feedbackField.setText("Only values from 1-" + gameModel.getBoard().getMaxPuzzleValue() + " are valid!");
                playSoundEffect(errorSound, 0.2);
            }
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void insertBoardValue(int row, int column, int value)
    {
        Board board = gameModel.getBoard();
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
        List<String> valueInsertHistorySaved = gameModel.getValueInsertHistorySaved();
        GameScenes boardView = gameModel.getGameScene();
        GameModes gameMode = gameModel.getGameMode();

        if(board.getBoard()[row][column] != value) // if already inserted, skip
        {
            if(board.getBoard()[row][column] != 0)
            {
                board.setBoardValue(row, column, 0);
            }

            if(board.placeValueInCell(row, column, value))
            {
                if(!valueInsertHistory.contains(activeTextField) && boardView != GameScenes.CustomScene)
                {
                    valueInsertHistory.add(activeTextField);
                    valueInsertHistorySaved.add(row+","+column);
                }

                feedbackField.setText("");

                if(undoButton.isDisable() && gameMode != GameModes.HardcoreMode)
                {
                    undoButton.setDisable(false);
                }

                if(!board.isGameFinished() || boardView == GameScenes.CustomScene)
                {
                    playSoundEffect(insertSound, 0.43);
                }
            }
            else
            {
                boardGridCells[row][column].clear(); // reset cell
                feedbackField.setText(board.getErrorMessage());
                checkAndUpdateLivesRemaining();
                updateTimeOnInsert();
                playSoundEffect(errorSound, 0.2);

            }

            updateFilledCells();
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void updateFilledCells()
    {
        Board board = gameModel.getBoard();
        GameScenes boardView = gameModel.getGameScene();

        filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());

        if(board.isGameFinished() && boardView != GameScenes.CustomScene)
        {
            puzzleHasBeenSolved();
        }
    }

    /**
     * @author Danny
     */
    public void puzzleHasBeenSolved()
    {
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();

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
        Board board = gameModel.getBoard();
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
        List<String> valueInsertHistorySaved = gameModel.getValueInsertHistorySaved();

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
            }

            feedbackField.setText("Value insertion undone in cell (" + (row + 1) + ", " + (column + 1) + ")");
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void showHint()
    {
        int hintsAvailable = gameModel.getHintsAvailable();

        if(hintsAvailable > 0 || gameModel.getUnlimitedHints())
        {
            if(activeTextField != null)
            {
                Board board = gameModel.getBoard();
                List<Node> hintInsertHistory = gameModel.getValueInsertHistory();
                List<String> hintInsertHistorySaved = gameModel.getValueInsertHistorySaved();

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

                        updateFilledCells();

                        gameModel.setHintsAvailable(hintsAvailable - 1);

                        feedbackField.setText("Solution for cell (" + (row + 1) + "," + (column + 1) + ") revealed!");

                        if(!gameModel.getUnlimitedHints())
                        {
                            pauseTransition.setDuration(Duration.seconds(3));
                            pauseTransition.setOnFinished(e ->  feedbackField.setText(gameModel.getHintsAvailable() + " hints left!"));
                            pauseTransition.play();
                        }

                        activeTextField = null;
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
        else
        {
            feedbackField.setText("No more hints available!");
        }

        boardGrid.requestFocus(); // un-focus all cells
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void undoHintInsertion()
    {
        Board board = gameModel.getBoard();
        List<Node> hintInsertHistory = gameModel.getValueInsertHistory();
        List<String> hintInsertHistorySaved = gameModel.getValueInsertHistorySaved();

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
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
        List<Node> hintInsertHistory = gameModel.getValueInsertHistory();
        GameScenes boardView = gameModel.getGameScene();
        GameModes gameMode = gameModel.getGameMode();

        while(!valueInsertHistory.isEmpty())
        {
            undoValueInsertion();
        }

        while(hintInsertHistory != null && !hintInsertHistory.isEmpty())
        {
            undoHintInsertion();
        }

        if(boardView == GameScenes.PuzzleScene)
        {
            if(gameModel.getGameOver())
            {
                gameOverState(false);
            }

            gameModel.setUserSolveTime(gameMode == GameModes.TimedMode || gameMode == GameModes.HardcoreMode? calculateUserSolvingTime(): 0);
            userSolveTimer.start();

            gameModel.setHintsAvailable(calculateHintsAvailable());

            if(gameMode == GameModes.DeathMode || gameMode == GameModes.HardcoreMode){
                gameModel.setLives(calculateLivesBasedOnBoardSize());
                livesRemainingField.setText("Lives: "+ gameModel.getLives());
            }

            if(boardGrid.isDisable())
            {
                boardGrid.setDisable(false);
            }

            if(gameMode == GameModes.DeathMode || gameMode == GameModes.HardcoreMode || gameMode == GameModes.TimedMode) {
                timeSolvingField.setStyle("-fx-fill: white;");
                livesRemainingField.setStyle("-fx-fill: white;");
                hintButton.setDisable(true);
            } else {
                hintButton.setDisable(false);
            }
            pauseResumeButton.setDisable(false);
        }

        feedbackField.setText("The puzzle has been reset!");

        undoButton.setDisable(true);

        if(gameModel.getGamePaused())
        {
            pauseResumeGame();
        }
    }

    /**
     * @author Danny
     */
    public void playSoundEffect(Media soundToPlay, double volume)
    {
        if(!gameModel.getSoundMuted())
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
     * @author Danny
     */

    public void muteUnmuteSound()
    {
        gameModel.setSoundMuted(!gameModel.getSoundMuted());

        updateSoundIcon();
    }

    /**
     * @author Danny
     */
    public void updateSoundIcon()
    {
        if(gameModel.getSoundMuted())
        {
            soundButtonImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("View/Media/sound off icon.png")).toExternalForm()));
        }
        else
        {
            soundButtonImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("View/Media/sound on icon.png")).toExternalForm()));
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) // updates user solving timer every 100 milliseconds
    {
        long userSolveTime = gameModel.getUserSolveTime();

        if(userSolveTime <= 0 && (gameModel.getGameMode() == GameModes.TimedMode || gameModel.getGameMode() == GameModes.HardcoreMode)){
            userSolveTimer.stop();
            undoButton.setDisable(true);
            hintButton.setDisable(true);
            pauseResumeButton.setDisable(true);
            boardGrid.setDisable(true);
            gameOverState(true);
            feedbackField.setText("You have run out of time!");
            timeSolvingField.setStyle("-fx-fill: red;");
            playSoundEffect(loseSound, 0.5);

        } else if (gameModel.getGameMode() == GameModes.TimedMode || gameModel.getGameMode() == GameModes.HardcoreMode) {
            gameModel.setUserSolveTime(userSolveTime - 100);
        } else {
            gameModel.setUserSolveTime(userSolveTime + 100);
        }

        // Display the time used by the Solver to solve the puzzle
        timeSolvingField.setText(formatTime(userSolveTime));
    }

    /**
     * @author Danny
     */
    private String formatTime(long milliseconds)
    {
        int totalSeconds = (int) (milliseconds / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = ((totalSeconds / 60) / 60) % 60;
        String secondsAsText = (seconds >= 10 ? String.valueOf(seconds) : "0" + seconds) + "." + (String.valueOf((long) (((milliseconds / 1000.0) - Math.floor(milliseconds / 1000.0)) * 1000)).charAt(0));
        String minutesAsText = minutes >= 10 ? String.valueOf(minutes) : "0" + minutes;
        String hoursAsText = hours >= 10 ? String.valueOf(hours) : "0" + hours;

        return "Time: " + hoursAsText + ":" + minutesAsText + ":" + secondsAsText;
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void pauseResumeGame()
    {
        List<Node> valueInsertHistory = gameModel.getValueInsertHistory();
        boolean gamePaused = gameModel.getGamePaused();

        feedbackField.setText("");

        if(!gamePaused)
        {
            userSolveTimer.stop();

            boardGrid.requestFocus(); // un-focus all cells

            gameOverPausedOverlay.setOpacity(0.8);
            pausedField.setOpacity(1.0);

            gameModel.setGamePaused(true);

            pauseResumeButton.setText("Resume");

            // Disable buttons
            undoButton.setDisable(true);
            hintButton.setDisable(true);
        }
        else
        {
            userSolveTimer.start();

            gameOverPausedOverlay.setOpacity(0);
            pausedField.setOpacity(0);

            gameModel.setGamePaused(false);

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
     * @author Danny
     */
    public void gameOverState(boolean isGameOver)
    {
        if(isGameOver)
        {
            gameOverPausedOverlay.setOpacity(0.8);
            gameOverField.setOpacity(1.0);
        }
        else
        {
            gameOverPausedOverlay.setOpacity(0.0);
            gameOverField.setOpacity(0.0);
        }

        gameModel.setGameOver(isGameOver);
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
    public ObjectNode convertDataIntoJson(ObjectMapper objectMapper) {
        Board board = gameModel.getBoard();
        List<String> valueInsertHistorySaved = gameModel.getValueInsertHistorySaved();
        List<String> hintInsertHistorySaved = gameModel.getHintInsertHistorySaved();

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
        jsonNode.put("userTime", gameModel.getUserSolveTime());

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
                    scrollBar.setStyle("-fx-opacity: 0; -fx-max-width: 0; -fx-pref-width: 0;");

                }
            }
        });
        // styles elements in list
        saveLoadSlotList.setStyle("-fx-border-width: 3px; "
            +"-fx-border-color: ffe767; "
            + "-fx-font-size: 18px; "
            + "-fx-font-family: 'Segoe View'; "
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
            if (gameModel.getSavingGame()) {
                saveGame(selectedSlot);
                saveLoadGameSlotView();
                saveLoadSceneSubtitle.setText("Game saved in slot " +loadSelectedSlot);
                pauseTransition.setOnFinished(e ->  saveLoadSceneSubtitle.setText("Choose a slot to save the game in!"));
                pauseTransition.play();
            } else if (!saveLoadSlotList.getItems().get(selectedSlot).equals("Slot " + loadSelectedSlot)) {
                loadGame(selectedSlot);
            } else if(saveLoadSlotList.getItems().get(selectedSlot).equals("Slot " + loadSelectedSlot)) {
                saveLoadSceneSubtitle.setText("Choose a saved board to load!");
            }
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
                        gameModel.setValueInsertHistorySaved(insertedValuesOnBoardSaved);
                    }
                    if(!jsonNode.get("HintHistory").isNull() && jsonNode.get("HintHistory").isArray()) {
                        List<String> hintValuesOnBoardSaved = new ArrayList<>();
                        for (JsonNode node : jsonNode.get("HintHistory")) {
                            hintValuesOnBoardSaved.add(node.asText());
                        }
                        gameModel.setHintInsertHistorySaved(hintValuesOnBoardSaved);
                    }
                    gameModel.setBoard(new Board(boardSizeBoxesSaved, boxSizeRowsColumnSaved, false, false));
                    convertJsonArrayIntoArray(boardArraySaved ,true);

                    Board board = gameModel.getBoard();
                    board.setFilledCells(filledCellsSaved);
                    if(solvedBoardArraySaved != null){
                        convertJsonArrayIntoArray(solvedBoardArraySaved,false);
                        board.getSolver().getSolvedBoard().setFilledCells(board.getAvailableCells());
                    }
                    gameModel.setSavedTimeLoaded(userSolvingTimeSaved);
                    gameModel.setGameSavedLoaded(true);

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
        Board board = gameModel.getBoard();

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
        gameModel.setSavingGame(true);
        gameModel.setPreSaveLoadUserTime(gameModel.getUserSolveTime());

        goToSaveLoadScene();
    }

    /**
     * @author Danny
     */
    public String createSolverFeedbackMessage()
    {
        Board board = gameModel.getBoard();

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
        gameModel.setGameScene(GameScenes.MenuScene);
        setActiveScene("MenuScene");
    }

    /**
     * @author Danny
     */
    public void goToPuzzleScene() throws IOException
    {
        gameModel.setGameScene(GameScenes.PuzzleScene);
        setActiveScene("PuzzleScene");
    }

    /**
     * @author Danny
     */
    public void goToCustomScene() throws IOException
    {
        gameModel.setGameScene(GameScenes.CustomScene);
        setActiveScene("CustomScene");
    }

    /**
     * @author Danny
     */
    public void goToSolverScene() throws IOException
    {
        gameModel.setGameScene(GameScenes.SolverScene);
        setActiveScene("SolverScene");
    }

    /**
     * @author Abinav
     */
    public void goToSaveLoadScene() throws IOException
    {
        gameModel.setGameScene(GameScenes.SaveLoadScene);
        setActiveScene("SaveLoadScene");
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    private void scaleScreen()
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
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("View/Scenes/" + sceneName + ".fxml")));

        if(currentScene != null)
        {
            currentScene.setRoot(root);
        }
        else
        {
            currentScene = new Scene(root);
        }

        currentScene.widthProperty().addListener((observable) -> scaleScreen()); // used for resizing View
        currentScene.heightProperty().addListener((observable) -> scaleScreen());

        scaleScreen();

        appStage.setScene(currentScene); // construct scene
        appStage.setTitle("Sudoku (Group 5)"); // window title
        appStage.setResizable(true); // disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("View/Media/sudoku icon.png")))); // add app icon to stage
        appStage.show(); // show window
    }
}
