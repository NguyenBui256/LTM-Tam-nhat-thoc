package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUserManager {
    private static ConcurrentHashMap<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    public static void addOnlineUser(String username, ClientHandler handler) {
        onlineUsers.put(username, handler);
    }

    public static void removeOnlineUser(String username) {
    	if (username != null) {
            onlineUsers.remove(username);
        }
    }

    public static boolean isOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public static ClientHandler getHandler(String username) {
        return onlineUsers.get(username);
    }

	public static List<ClientHandler> getAllHandlers() {
		List<ClientHandler> ch = new ArrayList<>();
		for (ClientHandler handler : onlineUsers.values()) {
		    ch.add(handler);
		}
		return ch;
	}
}
