package client.controller;

import client.network.MessageListener;
import client.network.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import dto.Message;
import dto.InviteRequest;
import dto.Status;
import common.StatusType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class ChallengeInviteController implements MessageListener {

    @FXML
    private ImageView avatarImage;
    @FXML
    private Label playerName;
    @FXML
    private Label playerElo;
    @FXML
    private Text inviteText;
    @FXML
    private Button acceptButton;
    @FXML
    private Button declineButton;

    private Network network;
    private String inviter;
    private InviteRequest inviteRequest;
    private Stage primaryStage;

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

    public void setInviteRequest(InviteRequest req) {
        this.inviteRequest = req;
        this.inviter = req.getInviter();
        Platform.runLater(() -> {
            playerName.setText(inviter);
            playerElo.setText("");
            inviteText.setText(inviter + " đã mời bạn tham gia trận đấu. Bạn có muốn chấp nhận?");
            avatarImage.setImage(new Image(getClass().getResourceAsStream("/images/user-interface.png")));
            if (!acceptButton.getScene().getWindow().isShowing()) {
                ((Stage) acceptButton.getScene().getWindow()).show();
            }
        });
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void onAcceptClicked() {
        sendResponse("ACCEPT");
        closeDialog();
    }

    @FXML
    private void onDeclineClicked() {
        sendResponse("REJECT");
        closeDialog();
    }

    private void sendResponse(String command) {
        if (network != null && inviteRequest != null) {
            try {
                Message msg = new Message(command, "CLIENT", inviteRequest);
                network.send(msg);
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

    @Override
    public void onMessageReceived(Message msg) {
        if (msg == null)
            return;

        switch (msg.getCommand()) {
            case "ACCEPT_RESPONSE" -> handleAccept(msg);
            case "REJECT_RESPONSE" -> handleReject(msg);
        }
    }

    private void handleAccept(Message msg) {
        if (msg == null)
            return;
        Object content = msg.getContent();
        if (content instanceof Status st) {
            if (st.getType() == StatusType.SUCCESS) {
                // Close dialog and navigate to waiting room (owner stage)
                Platform.runLater(() -> {
                    try {
                        // close this invite dialog
                        closeDialog();

                        // Try to switch main stage to the Game scene for both players
                        if (primaryStage != null) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameScene.fxml"));
                                Parent root = loader.load();
                                GameController controller = loader.getController();
                                // pass network if available
                                if (network != null)
                                    controller.setNetwork(network);

                                // determine players from inviteRequest if possible
                                String inviterName = null;
                                String invitedName = null;
                                if (inviteRequest != null) {
                                    inviterName = inviteRequest.getInviter();
                                    invitedName = inviteRequest.getInvited();
                                } else if (inviter != null) {
                                    inviterName = inviter;
                                }

                                // assume this controller is shown to the invited user, so current = invitedName
                                if (invitedName != null) {
                                    controller.setPlayers(invitedName, inviterName);
                                } else if (inviterName != null) {
                                    // fallback: set inviter as current and no opponent
                                    controller.setPlayers(inviterName, null);
                                }

                                primaryStage.setScene(new Scene(root));
                                primaryStage.setTitle("Game");
                                primaryStage.show();
                            } catch (IOException e) {
                                System.err.println("Lỗi load GameScene.fxml: " + e.getMessage());
                                InviteNotificationManager.getInstance()
                                        .showSimpleNotification("Đã chấp nhận. Vào phòng chờ...");
                            }
                        } else {
                            InviteNotificationManager.getInstance()
                                    .showSimpleNotification("Đã chấp nhận. Vào phòng chờ...");
                        }
                        // Unregister listener now that we've handled server response
                        try {
                            if (network != null)
                                network.removeMessageListener(this);
                        } catch (Exception ignore) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Không thể chấp nhận");
                    alert.setHeaderText(null);
                    alert.setContentText(st.getContent());
                    alert.showAndWait();
                });
            }
        }
    }

    private void handleReject(Message msg) {
        if (msg == null)
            return;
        Object content = msg.getContent();
        if (content instanceof Status st) {
            Platform.runLater(() -> {
                // Close dialog and show simple notification
                closeDialog();
                String text = st.getContent() != null && !st.getContent().isBlank() ? st.getContent() : "Đã từ chối.";
                InviteNotificationManager.getInstance().showSimpleNotification(text);
                try {
                    if (network != null)
                        network.removeMessageListener(this);
                } catch (Exception ignore) {
                }
            });
        }
    }
}