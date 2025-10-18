package server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static final Integer PORT =  2206;
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port: " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
