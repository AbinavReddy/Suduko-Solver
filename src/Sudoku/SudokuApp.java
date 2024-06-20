package Sudoku;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class SudokuApp extends Application
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
            FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("Resources/Scenes/MenuScene.fxml")); // Load FXML (initial)
            sceneLoader.load(); // Set FXML as root

            // Controller setup
            Controller controller = sceneLoader.getController();
            controller.setAppStage(appStage);
            controller.goToMenuScene();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}


