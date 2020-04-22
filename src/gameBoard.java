import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.sqrt;

public class gameBoard extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Create panes
        BorderPane borderPane = new BorderPane();
        GridPane gridPane = new GridPane();
        borderPane.setCenter(gridPane);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setGridLinesVisible(true);
        Scene scene = new Scene(borderPane, 700, 700);

        Font font = new Font("Consolas", 20);
        Font titleFont = new Font("Consolas", 32);

        //Title
        Label title = new Label("Boggle 4x4");
        title.setFont(titleFont);
        title.setTextFill(Color.BLUE);
        borderPane.setTop(title);
        borderPane.setAlignment(title, Pos.CENTER);

        //Put letters on gridPane
        int counter = 0;
        ArrayList<String> letters = rollDice();
        for(int row = 0; row < sqrt(letters.size()); row++){
            for(int column = 0; column < sqrt(letters.size()); column++){
                gridPane.add(new Text(letters.get(counter++)), row, column);
            }
        }

        primaryStage.setTitle("Boggle 4x4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public ArrayList<String> rollDice(){
        ArrayList<Die> dice = new ArrayList<>(); //Dice
        ArrayList<String> letters = new ArrayList<>(); //Letters chosen from dice

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
            letters.add(dice.get(i).getFace());
        }

        return letters;
    }
}
