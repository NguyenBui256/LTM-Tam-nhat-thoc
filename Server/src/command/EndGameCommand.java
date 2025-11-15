package src.command;

import src.ClientHandler;
import src.GameRoomManager;
import src.OnlineUserManager;
import dto.Message;
import dto.Status;
import dto.GameRoom;
import common.StatusType;

public class EndGameCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String user = handler.getUsername();
        GameRoom room = GameRoomManager.getPlayerRoom(user);
        if (room == null) {
            handler.sendMessage(new Message("END_GAME_RESPONSE", "SERVER", new Status(StatusType.ERROR, "No room")));
            return;
        }
        GameRoomManager.endGame(room.getRoomId());
        for (String p : room.getPlayers()) {
            OnlineUserManager.setUserStatus(p, "ONLINE");
        }
        for (String p : room.getPlayers()) {
            ClientHandler h = OnlineUserManager.getHandler(p);
            if (h != null)
                h.sendMessage(new Message("GAME_ENDED", "SERVER", new Status(StatusType.SUCCESS, "Ended")));
        }
        GameRoomManager.removeGameRoom(room.getRoomId());
    }
}
