package BasicSudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * @author Abinav
     */
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UI/MenuScene.fxml")); // Load FXML
            Parent root = loader.load(); // Set FXML as root
            Scene scene = new Scene(root); // Set root for scene
            SudokuApp controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.getIcons().addAll( // Add icons to stage
                    new Image(getClass().getResourceAsStream("UI/sudoku icon.png")));
            primaryStage.setResizable(true); // Disable resizable window
            primaryStage.setTitle("Sudoku"); // Window title
            primaryStage.setScene(scene); // Construct scene
            primaryStage.show(); // Show window


        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author Abinav
     */
    public static void main(String[] args) {
        launch(args);
    }

}


