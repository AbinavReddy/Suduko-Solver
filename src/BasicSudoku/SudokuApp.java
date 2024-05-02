package BasicSudoku;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.io.IOException;


public class SudokuApp implements Initializable, ActionListener
{
    private static SudokuBoard board;

    // JavaFX related
    private static Stage appStage;
    private static Scene currentScene;
    @FXML
    private GridPane boardGrid; // puzzle, solver
    private TextField[][] boardGridCells; // puzzle, solver
    @FXML
    private TextField boardSizeField; // menu
    @FXML
    private TextField boxSizeField; // menu
    @FXML
    private Text timeSolvingField; // puzzle, solver
    private final Timer solveTimer = new Timer(1000, this); // puzzle
    private int secondsSolving; // puzzle
    @FXML
    private Text filledCellsField; // puzzle, solver
    @FXML
    private Text errorMessageField; // puzzle

    private enum boardViewState
    {
        NoBoardShown, UnsolvedBoardShown, SolvedBoardShown
    }

    private static boardViewState boardView;

    /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @author Danny
     */
    public void initialize(URL url, ResourceBundle resourceBundle) // needed to inject JavaFX fields
    {
        if(boardView == boardViewState.UnsolvedBoardShown)
        {
            showBoard(true);

            // Start timer to keep track of time elapsed solving the puzzle
            secondsSolving = 0;
            solveTimer.start();

            filledCellsField.setText("Filled cells: " + board.getFilledCells() + "/" + board.getAvailableCells());
            errorMessageField.setText("");
        }
        else if(boardView == boardViewState.SolvedBoardShown)
        {
            showBoard(false);

            filledCellsField.setText("Filled cells: " + board.getSolver().getSolvedBoard().getFilledCells() + "/" + board.getSolver().getSolvedBoard().getAvailableCells());
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void chooseBoardSize() throws IOException
    {
        int boardSize = Integer.parseInt(boardSizeField.getText()); // number of boxes on each side of the board
        int boxSize = Integer.parseInt(boxSizeField.getText()); // number of rows or columns on each side of the boxes

        if(boardSize > 1 && boxSize > 1)
        {
            board = new SudokuBoard(boardSize, boxSize);
        }

        goToPuzzleScene();

        //board.getSolver().emptyCellsDebug(); // for testing (temp)
    }

    /**
     * @author Danny
     */
    public void userPressedKeyboard(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER) // user pressed Enter and is trying to insert a value
        {
            insertValue((Node) event.getTarget());
        }
    }

    /**
     * @author Danny
     */
    public void insertValue(Node boardGridCell)
    {
        int row = GridPane.getRowIndex(boardGridCell);
        int column = GridPane.getColumnIndex(boardGridCell);;
        int value = Integer.parseInt(boardGridCells[row][column].getText());

        if(board.placeValueInCell(row, column, value))
        {
            increaseFilledCells();

            boardGridCell.setDisable(true); // make cell uneditable
            boardGrid.requestFocus(); // un-focus all cells

            errorMessageField.setText("");
        }
        else
        {
            boardGridCells[row][column].clear(); // reset cell

            errorMessageField.setText(board.getErrorMessage());
        }
    }

    /**
     * @author Danny & Abinav
     */
    public void showBoard(boolean unsolved)
    {
        int[][] boardToShow = unsolved ? board.getBoard() : board.getSolver().getSolvedBoard().getBoard();
        int boardSize = board.getBoardSizeRowsColumns();

        int cellTextSize = 40 - ((board.getBoardSizeBoxes() - 3) * 10);
        //System.out.println(cellTextSize);
        boardGridCells = new TextField[boardSize][boardSize];

        for(int row = 0; row < boardSize; row++)
        {
            for (int column = 0; column < boardSize; column++)
            {
                // Style the text of the grid cell
                boardGridCells[row][column] = new TextField();
                TextField temp = boardGridCells[row][column];
                temp.setPrefSize(500, 500);
                temp.setStyle("-fx-border-width: 0px; " + "-fx-padding: 1px;" + "-fx-border-color: #000000; " + "-fx-background-color: #ffffff;" + "-fx-font-size: " + cellTextSize + "px; " + "-fx-font-family: 'Arial'; " + "-fx-control-inner-background:#bc8f8f;" + "-fx-text-fill: #f4a460;" + "-fx-opacity: 1;");
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

        drawBoardGridLines(true);
        drawBoardGridLines(false);
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void drawBoardGridLines(boolean processingRows) // border made in SceneBuilder
    {
        int boardSize = board.getBoardSizeRowsColumns();
        double length;
        double width;

        int substituteA; // variables used to avoid repetitive code
        int substituteB;

        for(int rowOrColumnA = 0; rowOrColumnA < boardSize; rowOrColumnA++)
        {
            for(int rowOrColumnB = 0; rowOrColumnB < boardSize; rowOrColumnB++)
            {
                substituteA = processingRows ? rowOrColumnA : rowOrColumnB;
                substituteB = processingRows ? rowOrColumnB : rowOrColumnA;

                if(rowOrColumnB % board.getBoardSizeBoxes() != 0)
                {
                    length = processingRows ? 1 : (Math.ceil(1000.0 / boardSize)); // 1000 = length and width of UI board (in pixels)
                    width = processingRows ? (Math.ceil(1000.0 / boardSize)) : 1;
                }
                else
                {
                    if(rowOrColumnB == 0 || rowOrColumnB == boardSize - 1)
                    {
                        length = processingRows ? 4 : (Math.ceil(1000.0 / boardSize));
                        width = processingRows ? (Math.ceil(1000.0 / boardSize)) : 4;
                    }
                    else
                    {
                        length = processingRows ? 3 : (Math.ceil(1000.0 / boardSize));
                        width = processingRows ? (Math.ceil(1000.0 / boardSize)) : 3;
                    }
                }

                Rectangle line = new Rectangle();
                line.setHeight(length);
                line.setWidth(width);
                line.setFill(Color.BLACK);

                if(processingRows)
                {
                    boardGrid.add(line, substituteA, substituteB);
                    GridPane.setValignment(line, VPos.TOP); // correct alignment in grid cell

                    if(rowOrColumnB == boardSize - 1)
                    {
                        Rectangle line2 = new Rectangle();

                        length = 4;
                        width = (Math.ceil(1000.0 / boardSize));
                        line2.setHeight(length);
                        line2.setWidth(width);

                        boardGrid.add(line2, substituteA, substituteB);
                        GridPane.setValignment(line2, VPos.BOTTOM);
                    }
                }
                else
                {
                    boardGrid.add(line, substituteA, substituteB);
                    GridPane.setHalignment(line, HPos.LEFT);

                    if(rowOrColumnB == boardSize - 1)
                    {
                        Rectangle line2 = new Rectangle();

                        length = (Math.ceil(1000.0 / boardSize));
                        width = 4;
                        line2.setHeight(length);
                        line2.setWidth(width);

                        boardGrid.add(line2, substituteA, substituteB);
                        GridPane.setHalignment(line2, HPos.RIGHT);
                    }
                }
            }
        }
    }

    /**
     * @author Danny
     */
    public void increaseFilledCells()
    {
        filledCellsField.setText("Filled cells: " + board.getFilledCells() + "/" + board.getAvailableCells());
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

        timeSolvingField.setText("Time solving: " + hoursAsText + ":" + minutesAsText + ":" + secondsAsText);
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
    }

    /**
     * @author Danny
     */
    public void goToSolverScene() throws IOException
    {
        boardView = boardViewState.SolvedBoardShown;

        setActiveScene("SolverScene");

        if(solveTimer.isRunning())
        {
            solveTimer.stop();
        }
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
        appStage.setResizable(true); // disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("UI/sudoku icon.png")))); // add app icon to stage
        appStage.show(); // show window
    }
}
