package client;

import java.io.IOException;
import java.net.URL;

import client.controller.GameController;
import client.network.MessageListener;
import client.network.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import dto.InviteRequest;
import dto.LoginRequest;
import dto.Message;
import dto.Status;
import common.CommandType;
import common.StatusType;

public class GameTest extends Application implements MessageListener {
    private Network networkA;
    private Network networkB;
    private Stage stageA;
    private Stage stageB;
    private boolean isPlayerALoggedIn = true;
    private boolean isPlayerBLoggedIn = true;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 🎮 Thông tin server
            String host = "127.0.0.1";
            int port = 2206;

            // ===== KHỞI TẠO NETWORK CHO NGƯỜI CHƠI A =====
            try {
                networkA = new Network(host, port);
                System.out.println("✓ Người A kết nối server thành công");
                networkA.addMessageListener(this);
            } catch (IOException e) {
                System.err.println("✗ Người A không thể kết nối đến server");
                e.printStackTrace();
            }

            // ===== KHỞI TẠO NETWORK CHO NGƯỜI CHƠI B =====
            try {
                networkB = new Network(host, port);
                System.out.println("✓ Người B kết nối server thành công");
                networkB.addMessageListener(this);
            } catch (IOException e) {
                System.err.println("✗ Người B không thể kết nối đến server");
                e.printStackTrace();
            }

            if (networkA == null || networkB == null) {
                System.err.println("Không thể kết nối. Kiểm tra server có đang chạy không?");
                return;
            }

            // ===== TẠO 2 STAGE CHO A VÀ B =====
            stageA = primaryStage;
            stageB = new Stage();

            // ===== KHỞI TẠO NGƯỜI A =====
            initializePlayerA(stageA, networkA);

