package server.command;

import server.ClientHandler;
import server.GameRoomManager;
import server.OnlineUserManager;
import server.game.GameManager;
import server.dto.GameRoom;
import server.dto.Message;
import server.dto.Status;
import server.common.StatusType;

public class AcceptCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String accepter = handler.getUsername();
        String inviter = (String) msg.getContent();
        String roomId = GameRoomManager.createGameRoom(inviter, accepter);
        GameRoom room = GameRoomManager.getGameRoom(roomId);
        OnlineUserManager.setUserStatus(inviter, "IN_GAME");
        OnlineUserManager.setUserStatus(accepter, "IN_GAME");
        ClientHandler inviterHandler = OnlineUserManager.getHandler(inviter);
        // Start the game immediately after accepting
        GameManager.getInstance().startGame(inviter, inviterHandler, accepter, handler, roomId);
        if (inviterHandler != null) {
            inviterHandler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", room));
        }
        handler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", room));
        handler.sendMessage(new Message("ACCEPT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Accepted")));
    }
}
