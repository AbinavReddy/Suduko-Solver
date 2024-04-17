package BasicSudoku;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage primaryStage) {
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SudokuBoard.fxml")); // Load FXML
            Parent root = loader.load(); // Set FXML as root
            Scene scene = new Scene(root); // Set root for scene
            primaryStage.setResizable(false); // Disable resizable window
            primaryStage.setTitle("Sudoku"); // Window title
            primaryStage.setScene(scene); // Construct scene
            primaryStage.show(); // Show window
        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        launch(args);
    }
}
