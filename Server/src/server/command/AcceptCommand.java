package server.command;

import dto.GameRoom;
import server.ClientHandler;
import server.GameRoomManager;
import server.OnlineUserManager;
import dto.Message;
import dto.Status;
import common.StatusType;
import dto.InviteRequest;
import server.game.GameManager;

public class AcceptCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        InviteRequest req = (InviteRequest) msg.getContent();
        String accepter = req.getInvited(); // Người nhận lời mời (bên accept)
        String inviter = req.getInviter(); // Người đã gửi lời mời (bên send)
        System.out.println("[SERVER] Trận đấu giữa: " + inviter + " và " + accepter);
        String roomId = GameRoomManager.createGameRoom(inviter, accepter);
        GameRoom room = GameRoomManager.getGameRoom(roomId);

        OnlineUserManager.setUserStatus(inviter, "IN_GAME");
        OnlineUserManager.setUserStatus(accepter, "IN_GAME");
        // Thông báo toàn bộ client cập nhật trạng thái cả hai
        System.out.println("[SERVER] Cập nhật trạng thái cả 2 người chơi");
        for (ClientHandler client : OnlineUserManager.getAllHandlers()) {
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", inviter,
                    "status", "IN_GAME")));
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", accepter,
                    "status", "IN_GAME")));
        }
        // Thông báo riêng cho inviter biết đã được accept
        System.out.println("[SERVER] Thông báo riêng cho inviter biết đã được accept");
        ClientHandler inviterHandler = OnlineUserManager.getHandler(inviter);
        // Send GAME_ROOM_CREATED to both player
        inviterHandler.sendMessage(new Message("ACCEPT_NOTIFY", "SERVER", accepter + " đã chấp nhận lời mời."));
        inviterHandler.sendMessage(new Message("ACCEPT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Accepted")));
        inviterHandler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", room));
        handler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", room));

        // ✅ Delay trước khi start game để client kip add listener
        // Clients cần thời gian để load FXML, set controller, add listener
        new Thread(() -> {
            try {
                Thread.sleep(1000);  // Wait 1 second
                System.out.println("[SERVER] Bắt đầu game sau delay 1 giây");
                // Start the game AFTER both players received GAME_ROOM_CREATED and added listeners
                GameManager.getInstance().startGame(inviter, inviterHandler, accepter, handler, roomId);
            } catch (InterruptedException e) {
                System.err.println("[SERVER] Lỗi delay start game: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
