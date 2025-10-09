package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import server.model.Message;
import server.model.Status;
import server.model.StatusType;

public class LogoutCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String username = handler.getUsername();
        OnlineUserManager.removeOnlineUser(username);
        handler.sendMessage(new Message("SERVER", new Status (StatusType.SUCCESS,"Logout success")));
        handler.close();
    }
}
