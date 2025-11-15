package src.command;

import src.ClientHandler;
import src.OnlineUserManager;
import common.StatusType;
import dto.Message;
import dto.Status;
import dto.InviteRequest;

public class RejectCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        InviteRequest req = (InviteRequest) msg.getContent();
        String rejecter = req.getInvited(); // Người từ chối là người nhận lời mời
        String inviter = req.getInviter();
        OnlineUserManager.setUserStatus(rejecter, "ONLINE");
        // Thông báo cho toàn bộ client cập nhật trạng thái rejecter về ONLINE
        for (ClientHandler client : OnlineUserManager.getAllHandlers()) {
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", rejecter,
                    "status", "ONLINE")));
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", inviter,
                    "status", "ONLINE")));
        }
        if (inviter != null) {
            ClientHandler inviterHandler = OnlineUserManager.getHandler(inviter);
            if (inviterHandler != null) {
                inviterHandler.sendMessage(new Message("REJECT_NOTIFY", "SERVER", rejecter + " đã từ chối lời mời."));
            }
        }
        handler.sendMessage(new Message("REJECT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Rejected")));
    }
}
