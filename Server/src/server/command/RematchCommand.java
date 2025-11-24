package server.command;

import server.ClientHandler;
import server.GameRoomManager;
import server.OnlineUserManager;
import dto.Message;
import dto.Status;
import dto.GameRoom;
import common.StatusType;
import server.RematchManager;

public class RematchCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String user = handler.getUsername();
        // Nội dung REMATCH từ client là gameId phòng cũ (trận vừa kết thúc)
        String oldRoomId = msg.getContent().toString();
        GameRoom oldRoom = GameRoomManager.getGameRoom(oldRoomId);
        if (oldRoom == null) {
            handler.sendMessage(
                    new Message("REMATCH_RESPONSE", "SERVER", new Status(StatusType.ERROR, "Không tìm thấy phòng cũ")));
            return;
        }

        String requester = user;
        String opponent = oldRoom.getPlayer1().equals(user) ? oldRoom.getPlayer2() : oldRoom.getPlayer1();

        // Sử dụng roomId cũ làm key pending; chưa tạo phòng mới ở bước này
        if (RematchManager.isPending(oldRoomId)) {
            handler.sendMessage(new Message("REMATCH_RESPONSE", "SERVER",
                    new Status(StatusType.ERROR, "Đã có yêu cầu rematch đang chờ")));
            return;
        }
        RematchManager.createPending(oldRoomId, requester, opponent);

        // Gửi thông báo cho người yêu cầu: đang chờ đối thủ
        handler.sendMessage(new Message("REMATCH_WAITING", "SERVER",
                new Status(StatusType.SUCCESS, "Đang chờ đối thủ chấp nhận")));

        // Gửi offer cho đối thủ, kèm roomId cũ để client biết context nếu cần
        ClientHandler opponentHandler = OnlineUserManager.getHandler(opponent);
        if (opponentHandler != null) {
            // Nội dung vẫn là tên requester (client hiện dùng để hiển thị), có thể mở rộng
            // gửi roomId nếu cần
            opponentHandler.sendMessage(new Message("REMATCH_OFFER", "SERVER", requester));
        }
    }
}
