package client;

import client.controller.GameController;
import client.network.MessageListener;
import client.network.Network;
import server.dto.GameUpdate;
import server.dto.LoginRequest;
import server.dto.Message;
import server.dto.MoveRequest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class TestMultiplayer implements MessageListener {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 2206;

    private Network network;
    private String username;
    private String opponent = "test"; // if admin, opponent test; if test, opponent admin
    private Random random = new Random();
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public TestMultiplayer(String username) {
        this.username = username;
        if ("admin".equals(username)) {
            opponent = "test";
        } else {
            opponent = "admin";
        }
    }

    public void start() throws IOException {
        network = new Network(SERVER_IP, SERVER_PORT);
        network.addMessageListener(this);
        System.out.println("[" + username + "] Connected to server");

        // Login
        LoginRequest loginReq = new LoginRequest(username, "password");
        Message loginMsg = new Message("LOGIN", username, loginReq);
        network.send(loginMsg);
        System.out.println("[" + username + "] Sent LOGIN");

        URL fxmlURL = getClass().getResource("/fxml/GameScene.fxml");
        System.out.println("FXML URL: " + fxmlURL);

        FXMLLoader loader = new FXMLLoader(fxmlURL);
        Parent root = loader.load();


        GameController controller = loader.getController();
        controller.setNetwork(network);
        controller.setGameId("testGame");
        controller.setUsername(username);


        stage.setTitle("Màn Game");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @Override
    public void onMessageReceived(Message msg) {
        System.out.println("[" + username + "] Received: " + msg.getCommand() + " - " + msg.getContent());
        switch (msg.getCommand()) {
            case "LOGIN_SUCCESS" -> {
                System.out.println("[" + username + "] Login success");
                if ("admin".equals(username)) {
                    // Admin sends invite to test
                    Message inviteMsg = new Message("INVITE", username, opponent);
                    try {
                        network.send(inviteMsg);
                        System.out.println("[" + username + "] Sent INVITE to " + opponent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case "LOGIN_FAILED" -> {
                System.out.println("[" + username + "] Login failed: " + msg.getContent());
            }
            case "INVITE" -> {
                // Test receives invite, accept
                Message acceptMsg = new Message("ACCEPT", username, msg.getContent()); // content is inviter
                try {
                    network.send(acceptMsg);
                    System.out.println("[" + username + "] Sent ACCEPT to " + msg.getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "START_GAME" -> {
                if (msg.getContent() instanceof GameUpdate update) {
                    String gameId = update.getGameId();
                    System.out.println("[" + username + "] Game started with id: " + gameId);

                    // Open GameScene
                    Platform.runLater(() -> {
                        try {
                            URL fxmlURL = getClass().getResource("/fxml/GameScene.fxml");
                            FXMLLoader loader = new FXMLLoader(fxmlURL);
                            Parent root = loader.load();
                            GameController controller = loader.getController();
                            controller.setNetwork(network);
                            controller.setUsername(username);
                            controller.setGameId(gameId);
                            Scene scene = new Scene(root, 800, 600);
                            stage.setTitle("Game - " + username);
                            stage.setScene(scene);
                            stage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    // Simulate playing: send random moves
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000); // wait for scene to load
                            for (int i = 0; i < 10; i++) { // send 10 moves
                                Thread.sleep(1000 + random.nextInt(2000)); // random delay
                                int seedIndex = random.nextInt(50); // 50 seeds
                                int choice = random.nextInt(3); // 0,1,2
                                MoveRequest req = new MoveRequest(gameId, seedIndex, choice);
                                Message moveMsg = new Message("MOVE", username, req);
                                network.send(moveMsg);
                                System.out.println("[" + username + "] Sent MOVE: gameId=" + gameId + ", seedIndex=" + seedIndex + ", choice=" + choice);
                            }
                            // Send END_GAME
                            Thread.sleep(2000);
                            Message endMsg = new Message("END_GAME", username, gameId);
                            network.send(endMsg);
                            System.out.println("[" + username + "] Sent END_GAME");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
            case "GAME_UPDATE" -> {
                System.out.println("[" + username + "] Game update received");
            }
            case "END_GAME" -> {
                System.out.println("[" + username + "] End game received: " + msg.getContent());
            }
        }
    }
}

