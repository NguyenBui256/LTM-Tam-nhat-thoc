package server.command;

import server.ClientHandler;
import server.GameRoomManager;
import server.OnlineUserManager;
import server.game.GameManager;
import server.dto.Message;
import server.dto.Status;
import server.dto.GameRoom;
import server.common.StatusType;

public class EndGameCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String user = msg.getSender(); // Get username from message sender
        System.out.println("[SERVER LOG] EndGameCommand executed for user: " + user);
        GameRoom room = GameRoomManager.getPlayerRoom(user);
        if (room == null) {
            System.out.println("[SERVER LOG] No room found for user: " + user);
            handler.sendMessage(new Message("END_GAME_RESPONSE", "SERVER", new Status(StatusType.ERROR, "No room")));
            return;
        }
        System.out.println("[SERVER LOG] Room found: " + room.getRoomId() + ", calling GameManager.endGame");
        // Call GameManager.endGame to finalize and send END_GAME with results
        GameManager.getInstance().endGame(user, room.getRoomId());
        // Update statuses and remove room
        for (String p : room.getPlayers()) {
            OnlineUserManager.setUserStatus(p, "ONLINE");
            System.out.println("[SERVER LOG] Set status to ONLINE for player: " + p);
        }
        GameRoomManager.removeGameRoom(room.getRoomId());
        System.out.println("[SERVER LOG] Removed game room: " + room.getRoomId());
    }
}
