package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class ChallengeInviteController {

    @FXML
    private ImageView avatarImage;

    @FXML
    private Label playerName;

    @FXML
    private Label playerElo;

    @FXML
    private Button acceptButton;

    @FXML
    private Button declineButton;

    @FXML
    private Text inviteText;

    @FXML
    public void initialize() {
        // Mock dữ liệu mẫu cho UI
        playerName.setText("Người chơi A");
        playerElo.setText("Điểm Elo: 1850");
        inviteText.setText("Mời bạn tham gia trận đấu phân loại hạt");

        try {
            // Load ảnh avatar mẫu
            Image img = new Image(getClass().getResourceAsStream("@../images/user.png"));
            avatarImage.setImage(img);
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh user.png");
        }
    }

    @FXML
    private void onAcceptClicked() {
        System.out.println("✅ Đã chấp nhận lời mời thách đấu!");
        // TODO: sau này gửi tín hiệu đến server
    }

    @FXML
    private void onDeclineClicked() {
        System.out.println("❌ Đã từ chối lời mời thách đấu!");
        // TODO: sau này gửi tín hiệu hủy tới server
    }
}
