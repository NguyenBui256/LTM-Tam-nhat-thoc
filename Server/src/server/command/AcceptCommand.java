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

        String roomId = GameRoomManager.createGameRoom(inviter, accepter);
        GameRoom room = GameRoomManager.getGameRoom(roomId);

        OnlineUserManager.setUserStatus(inviter, "IN_GAME");
        OnlineUserManager.setUserStatus(accepter, "IN_GAME");
        // Thông báo toàn bộ client cập nhật trạng thái cả hai
        for (ClientHandler client : OnlineUserManager.getAllHandlers()) {
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", inviter,
                    "status", "IN_GAME")));
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", accepter,
                    "status", "IN_GAME")));
        }
        // Thông báo riêng cho inviter biết đã được accept
        ClientHandler inviterHandler = OnlineUserManager.getHandler(inviter);
        if (inviterHandler != null) {
            inviterHandler.sendMessage(new Message("ACCEPT_NOTIFY", "SERVER", accepter + " đã chấp nhận lời mời."));
        }
        // Start the game immediately after accepting
        GameManager.getInstance().startGame(inviter, inviterHandler, accepter, handler, roomId);
        handler.sendMessage(new Message("ACCEPT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Accepted")));
    }
}
