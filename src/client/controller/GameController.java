package client.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.Random;

public class GameController {

    @FXML private Label timerLabel;
    @FXML private Label scoreYou;
    @FXML private Label scoreOpponent;
    @FXML private Pane boardPane;

    @FXML private VBox riceBasket;
    @FXML private VBox paddyBasket;
    @FXML private VBox cornBasket;

    private int timeLeft = 30;
    private int score = 0;
    private Timeline timer;
    private final Random random = new Random();


    @FXML
    public void initialize() {
        startTimer();
        generateMockSeeds();
    }

    /** Bộ đếm thời gian 30s */
    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            updateTimerLabel();
            if (timeLeft <= 0) {
                timer.stop();
                endGame();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerLabel() {
        timerLabel.setText(String.format("00:%02d", timeLeft));
    }

    private void endGame() {
        timerLabel.setText("Hết giờ!");
    }

    private void generateMockSeeds() {
        double width = boardPane.getPrefWidth();   // hoặc boardPane.getWidth()
        double height = boardPane.getPrefHeight();

        for (int i = 0; i < 50; i++) {
            String type = switch (random.nextInt(3)) {
                case 0 -> "rice";
                case 1 -> "paddy";
                default -> "corn";
            };

            // Random toàn bộ vùng bàn
            double x = random.nextDouble() * (width - 80);  // trừ bớt kích thước hạt
            double y = random.nextDouble() * (height - 80);
            addSeed(type, x, y);
        }
    }


    /** Thêm 1 hạt lên bàn chung */
    private void addSeed(String type, double x, double y) {
        ImageView seed = new ImageView(new Image(getClass().getResourceAsStream("/images/" + type + ".png")));
        seed.setFitWidth(60);
        seed.setFitHeight(60);
        seed.setLayoutX(x);
        seed.setLayoutY(y);
        seed.setUserData(type);

        // Khi click vào hạt
        seed.setOnMouseClicked(e -> collectSeed(seed));

        boardPane.getChildren().add(seed);
    }

    /** Khi click vào hạt */
    private void collectSeed(ImageView seed) {
        String type = (String) seed.getUserData();
        VBox target = switch (type) {
            case "rice" -> riceBasket;
            case "paddy" -> paddyBasket;
            default -> cornBasket;
        };

        // Tính vị trí mục tiêu (trung tâm rổ)
        double targetX = target.getLayoutX() + 60; // 60 ~ giữa ảnh 120px
        double targetY = target.getLayoutY() - 20;

        // Hiệu ứng bay
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), seed);
        tt.setToX(targetX - seed.getLayoutX());
        tt.setToY(targetY - seed.getLayoutY());
        tt.setOnFinished(e -> {
            boardPane.getChildren().remove(seed);
            score++;
            scoreYou.setText(String.valueOf(score));
        });
        tt.play();
    }
}
