package server.command;

import common.StatusType;
import dto.GameRoom;
import dto.Message;
import dto.Status;
import server.ClientHandler;
import server.GameRoomManager;
import server.OnlineUserManager;
import server.RematchManager;
import server.game.GameManager;

public class RematchAcceptCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String user = handler.getUsername();
        String oldRoomId = msg.getContent().toString(); // phòng cũ gửi từ client
        GameRoom oldRoom = GameRoomManager.getGameRoom(oldRoomId);
        if (oldRoom == null) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không tìm thấy phòng cũ")));
            return;
        }
        if (!RematchManager.isPending(oldRoomId)) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không có yêu cầu rematch")));
            return;
        }
        String requester = RematchManager.getRequester(oldRoomId);
        String opponent = RematchManager.getOpponent(oldRoomId);
        if (!user.equals(opponent)) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không đúng đối tượng chấp nhận")));
            return;
        }

        System.out.println("[SERVER] Rematch giữa: " + requester + " và " + opponent);
        // Tạo phòng mới chỉ sau khi đối thủ chấp nhận
        String newRoomId = GameRoomManager.createGameRoom(requester, opponent);
        GameRoom newRoom = GameRoomManager.getGameRoom(newRoomId);
        if (newRoom == null) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không tạo được phòng mới")));
            RematchManager.clear(oldRoomId);
            return;
        }

        ClientHandler requesterHandler = OnlineUserManager.getHandler(requester);
        ClientHandler opponentHandler = OnlineUserManager.getHandler(opponent);

        OnlineUserManager.setUserStatus(requester, "IN_GAME");
        OnlineUserManager.setUserStatus(opponent, "IN_GAME");
        // Thông báo toàn bộ client cập nhật trạng thái cả hai
        System.out.println("[SERVER] Cập nhật trạng thái cả 2 người chơi");
        for (ClientHandler ch : OnlineUserManager.getAllHandlers()) {
            ch.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER",
                    java.util.Map.of("name", requester, "status", "IN_GAME")));
            ch.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER",
                    java.util.Map.of("name", opponent, "status", "IN_GAME")));
        }

        RematchManager.clear(oldRoomId);

        // Thông báo riêng cho requester biết đã được accept
        System.out.println("[SERVER] Thông báo riêng cho requester biết đã được accept");
        if (requesterHandler != null) {
            requesterHandler
                    .sendMessage(new Message("REMATCH_ACCEPTED", "SERVER", opponent + " đã chấp nhận rematch."));
            requesterHandler
                    .sendMessage(new Message("REMATCH_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Accepted")));
            requesterHandler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", newRoom));
        }
        // Gửi cho opponent (handler hiện tại)
        handler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", newRoom));

        // ✅ Delay trước khi start game để client kip add listener
        // Clients cần thời gian để load FXML, set controller, add listener
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second
                System.out.println("[SERVER] Bắt đầu rematch sau delay 1 giây");
                // Start the game AFTER both players received GAME_ROOM_CREATED and added
                // listeners
                GameManager.getInstance().startGame(requester, requesterHandler, opponent, opponentHandler, newRoomId);
            } catch (InterruptedException e) {
                System.err.println("[SERVER] Lỗi delay start rematch: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}