package BasicSudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import java.util.Objects;

public class Main extends Application
{
    /**
     * @author Abinav
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * @author Danny & Abinav
     */
    public void start(Stage appStage)
    {
        try
        {
            // JavaFX setup
            FXMLLoader menuSceneLoader = new FXMLLoader(getClass().getResource("UI/MenuScene.fxml")); // Load FXML
            FXMLLoader puzzleSceneLoader = new FXMLLoader(getClass().getResource("UI/PuzzleScene.fxml"));
            FXMLLoader solverSceneLoader = new FXMLLoader(getClass().getResource("UI/SolverScene.fxml"));
            Parent menuSceneRoot = menuSceneLoader.load(); // Set FXML as root
            Parent puzzleSceneRoot = puzzleSceneLoader.load();
            Parent solverSceneRoot = solverSceneLoader.load();
            Scene menuScene = new Scene(menuSceneRoot); // Set root for scene
            Scene puzzleScene = new Scene(puzzleSceneRoot);
            Scene solverScene = new Scene(solverSceneRoot);

            // Controller setup
            SudokuApp controller = menuSceneLoader.getController();
            controller.setAppStage(appStage);
            controller.setScenes(menuScene, puzzleScene, solverScene);
            controller.changeActiveScene(menuScene); // Construct scene

            // Stage setup
            appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("UI/sudoku icon.png")))); // Add app icon to stage
            appStage.setResizable(true); // Disable resizable window
            appStage.setTitle("Sudoku"); // Window title
            appStage.show(); // Show window
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}


