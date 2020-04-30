import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class gameBoard extends Application {

    private Cell[][] cell = new Cell[4][4];
    private String[] gameLetters = new String[16];
    private int letterCounter = 0;
    private Boolean firstmove= true;
    private int lastCellRow;
    private int lastCellColumn;
    private StringBuilder word = new StringBuilder("");
    private ArrayList<String> foundWords = new ArrayList<>();

    //Variables for timer
    private static final Integer STARTTIME = 3;
    private Timeline timeline = new Timeline();
    private Label displayTime = new Label();
    private int minuteDisplay;
    private int secondDisplay;
    private IntegerProperty timerSeconds = new SimpleIntegerProperty(STARTTIME);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Create panes
        BorderPane borderPane = new BorderPane();
        borderPane.setBackground(new Background(new BackgroundFill(Color.PALEGOLDENROD, CornerRadii.EMPTY, Insets.EMPTY)));

//        final Group root = new Group(borderPane);

        rollDice();

//        Font font = new Font("Consolas", 32);
        Font titleFont = new Font("Consolas", 32);

        //Grid where letter cells are placed
        GridPane gameBoard= new GridPane();
        gameBoard.setPadding(new Insets(20.0D, 10.0D, 20.0D, 10.0D));
        gameBoard.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY)));

        gameBoard.setAlignment(Pos.CENTER);

        //set Cell objects to pane, and set token value of each cell
        for(int i=0; i < 4; i++)
            for(int j=0; j<4; j++){
                cell[i][j]= new Cell(i, j, gameLetters[letterCounter]);
                cell[i][j].setToken(gameLetters[letterCounter++]);
                gameBoard.add(cell[i][j],j, i);
            }

        //Constrain rows and columns
        for(int i = 0; (double)i < Math.sqrt(letterCounter); i++) {
            ColumnConstraints col = new ColumnConstraints(75.0D);
            RowConstraints row = new RowConstraints(75.0D);
            gameBoard.getColumnConstraints().add(col);
            gameBoard.getRowConstraints().add(row);
        }

        Scene scene = new Scene(borderPane, 600, 600);
        scene.setFill(Color.CORNFLOWERBLUE);

        //Title
        Label title = new Label("Boggle 4x4");
        title.setPadding(new Insets(40.0D, 10.0D, 30.0D, 10.0D));
        title.setFont(titleFont);
        title.setTextFill(Color.BLUE);
        borderPane.setTop(title);
        borderPane.setCenter(gameBoard);
        borderPane.setAlignment(title, Pos.CENTER);

        //Timer
        //Bind time label to timerSeconds
        displayTime.textProperty().bind(timerSeconds.asString());
        displayTime.setTextFill(Color.BLACK);
        displayTime.setStyle("-fx-font-size: 3em");
        timerSeconds.set(STARTTIME);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(STARTTIME + 1), e -> onFinished(), new KeyValue(timerSeconds, 0)));
        timeline.playFromStart();

        //VBox for UI for guessing words and displaying information
        VBox uiVBox = new VBox();
        HBox hBox = new HBox();
        Button enterWord = new Button("Enter Word");
        Button clear = new Button("Clear Selection");
        Label displayScore = new Label("0");
        hBox.getChildren().add(new Label("Score: "));
        hBox.getChildren().add(displayScore);
        hBox.setAlignment(Pos.CENTER);
        uiVBox.getChildren().add(displayTime);
        uiVBox.getChildren().add(enterWord);
        uiVBox.getChildren().add(clear);
        uiVBox.getChildren().add(hBox);

        uiVBox.setPadding(new Insets(10.0D, 10.0D, 50.0D, 10.0D));
        uiVBox.setAlignment(Pos.CENTER);

        borderPane.setBottom(uiVBox);

        clear.setOnMouseClicked(e -> clearSelection());
        enterWord.setOnMouseClicked(e -> {
            try {
                checkWord(gameBoard, scene);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });

        primaryStage.setMinHeight(650);
        primaryStage.setMinWidth(400);
        primaryStage.setTitle("Boggle 4x4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void onFinished() {
        System.out.println("Timer is up");
//        timeline.stop();
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

    public void clearSelection(){
        //Unselect all cells
        for (int i=0; i<4; i++){
            for (int j=0; j<4; j++) {
                cell[i][j].unselect();
            }
        }
        //Clear StringBuilder
        word.setLength(0);
    }

    public void checkWord(GridPane gameBoard, Scene scene) throws FileNotFoundException {
        boolean validWord = isWord();
        String targetWord = word.toString();
        targetWord = targetWord.toLowerCase();

        if (validWord){
            System.out.println(word.toString() + " is a word");
            foundWords.add(targetWord);
        }
        else{
            System.out.println(word.toString() + " is invalid");
        }
        clearSelection();
    }

    //Search dictionary to check word
    public boolean isWord() throws FileNotFoundException {
        String queryWord = word.toString();
        queryWord = queryWord.toLowerCase();
        String currentWord = "N/A";
        boolean isWord = false;

        Scanner input = new Scanner(new File("src/words_alpha.txt"));

        while (input.hasNext() && !isWord){
            currentWord = input.nextLine();
            if(currentWord.equals(queryWord))
                isWord = true;
        }
        return isWord;
    }

    //used the cell class from the Tic Tac Toe example
    public class Cell extends StackPane {
        // Indicate the row and column of this cell in the board
        private int row;
        private int column;

        private String letter;
        private Label label;
        private String token;
        private Boolean notClicked = true;
        private Color activeColor = Color.LIGHTBLUE;
        private Color inactiveColor = Color.WHITE;
        Font font = new Font("Consolas", 32);


        public Cell(int row, int column, String letter) {
            this.letter = letter;
            letter.toLowerCase();
            label = new Label(letter);
            this.row = row;
            this.column = column;
            this.setPrefSize(2000, 2000); // What happens without this?
            setStyle("-fx-border-color: black");// Set cell's border
            label.setFont(font);
            label.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(label);
            this.setAlignment(label, Pos.CENTER);

            this.setOnMouseClicked(e -> handleMouseClick());
        }

        /**Return token */
        public String getToken() {
            return token;
        }

        public String getLetter(){ return letter; }

        public void setToken(String token){
            this.token = token;
        }

        /** Handle a mouse click event */
        private void handleMouseClick() {
            // change cell color when clicked
            if(firstmove){
                this.setBackground(new Background(new BackgroundFill(activeColor, CornerRadii.EMPTY, Insets.EMPTY)));
                firstmove=false;
                lastCellRow= row;
                lastCellColumn= column;
                this.notClicked=false;
                word.append(letter);
            }
            if(adjacentToLastCell(row, column) && notClicked){
                this.setBackground(new Background(new BackgroundFill(activeColor,CornerRadii.EMPTY, Insets.EMPTY)));
                lastCellColumn=column;
                lastCellRow=row;
                this.notClicked=false;
                word.append(letter);
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

        public void unselect(){
            this.setBackground(new Background(new BackgroundFill(inactiveColor,CornerRadii.EMPTY, Insets.EMPTY)));
            firstmove = true;
            this.notClicked = true;
        }
    }
}

