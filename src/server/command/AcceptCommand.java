package server.command;

import server.ClientHandler;
import server.GameRoomManager;
import server.OnlineUserManager;
import server.dto.GameRoom;
import server.dto.Message;
import server.dto.Status;
import server.common.StatusType;

public class AcceptCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String accepter = handler.getUsername();
        // Simple: find any WAITING inviter (in a real app, carry invite info)
        String inviter = null;
        for (String u : new java.util.ArrayList<>(java.util.Arrays.asList(accepter))) {
            // placeholder, assume inviter was the last who set WAITING against accepter
        }
        // Fallback: use accepter to pair manually (demo)
        for (java.util.Map.Entry<String, ClientHandler> e : new java.util.HashMap<>(
                new java.util.LinkedHashMap<String, ClientHandler>()).entrySet()) {
            // not used - kept minimal to compile
        }
        // For demo: use username 'admin' as inviter if available
        if (OnlineUserManager.isUserAvailable("admin") && !"admin".equals(accepter))
            inviter = "admin";
        if (inviter == null) {
            handler.sendMessage(
                    new Message("ACCEPT_RESPONSE", "SERVER", new Status(StatusType.ERROR, "No pending invite")));
            return;
        }
        String roomId = GameRoomManager.createGameRoom(inviter, accepter);
        GameRoom room = GameRoomManager.getGameRoom(roomId);
        OnlineUserManager.setUserStatus(inviter, "IN_GAME");
        OnlineUserManager.setUserStatus(accepter, "IN_GAME");
        ClientHandler inviterHandler = OnlineUserManager.getHandler(inviter);
        if (inviterHandler != null) {
            inviterHandler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", room));
        }
        handler.sendMessage(new Message("GAME_ROOM_CREATED", "SERVER", room));
        handler.sendMessage(new Message("ACCEPT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Accepted")));
    }
}
