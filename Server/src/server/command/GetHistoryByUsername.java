package server.command;

import java.util.ArrayList;
import java.util.List;

import server.ClientHandler;
import server.dao.UserDAO;
import dto.Message;
import dto.GameDTO;
import server.model.Game;

public class GetHistoryByUsername implements Command{
    private UserDAO ud = new UserDAO();
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String username = handler.getUsername();
        System.out.println("[GetHistoryByUsername]: " + username);
        List<Game> games = ud.getHistoryByUsername(username);
        List<GameDTO> gamedtos = new ArrayList<>();
        for(Game x : games) {
            gamedtos.add(new GameDTO(x.getId(), x.getUserId_1(), x.getUserId_2(), x.getUserResult_1(), x.getUserResult_2(), x.getWinnerId(), x.getScoreDiff(), x.getTime()));
        }
        System.out.println("GAME: " + games.size());
        handler.sendMessage(new Message("GET_HISTORY_RESPONSE","SERVER",gamedtos));
    }

}
