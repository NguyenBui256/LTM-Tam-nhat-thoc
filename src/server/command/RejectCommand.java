package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import server.common.StatusType;
import server.dto.Message;
import server.dto.Status;

public class RejectCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String rejecter = handler.getUsername();
        OnlineUserManager.setUserStatus(rejecter, "ONLINE");
        handler.sendMessage(new Message("REJECT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Rejected")));
    }
}
