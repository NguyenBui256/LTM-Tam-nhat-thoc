package server.command;

import java.io.IOException;

import server.ClientHandler;
import server.OnlineUserManager;
import server.dto.GameStart;
import server.dto.Message;
import server.game.GameManager;

/**
 * INVITE flow:
 * - client sends Message with command=INVITE and content = targetUsername (String)
 * - target receives Message(command=INVITE) from server with content = inviterUsername
 * - target responds with Message(command=INVITE, content = "ACCEPT:inviterUsername") or "REJECT:inviterUsername"
 */
public class InviteCommand implements Command {
    @Override
    public void execute(ClientHandler handler, Message msg) throws Exception {
        Object content = msg.getContent();
        String inviter = handler.getUsername();

        if (content instanceof String) {
            String txt = (String) content;
            if (txt.contains(":")) {
                // response from invited user: e.g. "ACCEPT:inviterUsername" or "REJECT:inviterUsername"
                String[] parts = txt.split(":", 2);
                String resp = parts[0];
                String toUser = parts[1];
                ClientHandler toHandler = OnlineUserManager.getHandler(toUser);
                if (toHandler == null) {
                    handler.sendMessage(new Message("SERVER", "User is not online"));
                    return;
                }

                if ("ACCEPT".equalsIgnoreCase(resp)) {
                    // create a new game session and notify both players
                    GameStart gs = GameManager.getInstance().createGame(toHandler, toUser, handler, inviter);
                    // send GameStart to inviter (toHandler)
                    toHandler.sendMessage(new Message("GAME_START", "SERVER", gs));
                    // send GameStart to accepter (handler)
                    handler.sendMessage(new Message("GAME_START", "SERVER", gs));
                } else {
                    // REJECT -> notify inviter
                    toHandler.sendMessage(new Message("INVITE_REJECTED", "SERVER", inviter + " rejected your invite"));
                }
            } else {
                // initial invite: content is target username
                String target = txt;
                ClientHandler targetHandler = OnlineUserManager.getHandler(target);
                if (targetHandler == null) {
                    handler.sendMessage(new Message("SERVER", "User is not online"));
                    return;
                }
                // forward invite to target
                targetHandler.sendMessage(new Message("INVITE", inviter, inviter));
            }
        }
    }
}
