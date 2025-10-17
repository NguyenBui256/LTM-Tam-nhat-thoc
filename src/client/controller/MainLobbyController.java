package client.controller;

import client.network.Network;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.stage.Stage;
import server.common.StatusType;
import server.dto.Message;
import server.dto.Status;

import java.io.IOException;

public class MainLobbyController {

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

    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    public void initialize() {
        // 👤 Gán thông tin người chơi sau khi đăng nhập
        playerInfo.setText("Xin chào, Nguyen Van A | Elo: 1800 | Hạng: #25");

        // 👉 Điều hướng sang các màn hình khác
        btnOnline.setOnAction(e -> openPlayerList());
        btnRanking.setOnAction(e -> openRanking());
        btnHistory.setOnAction(e -> openHistory());
        btnLogout.setOnAction(this::logout); // ✅ Gán sự kiện đúng cách
    }

    private void openPlayerList() {
        System.out.println("➡️ Chuyển sang màn hình danh sách người chơi online");
        // TODO: load player_list.fxml
    }

    private void openRanking() {
        System.out.println("🏆 Chuyển sang bảng xếp hạng");
        // TODO: load ranking.fxml
    }

    private void openHistory() {
        System.out.println("📜 Mở lịch sử đấu của người chơi");
        // TODO: load history.fxml
    }

    private void logout(javafx.event.ActionEvent event) {
        System.out.println("🔚 Đang đăng xuất...");

        try {
            // Gửi yêu cầu logout đến server
            network.send(new Message("LOGOUT", "CLIENT", null));

            // Nhận phản hồi từ server
            Message received = network.receive();
            Status status = (Status) received.getContent();

            if (status.getType() == StatusType.SUCCESS) {
                System.out.println(" Đăng xuất thành công, quay về màn hình đăng nhập");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                // Truyền lại network cho LoginController
                LoginController controller = loader.getController();
                controller.setNetwork(this.network);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } else {
                System.err.println(" Đăng xuất thất bại: " + status.getContent());
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            System.err.println("⚠ Lỗi khi đăng xuất hoặc tải lại màn hình đăng nhập.");
        }
    }
}
