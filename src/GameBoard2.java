import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
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

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class GameBoard2 extends Application {

    private Boolean firstmove = true;
    private int letterCounter, score = 0;
    private int lastCellColumn, lastCellRow;
    private StringBuilder word = new StringBuilder("");
    private ArrayList<String> foundWords = new ArrayList<>();
    private Cell[][] cell = new Cell[4][4];
    private String[] gameLetters = new String[16];

    //Variables for timer
    int interval, remaining, minutes, seconds;
    int initialSeconds = 120;
    int delay = 1000;
    int period = 1000;
    private Label displayTime = new Label();
    private Timer timer;

    private DataInputStream fromServer;
    private DataOutputStream toServer;

    private TextField username = new TextField();
    private String usernameStorage;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Stage waitingStage = new Stage();
        BorderPane waitingPane = new BorderPane();
        waitingPane.setBackground(new Background(new BackgroundFill(Color.PALEGOLDENROD, CornerRadii.EMPTY, Insets.EMPTY)));
        HBox waitingBox = new HBox();
        waitingBox.getChildren().add(new Label("Waiting for game to start"));
        waitingBox.getChildren().add(username);
        waitingBox.setMaxSize(450, 450);
        waitingPane.setCenter(waitingBox);
        Scene waitingScene = new Scene(waitingPane, 400, 400);

        waitingStage.setTitle("Game Lobby");
        waitingStage.setScene(waitingScene);
        waitingStage.show();

        connectToServer();
        int i=0;
        while(true) {
            if(fromServer.available() <=0)
                continue;
            while (fromServer.available() > 0) {
                gameLetters[i] = fromServer.readUTF();
                System.out.println(gameLetters[i]);
                i++;
            }
            waitingStage.close();
            break;
        }
        //Create panes
        BorderPane borderPane = new BorderPane();
        borderPane.setBackground(new Background(new BackgroundFill(Color.PALEGOLDENROD, CornerRadii.EMPTY, Insets.EMPTY)));

        //Grid where letter cells are placed
        GridPane gameBoard = new GridPane();
        gameBoard.setPadding(new Insets(20.0D, 10.0D, 20.0D, 10.0D));
        gameBoard.setBackground(new Background(new BackgroundFill(Color.BEIGE, CornerRadii.EMPTY, Insets.EMPTY)));
        gameBoard.setAlignment(Pos.CENTER);

        //Fonts
        Font titleFont = new Font("Consolas", 32);
        Font messageFont = new Font("Consolas", 22);



        //set Cell objects to pane, and set token value of each cell
        for(int n=0; n < 4; n++)
            for(int j=0; j<4; j++){
                cell[n][j]= new Cell(n, j, gameLetters[letterCounter]);
                cell[n][j].setToken(gameLetters[letterCounter++]);
                gameBoard.add(cell[n][j],j, n);
            }

        //Constrain rows and columns
        for(i = 0; (double)i < Math.sqrt(letterCounter); i++) {
            ColumnConstraints col = new ColumnConstraints(75.0D);
            RowConstraints row = new RowConstraints(75.0D);
            gameBoard.getColumnConstraints().add(col);
            gameBoard.getRowConstraints().add(row);
        }

        Scene scene = new Scene(borderPane, 600, 600);

        //Title
        Label title = new Label("Boggle 4x4");
        title.setPadding(new Insets(40.0D, 10.0D, 30.0D, 10.0D));
        title.setFont(titleFont);
        title.setTextFill(Color.BLUE);
        borderPane.setTop(title);
        borderPane.setCenter(gameBoard);
        borderPane.setAlignment(title, Pos.CENTER);

        //VBox for UI for guessing words and displaying information
        VBox uiVBox = new VBox();
        HBox hBox = new HBox();
        Button enterWord = new Button("Enter Word");
        Button clear = new Button("Clear Selection");
        Label displayScore = new Label("" + score);
        Label displayMessage = new Label();
        displayScore.setFont(messageFont);
        displayMessage.setTextFill(Color.RED);
        displayMessage.setFont(messageFont);
        displayTime.setFont(messageFont);
        hBox.getChildren().add(new Label("Score: "));
        hBox.getChildren().add(displayScore);
        hBox.setAlignment(Pos.CENTER);
        uiVBox.getChildren().add(displayMessage);
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
                checkWord(displayScore, displayMessage);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });

        primaryStage.setMinHeight(675);
        primaryStage.setMinWidth(400);
        primaryStage.setTitle("Boggle 4x4");
        primaryStage.setScene(scene);
        primaryStage.show();

        //Make timer
        timer = new Timer();
        interval = initialSeconds;

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run () {
                Platform.runLater(() -> {
                    remaining = setInterval();
                    minutes = remaining / 60;
                    seconds = remaining % 60;
                    if (remaining > 59)
                        if (seconds > 9)
                            displayTime.setText(minutes + ":" + seconds);
                        else
                            //Display 0 in tens place
                            displayTime.setText(minutes + ":0" + seconds);
                    else if (seconds > 9)
                        displayTime.setText("" + seconds);
                    else
                        displayTime.setText("" + seconds);
                });
            }

        }, delay, period);

        //End program after timer runs out
        PauseTransition delay = new PauseTransition(Duration.seconds(initialSeconds));
        delay.setOnFinished( event -> {
            try {
                gameOver(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    private void connectToServer() throws IOException {
        Socket socket = new Socket("localhost", 8000);

        fromServer = new DataInputStream(socket.getInputStream());
        toServer = new DataOutputStream(socket.getOutputStream());
        if(username.getText().trim().isEmpty())
            usernameStorage= "Guest";
        else
            usernameStorage = username.getText();

        int i=0;
        while (fromServer.available()>0){
            gameLetters[i]= fromServer.readUTF();
            System.out.println(gameLetters[i]);
            i++;
        }
    }

    //Close main window and open score screen
    public void gameOver(Stage primaryStage) throws IOException {
        primaryStage.close();

        toServer.writeInt(score);
        toServer.writeUTF(usernameStorage);

        System.out.println("We made it");

        String winStatus=fromServer.readUTF();
        System.out.println("first read");
        int opponentScore= fromServer.readInt();
        String opponentName= fromServer.readUTF();

        Stage scoreStage = new Stage();
        scoreStage.setMinWidth(400);
        scoreStage.setMinHeight(400);

        scoreStage.setTitle("Game Over");
        BorderPane borderPane = new BorderPane();
        borderPane.setBackground(new Background(new BackgroundFill(Color.PALEGOLDENROD, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scoreScene = new Scene(borderPane, 400, 400);

        HBox hBox= new HBox();
        Label displayWinStatus = new Label(winStatus);
        displayWinStatus.setStyle("-fx-font-size: 2em");
        hBox.getChildren().add(displayWinStatus);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(75.0D, 10.0D, 10.0D, 10.0D));

        VBox showYourScore = new VBox();
        Label displayScore = new Label("Final Score: " + score);
        displayScore.setStyle("-fx-text-fill: green; -fx-font-size: 2em");
        showYourScore.setStyle("-fx-font-size: 10pt");
        showYourScore.getChildren().add(displayScore);
        showYourScore.setAlignment(Pos.CENTER);
        showYourScore.setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));

        VBox showOpponentScore= new VBox();
        Label displayOpponentName = new Label(opponentName);
        Label displayOpponentScore = new Label("Opponent Final Score: " + opponentScore);
        displayOpponentName.setStyle("-fx-text-fill: red; -fx-font-size: 2em");
        displayOpponentScore.setStyle("-fx-text-fill: red; -fx-font-size: 2em");
        showOpponentScore.getChildren().add(displayOpponentName);
        showOpponentScore.getChildren().add(displayOpponentScore);
        showOpponentScore.setStyle("-fx-font-size: 10pt;");
        showOpponentScore.setAlignment(Pos.CENTER);
        showOpponentScore.setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));

        VBox scoreContainer = new VBox();
        scoreContainer.getChildren().add(showYourScore);
        scoreContainer.getChildren().add(showOpponentScore);
        scoreContainer.setAlignment(Pos.CENTER);

        //Add words found
        VBox showWordsFound = new VBox();
        Label wordsFoundLabel = new Label("Words Found:");
        showWordsFound.getChildren().add(wordsFoundLabel);
        for(int i=0; i<foundWords.size(); i++)
            showWordsFound.getChildren().add(new Label(i+1 + ": " + foundWords.get(i)));
        showWordsFound.setAlignment(Pos.TOP_CENTER);
        showWordsFound.setPadding(new Insets(10.0D, 10.0D, 40.0D, 10.0D));

        borderPane.setTop(hBox);
//        borderPane.setLeft(showYourScore);
//        borderPane.setRight(showOpponentScore);
        borderPane.setCenter(scoreContainer);
        borderPane.setBottom(showWordsFound);

        scoreStage.setScene(scoreScene);
        scoreStage.show();

    }

    public int setInterval(){
        if (interval == 1)
            timer.cancel();
        return --interval;
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

    public void checkWord(Label scoreLabel, Label messageLabel) throws FileNotFoundException {
        boolean validWord = isWord();
        boolean alreadyFound = false;
        String targetWord = word.toString();
        targetWord = targetWord.toLowerCase();

        messageLabel.setText("");

        if (validWord){
            //Add word to found list if it hasn't been already
            for (String foundWord : foundWords){
                if (targetWord.equals(foundWord)) {
                    alreadyFound = true;
                    messageLabel.setText("\"" + targetWord + "\" already found");
                }
            }
            //Add word and tally points
            if (!alreadyFound && targetWord.length() > 2) {
                foundWords.add(targetWord);

                if (targetWord.length() == 3 || targetWord.length() == 4)
                    score += 1;
                else if (targetWord.length() == 5)
                    score += 2;
                else if (targetWord.length() == 6)
                    score += 3;
                else if (targetWord.length() == 7)
                    score += 5;
                else if (targetWord.length() > 7)
                    score += 11;
                scoreLabel.setText("" + score);
            }
            if (targetWord.length() < 3)
                messageLabel.setText("\"" + targetWord + "\" is too short");
        }
        else{
            messageLabel.setText("\"" + targetWord + "\" is not a valid word");
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

