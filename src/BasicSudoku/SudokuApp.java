package BasicSudoku;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class SudokuApp
{

    Board board;

    @FXML
    private TextField textField;
    private Stage primaryStage;

    /**
     * @author Abinav
     */
    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }


    /**
     * @author Abinav
     */
    @FXML
private void setBoard(ActionEvent event){
        String text = textField.getText();
        System.out.println(text);
        if(Integer.parseInt(text) > 0){
            board = new Board(Integer.parseInt(text));
            changeScene(primaryStage);
        }
    }

    /**
     * @author Abinav
     */
    private void changeScene(Stage stage){
        GridPane gp = new GridPane();

        for (int row = 0; row < board.getBoardSize(); row++) {
            for (int col = 0; col < board.getBoardSize() ; col++) {
                TextField cells = new TextField();
                cells.setPrefSize(32,20); // width = height * 1.6
               cells.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-family: 'Arial'; " +
                                "-fx-border-color: #333; " +
                                "-fx-border-width: 1px; " +
                                "-fx-background-color: #fff; " +
                                "-fx-text-fill: #666; " +
                                "-fx-padding: 5px;"
                );
                int value = board.getBoard()[row][col];
                if ( value == 0){
                    cells.setEditable(true);
                    cells.setPromptText("");
                }else {
                    cells.setPromptText(Integer.toString(value));
                    textField.setDisable(true);
                }
                gp.add(cells,col,row);
            }
        }
        Scene newScene = new Scene(gp,1200,1200);
        stage.setScene(newScene);
        stage.show();
    }

    // trying

}
