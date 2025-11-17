package client;

import java.io.IOException;
import java.net.URL;

import client.controller.LoginController;
import client.network.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        // 🖧 Thông tin server
        String host = "127.0.0.1";  // đổi lại IP server thật
        int port = 2206;

        Network network = null;
        try {
            network = new Network(host, port);
            System.out.println(" Kết nối server thành công: " + host + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(" Không thể kết nối đến server!");
        }

        try {
            // 🔹 Load file giao diện login.fxml
            URL fxmlURL = getClass().getResource("/fxml/login.fxml");
            System.out.println("FXML URL: " + fxmlURL);

            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();


            LoginController controller = loader.getController();
            controller.setNetwork(network);


            stage.setTitle("Đăng nhập - Tấm nhặt thóc");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải login.fxml hoặc khởi tạo giao diện!");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
