package client.controller;

import client.network.MessageListener;
import client.network.Network;
import client.controller.InviteNotificationManager;  // ✅ THÊM IMPORT
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import server.common.StatusType;
import server.dto.Message;
import server.dto.Status;

import java.io.IOException;

public class MainLobbyController implements MessageListener {

    @FXML private Label playerInfo;
    @FXML private Button btnOnline;
    @FXML private Button btnRanking;
    @FXML private Button btnHistory;
    @FXML private Button btnLogout;
    @FXML private Button btnTestInvite; // Nút test

    private Network network;
    private Stage primaryStage;  // ✅ THÊM: Lưu Stage chính

    // ✅ THÊM: Set Stage từ nơi load FXML (LoginController hoặc MainTest)
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        // Khởi tạo InviteNotificationManager khi có Stage
        InviteNotificationManager.getInstance().setPrimaryStage(stage);
        System.out.println("[MainLobbyController] PrimaryStage set & InviteManager ready!");
    }

    public void setNetwork(Network network) {
        this.network = network;
        if (this.network != null) {
            this.network.addMessageListener(this);
            System.out.println("[MainLobbyController] Network set successfully and listener registered.");
        } else {
            System.err.println("[MainLobbyController] Error: Network is null, cannot set up connection.");
            Platform.runLater(() -> {
                showAlert("Lỗi Mạng", "Không thể thiết lập kết nối mạng.");
            });
        }
    }

    @FXML
    public void initialize() {
        // 👤 Gán thông tin người chơi sau khi đăng nhập
        playerInfo.setText("Xin chào, Nguyen Van A | Elo: 1800 | Hạng: #25");
        System.out.println("[MainLobbyController] Initialized with player info: Nguyen Van A");

        // 👉 Điều hướng sang các màn hình khác
        btnOnline.setOnAction(e -> openPlayerList());
        btnRanking.setOnAction(e -> openRanking());
        btnHistory.setOnAction(e -> openHistory());
        btnLogout.setOnAction(this::logout);

    }

    @FXML
    public void testInvite() {  // ✅ ĐỔI: public thay vì private để FXML nhận diện
        System.out.println("[MainLobby] Bắt đầu test 3 lời mời giả lập...");

        // Kiểm tra Stage đã set chưa
        if (primaryStage == null) {
            System.err.println("[MainLobby] PrimaryStage chưa được set! Gọi setPrimaryStage() trước.");
            return;
        }

        // Giả lập 3 lời mời với độ trễ
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> InviteNotificationManager.getInstance().showInvite("DragonKing"));

                Thread.sleep(800);
                Platform.runLater(() -> InviteNotificationManager.getInstance().showInvite("NinjaShadow"));

                Thread.sleep(800);
                Platform.runLater(() -> InviteNotificationManager.getInstance().showInvite("PhoenixRise"));
            } catch (InterruptedException ignored) {}
        }).start();
    }

    // Các phương thức điều hướng (giữ nguyên)
    private void openPlayerList() {
        System.out.println("[MainLobbyController] Opening player list screen...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_list.fxml"));
            Parent root = loader.load();
            PlayerListController controller = loader.getController();
            controller.setNetwork(this.network);
            // ✅ Truyền Stage cho controller mới nếu cần
            if (controller instanceof HasPrimaryStage) {  // Interface tùy chọn
                ((HasPrimaryStage) controller).setPrimaryStage(primaryStage);
            }
            System.out.println("[MainLobbyController] player_list.fxml loaded successfully.");

            network.removeMessageListener(this);
            System.out.println("[MainLobbyController] Listener removed for PlayerListController.");

            Stage stage = (Stage) btnOnline.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Player List");
            stage.show();
            System.out.println("[MainLobbyController] Switched to Player List scene.");
        } catch (IOException e) {
            System.err.println("[MainLobbyController] Error loading player_list.fxml: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Giao Diện", "Không thể tải màn hình danh sách người chơi: " + e.getMessage());
            });
        }
    }

    private void openRanking() {
        System.out.println("[MainLobbyController] Opening ranking screen...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/leaderboard.fxml"));
            Parent root = loader.load();
            LeaderboardController controller = loader.getController();
            controller.setNetwork(this.network);
            System.out.println("[MainLobbyController] ranking.fxml loaded successfully.");

            network.removeMessageListener(this);
            System.out.println("[MainLobbyController] Listener removed for RankingController.");

            Stage stage = (Stage) btnRanking.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ranking");
            stage.show();
            System.out.println("[MainLobbyController] Switched to Ranking scene.");
        } catch (IOException e) {
            System.err.println("[MainLobbyController] Error loading ranking.fxml: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Giao Diện", "Không thể tải màn hình bảng xếp hạng: " + e.getMessage());
            });
        }
    }

    private void openHistory() {
        System.out.println("[MainLobbyController] Opening history screen...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MatchHistory.fxml"));
            Parent root = loader.load();
            MatchHistoryController controller = loader.getController();
            controller.setNetwork(this.network);
            System.out.println("[MainLobbyController] history.fxml loaded successfully.");

            network.removeMessageListener(this);
            System.out.println("[MainLobbyController] Listener removed for HistoryController.");

            Stage stage = (Stage) btnHistory.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("History");
            stage.show();
            System.out.println("[MainLobbyController] Switched to History scene.");
        } catch (IOException e) {
            System.err.println("[MainLobbyController] Error loading history.fxml: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Giao Diện", "Không thể tải màn hình lịch sử đấu: " + e.getMessage());
            });
        }
    }

    private void logout(ActionEvent event) {  // ✅ SỬA: import ActionEvent
        System.out.println("[MainLobbyController] Logging out...");
        try {
            Message msg = new Message("LOGOUT", "CLIENT", null);
            network.send(msg);
            System.out.println("[MainLobbyController] Logout request sent.");
        } catch (IOException e) {
            System.err.println("[MainLobbyController] Error sending logout request: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Đăng Xuất", "Không thể gửi yêu cầu đăng xuất đến server: " + e.getMessage());
            });
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        System.out.println("[MainLobbyController] Received message: command=" + (msg != null ? msg.getCommand() : "null"));
        if (msg != null && "LOGOUT_RESPONSE".equals(msg.getCommand())) {
            Status status = (Status) msg.getContent();
            System.out.println("[MainLobbyController] LOGOUT_RESPONSE received: status=" + (status != null ? status.getType() : "null") + ", content=" + (status != null ? status.getContent() : "null"));
            if (status != null) {
                Platform.runLater(() -> {
                    if (status.getType() == StatusType.SUCCESS) {
                        System.out.println("[MainLobbyController] Logout successful, switching to login screen...");
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                            Parent root = loader.load();
                            LoginController controller = loader.getController();
                            controller.setNetwork(this.network);
                            System.out.println("[MainLobbyController] login.fxml loaded successfully.");

                            network.removeMessageListener(this);
                            System.out.println("[MainLobbyController] Listener removed for LoginController.");

                            Stage stage = (Stage) btnLogout.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Login");
                            stage.show();
                            System.out.println("[MainLobbyController] Switched to Login scene.");
                        } catch (IOException e) {
                            System.err.println("[MainLobbyController] Error loading login.fxml: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("Lỗi Giao Diện", "Không thể tải màn hình đăng nhập: " + e.getMessage());
                        }
                    } else {
                        System.err.println("[MainLobbyController] Logout failed: " + status.getContent());
                        showAlert("Lỗi Đăng Xuất", "Đăng xuất thất bại: " + status.getContent());
                    }
                });
            } else {
                System.err.println("[MainLobbyController] Error: Status is null in LOGOUT_RESPONSE");
                Platform.runLater(() -> {
                    showAlert("Lỗi Dữ Liệu", "Phản hồi từ server không hợp lệ.");
                });
            }
        } else {
            System.out.println("[MainLobbyController] Ignored message: command=" + (msg != null ? msg.getCommand() : "null"));
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            System.out.println("[MainLobbyController] Showing alert: title=" + title + ", message=" + message);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        if (network != null) {
            System.out.println("[MainLobbyController] Cleaning up: removing listener and closing network.");
            network.removeMessageListener(this);
            try {
                network.close();
            } catch (IOException e) {
                System.err.println("[MainLobbyController] Error closing network: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

// ✅ TÙY CHỌN: Interface để truyền Stage cho các controller khác
interface HasPrimaryStage {
    void setPrimaryStage(Stage stage);
}