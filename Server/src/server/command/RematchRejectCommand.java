package server.command;

import common.StatusType;
import dto.GameRoom;
import dto.Message;
import dto.Status;
import server.ClientHandler;
import server.GameRoomManager;
import server.RematchManager;
import server.OnlineUserManager;

public class RematchRejectCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String user = handler.getUsername();
        // Lấy roomId cũ từ phòng hiện tại
        GameRoom currentRoom = GameRoomManager.getPlayerRoom(user);
        if (currentRoom == null) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không tìm thấy phòng hiện tại")));
            return;
        }
        String oldRoomId = currentRoom.getRoomId();
        if (!RematchManager.isPending(oldRoomId)) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không có yêu cầu rematch")));
            return;
        }
        String requester = RematchManager.getRequester(oldRoomId);
        String opponent = RematchManager.getOpponent(oldRoomId);
        if (!user.equals(opponent)) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Không đúng đối tượng từ chối")));
            return;
        }
        // Notify requester
        server.ClientHandler requesterHandler = OnlineUserManager.getHandler(requester);
        if (requesterHandler != null) {
            requesterHandler.sendMessage(new Message("REMATCH_REJECTED", "SERVER", opponent));
        }
        RematchManager.clear(oldRoomId);
        handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                new Status(StatusType.SUCCESS, "Đã từ chối rematch")));
    }
}