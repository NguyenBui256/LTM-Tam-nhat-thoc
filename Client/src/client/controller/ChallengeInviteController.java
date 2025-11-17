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
import dto.GameRoom;
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
    private String currentUser;

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
        // ✅ Add listener để nhận GAME_ROOM_CREATED từ server
        if (this.network != null) {
            this.network.addMessageListener(this);
        }
    }

    public void setInviteRequest(InviteRequest req) {
        this.inviteRequest = req;
        this.inviter = req.getInviter();
        Platform.runLater(() -> {
            playerName.setText(inviter);
            playerElo.setText(""); // TODO: real ELO
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
                Message msg = new Message(command, this.currentUser, inviteRequest);
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

        // ✅ Người nhận lời mời sẽ nhận GAME_ROOM_CREATED từ server
        switch (msg.getCommand()) {
            case "GAME_ROOM_CREATED" -> handleGameRoomCreated(msg);
            case "REJECT_RESPONSE" -> handleReject(msg);
        }
    }

    // ✅ Xử lý GAME_ROOM_CREATED từ server (không assume ai là mời hay được mời)
    private void handleGameRoomCreated(Message msg) {
        if (msg == null)
            return;
        Object content = msg.getContent();
        if (content instanceof GameRoom gameRoom) {
            Platform.runLater(() -> {
                try {
                    closeDialog();

                    if (primaryStage != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameScene.fxml"));
                            Parent root = loader.load();
                            GameController controller = loader.getController();
                            if (network != null)
                                controller.setNetwork(network);

                            // ✅ KHÔNG assume - dùng GameRoom từ server để xác định ai là ai
                            String player1 = gameRoom.getPlayer1();
                            String player2 = gameRoom.getPlayer2();
                            String currentUser = this.currentUser; // Được set từ InviteNotificationManager

                            // Xác định opponent dựa trên currentUser và GameRoom
                            String opponent;
                            if (currentUser != null && currentUser.equals(player1)) {
                                opponent = player2;
                            } else if (currentUser != null && currentUser.equals(player2)) {
                                opponent = player1;
                            } else {
                                System.err.println("[ChallengeInviteController] currentUser=" + currentUser 
                                    + " không khớp với player nào trong GameRoom (player1=" + player1 + ", player2=" + player2 + ")");
                                InviteNotificationManager.getInstance()
                                        .showSimpleNotification("Lỗi: Không tìm thấy thông tin người chơi");
                                return;
                            }

                            controller.setPlayers(currentUser, opponent);
                            controller.setCurrentPlayerName(currentUser);
                            System.out.println("[ChallengeInviteController] Game started: currentUser=" + currentUser 
                                + ", opponent=" + opponent + " (from GameRoom: player1=" + player1 + ", player2=" + player2 + ")");

                            primaryStage.setScene(new Scene(root));
                            primaryStage.setTitle("Game");
                            primaryStage.show();
                        } catch (IOException e) {
                            System.err.println("[ChallengeInviteController] Lỗi load GameScene.fxml: " + e.getMessage());
                            InviteNotificationManager.getInstance()
                                    .showSimpleNotification("Lỗi: Không thể tải giao diện game");
                        }
                    } else {
                        InviteNotificationManager.getInstance()
                                .showSimpleNotification("Lỗi: Không tìm thấy cửa sổ chính");
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

    public ImageView getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(ImageView avatarImage) {
        this.avatarImage = avatarImage;
    }

    public Label getPlayerName() {
        return playerName;
    }

    public void setPlayerName(Label playerName) {
        this.playerName = playerName;
    }

    public Label getPlayerElo() {
        return playerElo;
    }

    public void setPlayerElo(Label playerElo) {
        this.playerElo = playerElo;
    }

    public Text getInviteText() {
        return inviteText;
    }

    public void setInviteText(Text inviteText) {
        this.inviteText = inviteText;
    }

    public Button getAcceptButton() {
        return acceptButton;
    }

    public void setAcceptButton(Button acceptButton) {
        this.acceptButton = acceptButton;
    }

    public Button getDeclineButton() {
        return declineButton;
    }

    public void setDeclineButton(Button declineButton) {
        this.declineButton = declineButton;
    }

    public Network getNetwork() {
        return network;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public InviteRequest getInviteRequest() {
        return inviteRequest;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }
}