            // ===== KHỞI TẠO NGƯỜI B =====
            Platform.runLater(() -> {
                try {
                    initializePlayerB(stageB, networkB);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // ===== LOGIN SAU 1 GIÂY =====
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("\n[TEST] Bắt đầu login cho cả 2 người...");
                    loginPlayerA();
                    Thread.sleep(500);
                    loginPlayerB();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            // ===== GỬI INVITE SAU 4 GIÂY (sau khi cả 2 login) =====
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    if (isPlayerALoggedIn && isPlayerBLoggedIn) {
                        System.out.println("\n[TEST] ✅ Cả 2 đã login, bắt đầu gửi INVITE...");
                        sendInviteFromAToB();
                    } else {
                        System.out.println("\n[TEST] ❌ Chưa login xong: A=" + isPlayerALoggedIn + ", B=" + isPlayerBLoggedIn);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            stageA.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo giao diện cho Người chơi A
     */
    private void initializePlayerA(Stage stage, Network network) {
        try {
            URL fxmlURL = getClass().getResource("/fxml/GameScene.fxml");
            if (fxmlURL == null) {
                System.err.println("✗ Không tìm thấy GameScene.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();

            GameController controller = loader.getController();
            controller.setPrimaryStage(stageA);
            controller.setNetwork(network);
            controller.setUsername("duma");  // Tên đăng nhập hợp lệ

            stage.setTitle("🎮 Người chơi A");
            stage.setScene(new Scene(root, 800, 600));
            stage.setX(0);
            stage.setY(0);

            System.out.println("✓ Khởi tạo thành công Người A");

        } catch (IOException e) {
            System.err.println("✗ Lỗi khi tải GameScene.fxml cho Người A");
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo giao diện cho Người chơi B
     */
    private void initializePlayerB(Stage stage, Network network) {
        try {
            URL fxmlURL = getClass().getResource("/fxml/GameScene.fxml");
            if (fxmlURL == null) {
                System.err.println("✗ Không tìm thấy GameScene.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();

            GameController controller = loader.getController();
            controller.setPrimaryStage(stageB);
            controller.setNetwork(network);
            controller.setUsername("test");  // Tên display, không phải tên login

            stage.setTitle("🎮 Người chơi B");
            stage.setScene(new Scene(root, 800, 600));
            stage.setX(850);  // Xếp cạnh bên phải màn hình A
            stage.setY(0);

            System.out.println("✓ Khởi tạo thành công Người B");
            stage.show();

        } catch (IOException e) {
            System.err.println("✗ Lỗi khi tải GameScene.fxml cho Người B");
            e.printStackTrace();
        }
    }

    /**
     * Người A login vào server
     */
    private void loginPlayerA() {
        try {
            System.out.println("\n🔐 [Người A] Đang login...");

            LoginRequest loginReq = new LoginRequest("duma", "1234");
            Message loginMsg = new Message(
                CommandType.LOGIN.toString(),
                "duma",
                loginReq
            );

            networkA.send(loginMsg);
            System.out.println("✓ [Người A] Đã gửi LOGIN request");

        } catch (IOException e) {
            System.err.println("✗ Lỗi login người A");
            e.printStackTrace();
        }
    }

    /**
     * Người B login vào server
     */
    private void loginPlayerB() {
        try {
            System.out.println("\n🔐 [Người B] Đang login...");

            LoginRequest loginReq = new LoginRequest("test", "1234");
            Message loginMsg = new Message(
                CommandType.LOGIN.toString(),
                "test",
                loginReq
            );

            networkB.send(loginMsg);
            System.out.println("✓ [Người B] Đã gửi LOGIN request");

        } catch (IOException e) {
            System.err.println("✗ Lỗi login người B");
            e.printStackTrace();
        }
    }

    /**
     * Người A gửi lời mời cho B
     */
    private void sendInviteFromAToB() {
        try {
            System.out.println("\n📤 [Người A] Gửi lời mời tới Người B...");

            // Tạo yêu cầu lời mời: inviter="duma", invited="test"
            InviteRequest inviteReq = new InviteRequest("duma", "test");

            // Tạo message
            Message inviteMsg = new Message(
                CommandType.INVITE.toString(),
                "duma",
                inviteReq
            );

            // Gửi qua network của A
            networkA.send(inviteMsg);
            System.out.println("✓ [Người A] Đã gửi lời mời cho người B!");

        } catch (IOException e) {
            System.err.println("✗ Lỗi gửi lời mời từ A");
            e.printStackTrace();
        }
    }

    /**
     * Lắng nghe message từ server
     * Khi Người B nhận được INVITE, tự động chấp nhận
     */
    @Override
    public void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            System.out.println("[TEST] Nhận message: cmd=" + msg.getCommand() + ", sender=" + msg.getSender() + ", content=" + msg.getContent());
            if ("INVITE".equals(msg.getCommand())) {
                System.out.println("Có nhận được lời mời");
                if (msg.getContent() instanceof InviteRequest) {
                    InviteRequest req = (InviteRequest) msg.getContent();
                    System.out.println("\n🔔 [Người B] Nhận được lời mời từ: " + req.getInviter());
                    // Tự động chấp nhận lời mời sau 1 giây
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            System.out.println("B Bắt đầu gửi accept invite");
                            acceptInviteFromB(req);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } else if ("GAME_ROOM_CREATED".equals(msg.getCommand())) {
                System.out.println("\n✅ Phòng game được tạo! Sẵn sàng bắt đầu...");
            } else if ("INVITE_RESPONSE".equals(msg.getCommand())) {
                System.out.println("\n📬 [Người A] INVITE_RESPONSE: " + ((Status) msg.getContent()).getContent());
            } else if ("ACCEPT_RESPONSE".equals(msg.getCommand())) {
                System.out.println("\n✔️ [Người B] ACCEPT_RESPONSE: " + ((Status) msg.getContent()).getContent());
            }
        });
    }

    /**
     * Người B chấp nhận lời mời từ A
     * Content phải là username của người mời (duma)
     */
    private void acceptInviteFromB(InviteRequest req) {
        try {
            System.out.println("\n✅ [Người B] Chấp nhận lời mời từ Người A...");

            // Tạo message chấp nhận
            // Content phải là username của inviter (duma)
            Message acceptMsg = new Message(
                CommandType.ACCEPT.toString(),
                "test",
                req  // Content = username của người mời
            );

            // Gửi qua network của B
            networkB.send(acceptMsg);
            System.out.println("✓ [Người B] Đã gửi ACCEPT!");
            System.out.println("\n🎮 CHUẨN BỊ BẮT ĐẦU TRẬN ĐẤU - CẢ HAI NGƯỜI CHƠI ĐÃ SẴN SÀNG!");

        } catch (IOException e) {
            System.err.println("✗ Lỗi chấp nhận lời mời");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
