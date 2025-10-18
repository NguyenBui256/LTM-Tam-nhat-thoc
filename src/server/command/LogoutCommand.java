package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import server.common.StatusType;
import server.dto.Message;
import server.dto.Status;

public class LogoutCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String username = handler.getUsername();
        OnlineUserManager.removeOnlineUser(username);
        handler.sendMessage(new Message("LOGOUT_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "Logout success")));
        handler.close();
    }
}
