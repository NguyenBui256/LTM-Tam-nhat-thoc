package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import dto.PlayerRank;

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
            userList.computeIfPresent(username, (key, existingPlayerRank) -> {
                existingPlayerRank.setStatus("OFFLINE");
                return existingPlayerRank;
            });

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
    public static List<PlayerRank> getListUser(ClientHandler handler){
        List<PlayerRank> pl = new ArrayList<>();
        for(String x: userList.keySet()) {

            if(x.equals(handler.getUsername())) {
                System.out.println("Ban than");
                continue;
            }
            // Return a copy to avoid exposing internal mutable objects to serialization races
            PlayerRank original = userList.get(x);
            if (original != null) {
                PlayerRank copy = new PlayerRank(original.getName(), original.getStatus(), original.getElo(), original.getWins());
                pl.add(copy);
                System.out.println(copy.getName()+ " " + copy.getStatus());
            }
        }
        pl.sort((x, y) -> {
            boolean xIsOnline = "ONLINE".equals(x.getStatus());
            boolean yIsOnline = "ONLINE".equals(y.getStatus());

            if (xIsOnline && !yIsOnline) {
                return -1; // x đứng trước y (vì x online, y offline)
            } else if (!xIsOnline && yIsOnline) {
                return 1;  // y đứng trước x (vì y online, x offline)
            } else {
                return 0;  // Cả hai cùng online hoặc cùng offline, coi như bằng nhau
            }
        });
        return pl;
    }
}
