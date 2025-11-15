package server.command;

import server.ClientHandler;
import dto.Message;
import dto.MoveRequest;
import server.game.GameManager;

public class MoveCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        Object content = msg.getContent();
        if (content instanceof MoveRequest req) {
            System.out.println("[LOG] Received MOVE: gameId=" + req.getGameId() + ", selectedSeedIndex=" + req.getSelectedSeedIndex() + ", basketType=" + req.getBasketType());
            GameManager.getInstance().handleMove(msg.getSender(), req.getGameId(), req.getSelectedSeedIndex(), req.getBasketType());
        }
    }
}
