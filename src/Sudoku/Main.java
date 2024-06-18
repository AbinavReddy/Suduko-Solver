package Sudoku;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

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
            FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource("UI/MenuScene.fxml")); // Load FXML (initial)
            sceneLoader.load(); // Set FXML as root

            // Controller setup
            SceneController sceneController = sceneLoader.getController();
            sceneController.setGameController(new GameController());
            sceneController.setAppStage(appStage);
            sceneController.goToMenuScene();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}


