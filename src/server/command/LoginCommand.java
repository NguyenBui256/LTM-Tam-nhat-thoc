package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import server.dto.LoginRequest;
import server.dto.Message;
import server.dto.Status;
import server.common.StatusType;
import server.dao.UserDAO;

public class LoginCommand implements Command {
	private UserDAO userdao = new UserDAO();
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        LoginRequest login = (LoginRequest) msg.getContent();
        String username = login.getUsername();
        String password = login.getPassword();
        System.out.println("[SERVER] LOGIN command:" + username);
        if (OnlineUserManager.isOnline(username)) {
            handler.sendMessage(new Message("LOGIN_RESPONSE", "SERVER", new Status(StatusType.ERROR, "User is already logged in")));
        } else {
        	if(! userdao.checkLogin(username, password)) {
        		handler.sendMessage(new Message("LOGIN_RESPONSE","SERVER", new Status(StatusType.ERROR,"Password or username is incorrect!")));
        	}
            handler.setUsername(username);
            OnlineUserManager.addOnlineUser(username, handler);
            handler.sendMessage(new Message("LOGIN_RESPONSE","SERVER", new Status(StatusType.SUCCESS, "Login success")));
            for (ClientHandler h : OnlineUserManager.getAllHandlers()) {
                if (h != handler) {
                    h.sendMessage(new Message("SERVER", username));
                }
            }
        }
    }
}