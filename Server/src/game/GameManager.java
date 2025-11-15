package src.game;

import java.io.FileWriter;
import java.io.PrintWriter;
import src.dao.GameDAO;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import src.ClientHandler;
import dto.GameStart;
import dto.GameUpdate;

/**
 * Singleton manager to create and manage game sessions.
 * Uses simple CSV persistence for game history at ./game_history.csv
 */
public class GameManager {
    private static GameManager instance = new GameManager();
    private Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private Map<String, LeaderboardEntry> leaderboard = new ConcurrentHashMap<>();
    private GameDAO gameDAO;

    private GameManager() {}

    public static GameManager getInstance() { return instance; }

    public GameStart createGame(ClientHandler p1Handler, String p1, ClientHandler p2Handler, String p2) {
        String gameId = UUID.randomUUID().toString();
        long seed = Instant.now().toEpochMilli();
        List<Integer> seeds = generateSeeds(seed, 50); // generate 50 seeds
        GameSession s = new GameSession(gameId, p1, p2, seeds, seed, p1Handler, p2Handler);
        sessions.put(gameId, s);

        GameStart gs = new GameStart(gameId, seeds, seed, 30); // 30 seconds
        return gs;
    }

    private List<Integer> generateSeeds(long seed, int count) {
        Random r = new Random(seed);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < count; i++) out.add(r.nextInt(3)); // 0..2 representing type
        return out;
    }

    public void handleMove(String username, String gameId, int seedIndex, int choice) {
        GameSession s = sessions.get(gameId);
        if (s == null) return;
        synchronized (s) {
            s.processPick(username, seedIndex, choice);
            // send update to both players
            GameUpdate u = s.buildUpdate();
            try {
                s.getP1Handler().sendMessage(new dto.Message("GAME_UPDATE", "SERVER", u));
                s.getP2Handler().sendMessage(new dto.Message("GAME_UPDATE", "SERVER", u));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (s.isFinished()) {
                finalizeSession(s);
                sessions.remove(gameId);
            }
        }
    }

    private void finalizeSession(GameSession s) {
        // compute winner, score diff
        int score1 = s.getScore1();
        int score2 = s.getScore2();
        int diff = Math.abs(score1 - score2);
        int winnerId = score1 > score2 ? 1 : (score2 > score1 ? 2 : 0);

        // persist via DB if possible, otherwise fallback to CSV
        try {
            if (gameDAO == null) gameDAO = new GameDAO();
            boolean ok = gameDAO.insertGame(s.getId(), s.getP1(), s.getP2(), score1, score2);
            if (!ok) throw new RuntimeException("DB insert failed");
        } catch (Exception ex) {
            try (PrintWriter pw = new PrintWriter(new FileWriter("game_history.csv", true))) {
                pw.printf("%s,%s,%s,%d,%d,%d,%d\n", s.getId(), s.getP1(), s.getP2(), score1, score2, winnerId, diff);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // update leaderboard
        LeaderboardEntry e1 = leaderboard.getOrDefault(s.getP1(), new LeaderboardEntry(s.getP1()));
        LeaderboardEntry e2 = leaderboard.getOrDefault(s.getP2(), new LeaderboardEntry(s.getP2()));
        e1.totalPoints += score1; e2.totalPoints += score2;
        if (winnerId == 1) { e1.wins++; }
        else if (winnerId == 2) { e2.wins++; }
        leaderboard.put(s.getP1(), e1); leaderboard.put(s.getP2(), e2);
    }

    public Map<String, LeaderboardEntry> getLeaderboard() { return leaderboard; }

}

class LeaderboardEntry {
    String username;
    int totalPoints = 0;
    int wins = 0;
    LeaderboardEntry(String u) { username = u; }
}
