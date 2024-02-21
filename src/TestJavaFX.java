
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestJavaFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private int counter = 0;
    private Button button = new Button();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        this.button.setText("Im a counter! Click ME!!!");
        this.button.setOnAction(this::handleClick);
        StackPane root = new StackPane();
        root.getChildren().add(this.button);
        Scene scene = new Scene(root, 300, 250);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleClick(ActionEvent event) {
        this.counter++;
        this.button.setText("" + this.counter);
    }

    private void handleKey(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            this.counter++;
        } else if (event.getCode() == KeyCode.DOWN) {
            this.counter--;
        } else {
            return;
        }
        this.button.setText("" + this.counter);
    }
}

//YAHYA TEST.