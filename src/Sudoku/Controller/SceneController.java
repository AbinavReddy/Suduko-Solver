package Sudoku.Controller;

import Sudoku.Enums.BoardViewStates;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class SceneController extends GameController
{
    private static Stage appStage;
    private static Scene currentScene;
    private static GameController gameController;

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
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Sudoku/View/Scenes/" + sceneName + ".fxml")));

        if(currentScene != null)
        {
            currentScene.setRoot(root);
        }
        else
        {
            currentScene = new Scene(root);
        }

        currentScene.widthProperty().addListener((observable) -> scaleScreen()); // used for resizing View
        currentScene.heightProperty().addListener((observable) -> scaleScreen());

        scaleScreen();

        appStage.setScene(currentScene); // construct scene
        appStage.setTitle("Sudoku (Group 5)"); // window title
        appStage.setResizable(true); // disable resizable window
        appStage.getIcons().addAll(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Sudoku/View/Media/sudoku icon.png")))); // add app icon to stage
        appStage.show(); // show window
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    private void scaleScreen()
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

    /**
     * @author Danny
     */
    public void goToMenuScene() throws IOException
    {
        gameController.setBoardView(BoardViewStates.NoBoardShown);
        setActiveScene("MenuScene");
    }

    /**
     * @author Abinav
     */
    public void goToSaveLoadScene() throws IOException
    {
        gameController.setBoardView(BoardViewStates.NoBoardShownSaveLoad);
        setActiveScene("SaveLoadScene");
    }

    /**
     * @author Danny
     */
    public void goToPuzzleScene() throws IOException
    {
        gameController.setBoardView(BoardViewStates.UnsolvedBoardShown);
        setActiveScene("PuzzleScene");
    }

    /**
     * @author Danny
     */
    public void goToCustomScene() throws IOException
    {
        gameController.setBoardView(BoardViewStates.CustomBoardShown);
        setActiveScene("CustomScene");
    }

    /**
     * @author Danny
     */
    public void goToSolverScene() throws IOException
    {
        gameController.setBoardView(BoardViewStates.SolvedBoardShown);
        setActiveScene("SolverScene");
    }

    /**
     * @author Danny, Abinav & Yahya
     */
    public void setGameController(GameController gameController)
    {
        SceneController.gameController = gameController;

        gameController.setSceneController(this);
    }
}
