package src;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    public static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        private String roomId;
        private String user1;
        private String user2;

        public Room(String roomId, String user1, String user2) {
            this.roomId = roomId;
            this.user1 = user1;
            this.user2 = user2;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getUser1() {
            return user1;
        }

        public String getUser2() {
            return user2;
        }
    }

    public static class GameResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private String roomId;
        private String user1;
        private String user2;
        private int score1;
        private int score2;

        public GameResult(String roomId, String user1, String user2, int score1, int score2) {
            this.roomId = roomId;
            this.user1 = user1;
            this.user2 = user2;
            this.score1 = score1;
            this.score2 = score2;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getUser1() {
            return user1;
        }

        public String getUser2() {
            return user2;
        }

        public int getScore1() {
            return score1;
        }

        public int getScore2() {
            return score2;
        }
    }

    private static Map<String, Room> rooms = new ConcurrentHashMap<>();

    public static Room createRoom(String u1, String u2) {
        String id = UUID.randomUUID().toString();
        Room r = new Room(id, u1, u2);
        rooms.put(id, r);
        return r;
    }

    public static Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public static void removeRoom(String roomId) {
        rooms.remove(roomId);
    }
}
