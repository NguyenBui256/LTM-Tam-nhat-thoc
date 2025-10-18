package server.command;

import server.ClientHandler;
import server.OnlineUserManager;

import server.common.StatusType;
import server.dto.LoginRequest;
import server.dto.Message;
import server.dto.Status;
import server.dao.UserDAO;

public class LoginCommand implements Command {
	private UserDAO userdao = new UserDAO();
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        // String sender = msg.getSender();
        LoginRequest login = (LoginRequest) msg.getContent();
        String username = login.getUsername();
        String password = login.getPassword();
        System.out.println("username: "+username);
        if (OnlineUserManager.isOnline(username)) {
            handler.sendMessage(
                    new Message("LOGIN_RESPONSE", "SERVER", new Status(StatusType.SUCCESS, "User is logged in")));
        } else {
        	// check username password, mock data admin
//        	if(! (username.equals("admin") && password.equals("1234"))) {
//        		handler.sendMessage(new Message("SERVER", new Status(StatusType.ERROR,"Password or username is incorrect!")));
//        	}
        	if(userdao.checkLogin(username, password)) {
        		System.out.println("Login done:" + username);
        		handler.setUsername(username);
                OnlineUserManager.addOnlineUser(username, handler);
                handler.sendMessage(new Message("SERVER", new Status(StatusType.SUCCESS, "Login success")));
                for (ClientHandler h : OnlineUserManager.getAllHandlers()) {
                    if (h != handler) {
                        h.sendMessage(new Message("SERVER", username)); // will be converted to user's ingame.
                    }
                }
        	}else {
        		System.out.println("Login false: " + username);
        		handler.sendMessage(new Message("SERVER", new Status(StatusType.ERROR,"Username or password is incorrect!")));
        	}
        }
    }
}
