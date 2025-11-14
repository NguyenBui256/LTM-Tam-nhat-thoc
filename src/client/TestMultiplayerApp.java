package client;

import javafx.application.Application;
import javafx.stage.Stage;

public class TestMultiplayerApp extends Application {
    public TestMultiplayerApp() {}

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create two clients
            TestMultiplayer adminClient = new TestMultiplayer("admin");
            TestMultiplayer testClient = new TestMultiplayer("test");

            Stage adminStage = new Stage();
            Stage testStage = new Stage();

            adminClient.setStage(adminStage);
            testClient.setStage(testStage);

            adminClient.start();
            testClient.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
