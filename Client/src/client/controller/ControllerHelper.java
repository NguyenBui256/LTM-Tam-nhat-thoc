package client.controller;

import client.network.Network;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import dto.Message;
import java.io.IOException;

/**
 * Utility class để chia sẻ logic chung giữa các Controller
 * - Xử lý logout khi đóng cửa sổ
 * - Gửi LOGOUT message đến server
 */
public class ControllerHelper {
    
    /**
     * Thực hiện logout: gửi LOGOUT message đến server
     * 
     * @param currentUser tên người dùng hiện tại
     * @param network network connection
     */
    public static void performLogout(String currentUser, Network network) {
        if (currentUser == null || currentUser.isBlank()) {
            System.out.println("[ControllerHelper] No current user, skipping logout");
            return;
        }
        
        if (network == null) {
            System.err.println("[ControllerHelper] Network is null, cannot perform logout");
            return;
        }
        
        try {
            Message msg = new Message("LOGOUT", currentUser, null);
            network.send(msg);
            System.out.println("[ControllerHelper] Logout request sent for user: " + currentUser);
        } catch (IOException e) {
            System.err.println("[ControllerHelper] Error sending logout request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Thiết lập close event handler cho Stage
     * Kiểm tra nếu có currentUser thì gọi logout trước khi tắt
     * 
     * @param stage JavaFX Stage để gắn close handler
     * @param currentUser tên người dùng hiện tại (null nếu chưa đăng nhập)
     * @param network network connection
     */
    public static void setupWindowCloseHandler(javafx.stage.Stage stage, String currentUser, Network network) {
        if (stage == null) {
            System.err.println("[ControllerHelper] Stage is null, cannot setup close handler");
            return;
        }
        
        stage.setOnCloseRequest(event -> {
            System.out.println("[ControllerHelper] Window close requested. currentUser=" + currentUser);
            
            // Nếu có currentUser (đã đăng nhập), gửi logout trước khi tắt
            if (currentUser != null && !currentUser.isBlank()) {
                System.out.println("[ControllerHelper] User logged in, sending logout before closing...");
                performLogout(currentUser, network);
                
                // Đợi một chút để server xử lý logout
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
            
            // Đóng connection nếu có
            if (network != null) {
                try {
                    network.close();
                    System.out.println("[ControllerHelper] Network closed");
                } catch (IOException e) {
                    System.err.println("[ControllerHelper] Error closing network: " + e.getMessage());
                }
            }
            
            System.out.println("[ControllerHelper] Application closing");
            Platform.exit();
        });
    }
}
