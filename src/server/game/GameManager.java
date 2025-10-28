package server.game;

import java.io.FileWriter;
import java.io.PrintWriter;
import server.dao.GameDAO;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import server.ClientHandler;
import server.dao.UserDAO;
import server.dto.GameStart;
import server.dto.GameUpdate;
import server.model.Game;
import server.model.Seed;

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
        List<Seed> seeds = generateSeeds(seed); // generate 45 seeds: 15 each type
        UserDAO ud = new UserDAO();
        String player1Id = ud.getUserId(p1);
        String player2Id = ud.getUserId(p2);
        GameSession s = new GameSession(gameId, p1, p2, player1Id, player2Id, seeds, seed, p1Handler, p2Handler);
        sessions.put(gameId, s);

        GameStart gs = new GameStart(gameId, seeds, seed, 30); // 30 seconds
        return gs;
    }

    private List<Seed> generateSeeds(long seed) {
        List<Seed> out = new ArrayList<>();
        int id = 0;
        for (int i = 0; i < 15; i++) {
            out.add(new Seed(id++, Seed.SeedType.GAO));
            out.add(new Seed(id++, Seed.SeedType.THOC));
            out.add(new Seed(id++, Seed.SeedType.NGO));
        }
        // Shuffle using seed for reproducibility
        Collections.shuffle(out, new Random(seed));
        return out;
    }

    public void handleMove(String username, String gameId, int seedId, int choice, int basketType) {
        GameSession s = sessions.get(gameId);
        if (s == null) return;
        synchronized (s) {
            s.processPick(username, seedId, choice, basketType);
            // send update to both players
            GameUpdate u = s.buildUpdate();
            try {
                s.getP1Handler().sendMessage(new server.dto.Message("GAME_UPDATE", "SERVER", u));
                s.getP2Handler().sendMessage(new server.dto.Message("GAME_UPDATE", "SERVER", u));
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
        double score1 = s.getScore1();
        double score2 = s.getScore2();
        double diff = Math.abs(score1 - score2);
        int winner = score1 > score2 ? 1 : (score2 > score1 ? 2 : 0); // 1=p1 wins, 2=p2 wins, 0=draw

        // update elo
        UserDAO ud = new UserDAO();
        int elo1 = ud.getUserElo(s.getPlayer1Id());
        int elo2 = ud.getUserElo(s.getPlayer2Id());
        int eloChange = (int) Math.round(diff / 2.0);
        if (winner == 1) {
            ud.updateUserElo(s.getPlayer1Id(), elo1 + eloChange);
            ud.updateUserElo(s.getPlayer2Id(), elo2 - eloChange);
        } else if (winner == 2) {
            ud.updateUserElo(s.getPlayer1Id(), elo1 - eloChange);
            ud.updateUserElo(s.getPlayer2Id(), elo2 + eloChange);
        }
        // if draw, no change

        // persist via DB if possible, otherwise fallback to CSV
        try {
            if (gameDAO == null) gameDAO = new GameDAO();
            boolean ok = gameDAO.insertGame(s.getId(), s.getPlayer1Id(), s.getPlayer2Id(), score1, score2);
            if (!ok) throw new RuntimeException("DB insert failed");
        } catch (Exception ex) {
            try (PrintWriter pw = new PrintWriter(new FileWriter("game_history.csv", true))) {
                pw.printf("%s,%s,%s,%.2f,%.2f,%d,%.2f\n", s.getId(), s.getPlayer1Id(), s.getPlayer2Id(), score1, score2, winner, diff);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // update leaderboard
        LeaderboardEntry e1 = leaderboard.getOrDefault(s.getP1(), new LeaderboardEntry(s.getP1()));
        LeaderboardEntry e2 = leaderboard.getOrDefault(s.getP2(), new LeaderboardEntry(s.getP2()));
        e1.totalPoints += score1; e2.totalPoints += score2;
        if (winner == 1) { e1.wins++; }
        else if (winner == 2) { e2.wins++; }
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
