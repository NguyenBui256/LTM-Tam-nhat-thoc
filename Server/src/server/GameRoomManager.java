package server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dto.GameRoom;

public class GameRoomManager {
    private static ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> playerToRoom = new ConcurrentHashMap<>();

    public static String createGameRoom(String p1, String p2) {
        String id = UUID.randomUUID().toString();
        GameRoom r = new GameRoom(id, p1, p2);
        rooms.put(id, r);
        playerToRoom.put(p1, id);
        playerToRoom.put(p2, id);
        return id;
    }

    public static GameRoom getGameRoom(String id) {
        return rooms.get(id);
    }

    public static GameRoom getPlayerRoom(String user) {
        String id = playerToRoom.get(user);
        return id == null ? null : rooms.get(id);
    }

    public static void removeGameRoom(String id) {
        GameRoom r = rooms.remove(id);
        if (r != null) {
            playerToRoom.remove(r.getPlayer1());
            playerToRoom.remove(r.getPlayer2());
        }
    }

    public static void removePlayerFromRoom(String user) {
        String id = playerToRoom.remove(user);
        if (id != null) {
            GameRoom r = rooms.get(id);
            if (r != null) {
                r.getPlayers().remove(user);
                if (r.getPlayers().size() < 2)
                    rooms.remove(id);
            }
        }
    }

    public static List<GameRoom> getAllGameRooms() {
        return new ArrayList<>(rooms.values());
    }

    public static void startGame(String id) {
        GameRoom r = rooms.get(id);
        if (r != null)
            r.setStatus("PLAYING");
    }

    public static void endGame(String id) {
        GameRoom r = rooms.get(id);
        if (r != null)
            r.setStatus("FINISHED");
    }
}
