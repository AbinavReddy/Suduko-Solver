package BasicSudoku;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

public class SudokuApp
{
    Board sudokuBoard;

    // JavaFX related
    private Stage appStage;
    private Scene activeScene;
    private Scene menuScene;
    private Scene puzzleScene;
    private Scene solverScene;
    @FXML
    private TextField boardSizeField;

    /**
     * @author Abinav & Danny
     */
    public void initializeSudoku()
    {
        int userInput = Integer.parseInt(boardSizeField.getText());

        if(userInput > 0)
        {
            sudokuBoard = new Board(userInput);
        }

        changeActiveScene(puzzleScene);
    }

    /**
     * @author Abinav & Danny
     */
    public void changeActiveScene(Scene nextScene)
    {
        appStage.setScene(nextScene);
        activeScene = nextScene;
    }

    /**
     * @author Abinav
     */
    public void setAppStage(Stage appStage)
    {
        this.appStage = appStage;
    }

    /**
     * @author Danny
     */
    public void setScenes(Scene menuScene, Scene puzzleScene, Scene solverScene)
    {
        this.menuScene = menuScene;
        this.puzzleScene = puzzleScene;
        this.solverScene = solverScene;
    }
}
