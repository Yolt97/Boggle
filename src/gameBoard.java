import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.sqrt;

public class gameBoard extends Application {

    private Cell[][] cell = new Cell[4][4];
    private String[] gameLetters = new String[16];
    private int letterCounter = 0;
    private Boolean firstmove= true;
    private int lastCellRow;
    private int lastCellColumn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Create panes
        BorderPane borderPane = new BorderPane();
        GridPane gridPane = new GridPane();

        rollDice();

        GridPane pane1= new GridPane();
        //set Cell objects to pane, and set token value of each cell
        for(int i=0; i < 4; i++)
            for(int j=0; j<4; j++){
                cell[i][j]= new Cell(i, j);
                Label label = new Label(gameLetters[letterCounter]);
                label.setAlignment(Pos.CENTER_RIGHT);
                cell[i][j].getChildren().add(label);
                cell[i][j].setToken(gameLetters[letterCounter++]);
                pane1.add(cell[i][j],j, i);
                pane1.setAlignment(Pos.CENTER);

            }

        Scene scene = new Scene(borderPane, 200, 200);

        Font font = new Font("Consolas", 20);
        Font titleFont = new Font("Consolas", 32);

        //Title
        Label title = new Label("Boggle 4x4");
        title.setFont(titleFont);
        title.setTextFill(Color.BLUE);
        borderPane.setTop(title);
        borderPane.setCenter(pane1);
        borderPane.setAlignment(title, Pos.CENTER);


        primaryStage.setTitle("Boggle 4x4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void rollDice(){
        ArrayList<Die> dice = new ArrayList<>(); //Dice
        // ArrayList<String> letters = new ArrayList<>(); //Letters chosen from dice

        //Add unrolled dice
        dice.add(new Die(new String[]{"A", "A", "E", "E", "G", "N"}));
        dice.add(new Die(new String[]{"A", "B", "B", "J", "O", "O"}));
        dice.add(new Die(new String[]{"A", "C", "H", "O", "P", "S"}));
        dice.add(new Die(new String[]{"A", "F", "F", "K", "P", "S"}));
        dice.add(new Die(new String[]{"A", "O", "O", "T", "T", "W"}));
        dice.add(new Die(new String[]{"C", "I", "M", "O", "T", "U"}));
        dice.add(new Die(new String[]{"D", "E", "I", "L", "R", "X"}));
        dice.add(new Die(new String[]{"D", "E", "L", "R", "V", "Y"}));
        dice.add(new Die(new String[]{"D", "I", "S", "I", "T", "Y"}));
        dice.add(new Die(new String[]{"E", "E", "G", "H", "N", "W"}));
        dice.add(new Die(new String[]{"E", "E", "I", "N", "S", "U"}));
        dice.add(new Die(new String[]{"E", "H", "R", "T", "V", "W"}));
        dice.add(new Die(new String[]{"E", "I", "O", "S", "S", "T"}));
        dice.add(new Die(new String[]{"E", "L", "R", "T", "T", "Y"}));
        dice.add(new Die(new String[]{"H", "I", "M", "N", "U", "Qu"}));
        dice.add(new Die(new String[]{"H", "L", "N", "N", "R", "Z"}));

        //Shuffle dice order
        Collections.shuffle(dice);

        //Roll each die and select the letter
        for (int i=0; i<dice.size(); i++){
            dice.get(i).roll();
            gameLetters[i]=(dice.get(i).getFace());
        }
    }

    //used the cell class from the Tic Tac Toe example
    public class Cell extends Pane {
        // Indicate the row and column of this cell in the board
        private int row;
        private int column;

        private String token;
        private Boolean notClicked = true;

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            this.setPrefSize(2000, 2000); // What happens without this?
            setStyle("-fx-border-color: black");// Set cell's border

            this.setOnMouseClicked(e -> handleMouseClick());
        }

        /**Return token */
        public String getToken() {
            return token;
        }

        public void setToken(String token){
            this.token = token;
        }

        /** Handle a mouse click event */
        private void handleMouseClick() {
            // change cell color when clicked
            if(firstmove){
                this.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY,CornerRadii.EMPTY, Insets.EMPTY)));
                firstmove=false;
                lastCellRow= row;
                lastCellColumn= column;
                this.notClicked=false;
            }
            if(adjacentToLastCell(row, column) && notClicked){
                this.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY,CornerRadii.EMPTY, Insets.EMPTY)));
                lastCellColumn=column;
                lastCellRow=row;
                this.notClicked=false;
            }
        }

        private Boolean adjacentToLastCell(int cellRow, int cellColumn){
            if((cellRow == (lastCellRow - 1) || cellRow == (lastCellRow + 1)) && cellColumn == lastCellColumn)
                return true;
            else if((cellColumn == (lastCellColumn - 1) || cellColumn == (lastCellColumn + 1)) && cellRow == lastCellRow)
                return true;
            else if((cellColumn == (lastCellColumn - 1) || cellColumn == (lastCellColumn + 1)) && (cellRow == (lastCellRow - 1) || cellRow == (lastCellRow + 1)))
                return true;
            return false;

        }
    }
}

