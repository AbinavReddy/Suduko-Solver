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
    public void chooseBoardDimensions() throws IOException
    {
        int userInput = Integer.parseInt(boardSizeField.getText());

        if(userInput > 0)
        {
            board = new SudokuBoard(userInput);
        }

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
                boardGridCells[row][column] = new TextField();
                TextField temp = boardGridCells[row][column];
                temp.setPrefSize(32,20); // width = height * 1.6
                temp.setStyle("-fx-font-size: 16px; " + "-fx-font-family: 'Arial'; " + "-fx-border-color: #333; " + "-fx-border-width: 1px; " + "-fx-background-color: #fff; " + "-fx-text-fill: #666; " + "-fx-padding: 5px;");
                temp.setPromptText(String.valueOf(boardToShow[row][column])); // fill grid with values from board (unsolved or solved)
                temp.setEditable(true);

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
