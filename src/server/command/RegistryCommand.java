package server.command;

import java.io.IOException;

import server.ClientHandler;
import server.common.StatusType;
import server.dao.UserDAO;
import server.dto.Message;
import server.dto.RegistryRequest;
import server.dto.Status;

import server.common.StatusType;
import server.dao.UserDAO;
import server.model.User;

import java.io.IOException;

public class RegistryCommand implements Command {
	private UserDAO ud = new UserDAO();

	@Override
	public void execute(ClientHandler handler, Message msg) {
		RegistryRequest request = (RegistryRequest) msg.getContent();
		User user = new User(request.getUsername(), request.getPassword(), request.getEmail(), request.getName());

		String responseType = "REGISTRY_RESPONSE";
		Status responseStatus;

		if (ud.checkExistUser(user.getUsername())) {
			responseStatus = new Status(StatusType.ERROR, "Username is already in use, please choose another.");
		} else if (ud.insertUser(user)) {
			responseStatus = new Status(StatusType.SUCCESS, "Registration successful!");
		} else {
			responseStatus = new Status(StatusType.ERROR, "Registration failed, please try again.");
		}

		try {
			handler.sendMessage(new Message(responseType, "SERVER", responseStatus));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
