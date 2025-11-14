package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import server.dto.LoginRequest;
import server.dto.Message;
import server.dto.Status;
import server.common.StatusType;

public class LoginCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        LoginRequest login = (LoginRequest) msg.getContent();
        String username = login.getUsername();
        String password = login.getPassword();
        if (OnlineUserManager.isOnline(username)) {
            handler.sendMessage(new Message("LOGIN_RESPONSE", "SERVER", new Status(StatusType.ERROR, "User is already logged in")));
        } else {
            // Check username and password, mock data
            boolean validLogin = (username.equals("admin") && password.equals("1234")) ||
                                (username.equals("test") && password.equals("1234"));
            
            if (!validLogin) {
                handler.sendMessage(new Message("LOGIN_RESPONSE", "SERVER", new Status(StatusType.ERROR, "Password or username is incorrect!")));
            } else {
                handler.setUsername(username);
                OnlineUserManager.addOnlineUser(username, handler);
                handler.sendMessage(new Message("LOGIN_RESPONSE", "SERVER", username));
                for (ClientHandler h : OnlineUserManager.getAllHandlers()) {
                    if (h != handler) {
                        h.sendMessage(new Message("USER_ONLINE", "SERVER", username)); // Notify other users
                    }
                }
            }
        }
    }
}