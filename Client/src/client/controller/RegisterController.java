package client.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import client.network.Network;
import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label lblRegisterError;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;
    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }
    @FXML
    protected void onRegisterClicked(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 1. Kiểm tra dữ liệu đầu vào (Validation)
        if (!validateInput(username, email, password, confirmPassword)) {
            return; // Dừng lại nếu dữ liệu không hợp lệ
        }

        // 2. Xử lý logic đăng ký

        System.out.println("Đang thực hiện đăng ký...");

        System.out.println("Tên đăng nhập: " + username);
        System.out.println("Email: " + email);
        System.out.println("Đăng ký thành công!");

        // Sau khi đăng ký thành công, chuyển về trang đăng nhập
        try {
            switchToLogin(event);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi: Không thể tải trang đăng nhập.");
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu người dùng nhập vào.
     *
     * @return true nếu hợp lệ, false nếu không hợp lệ.
     */
    private boolean validateInput( String username, String email, String password, String confirmPassword) {
        // Kiểm tra xem có trường nào bị bỏ trống không
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return false;
        }

        // Kiểm tra mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            return false;
        }


        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!email.matches(emailRegex)) {
            showError("Định dạng email không hợp lệ!");
            return false;
        }

        // Nếu tất cả đều hợp lệ
        lblRegisterError.setVisible(false);
        return true;
    }

    /**
     * Hiển thị thông báo lỗi trên giao diện.
     *
     * @param message Nội dung lỗi cần hiển thị.
     */
    private void showError(String message) {
        lblRegisterError.setText(message);
        lblRegisterError.setVisible(true);
    }

    @FXML
    protected void onLoginClicked(ActionEvent event) throws IOException {
        switchToLogin(event);
    }


    private void switchToLogin(ActionEvent event) throws IOException {
        // Lấy Stage (cửa sổ) hiện tại từ sự kiện
        Stage stage = (Stage) ((Hyperlink) event.getSource()).getScene().getWindow();

        // Tải file FXML của màn hình đăng nhập
        // !!! LƯU Ý: Hãy chắc chắn rằng đường dẫn này là chính xác trong dự án của bạn !!!
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent loginRoot = loader.load();

        Scene scene = new Scene(loginRoot);
        stage.setScene(scene);
        stage.setTitle("Đăng nhập"); // Cập nhật lại tiêu đề cửa sổ
        stage.show();
    }
}