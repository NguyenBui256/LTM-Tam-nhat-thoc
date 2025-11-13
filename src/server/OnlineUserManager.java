package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import server.dto.PlayerRank;
import server.model.User;

public class OnlineUserManager {
    private static ConcurrentHashMap<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> userStatus = new ConcurrentHashMap<>(); // username -> status
    private static ConcurrentHashMap<String, PlayerRank> userList = new ConcurrentHashMap<>();
    
	public static void setUserList(List<PlayerRank> userList) {
		userList.forEach(u -> OnlineUserManager.userList.put(u.getName(), u));
	}

	public static void addOnlineUser(String username, ClientHandler handler) {
        if (username != null && handler != null) {
            onlineUsers.put(username, handler);
            userStatus.put(username, "ONLINE");
            setUserStatus(username, "ONLINE");
        }
    }
    
    public static void removeOnlineUser(String username) {
        if (username != null) {
            onlineUsers.remove(username);
            userStatus.remove(username);
        }
    }

    public static boolean isOnline(String username) {
        return username != null && onlineUsers.containsKey(username);
    }

    public static ClientHandler getHandler(String username) {
        return username != null ? onlineUsers.get(username) : null;
    }

    public static List<ClientHandler> getAllHandlers() {
        List<ClientHandler> ch = new ArrayList<>();
        for (ClientHandler handler : onlineUsers.values()) {
            ch.add(handler);
        }
        return ch;
    }

    public static void setUserStatus(String username, String status) {
        if (username != null && status != null) {
            userStatus.put(username, status);
            userList.computeIfPresent(username, (key, existingPlayerRank) -> {
                existingPlayerRank.setStatus(status);
                return existingPlayerRank;
            });
        }
    }

    public static String getUserStatus(String username) {
        return username != null ? userStatus.get(username) : null;
    }

    public static boolean isUserAvailable(String username) {
        if (username == null)
            return false;
        String status = userStatus.get(username);
        return status != null && status.equals("ONLINE");
    }
    public static List<PlayerRank> getListUser(){
    	List<PlayerRank> pl = new ArrayList<>();
    	for(String x: userList.keySet()) {
    		pl.add(userList.get(x));
    	}
    	return pl;
    }
}
