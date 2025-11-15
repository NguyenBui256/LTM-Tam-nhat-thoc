package src;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import src.dao.UserDAO;
import dto.PlayerRank;
import src.ClientHandler;
import src.OnlineUserManager;
public class Server {
	private static UserDAO userdao = new UserDAO();
	private static final Integer PORT =  2206;
    public static void main(String[] args) {
    	List<PlayerRank> userlist = userdao.getAllUser();
    	OnlineUserManager.setUserList(userlist);
    	for(PlayerRank x: OnlineUserManager.getListUser()) {
    		System.out.println(x.getName());
    	}
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
