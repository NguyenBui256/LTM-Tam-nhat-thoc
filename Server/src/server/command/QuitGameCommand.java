package server.command;

import server.ClientHandler;
import dto.Message;
import server.game.GameManager;

public class QuitGameCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        String username = msg.getSender();
        String gameId = msg.getContent() instanceof String ? (String) msg.getContent() : null;
        
        if (gameId == null) {
            System.out.println("[LOG] QUIT_GAME: gameId is null for user " + username);
            return;
        }
        
        System.out.println("[LOG] QUIT_GAME: user=" + username + ", gameId=" + gameId);
        GameManager.getInstance().handleQuitGame(username, gameId);
    }
}
