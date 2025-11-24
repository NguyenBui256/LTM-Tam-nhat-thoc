package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple manager to track pending rematch requests per room.
 * roomId -> Pending(data)
 */
public class RematchManager {
    private static class Pending {
        String requester;
        String opponent;

        Pending(String requester, String opponent) {
            this.requester = requester;
            this.opponent = opponent;
        }
    }

    private static final Map<String, Pending> pendingMap = new ConcurrentHashMap<>();

    public static boolean isPending(String roomId) {
        return pendingMap.containsKey(roomId);
    }

    public static void createPending(String roomId, String requester, String opponent) {
        pendingMap.put(roomId, new Pending(requester, opponent));
    }

    public static String getRequester(String roomId) {
        Pending p = pendingMap.get(roomId);
        return p == null ? null : p.requester;
    }

    public static String getOpponent(String roomId) {
        Pending p = pendingMap.get(roomId);
        return p == null ? null : p.opponent;
    }

    public static void clear(String roomId) {
        pendingMap.remove(roomId);
    }
}