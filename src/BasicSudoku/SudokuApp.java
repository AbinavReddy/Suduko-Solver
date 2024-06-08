package BasicSudoku;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.binding.Bindings;
import javafx.fxml.Initializable;
import java.awt.*;
import java.awt.event.ActionListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
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

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
    private Text confirmationText;

    @FXML
    private Text viewTitle;

    @FXML
    private Button confirm;

    @FXML
    private ListView selectList;

    private boolean gamePaused;

    @FXML
    private Button undoButton; // puzzle
    @FXML
    private Button hintButton; // puzzle

    private List<String> list = new ArrayList<>(Arrays.asList("slot 1", "slot 2", "slot 3", "slot 4", "slot 5"));

    private enum boardViewState
    {
        NoBoardShown, UnsolvedBoardShown, SolvedBoardShown, CustomBoardShown
    }

    private static boardViewState boardView = boardViewState.NoBoardShown;

    /**
     * @author Danny, Abinav & Yahya
     */
    public void initialize(URL url, ResourceBundle resourceBundle) // initializes game scenes and is also needed to inject some JavaFX fields
    {
        if(boardView == boardViewState.UnsolvedBoardShown) // PuzzleScene
        {
            if(board.getIsCustomBoard() && board.getIsSolverCandidate())
            {
                board.solve();
            }

            showBoardValues(true);

            userSolvingTime = 0;
            userSolveTimer.start();

            filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());
            feedbackField.setText("");

            valueInsertHistory = new ArrayList<>();
            hintInsertHistory = new ArrayList<>();
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
            if(board.getIsCustomBoard() && board.getIsSolverCandidate() && !board.getSolver().getSolvedBoard().isGameFinished()) // last condition is to not solve twice
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

            if(board.getSolver().getSolvedBoard().isGameFinished())
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
                else
                {
                    feedbackField.setText("The puzzle is unsolvable!");
                }
            }

            if(userSolveTimer.isRunning())
            {
                userSolveTimer.stop();
            }
        }
        else // MenuScene
        {
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
            for (int column = 0; column < boardSizeRowsColumns; column++)
            {
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
                    temp.setDisable(true);
                }

                boardGrid.add(temp, column, row); // fill the grid with created cells
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
            }

            updateFilledCellsUI();

            feedbackField.setText("");
        }
        else
        {
            valueInsertHistory.remove(boardGridCell);

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


    public ObjectNode convertDataIntoJson( ObjectMapper objectMapper, String position) {

            ObjectNode jsonNode = objectMapper.createObjectNode();

            int[][] boardArray = board.getBoard();
            ArrayNode boardNode = objectMapper.createArrayNode(); // Create an ArrayNode to represent the board in JSON format

            for (int[] row : boardArray) { // formats into json array
                ArrayNode rowNode = objectMapper.createArrayNode();
                for (int value : row) {
                    rowNode.add(value);
                }
                boardNode.add(rowNode);
            }

            // saves following data as json object
            jsonNode.set(position+"board", boardNode);
            jsonNode.put(position+"filledcells", board.getFilledCells());
            jsonNode.put(position+"userTime", userSolvingTime);
            jsonNode.set(position+"boardsizeBoxes", objectMapper.convertValue(board.getBoardSizeBoxes(), JsonNode.class));
            jsonNode.set(position+"slot1 boxsizeRowsColumn", objectMapper.convertValue(board.getBoxSizeRowsColumns(), JsonNode.class));
            jsonNode.set(position+"slot1 board", boardNode);

        return jsonNode;
    }

    public void saveGame(int slotNo) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("saveLoad.json");

        ArrayNode arrayNode;
        if(jsonFile.exists()) {
            JsonNode file = objectMapper.readTree(jsonFile);
            if (file.isArray()) {
                arrayNode = (ArrayNode) file;
            } else {
                arrayNode = objectMapper.createArrayNode();
            }
        } else {
                arrayNode = objectMapper.createArrayNode();
            }

        ObjectNode gameStateAsJson = convertDataIntoJson(objectMapper,Integer.toString(slotNo));
            if (slotNo >=0 && slotNo < 5) arrayNode.insert(slotNo,gameStateAsJson);

        // saving the json obj in specified slot
        objectMapper.writeValue(jsonFile, arrayNode);
        System.out.println("Data saved to saveLoad.json");

    }

    public void loadGame(int slotNo){
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("saveLoad.json");
        try {


            ArrayNode nodes;
            if(jsonFile.exists()) {
                nodes = (ArrayNode) objectMapper.readTree(jsonFile);

                if (slotNo >=0 && slotNo < 5) {
                    JsonNode jsonNode = nodes.get(slotNo);
                    int filledCells = jsonNode.get("filledcells").asInt();
                    int boardSizeBoxesNode = jsonNode.get("boardsizeBoxes").asInt();
                    int boxSizeRowsColumnNode = jsonNode.get("boxsizeRowsColumn").asInt();
                    long userSolvingTime = jsonNode.get("userTime").asLong();
                    int[][] boardArray = objectMapper.convertValue(jsonNode.get("board"), int[][].class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveGameSlotView() throws IOException {
            userSolveTimer.stop();
            boardGrid.requestFocus(); // un-focus all cells
            gamePausedOverlay.setOpacity(0.8);
            confirmationText.setOpacity(0.9);
            confirmationText.setText("Press Confirm to save in the selected slot");
            confirm.setOpacity(0.85);
            viewTitle.setOpacity(0.9);
            viewTitle.setText("Select a slot to save in");



        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("saveLoad.json");

        if (jsonFile.exists()) {
            try {
                // Read the JSON file
                JsonNode jNode = objectMapper.readTree(jsonFile);

                // checks if nodes exist in jsonfile
                if (!(jNode instanceof MissingNode) && jNode.isArray()) {
                    ArrayNode node = (ArrayNode) jNode;

                    if (!node.isEmpty()) {
                        // Iterate through the JSON nodes and populate the list
                        for (JsonNode jsonNode : node) {
                            String boxSizeRC = Integer.toString(jsonNode.get("boxsizeRowsColumn").asInt());
                            String boardSizeB = Integer.toString(jsonNode.get("boardSizeBoxes").asInt());
                            list.add(boxSizeRC + "x" + boardSizeB);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        selectList.getItems().addAll(list);
        selectList.setFixedCellSize(50.0);
        selectList.prefHeightProperty().bind(Bindings.size(selectList.getItems()).multiply(50));

        selectList.setOpacity(0.95);
    }

    public void onPressedConfirm(){
        userSolveTimer.start();
        gamePausedOverlay.setOpacity(0);
        confirmationText.setOpacity(0);
        confirmationText.setText("");
        confirm.setOpacity(0);
        viewTitle.setOpacity(0);
        viewTitle.setText("");
        selectList.setOpacity(0);
    }



    //  shut down the executor service when your application exits
    public void shutdownExecutorService() {
        System.out.println("reached here");
        executorService.shutdown();
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
