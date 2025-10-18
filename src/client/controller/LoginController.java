package client.controller;

import client.network.Network;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import server.dto.LoginRequest;
import server.dto.Message;
import server.dto.Status;
import server.common.StatusType;
import javafx.scene.Node;
import server.dto.Message;
import server.dto.Status;

import java.io.IOException;

public class LoginController {

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
    public void setNetwork(Network network){
        this.network = network;
    }

    @FXML
    private void onLoginClicked(ActionEvent event) throws IOException {
        lblLoginError.setVisible(false);
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username,password);
        Message msg = new Message("LOGIN","CLIENT",loginRequest);
        network.send(msg);

        try{
            Message received = network.receive();
            Status status = (Status) received.getContent();
            if (status.getType() == StatusType.SUCCESS){

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
                Parent root = loader.load();

                MainLobbyController controller =    loader.getController();
                controller.setNetwork(this.network);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }else{
                String error = status.getContent();
                lblLoginError.setText(error);
                lblLoginError.setVisible(true);
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết nối tới server. Vui lòng thử lại sau.");
        }
    }


    @FXML
    private void onForgotPasswordClicked(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Quên mật khẩu");
    }


    @FXML
    private void onRegisterClicked(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Đăng ký", "Đăng ký ");
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
