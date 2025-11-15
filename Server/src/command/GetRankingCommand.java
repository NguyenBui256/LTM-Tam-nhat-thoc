package src.command;

import src.ClientHandler;
import src.dao.GameDAO;
import dto.Message;
import dto.PlayerStatus;
import src.dao.UserDAO;
import src.OnlineUserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetRankingCommand implements Command {

	@Override
	public void execute(ClientHandler handler, Message msg) throws Exception {
		UserDAO dao = new UserDAO();
		GameDAO gameDAO = new GameDAO();
		// Return full ranking (no limit) as requested
		List<PlayerStatus> dbRanking = dao.getRanking();
		Map<String, Integer> map = gameDAO.getWinsForAllUsers();
		List<PlayerStatus> ranking = new ArrayList<>();
		for (PlayerStatus p : dbRanking) {
			String username = p.getUsername();
			int elo = p.getElo();
			String status = OnlineUserManager.getUserStatus(username);
			if (status == null) status = "OFFLINE";
			PlayerStatus np = new PlayerStatus(username, status, elo, map.get(username));
			ranking.add(np);
		}

		// send response back to client
		Message response = new Message("RANKING_RESPONSE", "SERVER", ranking);
		handler.sendMessage(response);
	}

}
