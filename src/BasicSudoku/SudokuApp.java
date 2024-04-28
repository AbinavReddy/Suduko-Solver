package BasicSudoku;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import java.util.Objects;
import java.io.IOException;

public class SudokuApp implements Initializable
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

    private enum boardViewState
    {
        NoBoardShown, UnsolvedBoardShown, SolvedBoardShown
    }
    private static boardViewState boardView;

    /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @author Danny
     */
    public void initialize(URL url, ResourceBundle resourceBundle) // needed to inject boardGrid
    {
        if(boardView == boardViewState.UnsolvedBoardShown)
        {
            showBoard(true);
        }
        else if(boardView == boardViewState.SolvedBoardShown)
        {
            showBoard(false);
        }
    }
    /**
     * @author Abinav & Danny

     */
    public void chooseBoardSize() throws IOException
    {
        int wantedSize = Integer.parseInt(boardSizeField.getText());

        if(wantedSize > 1)
        {
            board = new SudokuBoard(wantedSize);
        }

        playSudoku();
    }

    public void playSudoku() throws IOException
    {
        goToPuzzleScene();
    }

    /**
     * @author Danny & Abinav
     */
    public void showBoard(boolean unsolved)
    {
        int[][] boardToShow = unsolved ? board.getBoard() : board.getSolver().board.getBoard();
        int boardSize = board.getBoardSize();

        boardGridCells = new TextField[boardSize][boardSize];

        for(int row = 0; row < boardSize; row++)
        {
            for (int column = 0; column < boardSize; column++)
            {
                // Style the text of the grid cell
                boardGridCells[row][column] = new TextField();
                TextField temp = boardGridCells[row][column];
                temp.setPrefSize(100, 100);
                temp.setStyle("-fx-border-width: 1px; " + "-fx-padding: 5px;" + "-fx-border-color: #000000; " + "-fx-background-color: #ffffff;" + "-fx-font-size: 36px; " + "-fx-font-family: 'Arial'; " + "-fx-control-inner-background:#bc8f8f;" + "-fx-text-fill: #f4a460;" + "-fx-opacity: 1;");
                temp.setAlignment(Pos.CENTER);

                // Fill the grid cell
                if(boardToShow[row][column] == 0)
                {
                    temp.setPromptText(""); // empty cell-fx-opacity
                    temp.setEditable(true);
                }
                else
                {
                    temp.setPromptText(String.valueOf(boardToShow[row][column])); // fill grid with values from board (unsolved or solved)
                    temp.setDisable(true);
                }

                boardGrid.add(temp, column, row);
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

        appStage.setScene(currentScene); // Construct scene
        appStage.setTitle("Sudoku"); // Window title
        appStage.setResizable(true); // Disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("UI/sudoku icon.png")))); // Add app icon to stage
        appStage.show(); // Show window
    }
}
