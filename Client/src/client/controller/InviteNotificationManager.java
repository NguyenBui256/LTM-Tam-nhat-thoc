package client.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import dto.InviteRequest;
import client.network.Network;

public class InviteNotificationManager {

    private static InviteNotificationManager instance;
    private Stage primaryStage;
    private final List<Popup> activePopups = new ArrayList<>();
    private Network network;
    private static String currentUsername;

    public InviteNotificationManager() {
    }

    private InviteNotificationManager(String currentUsername) {
    }

    public static InviteNotificationManager getInstance() {
        if (instance == null) {
            instance = new InviteNotificationManager();
        }
        return instance;
    }

    /**
     * Thiết lập Stage chính - BẮT BUỘC gọi trước khi showInvite()
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        System.out.println("[InviteManager] PrimaryStage đã được set: " + stage.getTitle());
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * Hiển thị thông báo mời chơi
     */
    public void showInvite(InviteRequest req) {
        if (primaryStage == null) {
            System.err.println("[InviteManager] LỖI: primaryStage chưa được set! Gọi setPrimaryStage() trước.");
            return;
        }
        if (req == null || req.getInviter() == null || req.getInvited() == null || req.getInviter().trim().isEmpty()
                || req.getInvited().trim().isEmpty()) {
            System.err.println("[InviteManager] LỖI: InviteRequest không hợp lệ: " + req);
            return;
        }
        Platform.runLater(() -> createAndShowPopup(req));
    }

    /**
     * Tạo và hiển thị popup (chạy trên JavaFX Thread)
     */
    private void createAndShowPopup(InviteRequest req) {
        String inviter = req.getInviter();
        Label label = new Label(inviter + " đã mời bạn chơi!");
        label.setStyle("""
                -fx-background-color: #2c3e50;
                -fx-text-fill: #ecf0f1;
                -fx-padding: 12 24;
                -fx-background-radius: 12;
                -fx-font-size: 14px;
                -fx-font-family: 'Segoe UI', Helvetica, Arial, sans-serif;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);
                """);

        StackPane container = new StackPane(label);
        container.setPrefSize(300, 60);
        container.setStyle("-fx-background-color: transparent;");

        Popup popup = new Popup();
        popup.getContent().add(container);
        popup.setAutoHide(true); // Tự ẩn khi click ra ngoài

        // Tính vị trí: góc trên-phải, cách 20px
        Scene scene = primaryStage.getScene();
        double x = primaryStage.getX() + scene.getWidth() - 330;
        double y = primaryStage.getY() + 20;

        popup.show(primaryStage, x, y);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Đẩy các popup cũ xuống
        for (Popup existing : activePopups) {
            StackPane pane = (StackPane) existing.getContent().get(0);
            TranslateTransition move = new TranslateTransition(Duration.millis(250), pane);
            move.setByY(75);

            // Click để mở dialog
            move.play();
        }

        activePopups.add(0, popup); // Thêm vào đầu danh sách
        label.setOnMouseClicked(e -> {
            hidePopup(popup);
            showChallengeDialog(req);
        });

        // Tự ẩn sau 10 giây (tăng từ 5s)
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> hidePopup(popup));
        }).start();
    }

    /**
     * Ẩn popup với hiệu ứng
     */
    private void hidePopup(Popup popup) {
        if (!activePopups.contains(popup))
            return;

        StackPane container = (StackPane) popup.getContent().get(0);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            popup.hide();
            activePopups.remove(popup);

            // Kéo các popup còn lại lên
            for (Popup remaining : activePopups) {
                StackPane pane = (StackPane) remaining.getContent().get(0);
                TranslateTransition moveUp = new TranslateTransition(Duration.millis(250), pane);
                moveUp.setByY(-75);
                moveUp.play();
            }
        });
        fadeOut.play();
    }

    /**
     * Mở popup chi tiết lời mời
     */
    private void showChallengeDialog(InviteRequest req) {
        String inviter = req.getInviter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InvitePopup.fxml"));
            Parent root = loader.load();
            ChallengeInviteController controller = loader.getController();
            controller.setCurrentUser(currentUsername);
            controller.setInviteRequest(req);
            controller.setNetwork(this.network); // Truyền luôn network vào
            // truyền primary stage cho controller để nó có thể chuyển màn hình chính
            controller.setPrimaryStage(this.primaryStage);
            // Đăng ký controller để nó nhận được ACCEPT_RESPONSE / REJECT_RESPONSE
            if (this.network != null) {
                this.network.addMessageListener(controller);
            }
            Stage dialogStage = new Stage();
            dialogStage.setScene(new Scene(root));
            dialogStage.setTitle("Lời mời từ " + inviter);
            dialogStage.initOwner(primaryStage);
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.NONE);
            // Do not unregister controller here: controller will remove itself
            // after receiving ACCEPT_RESPONSE / REJECT_RESPONSE to avoid race conditions

            dialogStage.show();
        } catch (Exception e) {
            System.err.println("[InviteManager] Lỗi khi mở InvitePopup.fxml:");
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị thông báo đơn giản (ví dụ: ACCEPT/REJECT) ở góc trên phải.
     */
    public void showSimpleNotification(String message) {
        if (primaryStage == null) {
            System.err.println(
                    "[InviteManager] LỖI: primaryStage chưa được set! Không thể hiển thị thông báo: " + message);
            return;
        }
        if (message == null || message.trim().isEmpty())
            return;
        Platform.runLater(() -> {
            Label label = new Label(message);
            label.setStyle(
                    "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 10 16; -fx-background-radius: 8;");
            StackPane container = new StackPane(label);
            container.setPrefSize(320, 56);
            Popup popup = new Popup();
            popup.getContent().add(container);
            popup.setAutoHide(true);

            Scene scene = primaryStage.getScene();
            double x = primaryStage.getX() + scene.getWidth() - 350;
            double y = primaryStage.getY() + 20;

            popup.show(primaryStage, x, y);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), container);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Auto-hide after 3s
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(() -> popup.hide());
            }).start();
        });
    }

    // Optional: Xóa tất cả popup
    public void clearAll() {
        Platform.runLater(() -> {
            for (Popup popup : new ArrayList<>(activePopups)) {
                hidePopup(popup);
            }
        });
    }

    public static void setInstance(InviteNotificationManager instance) {
        InviteNotificationManager.instance = instance;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public List<Popup> getActivePopups() {
        return activePopups;
    }

    public Network getNetwork() {
        return network;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        InviteNotificationManager.currentUsername = currentUsername;
    }
}