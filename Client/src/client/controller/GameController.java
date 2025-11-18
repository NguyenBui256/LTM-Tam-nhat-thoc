package client.controller;

import client.network.MessageListener;
import client.network.Network;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dto.GameUpdate;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.web.WebView;
import common.CommandType;
import dto.Message;
import dto.MoveRequest;
import dto.GameStart;

public class GameController implements MessageListener {

    // === FXML UI Elements ===
    @FXML
    private Label scoreYou, scoreOpponent, timerLabel;

    @FXML
    private Pane boardPane;

    @FXML
    private javafx.scene.control.Button exitButton;

    // Player Baskets (You) - Left side
    @FXML
    private VBox riceBasket, paddyBasket, cornBasket;

    @FXML
    private Label riceCount, paddyCount, cornCount;
    
    // Opponent Baskets - Right side
    @FXML
    private VBox riceBasketOpp, paddyBasketOpp, cornBasketOpp;

    @FXML
    private Label riceCountOpp, paddyCountOpp, cornCountOpp;

    // === Game Data ===
    private List<Integer> seeds; // 0 = rice, 1 = paddy, 2 = corn
    private List<ImageView> seedImages = new ArrayList<>();
    private List<Boolean> seedTaken = new ArrayList<>();
    private ImageView selectedSeed = null;
    private int selectedSeedIndex = -1;
    private Random random = new Random();

    // === KHÓA PHÍM KHI ĐANG XỬ LÝ HẠT ===
    private boolean isProcessing = false;
    
    // === TRẠNG THÁI TRÒ CHƠI ===
    private boolean gameFinished = false;

    // === Network & Stage ===
    private Network network;
    private String gameId;
    private Stage primaryStage;
    //    private String username;
    
    // === Player Info ===
    private String currentPlayerName;
    private String opponentName;
    private int currentPlayerScore = 0;
    private int opponentScore = 0;

    // === Initialize ===
    public void initialize() {
        System.out.println("[LOG]: " + currentPlayerName + "   initialize() được gọi");

        // Setup exit button
        if (exitButton != null) {
            exitButton.setOnAction(e -> onExitGameClicked());
        }

        Platform.runLater(() -> {
            System.out.println(
                "[LOG]: " + currentPlayerName + "   Platform.runLater() - Bắt đầu khởi tạo game"
            );

            setupBoardClickListener();
            setupKeyboardListener();

            boardPane.setFocusTraversable(true);

            Platform.runLater(() -> {
                boardPane.requestFocus();
                System.out.println(
                    "[LOG]: " + currentPlayerName + "   CUỐI CÙNG: requestFocus() cho boardPane"
                );
            });

            // Gửi yêu cầu tạo game mới
            //            Message createMsg = new Message(
            //                CommandType.CREATE_GAME.toString(),
            //                currentPlayerName,
            //                gameId
            //            );
            //            try {
            //                network.send(createMsg);
            //                System.out.println("[LOG]: " + currentPlayerName + "   Sent CREATE_GAME: " + gameId);
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //            }
            //
            //            System.out.println("[LOG]: " + currentPlayerName + "   Đã gửi yêu cầu tạo game. Chờ server...");
        });
    }

    // === Setters ===
    public void setNetwork(Network network) {
        this.network = network;
        if (this.network != null) {
            this.network.addMessageListener(this);
        }
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        InviteNotificationManager.getInstance().setPrimaryStage(stage);
        InviteNotificationManager.getInstance().setNetwork(this.network);
        // Gắn close handler - sẽ kiểm tra currentPlayerName khi đóng cửa sổ
        ControllerHelper.setupWindowCloseHandler(stage, this.currentPlayerName, this.network);
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
            occupied.add(new double[] { x, y });
        }

