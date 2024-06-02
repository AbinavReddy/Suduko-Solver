package BasicSudoku;

import javafx.fxml.Initializable;
import java.awt.event.ActionListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
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
import javax.swing.*;
import java.util.Objects;
import java.io.IOException;


public class SudokuApp implements Initializable, ActionListener
{
    private static SudokuBoard board;
    private static List<int[]> valueInsertHistory;

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
    private final Timer solveTimer = new Timer(1000, this); // puzzle
    private int secondsSolving; // puzzle
    @FXML
    private Text filledCellsField; // puzzle, solver, custom
    @FXML
    private Text errorMessageField; // puzzle, custom
    @FXML
    private Text solverFeedbackField; // solver
    @FXML
    private Button hintButton;

    private enum boardViewState
    {
        NoBoardShown, UnsolvedBoardShown, SolvedBoardShown, CustomBoardShown
    }

    private static boardViewState boardView;

    /**
     * @author Danny
     */
    public void initialize(URL url, ResourceBundle resourceBundle) // needed to inject some JavaFX fields
    {
        if(boardView == boardViewState.UnsolvedBoardShown || boardView == boardViewState.CustomBoardShown)
        {
            showBoardValues(true);

            filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());
            errorMessageField.setText("");

            // Start timer to keep track of time elapsed solving (PuzzleScene only)
            if(timeSolvingField != null)
            {
                secondsSolving = 0;
                solveTimer.start();
            }
        }
        else if(boardView == boardViewState.SolvedBoardShown)
        {
            showBoardValues(false);

            filledCellsField.setText("Filled: " + board.getSolver().getSolvedBoard().getFilledCells() + "/" + board.getSolver().getSolvedBoard().getAvailableCells());

            if(board.getSolver().getSolvedWithStrategies() && !board.getSolver().getSolvedWithBacktracking())
            {
                solverFeedbackField.setText("The puzzle was solved with strategies!");
            }
            else if(board.getSolver().getSolvedWithStrategies() && board.getSolver().getSolvedWithBacktracking())
            {
                solverFeedbackField.setText("The puzzle was solved with strategies and backtracking!");
            }
            else
            {
                solverFeedbackField.setText("The puzzle was solved with backtracking!");
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
        double cellWidthLength = Math.ceil(850.0 / boardSizeRowsColumns); // 1000 = length and width the UI board (in pixels)

        boardGridCells = new TextField[boardSizeRowsColumns][boardSizeRowsColumns];
        int cellTextSize = 40 - ((board.getBoardSizeBoxes() - 3) * 10);

        for(int row = 0; row < boardSizeRowsColumns; row++)
        {
            for (int column = 0; column < boardSizeRowsColumns; column++)
            {
                // Style the text of the grid cell
                boardGridCells[row][column] = new TextField();
                TextField temp = boardGridCells[row][column];
                temp.setOnMouseClicked(event -> updateActiveTextField(temp));
                temp.setPrefSize(cellWidthLength, cellWidthLength);
                temp.setStyle("-fx-border-width: 0px; " + "-fx-padding: 1px;" + "-fx-border-color: #000000; " + "-fx-background-color: #ffffff;" + "-fx-font-size: " + cellTextSize + "px; " + "-fx-font-family: 'Arial'; " + "-fx-control-inner-background:#c0c0c0;" + "-fx-text-fill: #960000;" + "-fx-opacity: 1;");
                temp.setAlignment(Pos.CENTER);

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
        double cellWidthLength = Math.ceil(850.0 / boardSizeRowsColumns); // 850 = length and width the UI board (in pixels)
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
                    lengthOfLine = processingRows ? 1 : cellWidthLength;
                    widthOfLine = processingRows ? cellWidthLength : 1;
                }
                else if(rowOrColumnB != 0 && rowOrColumnB != boardSizeRowsColumns - 1) // borders of the boxes
                {
                    lengthOfLine = processingRows ? 3 : cellWidthLength;
                    widthOfLine = processingRows ? cellWidthLength : 3;
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
     * @author Danny
     */
    public void insertBoardValue(Node boardGridCell)
    {
        int row = GridPane.getRowIndex(boardGridCell);
        int column = GridPane.getColumnIndex(boardGridCell);
        int value = Integer.parseInt(boardGridCells[row][column].getText());

        if(board.placeValueInCell(row, column, value))
        {
            boardGridCell.setDisable(true); // make cell uneditable
            boardGrid.requestFocus(); // un-focus all cells

            valueInsertHistory.add(new int[] {row, column});

            updateFilledCells();

            errorMessageField.setText("");
        }
        else
        {
            boardGridCells[row][column].clear(); // reset cell

            errorMessageField.setText(board.getErrorMessage());
        }
    }

    /**
     * @author Danny
     */
    public void updateFilledCells()
    {
        filledCellsField.setText("Filled: " + board.getFilledCells() + "/" + board.getAvailableCells());

        if(board.getFilledCells() == board.getAvailableCells())
        {
            errorMessageField.setText("You have solved the Sudoku!");
        }
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void undoValueInsertion()
    {
        if(!valueInsertHistory.isEmpty())
        {
            int row = valueInsertHistory.get(valueInsertHistory.size() - 1)[0];
            int column = valueInsertHistory.get(valueInsertHistory.size() - 1)[1];

            board.setBoardValue(row, column, 0);
            updateFilledCells();

            boardGridCells[row][column].clear();
            boardGridCells[row][column].setDisable(false);

            valueInsertHistory.remove(valueInsertHistory.size() - 1);

            errorMessageField.setText("Value insertion undone in cell (" + row + ", " + column + ")");
        }
        else
        {
            errorMessageField.setText("There are no insertions to undo!");
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

        errorMessageField.setText("The puzzle has been reset!");
    }

    public void updateActiveTextField(TextField boardGridCell)
    {
        if(activeTextField == null)
        {
            activeTextField = boardGridCell;
        }

        if(activeTextField.getLength() != 0)
        {
            insertBoardValue(activeTextField);
        }

        activeTextField = boardGridCell;
    }

    public void showHint()
    {
        int row = GridPane.getRowIndex(activeTextField);
        int column = GridPane.getColumnIndex(activeTextField);

        if(!boardGridCells[row][column].isDisable())
        {
            board.setBoardValue(row, column, board.getSolver().getSolvedBoard().getBoard()[row][column]);
            updateFilledCells();

            activeTextField.clear();
            activeTextField.setPromptText(String.valueOf(board.getSolver().getSolvedBoard().getBoard()[row][column]));

            boardGridCells[row][column].setDisable(true);

            boardGrid.requestFocus(); // un-focus all cells
        }
        else
        {
            errorMessageField.setText("No cell selected for a hint!");
        }
    }

    /**
     * @author Danny
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) // updates solving timer every second
    {
        secondsSolving++;

        // Get time as strings
        int seconds = secondsSolving % 60;
        int minutes = (secondsSolving / 60) % 60;
        int hours = ((secondsSolving / 60) / 60) % 60;
        String secondsAsText = seconds >= 10 ? String.valueOf(seconds) : "0" + seconds;
        String minutesAsText = minutes >= 10 ? String.valueOf(minutes) : "0" + minutes;
        String hoursAsText = hours >= 10 ? String.valueOf(hours) : "0" + hours;

        timeSolvingField.setText("Time: " + hoursAsText + ":" + minutesAsText + ":" + secondsAsText);
    }

    /**
     * @author Danny
     */
    public void goToMenuScene() throws IOException
    {
        boardView = boardViewState.NoBoardShown;
        setActiveScene("MenuScene");

        if(solveTimer.isRunning())
        {
            solveTimer.stop();
        }
    }

    /**
     * @author Danny
     */
    public void goToPuzzleScene() throws IOException
    {
        boardView = boardViewState.UnsolvedBoardShown;
        setActiveScene("PuzzleScene");

        valueInsertHistory = new ArrayList<>();
    }

    /**
     * @author Danny
     */
    public void goToSolverScene() throws IOException
    {
        if(board.getIsCustomBoard())
        {
            board.solveBoard();
        }

        boardView = boardViewState.SolvedBoardShown;
        setActiveScene("SolverScene");

        if(solveTimer.isRunning())
        {
            solveTimer.stop();
        }
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
    public void setAppStage(Stage stage)
    {
        appStage = stage;
    }

    /**
     * @author Danny & Abinav
     */
    public void setActiveScene(String sceneName) throws IOException
    {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("UI/" + sceneName + ".fxml")));

        if(currentScene == null)
        {
            currentScene = new Scene(root);
        }
        else
        {
            currentScene.setRoot(root);
        }

        appStage.setScene(currentScene); // construct scene
        appStage.setTitle("Sudoku (Group 5)"); // window title
        appStage.setResizable(false); // disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("UI/Media/sudoku icon.png")))); // add app icon to stage
        appStage.show(); // show window
    }
}
