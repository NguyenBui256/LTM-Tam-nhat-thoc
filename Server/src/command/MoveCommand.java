package src.command;

import src.ClientHandler;
import dto.Message;
import src.game.GameManager;

public class MoveCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        // content expected to be String "gameId:seedIndex:choice"
        Object content = msg.getContent();
        if (content instanceof String) {
            String s = (String) content;
            String[] parts = s.split(":");
            if (parts.length >= 3) {
                String gameId = parts[0];
                int seedIndex = Integer.parseInt(parts[1]);
                int choice = Integer.parseInt(parts[2]);
                GameManager.getInstance().handleMove(handler.getUsername(), gameId, seedIndex, choice);
            }
        }
    }
}
