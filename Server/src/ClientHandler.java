package src;

import common.CommandType;
import dto.Message;

import java.io.*;
import java.net.Socket;

import src.command.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
    }

    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void run() {
    	
        try {
            while (!socket.isClosed()) {
                Message msg = (Message) in.readObject();
                System.out.println(msg.getSender());
                handleCommand(msg);
            }
        } catch (Exception e) {
        	System.out.println(e);
            System.out.println("Client disconnected: " + username);
            if (username != null) {
                OnlineUserManager.removeOnlineUser(username);
            }
        }
    }

    private void handleCommand(Message msg) throws Exception {
        CommandType type = CommandType.valueOf(msg.getCommand());
        System.out.println(type);
        Command command;
        switch (type) {
            case REGISTRY:
                command = new RegistryCommand();
                break;
            case LOGIN:
                command = new LoginCommand();
                break;
            case LOGOUT:
                command = new LogoutCommand();
                break;
            case INVITE:
                command = new InviteCommand();
                break;
            case ACCEPT:
                command = new AcceptCommand();
                break;
            case REJECT:
                command = new RejectCommand();
                break;
            case END_GAME:
                command = new EndGameCommand();
                break;
            case REMATCH:
                command = new RematchCommand();
                break;
            case GET_HISTORY:
                command = new GetHistoryByUsername();
                break;
            case GET_RANKING:
                command = new GetRankingCommand();
                break;
            case GET_ONLINE_PLAYERS:
                command = new GetRanking();
                break;
            default:
                throw new IllegalArgumentException("Unknown command");
        }
        
        command.execute(this, msg);
    }
}
