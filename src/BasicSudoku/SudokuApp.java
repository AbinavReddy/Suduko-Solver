package BasicSudoku;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.TextField;
import java.util.Objects;
import java.io.IOException;

public class SudokuApp
{
    Board sudokuBoard;

    // JavaFX related
    private static Stage appStage;
    private static Scene activeScene;
    @FXML
    private TextField boardSizeField;

    /**
     * @author Abinav & Danny
     */
    public void initializeSudoku() throws IOException
    {
        int userInput = Integer.parseInt(boardSizeField.getText());

        if(userInput > 0)
        {
            sudokuBoard = new Board(userInput);
        }

        goToPuzzleScene();
    }

    /**
     * @author Danny
     */
    public void goToMenuScene() throws IOException
    {
        setScene("MenuScene");
    }

    /**
     * @author Danny
     */
    public void goToPuzzleScene() throws IOException
    {
        setScene("PuzzleScene");
    }

    /**
     * @author Danny
     */
    public void goToSolverScene() throws IOException
    {
        setScene("SolverScene");
    }

    /**
     * @author Abinav
     */
    public void setStage(Stage nextStage)
    {
        appStage = nextStage;
    }

    /**
     * @author Danny & Abinav
     */
    public void setScene(String sceneName) throws IOException
    {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("UI/" + sceneName + ".fxml")));

        if(activeScene == null)
        {
            activeScene = new Scene(root);
        }
        else
        {
            activeScene.setRoot(root);
        }

        appStage.setScene(activeScene); // Construct scene
        appStage.setTitle("Sudoku"); // Window title
        appStage.setResizable(true); // Disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("UI/sudoku icon.png")))); // Add app icon to stage
        appStage.show(); // Show window
    }
}
