package client.controller;

import client.network.MessageListener;
import client.network.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import dto.LoginRequest;
import dto.Message;
import dto.Status;
import common.StatusType;

import java.io.IOException;

public class LoginController implements MessageListener {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Label lblLoginError;

    private Network network;
    // Tên người chơi hiện tại (được gán khi nhấn Login)
    private String currentUser;

    @FXML
    public void initialize() {
        loginButton.setDefaultButton(true);

    }
    public void setNetwork(Network network) {
        this.network = network;
        if (this.network != null) {
            this.network.addMessageListener(this); // Đăng ký listener
            System.out.println("[LoginController] Network set successfully and listener registered.");
        } else {
            System.err.println("[LoginController] Error: Network is null, cannot set up connection.");
            showAlert(Alert.AlertType.ERROR, "Lỗi Mạng", "Không thể thiết lập kết nối mạng.");
        }
    }

    @FXML
    private void onLoginClicked(ActionEvent event) {
        System.out.println("[LoginController] Login button clicked.");
        lblLoginError.setVisible(false);
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Lưu lại username để truyền sang màn hình khác sau khi đăng nhập thành công
        this.currentUser = username;

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("[LoginController] Username or password is empty.");
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            Message msg = new Message("LOGIN", currentUser, loginRequest);
            System.out.println("[LoginController] Sending login request: username=" + username);
            network.send(msg);
        } catch (IOException e) {
            System.err.println("[LoginController] Error sending login request: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Lỗi Gửi Yêu Cầu",
                        "Không thể gửi yêu cầu đăng nhập đến server: " + e.getMessage());
            });
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        System.out.println("[LoginController] Received message: command=" + (msg != null ? msg.getCommand() : "null"));
        if (msg != null && "LOGIN_RESPONSE".equals(msg.getCommand())) {
            Status status = (Status) msg.getContent();
            System.out.println(
                    "[LoginController] LOGIN_RESPONSE received: status=" + (status != null ? status.getType() : "null")
                            + ", content=" + (status != null ? status.getContent() : "null"));

            Platform.runLater(() -> {
                if (status != null && status.getType() == StatusType.SUCCESS) {
                    System.out.println("[LoginController] Login successful, loading main_lobby.fxml...");

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
                        Parent root = loader.load();
                        System.out.println("[LoginController] main_lobby.fxml loaded successfully.");

                        MainLobbyController controller = loader.getController();
                        controller.setNetwork(this.network);
                        // Truyền Stage cho MainLobbyController để InviteNotificationManager hoạt động
                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        controller.setPrimaryStage(stage);
                        // Lưu username vào Network để tái sử dụng khi chuyển màn hình
                        if (this.network != null)
                            this.network.setCurrentUser(this.currentUser);
                        // Truyền tên người dùng đã đăng nhập cho MainLobbyController (dự phòng)
                        controller.setCurrentUser(this.currentUser);
                        controller.setPrimaryStage(stage);
                        stage.setScene(new Scene(root));
                        stage.setTitle("Main Lobby");
                        stage.show();
                        System.out.println("[LoginController] Network passed to MainLobbyController.");

                        network.removeMessageListener(this);
                        System.out.println("[LoginController] Listener removed from Network.");

                        // Stage stage = (Stage) loginButton.getScene().getWindow(); // This line is
                        // removed
                        // stage.setScene(new Scene(root)); // This line is removed
                        // stage.setTitle("Main Lobby"); // This line is removed
                        // stage.show(); // This line is removed
                        System.out.println("[LoginController] Switched to Main Lobby scene.");
                    } catch (IOException e) {
                        System.err.println("[LoginController] Error loading main_lobby.fxml: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện",
                                "Không thể tải màn hình chính: " + e.getMessage());
                    }
                } else {
                    String error = status != null ? status.getContent() : "Phản hồi từ server không hợp lệ";
                    System.out.println("[LoginController] Login failed: " + error);
                    lblLoginError.setText(error);
                    lblLoginError.setVisible(true);
                }
            });
        } else {
            System.out
                    .println("[LoginController] Ignored message: command=" + (msg != null ? msg.getCommand() : "null"));
        }
    }

    @FXML
    private void onForgotPasswordClicked(ActionEvent event) {
        System.out.println("[LoginController] Forgot Password link clicked.");
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng quên mật khẩu chưa được triển khai.");
    }

    @FXML
    private void onRegisterClicked(ActionEvent event) {
        System.out.println("[LoginController] Register link clicked.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();

            // Nếu cần truyền network sang RegisterController
            Object controller = loader.getController();
            if (controller instanceof RegisterController) {
                ((RegisterController) controller).setNetwork(this.network);
                System.out.println("[LoginController] Network passed to RegisterController.");
            }

            Stage stage = (Stage) ((Hyperlink) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
            System.out.println("[LoginController] Switched to Register scene.");
        } catch (IOException e) {
            System.err.println("[LoginController] Error loading register.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể tải giao diện đăng ký: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            System.out.println(
                    "[LoginController] Showing alert: type=" + type + ", title=" + title + ", message=" + message);
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        if (network != null) {
            System.out.println("[LoginController] Cleaning up: removing listener and closing network.");
            network.removeMessageListener(this);
            try {
                network.close();
            } catch (IOException e) {
                System.err.println("[LoginController] Error closing network: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        // Gắn close handler - không có currentUser ở LoginController
        ControllerHelper.setupWindowCloseHandler(stage, null, this.network);
    }
}