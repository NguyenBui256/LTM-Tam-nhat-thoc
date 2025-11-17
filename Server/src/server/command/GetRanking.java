package server.command;

import server.ClientHandler;
import server.OnlineUserManager;
import dto.Message;

public class GetRanking implements Command {

    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        // TODO Auto-generated method stub

        System.out.println("[GetRanking] in getRanking");
        // Log the list we are about to send to help debug status mismatches
        java.util.List<dto.PlayerRank> list = OnlineUserManager.getListUser(handler);
        System.out.println("[GetRanking] sending ONLINE_PLAYERS_RESPONSE with " + list.size() + " entries");
        for (dto.PlayerRank p : list) {
            System.out.println("[GetRanking] -> " + p.getName() + " : " + p.getStatus());
        }
        handler.sendMessage(new Message("ONLINE_PLAYERS_RESPONSE", "SERVER", list));
    }

}