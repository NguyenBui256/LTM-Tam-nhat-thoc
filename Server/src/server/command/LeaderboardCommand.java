package server.command;

import java.util.List;
import java.util.Map;

import server.ClientHandler;
import dto.Message;
import server.game.GameManager;
import server.dao.GameDAO;

/**
 * Message content (String): "POINTS" or "WINS"
 */
public class LeaderboardCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        Object content = msg.getContent();
        String mode = "POINTS";
        if (content instanceof String) mode = ((String) content).toUpperCase();

        GameDAO dao = null;
        try { dao = new GameDAO(); } catch (Exception e) { dao = null; }

        if ("WINS".equals(mode)) {
            if (dao != null) {
                List<Map<String, Object>> rows = dao.getLeaderboardByWins();
                handler.sendMessage(new Message("LEADERBOARD", "SERVER", rows));
                return;
            }
        } else {
            if (dao != null) {
                List<Map<String, Object>> rows = dao.getLeaderboardByPoints();
                handler.sendMessage(new Message("LEADERBOARD", "SERVER", rows));
                return;
            }
        }

        // fallback to in-memory leaderboard
        handler.sendMessage(new Message("LEADERBOARD", "SERVER", GameManager.getInstance().getLeaderboard()));
    }
}
