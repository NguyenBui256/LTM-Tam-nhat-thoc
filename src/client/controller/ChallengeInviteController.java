package client.controller;

import client.network.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import server.dto.Message;

import java.io.IOException;

public class ChallengeInviteController {

    @FXML private ImageView avatarImage;
    @FXML private Label playerName;
    @FXML private Label playerElo;
    @FXML private Text inviteText;
    @FXML private Button acceptButton;
    @FXML private Button declineButton;

    private Network network;
    private String inviter;

    // Gọi từ InviteNotificationManager
    public void setInviterInfo(String inviter, String elo, String message) {
        this.inviter = inviter;
        Platform.runLater(() -> {
            playerName.setText(inviter);
            playerElo.setText("Điểm Elo: " + elo);
            inviteText.setText(message);
            // Có thể tải avatar thật ở đây
            avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/user-interface.png")));
        });
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    private void onAcceptClicked() {
        sendResponse("ACCEPT_INVITE");
        closeDialog();
    }

    @FXML
    private void onDeclineClicked() {
        sendResponse("DECLINE_INVITE");
        closeDialog();
    }

    private void sendResponse(String command) {
        if (network != null && inviter != null) {
            try {
                Message msg = new Message(command, inviter, null);
                network.send(msg);
                System.out.println("[ChallengeInviteController] Gửi " + command + " tới " + inviter);
            } catch (IOException e) {
                System.err.println("Lỗi gửi phản hồi lời mời: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) acceptButton.getScene().getWindow();
        stage.close();
    }
}