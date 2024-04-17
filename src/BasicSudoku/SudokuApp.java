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

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }




    @FXML
private void setBoard(ActionEvent event){
        // abinav
        String text = textField.getText();
        System.out.println(text);
        if(Integer.parseInt(text) > 0){
            board = new Board(Integer.parseInt(text), 30, false);
            changeScene(primaryStage);
        }
    }

    private void changeScene(Stage stage){
        // abinav
        GridPane gp = new GridPane();

        for (int row = 0; row < board.getBoardSize(); row++) {
            for (int col = 0; col < board.getBoardSize() ; col++) {
                TextField cells = new TextField();
                cells.setPrefSize(30,30);
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
                    cells.setEditable(false);
                }
                gp.add(cells,col,row);
            }
        }
        Scene newScene = new Scene(gp,1300,1200);
        stage.setScene(newScene);
        stage.show();
    }


}
