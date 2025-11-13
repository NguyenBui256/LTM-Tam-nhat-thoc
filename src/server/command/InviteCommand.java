package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import server.dto.Message;
import server.dto.InviteRequest;
import server.dto.Status;
import server.common.StatusType;

public class InviteCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        InviteRequest req = (InviteRequest) msg.getContent();
        String inviter = req.getInviter();
        String invited = req.getInvited();
        if (!OnlineUserManager.isOnline(invited)) {
            handler.sendMessage(new Message("INVITE_RESPONSE", "SERVER", new Status(StatusType.ERROR, "User offline")));
            return;
        }
        if (!OnlineUserManager.isUserAvailable(invited)) {
            handler.sendMessage(new Message("INVITE_RESPONSE", "SERVER", new Status(StatusType.ERROR, "User busy")));
            return;
        }
        OnlineUserManager.setUserStatus(inviter, "WAITING");
        // Thông báo cho toàn bộ client biết người inviter đã chuyển sang WAITING
        for (ClientHandler client : OnlineUserManager.getAllHandlers()) {
            client.sendMessage(new Message("PLAYER_STATUS_CHANGE", "SERVER", java.util.Map.of(
                    "name", inviter,
                    "status", "WAITING")));
        }
        ClientHandler invitedHandler = OnlineUserManager.getHandler(invited);
        invitedHandler
                .sendMessage(new Message("INVITE", "SERVER", req)); // Gửi đúng đối tượng InviteRequest
        handler.sendMessage(new Message("INVITE_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Invite sent")));
    }
}
