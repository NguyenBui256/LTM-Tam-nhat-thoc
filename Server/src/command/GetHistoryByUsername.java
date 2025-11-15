package src.command;

import java.util.List;

import src.ClientHandler;
import src.dao.UserDAO;
import dto.Message;
import model.Game;

public class GetHistoryByUsername implements Command{
	private UserDAO ud = new UserDAO();
	@Override
	public void execute(ClientHandler handler, Message msg) throws Exception {
		String username = handler.getUsername();
		System.out.println("[GetHistoryByUsername]: " + username);
		List<Game> games = ud.getHistoryByUsername(username);
		handler.sendMessage(new Message("GET_HISTORY_RESPONSE","SERVER",games));
	}
	
}
