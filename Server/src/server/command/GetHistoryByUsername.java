package server.command;

import java.util.List;

import server.ClientHandler;
import server.dao.UserDAO;
import dto.Message;
import server.model.Game;

public class GetHistoryByUsername implements Command{
	private UserDAO ud = new UserDAO();
	@Override
	public void execute(ClientHandler handler, Message msg) throws Exception {
		String username = handler.getUsername();
		List<Game> games = ud.getHistoryByUsername(username);
		handler.sendMessage(new Message("GET_HISTORY_RESPONSE","SERVER",games));
	}
	
}
