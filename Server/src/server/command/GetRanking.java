package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import dto.Message;

public class GetRanking implements Command {

	@Override
	public void execute(ClientHandler handler, Message msg) throws Exception {
		// TODO Auto-generated method stub

		System.out.println("[GetRanking] in getRanking");
		handler.sendMessage(new Message("ONLINE_PLAYERS_RESPONSE", "SERVER", OnlineUserManager.getListUser()));
	}

}