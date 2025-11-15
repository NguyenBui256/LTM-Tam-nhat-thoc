package src.command;

import src.ClientHandler;
import dto.Message;

public interface Command {
    void execute(ClientHandler handler, Message msg) throws Exception;
}
