package client.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameController {

    // === FXML UI Elements ===
    @FXML private Label scoreYou, timerLabel;
    @FXML private Pane boardPane;

    // Player Baskets (You)
    @FXML private VBox riceBasket, paddyBasket, cornBasket;
    @FXML private Label riceCount, paddyCount, cornCount;

    // === Game Data ===
    private List<Integer> seeds; // 0 = rice, 1 = paddy, 2 = corn
    private List<ImageView> seedImages = new ArrayList<>();
    private List<Boolean> seedTaken = new ArrayList<>();
    private ImageView selectedSeed = null;
    private int selectedSeedIndex = -1;
    private Random random = new Random();

    // === KHÓA PHÍM KHI ĐANG XỬ LÝ HẠT ===
    private boolean isProcessing = false;

    // === Initialize ===
    public void initialize() {
        System.out.println("[LOG] initialize() được gọi");

        Platform.runLater(() -> {
            System.out.println("[LOG] Platform.runLater() - Bắt đầu khởi tạo game");

            seeds = generateSeeds(50);
            seedTaken = new ArrayList<>(Collections.nCopies(seeds.size(), false));
            initializeBoard();
            setupBoardClickListener();
            setupKeyboardListener();
            startTimer(30); // 30 giây

            boardPane.setFocusTraversable(true);

            Platform.runLater(() -> {
                boardPane.requestFocus();
                System.out.println("[LOG] CUỐI CÙNG: requestFocus() cho boardPane");
            });

            System.out.println("[LOG] Game khởi tạo xong. Click hạt → bấm A/B/C để bỏ rổ.");
        });
    }

    // === TẠO 50 HẠT CỐ ĐỊNH: 15 GẠO, 15 THÓC, 20 NGÔ ===
    private List<Integer> generateSeeds(int totalCount) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) list.add(0); // Gạo
        for (int i = 0; i < 20; i++) list.add(1); // Thóc
        for (int i = 0; i < 20; i++) list.add(2); // Ngô

        Collections.shuffle(list, random); // Xào vị trí

        return list;
    }

    // === VẼ HẠT LÊN BÀN (TRÁNH CHỒNG LẤN) ===
    private void initializeBoard() {
        double boardWidth = 516;
        double boardHeight = 450;
        double seedSize = 40;
        List<double[]> occupied = new ArrayList<>();


        for (int i = 0; i < seeds.size(); i++) {
            String path = switch (seeds.get(i)) {
                case 0 -> "/images/rice.png";
                case 1 -> "/images/paddy.png";
                case 2 -> "/images/corn.png";
                default -> "/images/rice.png";
            };

            Image image = new Image(getClass().getResourceAsStream(path));
            if (image.isError()) {
                System.err.println("[LỖI] Không tải được ảnh: " + path);
                continue;
            }

            ImageView seed = new ImageView(image);
            seed.setFitWidth(seedSize);
            seed.setFitHeight(seedSize);
            seed.setPickOnBounds(true);
            seed.setMouseTransparent(false);

            double x, y;
            boolean valid;
            int attempts = 0;
            do {
                valid = true;
                x = random.nextDouble() * (boardWidth - seedSize);
                y = random.nextDouble() * (boardHeight - seedSize);
                for (double[] pos : occupied) {
                    double dx = x - pos[0];
                    double dy = y - pos[1];
                    if (Math.sqrt(dx * dx + dy * dy) < seedSize) {
                        valid = false;
                        break;
                    }
                }
                attempts++;
            } while (!valid && attempts < 100);

            if (!valid) {
                x = 50 + (i % 10) * seedSize;
                y = 50 + (i / 10) * seedSize;
            }

            seed.setX(x);
            seed.setY(y);
            boardPane.getChildren().add(seed);
            seedImages.add(seed);
            occupied.add(new double[]{x, y});
        }

        long rice = seeds.stream().filter(s -> s == 0).count();
        long paddy = seeds.stream().filter(s -> s == 1).count();
        long corn = seeds.stream().filter(s -> s == 2).count();

    }

    // === Click vào hạt ===
    private void setupBoardClickListener() {
        boardPane.setOnMouseClicked(event -> {
            if (selectedSeed != null || isProcessing) {
                return;
            }

            double clickX = event.getX();
            double clickY = event.getY();

            for (int i = 0; i < seedImages.size(); i++) {
                if (seedTaken.get(i)) continue;

                ImageView seed = seedImages.get(i);
                if (seed.getBoundsInParent().contains(clickX, clickY)) {
                    System.out.println("[LOG] CHỌN HẠT THÀNH CÔNG: index=" + i + ", loại=" + seeds.get(i));
                    selectSeed(seed, i);
                    return;
                }
            }
        });
    }

    // === CHỌN HẠT + TỰ ĐỘNG LẤY FOCUS ===
    private void selectSeed(ImageView seed, int index) {
        selectedSeed = seed;
        selectedSeedIndex = index;
        seed.setScaleX(1.3);
        seed.setScaleY(1.3);
        seed.setStyle("-fx-effect: dropshadow(gaussian, yellow, 12, 0.8, 0, 0);");

        System.out.println("[LOG] ĐÃ CHỌN HẠT: index=" + index + ", loại=" + seeds.get(index) + " (0=gạo,1=thóc,2=ngô)");
        System.out.println("[LOG] Bấm A (Gạo), B (Thóc), C (Ngô) để bỏ vào rổ");

        Platform.runLater(() -> {
            boardPane.requestFocus();
            System.out.println("[LOG] ĐÃ TỰ ĐỘNG LẤY FOCUS SAU KHI CHỌN HẠT");
        });
    }

    // === Bắt phím A, B, C ===
    private void setupKeyboardListener() {
        if (boardPane.getScene() != null) {
            attachKeyListener();
        } else {
            boardPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    System.out.println("[LOG] Scene đã sẵn sàng → Gắn key listener");
                    attachKeyListener();
                }
            });
        }
    }

    private void attachKeyListener() {
        boardPane.getScene().setOnKeyPressed(event -> {

            // KHÓA PHÍM KHI ĐANG XỬ LÝ
            if (isProcessing) {
                return;
            }

            if (selectedSeed == null) {
                return;
            }

            int basketType = switch (event.getCode()) {
                case A -> { System.out.println("[LOG] Nhận phím A → Rổ Gạo"); yield 0; }
                case B -> { System.out.println("[LOG] Nhận phím B → Rổ Thóc"); yield 1; }
                case C -> { System.out.println("[LOG] Nhận phím C → Rổ Ngô"); yield 2; }
                default -> { System.out.println("[LOG] Phím không hợp lệ: " + event.getCode()); yield -1; }
            };

            if (basketType != -1) {
                isProcessing = true; // KHÓA PHÍM
                handleBasketChoice(basketType);
            }
        });

        Platform.runLater(() -> {
            boardPane.requestFocus();

        });
    }

    // === XỬ LÝ CHỌN RỔ ===
    private void handleBasketChoice(int basketType) {

        VBox targetBasket;
        Label countLabel;

        switch (basketType) {
            case 0 -> { targetBasket = riceBasket; countLabel = riceCount; }
            case 1 -> { targetBasket = paddyBasket; countLabel = paddyCount; }
            case 2 -> { targetBasket = cornBasket; countLabel = cornCount; }
            default -> { isProcessing = false; return; }
        }

        int seedType = seeds.get(selectedSeedIndex);
        boolean isCorrect = (seedType == basketType);
        System.out.println("[LOG] Hạt loại " + seedType + " → " + (isCorrect ? "ĐÚNG" : "SAI"));

        double targetX = targetBasket.getLayoutX() + targetBasket.getWidth() / 2 - 20;
        double targetY = targetBasket.getLayoutY() + targetBasket.getHeight() / 2 - 20;
        double originX = selectedSeed.getX();
        double originY = selectedSeed.getY();

        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.8), selectedSeed);
        tt.setToX(targetX - originX);
        tt.setToY(targetY - originY);
        tt.setOnFinished(event -> {

            if (isCorrect) {
                boardPane.getChildren().remove(selectedSeed);
                seedTaken.set(selectedSeedIndex, true);
                int count = Integer.parseInt(countLabel.getText()) + 1;
                countLabel.setText(String.valueOf(count));
                int score = Integer.parseInt(scoreYou.getText()) + 1;
                scoreYou.setText(String.valueOf(score));

            } else {
                resetSeedPosition();

            }

            resetSelection();
            boardPane.requestFocus();

            // MỞ KHÓA PHÍM
            isProcessing = false;

        });

        tt.play();

    }

    private void resetSeedPosition() {
        if (selectedSeed != null) {
            selectedSeed.setTranslateX(0);
            selectedSeed.setTranslateY(0);
        }
    }

    private void resetSelection() {
        if (selectedSeed != null) {
            selectedSeed.setScaleX(1.0);
            selectedSeed.setScaleY(1.0);
            selectedSeed.setStyle("");
        }
        selectedSeed = null;
        selectedSeedIndex = -1;
    }

    // === Timer ===
    private void startTimer(int seconds) {
        System.out.println("[LOG] startTimer(" + seconds + "s) bắt đầu");
        new Thread(() -> {
            for (int i = seconds; i >= 0; i--) {
                final int time = i;
                Platform.runLater(() -> timerLabel.setText(String.format("%02d:%02d", time / 60, time % 60)));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> {
                timerLabel.setText("Hết giờ!");
                boardPane.setDisable(true);
                int you = Integer.parseInt(scoreYou.getText());
                timerLabel.setText("Kết thúc! Điểm: " + you);

            });
        }).start();
    }
}