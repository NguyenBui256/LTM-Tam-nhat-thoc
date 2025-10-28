package server.command;

import server.ClientHandler;
import server.dto.Message;
import server.game.GameManager;

public class MoveCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        // content expected to be String "gameId:seedId:choice:basketType"
        Object content = msg.getContent();
        if (content instanceof String) {
            String s = (String) content;
            String[] parts = s.split(":");
            if (parts.length >= 4) {
                String gameId = parts[0];
                int seedId = Integer.parseInt(parts[1]);
                int choice = Integer.parseInt(parts[2]);
                int basketType = Integer.parseInt(parts[3]);
                GameManager.getInstance().handleMove(handler.getUsername(), gameId, seedId, choice, basketType);
            }
        }
    }
}
