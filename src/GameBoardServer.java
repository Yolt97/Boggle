/*
    Author: Aaron Westhoff
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class GameBoardServer extends Application {

    private int sessionNum = 1;

    public void start(Stage primaryStage) {
        TextArea serverLog = new TextArea();

        Scene scene= new Scene(new ScrollPane(serverLog), 450, 200);
        primaryStage.setTitle("Boggle Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread( () -> {

            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> serverLog.appendText(new Date() + ": Server started at port 8000\n"));

                while(true){
                    Platform.runLater(() -> serverLog.appendText(new Date() + ": Waiting for players to join\n"));

                    Socket player1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        serverLog.appendText(new Date() + ": Player 1 joined session\n");
                        serverLog.appendText("Player 1's IP address " +
                                player1.getInetAddress().getHostAddress() + '\n');
                    });

                    Socket player2 = serverSocket.accept();
                    Platform.runLater(() -> {
                        serverLog.appendText(new Date() +
                                ": Player 2 joined session\n");
                        serverLog.appendText("Player 2's IP address " +
                                player2.getInetAddress().getHostAddress() + '\n');
                    });

                    Platform.runLater(() ->
                            serverLog.appendText(new Date() +
                                    ": Start a thread for session " + sessionNum++ + '\n'));

                    new Thread(new HandleSession(player1, player2)).start();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    class HandleSession implements Runnable {
        private Socket player1;
        private Socket player2;

        private String[] gameLetters = new String[16];
        private int letterCounter = 0;

        private DataInputStream fromPlayer1;
        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer2;

        public HandleSession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;

            //get letters for cells
            rollDice();
        }

        @Override
        public void run() {
            try {
                DataInputStream fromPlayer1 = new DataInputStream(
                        player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(
                        player1.getOutputStream());
                DataInputStream fromPlayer2 = new DataInputStream(
                        player2.getInputStream());
                DataOutputStream toPlayer2 = new DataOutputStream(
                        player2.getOutputStream());

                for(int i= 0; i < 16; i++)
                    toPlayer1.writeUTF(gameLetters[i]);
                for(int i= 0; i< 16; i++)
                    toPlayer2.writeUTF(String.valueOf(gameLetters[i]));

                toPlayer1.flush();
                toPlayer2.flush();

                int player1Score= fromPlayer1.readInt();
                String player1Username= fromPlayer1.readUTF();
                int player2Score= fromPlayer2.readInt();
                String player2Username= fromPlayer2.readUTF();

                if(player1Score > player2Score){
                    toPlayer1.writeUTF("You Win!");
                    toPlayer1.writeInt(player2Score);
                    toPlayer1.writeUTF(player2Username);

                    toPlayer2.writeUTF(player1Username + " wins");
                    toPlayer2.writeInt(player1Score);
                    toPlayer2.writeUTF(player1Username);

                }
                else if (player1Score < player2Score){
                    toPlayer1.writeUTF(player2Username + " wins");
                    toPlayer1.writeInt(player2Score);
                    toPlayer1.writeUTF(player2Username);

                    toPlayer2.writeUTF("You win");
                    toPlayer2.writeInt(player1Score);
                    toPlayer2.writeUTF(player1Username);
                    System.out.println("write to clients2");
                }
                else{
                    toPlayer1.writeUTF("Its a tie");
                    toPlayer1.writeInt(player2Score);
                    toPlayer1.writeUTF(player2Username);
                    toPlayer2.writeUTF("Its a tie");
                    toPlayer2.writeInt(player1Score);
                    toPlayer2.writeUTF(player1Username);
                    System.out.println("write to clients2");
                }
                toPlayer1.flush();
                toPlayer2.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void rollDice() {
            ArrayList<Die> dice = new ArrayList<>(); //Dice

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
            for (int i = 0; i < dice.size(); i++) {
                dice.get(i).roll();
                gameLetters[i] = (dice.get(i).getFace());
            }
        }
    }
}
