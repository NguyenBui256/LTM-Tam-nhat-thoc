package server.command;

import java.io.IOException;

import server.dao.UserDAO;
import server.ClientHandler;
import server.dto.Message;
import server.dto.RegistryRequest;
import server.dto.Status;
import server.common.StatusType;
import server.model.User;

public class RegistryCommand implements Command {
	private UserDAO ud = new UserDAO();
	@Override
	public void execute(ClientHandler handler, Message msg) {
	    RegistryRequest request = (RegistryRequest) msg.getContent();
	    User user = request.getU();

	    String responseType = "SERVER";
	    Status responseStatus;

	    if (ud.checkExistUser(user.getUsername())) {
	        responseStatus = new Status(StatusType.ERROR, "Username is already in use, please choose another.");
	    } else if (ud.insertUser(user)) {
	        responseStatus = new Status(StatusType.SUCCESS, "Registration successful!");
	    } else {
	        responseStatus = new Status(StatusType.ERROR, "Registration failed, please try again.");
	    }

	    try {
	        handler.sendMessage(new Message(responseType, responseStatus));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
