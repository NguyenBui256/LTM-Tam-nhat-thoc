package client.controller;

import client.network.MessageListener;
import client.network.Network;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import common.StatusType;
import dto.Message;
import dto.Status;

import java.io.IOException;

public class MainLobbyController implements MessageListener {

    @FXML
    private Label playerInfo;
    @FXML
    private Button btnOnline;
    @FXML
    private Button btnRanking;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnTestInvite; // Nút test

    private Network network;
    private Stage primaryStage; // ✅ THÊM: Lưu Stage chính
    // Tên người chơi hiện tại được truyền từ LoginController
    private String currentUser;

    // ✅ THÊM: Set Stage từ nơi load FXML (LoginController hoặc MainTest)
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        // Khởi tạo InviteNotificationManager khi có Stage
        InviteNotificationManager.getInstance().setPrimaryStage(stage);
        System.out.println("[MainLobbyController] PrimaryStage set & InviteManager ready!");
    }

    public void setNetwork(Network network) {
        this.network = network;
        InviteNotificationManager.getInstance().setNetwork(network);
        if (this.network != null) {
            this.network.addMessageListener(this);
            System.out.println("[MainLobbyController] Network set successfully and listener registered.");
            // Nếu Network chứa username (được set sau khi đăng nhập), truyền luôn vào
            // controller
            try {
                String nu = this.network.getCurrentUser();
                if (nu != null && !nu.isBlank()) {
                    setCurrentUser(nu);
                }
            } catch (Exception ignore) {
            }
        } else {
            System.err.println("[MainLobbyController] Error: Network is null, cannot set up connection.");
            Platform.runLater(() -> {
                showAlert("Lỗi Mạng", "Không thể thiết lập kết nối mạng.");
            });
        }
    }

    // Được gọi từ LoginController để truyền tên người chơi hiện tại
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        System.out.println("[MainLobbyController] currentUser set to: " + currentUser);
        // Cập nhật thông tin hiển thị nếu UI đã được khởi tạo
        if (playerInfo != null && currentUser != null && !currentUser.isBlank()) {
            playerInfo.setText("Xin chào, " + currentUser);
        }
    }

    @FXML
    public void initialize() {

        // 👉 Điều hướng sang các màn hình khác
        btnOnline.setOnAction(e -> openPlayerList());
        btnRanking.setOnAction(e -> openRanking());
        btnHistory.setOnAction(e -> openHistory());
        btnLogout.setOnAction(this::logout);

    }

    @FXML
    public void testInvite() { // ✅ ĐỔI: public thay vì private để FXML nhận diện
        System.out.println("[MainLobby] Bắt đầu test 3 lời mời giả lập...");

        // Kiểm tra Stage đã set chưa
        if (primaryStage == null) {
            System.err.println("[MainLobby] PrimaryStage chưa được set! Gọi setPrimaryStage() trước.");
            return;
        }
    }

    // Các phương thức điều hướng (giữ nguyên)
    private void openPlayerList() {
        System.out.println("[MainLobbyController] Opening player list screen...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/player_list.fxml"));
            Parent root = loader.load();
            PlayerListController controller = loader.getController();
            controller.setNetwork(this.network);
            // Truyền tên user hiện tại sang PlayerListController (nếu có)
            controller.setCurrentUser(this.currentUser);
            // ✅ Truyền Stage cho controller mới nếu cần
            if (controller instanceof HasPrimaryStage) { // Interface tùy chọn
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
            controller.setCurrentUser(this.currentUser);
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
            controller.setCurrentUser(this.currentUser);
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

    private void logout(ActionEvent event) { // ✅ SỬA: import ActionEvent
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
        System.out.println(
                "[MainLobbyController] Received message: command=" + (msg != null ? msg.getCommand() : "null"));
        if (msg == null)
            return;
        switch (msg.getCommand()) {
            case "ACCEPT_NOTIFY" -> {
                String content = msg.getContent() instanceof String ? (String) msg.getContent()
                        : "Đối phương đã chấp nhận lời mời. Vào phòng chờ...";
                Platform.runLater(() -> {
                    try {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/waiting_room.fxml"));
                            Parent root = loader.load();
                            WaitingRoomController controller = loader.getController();
                            // Optionally pass network to waiting room: controller.setNetwork(network);
                            primaryStage.setScene(new Scene(root));
                            primaryStage.setTitle("Waiting Room");
                            primaryStage.show();
                        } catch (IOException e) {
                            System.err.println("Lỗi load waiting_room.fxml: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        // fallback to simple alert/notification
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Đã chấp nhận lời mời");
                        alert.setHeaderText(null);
                        alert.setContentText(content);
                        alert.showAndWait();
                    }
                });
            }
            case "REJECT_NOTIFY" -> {
                String content = msg.getContent() instanceof String ? (String) msg.getContent()
                        : "Đối phương đã từ chối lời mời.";
                Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Đã từ chối lời mời");
                    alert.setHeaderText(null);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
            }
            // Các xử lý cũ giữ nguyên bên dưới
            case "LOGOUT_RESPONSE" -> {
                Status status = (Status) msg.getContent();
                System.out.println("[MainLobbyController] LOGOUT_RESPONSE received: status="
                        + (status != null ? status.getType() : "null") + ", content="
                        + (status != null ? status.getContent() : "null"));
                if (status != null) {
                    Platform.runLater(() -> {
                        if (status.getType() == StatusType.SUCCESS) {
                            System.out.println("[MainLobbyController] Logout successful, switching to login screen...");
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                                
                                Parent root = loader.load();


                                LoginController controller = loader.getController();
                                controller.setNetwork(network);
                                
                               
                                Stage stage = (Stage) btnLogout.getScene().getWindow();
                                stage.setScene(new Scene(root));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert("Lỗi", "Không thể trở về màn hình đăng nhập." + ex.getMessage());
                            }
                        } else {
                            showAlert("Lỗi Đăng Xuất", status.getContent());
                        }
                    });
                }
            }
            // Bạn có thể bổ sung thêm các trường hợp khác nếu cần
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            System.out.println("[MainLobbyController] Showing alert: title=" + title + ", message=" + message);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
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