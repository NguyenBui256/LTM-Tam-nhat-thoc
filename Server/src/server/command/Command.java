package server.command;

import server.ClientHandler;
import dto.Message;

public interface Command {
    void execute(ClientHandler handler, Message msg) throws Exception;
}