        long rice = seeds
            .stream()
            .filter(s -> s == 0)
            .count();
        long paddy = seeds
            .stream()
            .filter(s -> s == 1)
            .count();
        long corn = seeds
            .stream()
            .filter(s -> s == 2)
            .count();
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
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   CHỌN HẠT THÀNH CÔNG: index=" +
                            i +
                            ", loại=" +
                            seeds.get(i)
                    );
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
        seed.setStyle(
            "-fx-effect: dropshadow(gaussian, yellow, 12, 0.8, 0, 0);"
        );

        System.out.println(
            "[LOG]: " + currentPlayerName + "   ĐÃ CHỌN HẠT: index=" +
                index +
                ", loại=" +
                seeds.get(index) +
                " (0=gạo,1=thóc,2=ngô)"
        );
        System.out.println("[LOG]: " + currentPlayerName + "   Bấm 1 (Gạo), 2 (Thóc), 3 (Ngô) để bỏ vào rổ");

        Platform.runLater(() -> {
            boardPane.requestFocus();
            System.out.println("[LOG]: " + currentPlayerName + "   ĐÃ TỰ ĐỘNG LẤY FOCUS SAU KHI CHỌN HẠT");
        });
    }

    // === Bắt phím 1,2,3 ===
    private void setupKeyboardListener() {
        if (boardPane.getScene() != null) {
            attachKeyListener();
        } else {
            boardPane
                .sceneProperty()
                .addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        System.out.println(
                            "[LOG]: " + currentPlayerName + "   Scene đã sẵn sàng → Gắn key listener"
                        );
                        attachKeyListener();
                    }
                });
        }
    }

    private void attachKeyListener() {
        boardPane
            .getScene()
            .setOnKeyPressed(event -> {
                // KHÓA PHÍM KHI ĐANG XỬ LÝ
                if (isProcessing) {
                    return;
                }

                if (selectedSeed == null) {
                    return;
                }

                int basketType = switch (event.getCode()) {
                    case DIGIT1 -> {
                        System.out.println("[LOG]: " + currentPlayerName + "   Nhận phím 1 → Rổ Gạo");
                        yield 0;
                    }
                    case DIGIT2 -> {
                        System.out.println("[LOG]: " + currentPlayerName + "   Nhận phím 2 → Rổ Thóc");
                        yield 1;
                    }
                    case DIGIT3 -> {
                        System.out.println("[LOG]: " + currentPlayerName + "   Nhận phím 3 → Rổ Ngô");
                        yield 2;
                    }
                    default -> {
                        System.out.println(
                            "[LOG]: " + currentPlayerName + "   Phím không hợp lệ: " + event.getCode()
                        );
                        yield -1;
                    }
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
        // Gửi MOVE command đến server
        int actualSeedType = seeds.get(selectedSeedIndex);
        System.out.println(
            "[CLIENT LOG] Player " + currentPlayerName + " picks seed#" + selectedSeedIndex +
            " (actual type: " + actualSeedType + ") as type " + basketType + 
            " = " + (basketType == actualSeedType ? "✓ CORRECT" : "✗ WRONG")
        );
        MoveRequest req = new MoveRequest(
            gameId,
            selectedSeedIndex,
            basketType
        );
        Message moveMsg = new Message(
            CommandType.MOVE.toString(),
            currentPlayerName,
            req
        );
        try {
            network.send(moveMsg);
            System.out.println(
                "[CLIENT LOG] Sent MOVE: gameId=" +
                    gameId +
                    ", seedIndex=" +
                    selectedSeedIndex +
                    ", choice=" +
                    basketType
            );
        } catch (Exception e) {
            System.out.println(
                "[CLIENT LOG] Error sending MOVE: " + e.getMessage()
            );
            e.printStackTrace();
        }

        // Reset selection và mở khóa phím ngay lập tức
        resetSelection();
        boardPane.requestFocus();
        isProcessing = false;

        // UI sẽ được update qua GAME_UPDATE từ server
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
        System.out.println("[CLIENT LOG] startTimer(" + seconds + "s) bắt đầu");
        new Thread(() -> {
            for (int i = seconds; i >= 0; i--) {
                final int time = i;
                Platform.runLater(() ->
                    timerLabel.setText(
                        String.format("%02d:%02d", time / 60, time % 60)
                    )
                );
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> {
                System.out.println(
                    "[CLIENT LOG] Timer expired, waiting for server to end game"
                );
                // Server will send END_GAME when time up
            });
        })
            .start();
    }

    // === Message Listener ===
     @Override
     public void onMessageReceived(Message msg) {
         System.out.println("[CLIENT LOG] Received from Server: " + msg.getCommand());
         Platform.runLater(() -> {
             // Handle opponent disconnection
             if ("OPPONENT_QUIT".equals(msg.getCommand())) {
                 handleOpponentQuit();
                 return;
             }
             
             if ("START_GAME".equals(msg.getCommand())) {
                // Handle start game from server
                if (msg.getContent() instanceof GameStart gs) {
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   Received START_GAME for game: " + gs.getGameId()
                    );
                    setGameId(gs.getGameId());
                    currentPlayerName = gs.getCurrentPlayerName();
                    opponentName = gs.getOpponentName();
                    
                    // Debug logging to check username vs currentPlayerName
                    System.out.println("[DEBUG START_GAME] username=" + currentPlayerName + ", currentPlayerName=" + currentPlayerName +
                        ", opponentName=" + opponentName + ", gs.getCurrentPlayerName()=" + gs.getCurrentPlayerName() +
                        ", gs.getOpponentName()=" + gs.getOpponentName());
                    
                    seeds = gs.getSeeds();
                    System.out.println(
                        "[CLIENT LOG] Received seeds with seed hash=" + gs.getSeed() + ", first 10 seeds: " + seeds.subList(0, Math.min(10, seeds.size()))
                    );
                    seedTaken = new ArrayList<>(
                        Collections.nCopies(seeds.size(), false)
                    );
                    initializeBoard();
                    startTimer(gs.getTimeLimitSec());
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   Game khởi tạo xong. Click hạt → bấm 1/2/3 để bỏ rổ."
                    );
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   Người chơi hiện tại: " + currentPlayerName + 
                        ", Đối thủ: " + opponentName
                    );
                }
            } else if ("END_GAME".equals(msg.getCommand())) {
                // Handle end game result
                System.out.println("[CLIENT LOG] Received END_GAME message");
                if (msg.getContent() instanceof String content) {
                    System.out.println(
                        "[CLIENT LOG] END_GAME content: " + content
                    );
                    // Parse content with new format: gameId=...,winner=...,yourScore=...,opponentScore=...,yourEloChange=...,opponentEloChange=...
                    String[] pairs = content.split(",");
                    String winner = "",
                        yourScore = "0",
                        opponentScoreValue = "0",
                        yourEloChange = "0";
                    for (String pair : pairs) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            switch (kv[0]) {
                                case "winner" -> winner = kv[1];
                                case "yourScore" -> yourScore = kv[1];
                                case "opponentScore" -> opponentScoreValue = kv[1];
                                case "yourEloChange" -> yourEloChange = kv[1];
                            }
                        }
                    }
                    System.out.println(
                        "[CLIENT LOG] Parsed: winner=" +
                            winner +
                            ", yourScore=" +
                            yourScore +
                            ", opponentScore=" +
                            opponentScoreValue +
                            ", yourEloChange=" +
                            yourEloChange
                    );
                    
                    // Tạo thông báo kết quả đẹp hơn
                    showGameResultDialog(winner, yourScore, opponentScoreValue, yourEloChange);
                }
            } else if ("GAME_UPDATE".equals(msg.getCommand())) {
                // Handle game update from server
                if (msg.getContent() instanceof GameUpdate update) {
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   Received GAME_UPDATE for game: " +
                            update.getGameId()
                    );

                    // Handle animation for the last move
                    int lastIndex = update.getLastIndex();
                    int lastChoice = update.getLastChoice();
                    
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   GAME_UPDATE: lastIndex=" + lastIndex + ", lastChoice=" + lastChoice + 
                        ", currentPlayerName=" + currentPlayerName
                    );
                    System.out.println(
                        "[DEBUG] takenBy map type: " + update.getTakenBy().getClass().getName() + 
                        ", size: " + update.getTakenBy().size() + 
                        ", isEmpty: " + update.getTakenBy().isEmpty()
                    );
                    
                    if (
                        lastIndex != -1 &&
                        lastChoice != -1 &&
                        !seedTaken.get(lastIndex)
                    ) {
                        ImageView seed = seedImages.get(lastIndex);
                        int actual = seeds.get(lastIndex);
                        boolean correct = (lastChoice == actual);
                        
                        // Xác định ai nhặt hạt này
                        System.out.println("[LOG] " + currentPlayerName + " DANH SÁCH TakenBy (size=" + update.getTakenBy().size() + "):");
                        for (Integer key : update.getTakenBy().keySet()) {
                            System.out.println("[LOG] " + currentPlayerName + ": " + key + ": " + update.getTakenBy().get(key));
                        }
                        System.out.println("[DEBUG] Looking for lastIndex=" + lastIndex + " in takenBy map");
                        String playerWhoTook = update.getTakenBy().get(lastIndex);
                        System.out.println("[DEBUG] Result: playerWhoTook=" + playerWhoTook + ", map.containsKey(" + lastIndex + ")=" + update.getTakenBy().containsKey(lastIndex));
                        boolean isCurrentPlayer = currentPlayerName.equals(playerWhoTook);
                        
                        System.out.println(
                            "[LOG]: " + currentPlayerName + "   playerWhoTook=" + playerWhoTook + ", isCurrentPlayer=" + isCurrentPlayer +
                            ", correct=" + correct
                        );

                        if (correct) {
                            // Animate to correct basket
                            VBox targetBasket;
                            Label countLabel;
                            
                            // LOGIC QUAN TRỌNG: Xác định hướng bay dựa trên người nhặt và màn hình hiện tại
                            // Nếu người chơi hiện tại nhặt hạt → bay về bên trái (rổ của mình)
                            // Nếu đối thủ nhặt hạt → bay về bên phải (rổ của đối thủ trên màn hình này)
                            if (isCurrentPlayer) {
                                // Người chơi hiện tại nhặt hạt → bay về bên trái (rổ của mình)
                                switch (lastChoice) {
                                    case 0 -> {
                                        targetBasket = riceBasket;
                                        countLabel = riceCount;
                                    }
                                    case 1 -> {
                                        targetBasket = paddyBasket;
                                        countLabel = paddyCount;
                                    }
                                    case 2 -> {
                                        targetBasket = cornBasket;
                                        countLabel = cornCount;
                                    }
                                    default -> {
                                        targetBasket = null;
                                        countLabel = null;
                                    }
                                }
                            } else {
                                // Đối thủ nhặt hạt → bay về bên phải (rổ của đối thủ trên màn hình này)
                                switch (lastChoice) {
                                    case 0 -> {
                                        targetBasket = riceBasketOpp;
                                        countLabel = riceCountOpp;
                                    }
                                    case 1 -> {
                                        targetBasket = paddyBasketOpp;
                                        countLabel = paddyCountOpp;
                                    }
                                    case 2 -> {
                                        targetBasket = cornBasketOpp;
                                        countLabel = cornCountOpp;
                                    }
                                    default -> {
                                        targetBasket = null;
                                        countLabel = null;
                                    }
                                }
                            }
                            
                            if (targetBasket != null) {
                                // Cách tiếp cận đơn giản hơn: sử dụng tọa độ cố định từ FXML
                                // Dựa vào layoutX và layoutY từ FXML
                                double targetX, targetY;
                                double originX = seed.getX();
                                double originY = seed.getY();
                                
                                // Sử dụng tọa độ cố định cho các rổ
                                if (targetBasket == riceBasket || targetBasket == paddyBasket || targetBasket == cornBasket) {
                                    // Rổ bên trái (của người chơi hiện tại)
                                    // Xấp xỉ tọa độ: layoutX=14, width=110 → centerX=69
                                    // layoutY thay đổi tùy rổ
                                    if (targetBasket == riceBasket) {
                                        targetX = 69 - 142 - 20; // 69 (center của rổ) - 142 (boardPane X) - 20 (nửa hạt)
                                        targetY = 180 - 90 - 20; // 180 (center của rổ gạo) - 90 (boardPane Y) - 20 (nửa hạt)
                                    } else if (targetBasket == paddyBasket) {
                                        targetX = 69 - 142 - 20;
                                        targetY = 280 - 90 - 20; // 280 (center của rổ thóc) - 90 (boardPane Y) - 20
                                    } else { // cornBasket
                                        targetX = 69 - 142 - 20;
                                        targetY = 380 - 90 - 20; // 380 (center của rổ ngô) - 90 (boardPane Y) - 20
                                    }
                                } else {
                                    // Rổ bên phải (của đối thủ)
                                    // Xấp xỉ tọa độ: layoutX=672, width=110 → centerX=727
                                    if (targetBasket == riceBasketOpp) {
                                        targetX = 727 - 142 - 20; // 727 (center của rổ) - 142 (boardPane X) - 20
                                        targetY = 180 - 90 - 20; // 180 (center của rổ gạo) - 90 (boardPane Y) - 20
                                    } else if (targetBasket == paddyBasketOpp) {
                                        targetX = 727 - 142 - 20;
                                        targetY = 280 - 90 - 20; // 280 (center của rổ thóc) - 90 (boardPane Y) - 20
                                    } else { // cornBasketOpp
                                        targetX = 727 - 142 - 20;
                                        targetY = 380 - 90 - 20; // 380 (center của rổ ngô) - 90 (boardPane Y) - 20
                                    }
                                }

                                System.out.println(
                                    "[DEBUG ANIMATION] " + currentPlayerName + " - " +
                                    "playerWhoTook=" + playerWhoTook + ", isCurrentPlayer=" + isCurrentPlayer +
                                    ", targetBasket=" + targetBasket.getId() +
                                    ", targetX=" + targetX + ", targetY=" + targetY
                                );

                                TranslateTransition tt =
                                    new TranslateTransition(
                                        Duration.seconds(0.8),
                                        seed
                                    );
                                tt.setToX(targetX - originX);
                                tt.setToY(targetY - originY);
                                tt.setOnFinished(event -> {
                                    boardPane.getChildren().remove(seed);
                                    seedTaken.set(lastIndex, true);
                                    int count =
                                        Integer.parseInt(countLabel.getText()) +
                                        1;
                                    countLabel.setText(String.valueOf(count));
                                });
                                tt.play();
                            }
                        } else {
                            // Reset position for wrong choice
                            TranslateTransition tt = new TranslateTransition(
                                Duration.seconds(0.5),
                                seed
                            );
                            tt.setToX(0);
                            tt.setToY(0);
                            tt.setOnFinished(event -> {
                                seed.setScaleX(1.0);
                                seed.setScaleY(1.0);
                                seed.setStyle("");
                                selectedSeed = null;
                                selectedSeedIndex = -1;
                            });
                            tt.play();
                        }
                    }

                    // Update taken seeds (for any missed ones, though should be handled above)
                    for (Map.Entry<Integer, String> entry : update
                        .getTakenBy()
                        .entrySet()) {
                        int index = entry.getKey();
                        if (!seedTaken.get(index) && index != lastIndex) {
                            // avoid double handling
                            seedTaken.set(index, true);
                            ImageView seed = seedImages.get(index);
                            boardPane.getChildren().remove(seed);
                        }
                    }
                    
                    // Update scores using the new field names
                    // The server sends scores from the perspective of each player
                    // We need to determine which score belongs to the current client (username)
                    int yourScore, opponentScoreValue;
                    
                    // Debug logging to understand the issue
                    System.out.println("[DEBUG SCORE] " + currentPlayerName +
                        ", update.getCurrentPlayerName()=" + update.getCurrentPlayerName() +
                        ", update.getCurrentPlayerScore()=" + update.getCurrentPlayerScore() +
                        ", update.getOpponentScore()=" + update.getOpponentScore());
                    
                    // Check if the current player name in the update matches this client's currentPlayerName
                    // Both currentPlayerName and update.getCurrentPlayerName() are from the server's perspective
                    if (currentPlayerName != null && currentPlayerName.equals(update.getCurrentPlayerName())) {
                        // This client is the "current player" in the update
                        yourScore = update.getCurrentPlayerScore();
                        opponentScoreValue = update.getOpponentScore();
                        System.out.println("[DEBUG SCORE] " + currentPlayerName + " - Using direct scores (you=" + yourScore + ", opponent=" + opponentScoreValue + ")");
                    } else {
                        // This client is the "opponent" in the update, so swap the scores
                        yourScore = update.getOpponentScore();
                        opponentScoreValue = update.getCurrentPlayerScore();
                        System.out.println("[DEBUG SCORE] " + currentPlayerName + " - Using swapped scores (you=" + yourScore + ", opponent=" + opponentScoreValue + ")");
                    }
                    
                    currentPlayerScore = yourScore;
                    opponentScore = opponentScoreValue;
                    scoreYou.setText(String.valueOf(yourScore));
                    scoreOpponent.setText(String.valueOf(opponentScoreValue));
                    
                    System.out.println(
                        "[LOG]: " + currentPlayerName + "   Cập nhật điểm - " + currentPlayerName + ": " + 
                        currentPlayerScore + ", " + opponentName + ": " + opponentScore
                    );

                    // Check if game finished (all seeds taken)
                    if (update.getTakenBy().size() == seeds.size()) {
                        // Game finished
                        String result;
                        if (currentPlayerScore > opponentScore) {
                            result = "Bạn thắng!";
                        } else if (currentPlayerScore < opponentScore) {
                            result = "Bạn thua!";
                        } else {
                            result = "Hòa!";
                        }
                        timerLabel.setText("Game Over");
                        boardPane.setDisable(true);

                        // TODO: Gửi END_GAME cho Server
                        Message quitMsg = new Message(
                                CommandType.END_GAME.toString(),
                                currentPlayerName,
                                gameId
                        );
                        try {
                            network.send(quitMsg);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }

    // === Hiển thị thông báo kết quả đẹp hơn ===
    private void showGameResultDialog(String winner, String yourScore, String opponentScoreValue, String yourEloChange) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Kết thúc trò chơi");
        
        // Debug logging for END_GAME
        System.out.println("[DEBUG END_GAME] " + currentPlayerName +
            ", currentPlayerName=" + currentPlayerName + ", opponentName=" + opponentName +
            ", winner=" + winner + ", yourScore=" + yourScore + ", opponentScore=" + opponentScoreValue +
            ", yourEloChange=" + yourEloChange);
        
        // Xác định kết quả cho cả 2 người chơi
        boolean isCurrentUserWin = currentPlayerName != null && currentPlayerName.equals(winner);
        boolean isDraw = "Draw".equals(winner);
        
        // Tạo nội dung HTML với thiết kế đẹp hơn
        String content = "<html>" +
                "<head>" +
                "<style>" +
                "body { " +
                "  font-family: 'Segoe UI', Arial, sans-serif; " +
                "  background-color: #FFF9ED; " + /* Nền màu kem giấy */
                "  margin: 0; " +
                "  padding: 20px; " +
                "  color: #5D4037; " + /* Chữ màu nâu đất */
                "} " +
                ".game-result { " +
                "  text-align: center; " +
                "  margin-bottom: 20px; " +
                "  border-bottom: 2px dashed #C78D44; " + /* Đường kẻ phân cách nét đứt màu gỗ */
                "  padding-bottom: 15px; " +
                "} " +
                ".result-title { " +
                "  font-size: 32px; " +
                "  font-weight: bold; " +
                "  margin-bottom: 5px; " +
                "  text-transform: uppercase; " +
                "} " +
                /* Màu sắc dân gian hơn */
                ".win { color: #2E7D32; } " + /* Xanh lá rừng */
                ".lose { color: #C62828; } " + /* Đỏ gạch */
                ".draw { color: #A1887F; } " + /* Nâu nhạt */

                ".players-container { " +
                "  display: flex; " +
                "  justify-content: space-between; " +
                "  align-items: center; " + /* Căn giữa theo chiều dọc */
                "  margin-top: 30px; " + /* Tăng khoảng cách trên */
                "  margin-bottom: 20px; " +
                "} " +
                ".player-card { " +
                "  background: #FFFFFF; " + /* Nền trắng */
                "  border-radius: 12px; " +
                "  padding: 15px; " +
                "  width: 42%; " +
                "  border: 2px solid #E0E0E0; " + /* Viền mặc định nhạt */
                "  box-shadow: 4px 4px 0px rgba(199, 141, 68, 0.2); " + /* Bóng đổ cứng màu gỗ */
                "  text-align: center; " +
                "} " +

                /* Style riêng cho người Thắng/Thua/Hòa */
                ".player-winner { " +
                "  background: #F1F8E9; " + /* Nền xanh rất nhạt */
                "  border-color: #2E7D32; " + /* Viền xanh đậm */
                "} " +
                ".player-loser { " +
                "  background: #FFEBEE; " + /* Nền đỏ rất nhạt */
                "  border-color: #EF9A9A; " + /* Viền đỏ nhạt */
                "} " +
                ".player-draw { " +
                "  background: #EFEBE9; " +
                "  border-color: #A1887F; " +
                "} " +

                ".player-name { " +
                "  font-size: 16px; " +
                "  font-weight: bold; " +
                "  margin-bottom: 5px; " +
                "  color: #3E2723; " + /* Tên người chơi màu nâu đen */
                "} " +
                ".player-score { " +
                "  font-size: 28px; " +
                "  font-weight: 900; " +
                "  margin-bottom: 10px; " +
                "  color: #5D4037; " + /* Điểm số màu nâu đất */
                "} " +
                ".player-elo { " +
                "  font-size: 14px; " +
                "  font-weight: bold; " +
                "  padding: 4px 12px; " +
                "  border-radius: 15px; " +
                "  display: inline-block; " +
                "} " +

                /* Badge Elo style phẳng, màu trầm */
                ".elo-plus { " +
                "  background: #C8E6C9; " + /* Nền xanh nhạt */
                "  color: #1B5E20; " + /* Chữ xanh đậm */
                "} " +
                ".elo-minus { " +
                "  background: #FFCDD2; " + /* Nền đỏ nhạt */
                "  color: #B71C1C; " + /* Chữ đỏ đậm */
                "} " +
                ".elo-neutral { " +
                "  background: #D7CCC8; " + /* Nền nâu xám nhạt */
                "  color: #5D4037; " + /* Chữ nâu đất */
                "} " +

                ".vs-divider { " +
                "  font-size: 24px; " +
                "  font-weight: bold; " +
                "  color: #C78D44; " + /* Chữ VS là màu gỗ */
                "  font-family: 'Segoe UI', serif;" +
                "} " +
                "</style>" +
                "</head>" +
                "<body>" +

                "<div class='game-result'>" +
                "<div class='result-title " + (isDraw ? "draw" : isCurrentUserWin ? "win" : "lose") + "'>";

// Tiêu đề kết quả
        if (isDraw) {
            content += "HÒA";
        } else if (isCurrentUserWin) {
            content += "CHIẾN THẮNG";
        } else {
            content += "THẤT BẠI";
        }

        content += "</div></div>" +
                "<div class='players-container'>" +
                // Thẻ của BẠN
                "<div class='player-card " + (isCurrentUserWin ? "player-winner" : isDraw ? "player-draw" : "player-loser") + "'>" +
                "<div class='player-name'>" + currentPlayerName + " (Bạn)</div>" +
                "<div class='player-score'>" + yourScore + "</div>" +
                "<div class='player-elo " + getEloClass(yourEloChange) + "'>" + getEloText(yourEloChange) + "</div>" +
                "</div>" +

                "<div class='vs-divider'>VS</div>" +

                // Thẻ của ĐỐI THỦ
                "<div class='player-card " + (!isCurrentUserWin && !isDraw ? "player-winner" : isDraw ? "player-draw" : "player-loser") + "'>" +
                "<div class='player-name'>" + opponentName + "</div>" +
                "<div class='player-score'>" + opponentScoreValue + "</div>" +
                "<div class='player-elo " + getOpponentEloClass(yourEloChange) + "'>" + getOpponentEloText(yourEloChange) + "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";
        
        alert.setHeaderText(null);
        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().setPrefHeight(350);
        
        // Sử dụng WebView để hiển thị HTML
        WebView webView = new WebView();
        webView.getEngine().loadContent(content);
        webView.setPrefSize(480, 320);
        
        alert.getDialogPane().setContent(webView);
        gameFinished = true;
        alert.showAndWait();
        
        // Khi đóng dialog, quay về danh sách người chơi
        returnToPlayerList();
    }
    
    // === Helper methods cho Elo ===
    private String getEloClass(String eloChange) {
        try {
            int elo = Integer.parseInt(eloChange);
            if (elo > 0) return "elo-plus";
            if (elo < 0) return "elo-minus";
            return "elo-neutral";
        } catch (Exception e) {
            return "elo-neutral";
        }
    }
    
    private String getEloText(String eloChange) {
        try {
            int elo = Integer.parseInt(eloChange);
            if (elo > 0) return "Elo +" + elo;
            if (elo < 0) return "Elo " + elo;
            return "Elo không đổi";
        } catch (Exception e) {
            return "Elo: " + eloChange;
        }
    }
    
    private String getOpponentEloClass(String eloChange) {
        // Đối thủ có elo ngược lại
        try {
            int elo = Integer.parseInt(eloChange);
            if (elo > 0) return "elo-minus";  // Nếu bạn cộng elo, đối thủ trừ
            if (elo < 0) return "elo-plus";   // Nếu bạn trừ elo, đối thủ cộng
            return "elo-neutral";
        } catch (Exception e) {
            return "elo-neutral";
        }
    }
    
    private String getOpponentEloText(String eloChange) {
        try {
            int elo = Integer.parseInt(eloChange);
            if (elo > 0) return "Elo -" + elo;   // Đối thủ trừ elo
            if (elo < 0) return "Elo +" + Math.abs(elo);  // Đối thủ cộng elo
            return "Elo không đổi";
        } catch (Exception e) {
            return "Elo: ?";
        }
    }

    // === XỬ LÝ THOÁT GAME ===
    private void onExitGameClicked() {
        if (gameFinished) {
            // Nếu game đã kết thúc, quay về danh sách người chơi ngay
            returnToPlayerList();
        } else {
            // Nếu game đang chơi, hỏi xác nhận
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Thoát trò chơi");
            confirmDialog.setHeaderText(null);
            confirmDialog.setContentText("Bạn chắc chắn muốn thoát? Trò chơi sẽ bị dừng lại.");
            
            var result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                quitGame();
            }
        }
    }

    private void quitGame() {
        System.out.println("[CLIENT LOG] Player " + currentPlayerName + " is quitting game " + gameId);
        
        try {
            // Gửi QUIT_GAME message đến server
            Message quitMsg = new Message(
                CommandType.QUIT_GAME.toString(),
                currentPlayerName,
                gameId
            );
            network.send(quitMsg);
            System.out.println("[CLIENT LOG] Sent QUIT_GAME message for gameId=" + gameId);
        } catch (Exception e) {
            System.out.println("[CLIENT LOG] Error sending QUIT_GAME: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Quay về danh sách người chơi
        Platform.runLater(this::returnToPlayerList);
    }

    private void returnToPlayerList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_list.fxml"));
            Parent root = loader.load();
            PlayerListController controller = loader.getController();

            if (this.network != null) {
                controller.setCurrentUser(this.currentPlayerName);
                controller.setNetwork(this.network);
            }
            
            // Use stored primaryStage instead of trying to get from UI element
            if (primaryStage != null) {
                controller.setPrimaryStage(primaryStage);
                primaryStage.setScene(new Scene(root));
                primaryStage.setTitle("Danh sách người chơi");
                primaryStage.show();
            } else {
                // Fallback: try to get stage from exitButton if primaryStage is not set
                Stage stage = (Stage) exitButton.getScene().getWindow();
                if (stage != null) {
                    controller.setPrimaryStage(stage);
                    stage.setScene(new Scene(root));
                    stage.setTitle("Danh sách người chơi");
                    stage.show();
                } else {
                    System.err.println("[CLIENT LOG] Cannot get stage reference");
                    return;
                }
            }
            
            System.out.println("[CLIENT LOG] Returned to player list");
        } catch (IOException e) {
            System.out.println("[CLIENT LOG] Error loading player list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOpponentQuit() {
        System.out.println("[CLIENT LOG] Opponent quit the game");
        gameFinished = true;
        
        // Show notification that opponent quit
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đối thủ rời khỏi trò chơi");
        alert.setHeaderText(null);
        alert.setContentText("Đối thủ của bạn đã rời khỏi trò chơi. Bạn được coi là người chiến thắng.");
        
        alert.showAndWait();
        
        // Return to player list
        returnToPlayerList();
    }

    public void setPlayers(String currentUser, String opponent) {
        this.currentPlayerName = currentUser;
        this.opponentName = opponent;
        System.out.println("[GameController] Players set: you=" + currentUser + " opponent=" + opponent);
    }

    public Label getScoreYou() {
        return scoreYou;
    }

    public void setScoreYou(Label scoreYou) {
        this.scoreYou = scoreYou;
    }

    public Label getScoreOpponent() {
        return scoreOpponent;
    }

    public void setScoreOpponent(Label scoreOpponent) {
        this.scoreOpponent = scoreOpponent;
    }

    public Label getTimerLabel() {
        return timerLabel;
    }

    public void setTimerLabel(Label timerLabel) {
        this.timerLabel = timerLabel;
    }

    public Pane getBoardPane() {
        return boardPane;
    }

    public void setBoardPane(Pane boardPane) {
        this.boardPane = boardPane;
    }

    public VBox getRiceBasket() {
        return riceBasket;
    }

    public void setRiceBasket(VBox riceBasket) {
        this.riceBasket = riceBasket;
    }

    public VBox getPaddyBasket() {
        return paddyBasket;
    }

    public void setPaddyBasket(VBox paddyBasket) {
        this.paddyBasket = paddyBasket;
    }

    public VBox getCornBasket() {
        return cornBasket;
    }

    public void setCornBasket(VBox cornBasket) {
        this.cornBasket = cornBasket;
    }

    public Label getRiceCount() {
        return riceCount;
    }

    public void setRiceCount(Label riceCount) {
        this.riceCount = riceCount;
    }

    public Label getPaddyCount() {
        return paddyCount;
    }

    public void setPaddyCount(Label paddyCount) {
        this.paddyCount = paddyCount;
    }

    public Label getCornCount() {
        return cornCount;
    }

    public void setCornCount(Label cornCount) {
        this.cornCount = cornCount;
    }

    public VBox getRiceBasketOpp() {
        return riceBasketOpp;
    }

    public void setRiceBasketOpp(VBox riceBasketOpp) {
        this.riceBasketOpp = riceBasketOpp;
    }

    public VBox getPaddyBasketOpp() {
        return paddyBasketOpp;
    }

    public void setPaddyBasketOpp(VBox paddyBasketOpp) {
        this.paddyBasketOpp = paddyBasketOpp;
    }

    public VBox getCornBasketOpp() {
        return cornBasketOpp;
    }

    public void setCornBasketOpp(VBox cornBasketOpp) {
        this.cornBasketOpp = cornBasketOpp;
    }

    public Label getRiceCountOpp() {
        return riceCountOpp;
    }

    public void setRiceCountOpp(Label riceCountOpp) {
        this.riceCountOpp = riceCountOpp;
    }

    public Label getPaddyCountOpp() {
        return paddyCountOpp;
    }

    public void setPaddyCountOpp(Label paddyCountOpp) {
        this.paddyCountOpp = paddyCountOpp;
    }

    public Label getCornCountOpp() {
        return cornCountOpp;
    }

    public void setCornCountOpp(Label cornCountOpp) {
        this.cornCountOpp = cornCountOpp;
    }

    public List<Integer> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<Integer> seeds) {
        this.seeds = seeds;
    }

    public List<ImageView> getSeedImages() {
        return seedImages;
    }

    public void setSeedImages(List<ImageView> seedImages) {
        this.seedImages = seedImages;
    }

    public List<Boolean> getSeedTaken() {
        return seedTaken;
    }

    public void setSeedTaken(List<Boolean> seedTaken) {
        this.seedTaken = seedTaken;
    }

    public ImageView getSelectedSeed() {
        return selectedSeed;
    }

    public void setSelectedSeed(ImageView selectedSeed) {
        this.selectedSeed = selectedSeed;
    }

    public int getSelectedSeedIndex() {
        return selectedSeedIndex;
    }

    public void setSelectedSeedIndex(int selectedSeedIndex) {
        this.selectedSeedIndex = selectedSeedIndex;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean processing) {
        isProcessing = processing;
    }

    public Network getNetwork() {
        return network;
    }

    public String getGameId() {
        return gameId;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public int getCurrentPlayerScore() {
        return currentPlayerScore;
    }

    public void setCurrentPlayerScore(int currentPlayerScore) {
        this.currentPlayerScore = currentPlayerScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }
}
