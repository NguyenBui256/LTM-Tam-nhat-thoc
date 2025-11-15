package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import dto.LoginRequest;
import dto.Message;
import dto.Status;
import server.model.User;
import common.StatusType;
import server.dao.UserDAO;
import java.util.*;
public class LoginCommand implements Command {
	private UserDAO userdao = new UserDAO();
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        LoginRequest login = (LoginRequest) msg.getContent();
        String username = login.getUsername();
        String password = login.getPassword();
        System.out.println("[SERVER] LOGIN command:" + username);
        User u = new User();
        if (OnlineUserManager.isOnline(username)) {
            handler.sendMessage(new Message("LOGIN_RESPONSE", "SERVER", new Status(StatusType.ERROR, "User is already logged in")));
        } else {
        	if(! userdao.checkLogin(username, password)) {
        		handler.sendMessage(new Message("LOGIN_RESPONSE","SERVER", new Status(StatusType.ERROR,"Password or username is incorrect!")));
        	}
            handler.setUsername(username);
            u.setUsername(username);
            u.setStatus("ONLINE");
            OnlineUserManager.addOnlineUser(username, handler);
            handler.sendMessage(new Message("LOGIN_RESPONSE","SERVER", new Status(StatusType.SUCCESS, "Login success")));
            for (ClientHandler h : OnlineUserManager.getAllHandlers()) {
                if (h != handler) {
                	h.sendMessage(new Message("PLAYER_STATUS_CHANGE","SERVER", Map.of("name", username, "status", "ONLINE")));
                }
            }
        }
    }
}