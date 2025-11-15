package client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import client.network.Network;

/**
 * Controller cho màn hình phòng chờ (Waiting Room).
 * Giúp test các thay đổi UI như khi người chơi còn lại chấp nhận lời mời.
 */
public class WaitingRoomController {

    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    private Label player1Name;
    @FXML
    private Label player1Elo;
    @FXML
    private Label player1Status;

    @FXML
    private Label player2Name;
    @FXML
    private Label player2Elo;
    @FXML
    private Label player2Status;

    @FXML
    private Label waitingLabel;
    @FXML
    private Button exitButton;

    @FXML
    private VBox player1Box;
    @FXML
    private VBox player2Box;

    @FXML
    public void initialize() {
        // Gán dữ liệu giả lập ban đầu
        player1Name.setText("Nguyễn Văn A");
        player1Elo.setText("ELO: 1250");
        player1Status.setText("Đã sẵn sàng");

        player2Name.setText("Trần Thị C");
        player2Elo.setText("ELO: 1320");
        player2Status.setText("Đang chờ...");
        player2Status.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-background-radius: 15;");

        waitingLabel.setText("Đang chờ người chơi khác sẵn sàng...");

        // 🧪 TEST: Giả lập "server" gửi sự kiện sau 5 giây
        simulateOpponentAccept();
    }

    /**
     * Hàm này mô phỏng việc người chơi 2 nhấn "Chấp nhận" sau khi được mời.
     * Trong thực tế, bạn sẽ gọi hàm này từ luồng nhận dữ liệu server.
     */
    private void simulateOpponentAccept() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Giả lập người chơi kia bấm OK sau 5 giây
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(this::setPlayer2Ready); // cập nhật UI trên luồng JavaFX
        }).start();
    }

    /**
     * 🟢 Hàm cập nhật giao diện khi người chơi 2 đã sẵn sàng
     * → Đây là hàm bạn sẽ gọi khi nhận tín hiệu từ server.
     */
    public void setPlayer2Ready() {
        player2Status.setText("Đã sẵn sàng");
        player2Status.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-background-radius: 15;");

        waitingLabel.setText(" Tất cả người chơi đã sẵn sàng! Trận đấu sắp bắt đầu...");
        waitingLabel.setStyle("-fx-background-color: lightgreen; -fx-font-weight: bold; -fx-background-radius: 15;");
    }

    /**
     * 📤 Sự kiện khi nhấn nút "Thoát phòng"
     */
    @FXML
    private void onExitRoom() {
        System.out.println("🛑 Người chơi đã rời phòng chờ.");

    }
}
