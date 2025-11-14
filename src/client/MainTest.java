package client;

import client.controller.InviteNotificationManager;
import client.controller.LoginController;
import client.controller.MainLobbyController;
import client.network.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.dto.LoginRequest;
import server.dto.Message;
import javafx.scene.control.Alert;

import java.io.IOException;

public class MainTest extends Application {

    private Network network;
    private static final String SERVER_IP = "127.0.0.1";  // Thay IP server nếu cần
    private static final int SERVER_PORT = 2206;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Kết nối server: " + SERVER_IP + ":" + SERVER_PORT);

        // Bước 1: Tạo kết nối mạng
        try {
            network = new Network(SERVER_IP, SERVER_PORT);
            System.out.println("Kết nối server thành công!");

            // Bước 2: Tự động đăng nhập (admin/123)
            sendAutoLogin();

            // Bước 3: Chờ 1.5s rồi load Lobby (đảm bảo server trả lời)
            Platform.runLater(() -> {
                try { Thread.sleep(1500); } catch (Exception ignored) {}
                loadMainLobby(primaryStage);
            });

        } catch (IOException e) {
            System.err.println("Không thể kết nối server: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit(primaryStage, "Lỗi Kết Nối", "Không thể kết nối đến server:\n" + e.getMessage());
        }
    }

    private void sendAutoLogin() {
        new Thread(() -> {
            try {
                Thread.sleep(800); // Chờ kết nối ổn định
                LoginRequest loginRequest = new LoginRequest("admin", "123");
                Message msg = new Message("LOGIN", "CLIENT", loginRequest);
                System.out.println("[MainTest] Gửi đăng nhập tự động: admin/123");
                network.send(msg);
            } catch (Exception e) {
                System.err.println("Lỗi gửi đăng nhập tự động: " + e.getMessage());
            }
        }).start();
    }

    private void loadMainLobby(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
            Parent root = loader.load();

            MainLobbyController controller = loader.getController();
            controller.setNetwork(network);
            controller.setPrimaryStage(stage); // QUAN TRỌNG: truyền Stage

            // TỰ ĐỘNG TEST 3 LỜI MỜI SAU KHI VÀO LOBBY
            Platform.runLater(() -> {
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                controller.testInvite();
            });

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Main Lobby - TEST MODE");
            stage.setWidth(800);
            stage.setHeight(600);
            stage.show();

            System.out.println("[MainTest] Đã vào Lobby! 3 lời mời sẽ hiện sau 1 giây...");

        } catch (IOException e) {
            System.err.println("Lỗi load main_lobby.fxml: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit(stage, "Lỗi Giao Diện", "Không thể tải Lobby:\n" + e.getMessage());
        }
    }

    private void showErrorAndExit(Stage stage, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(stage);
            alert.showAndWait();
            Platform.exit();
        });
    }

    @Override
    public void stop() {
        if (network != null) {
            try {
                network.close();
                System.out.println("[MainTest] Đã đóng kết nối mạng.");
            } catch (IOException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
    }

    // HÀM MAIN RIÊNG ĐỂ TEST
    public static void main(String[] args) {
        launch(args);
    }
}