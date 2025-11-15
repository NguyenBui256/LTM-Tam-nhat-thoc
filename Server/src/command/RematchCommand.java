package src.command;

import src.ClientHandler;
import src.GameRoomManager;
import src.OnlineUserManager;
import dto.Message;
import dto.Status;
import dto.GameRoom;
import common.StatusType;

public class RematchCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String user = handler.getUsername();
        GameRoom room = GameRoomManager.getPlayerRoom(user);
        if (room == null) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER", new Status(StatusType.ERROR, "No room")));
            return;
        }
        room.setStatus("WAITING");
        for (String p : room.getPlayers()) {
            ClientHandler h = OnlineUserManager.getHandler(p);
            if (h != null) {
                h.sendMessage(new Message("REMATCH_READY", "SERVER", new Status(StatusType.SUCCESS, "Ready")));
                h.sendMessage(new Message("GAME_ROOM_UPDATE", "SERVER", room));
            }
        }
    }
}
