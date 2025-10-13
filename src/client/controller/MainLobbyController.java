package client.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainLobbyController {

    @FXML
    private Label playerInfo;
    @FXML
    private Button btnOnline;
    @FXML
    private Button btnRanking;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
        // 👤 Gán thông tin người chơi sau khi đăng nhập
        playerInfo.setText("Xin chào, Nguyen Van A | Elo: 1800 | Hạng: #25");

        // 👉 Điều hướng sang các màn hình khác
        btnOnline.setOnAction(e -> openPlayerList());
        btnRanking.setOnAction(e -> openRanking());
        btnSettings.setOnAction(e -> openSettings());
        btnLogout.setOnAction(e -> logout());
    }

    private void openPlayerList() {
        System.out.println("➡️ Chuyển sang màn hình danh sách người chơi online");
        // TODO: load FXML danh sách người chơi
    }

    private void openRanking() {
        System.out.println("🏆 Chuyển sang bảng xếp hạng");
    }

    private void openSettings() {
        System.out.println("⚙️ Mở cài đặt game");
    }

    private void logout() {
        System.out.println("🔚 Đăng xuất người dùng");
        // TODO: quay lại màn hình đăng nhập
    }
}
