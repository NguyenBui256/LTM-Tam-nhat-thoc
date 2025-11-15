package server.game;

import java.io.FileWriter;
import java.io.PrintWriter;

import dto.*;
import server.dao.GameDAO;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import server.ClientHandler;
import server.model.Game;
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

    public GameSession createGameSession(String gameId, ClientHandler p1Handler, String p1, ClientHandler p2Handler, String p2) {
        long seed = Instant.now().toEpochMilli();
        List<Integer> seeds = generateSeeds(seed, 50); // generate 50 seeds
        GameSession s = new GameSession(gameId, p1, p2, seeds, seed, p1Handler, p2Handler);
        sessions.put(gameId, s);
        return s;
    }

    private List<Integer> generateSeeds(long seed, int count) {
        Random r = new Random(seed);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < count; i++) out.add(r.nextInt(3)); // 0..2 representing type
        return out;
    }

    public void handleMove(String username, String gameId, int selectedSeedIndex, int basketType) {
        System.out.println("[LOG] handleMove() CALLED - user=" + username + ", gameId=" + gameId + ", index=" + selectedSeedIndex + ", basket=" + basketType);
        GameSession s = sessions.getOrDefault(gameId, null);
        if (s == null) {
            System.out.println("[LOG] ❌ ERROR: GameSession object is null - gameId không tồn tại!");
            System.out.println("[LOG] Available sessions: " + sessions.keySet());
            return;
        }
        System.out.println("[LOG] ✓ Found GameSession. Calling processPick...");
        try {
            System.out.println("[LOG] About to call processPick for gameSession " + gameId);
            s.processPick(username, selectedSeedIndex, basketType);
            System.out.println("[LOG] processPick completed successfully");
        } catch (Exception e) {
            System.out.println("[LOG] ❌ ERROR in processPick: " + e.getMessage());
            e.printStackTrace();
        }
//        synchronized (s) {
            // send update to each player separately
            try {
                GameUpdate u1 = s.buildUpdate(s.getP1(), s.getP2());
                GameUpdate u2 = s.buildUpdate(s.getP2(), s.getP1());
                System.out.println("[LOG] Sending GAME_UPDATE to " + s.getP1() + " with takenBy size: " + u1.getTakenBy().size());
                System.out.println("[LOG] Sending GAME_UPDATE to " + s.getP2() + " with takenBy size: " + u2.getTakenBy().size());
                if (s.getP1Handler() != null) s.getP1Handler().sendMessage(new Message("GAME_UPDATE", "SERVER", u1));
                if (s.getP2Handler() != null) s.getP2Handler().sendMessage(new Message("GAME_UPDATE", "SERVER", u2));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (s.isFinished()) {
                GameResult result = finalizeSession(s);

                // Create separate END_GAME messages for each player with their perspective
                String p1Content = "gameId=" + gameId + ",winner=" + result.getWinner() +
                    ",yourScore=" + result.getScore1() + ",opponentScore=" + result.getScore2() +
                    ",yourEloChange=" + result.getEloChange1() + ",opponentEloChange=" + result.getEloChange2();

                String p2Content = "gameId=" + gameId + ",winner=" + result.getWinner() +
                    ",yourScore=" + result.getScore2() + ",opponentScore=" + result.getScore1() +
                    ",yourEloChange=" + result.getEloChange2() + ",opponentEloChange=" + result.getEloChange1();

                System.out.println("[SERVER LOG] Game finished - Sending END_GAME to P1 (" + s.getP1() + "): " + p1Content);
                System.out.println("[SERVER LOG] Game finished - Sending END_GAME to P2 (" + s.getP2() + "): " + p2Content);

                try {
                    if (s.getP1Handler() != null) {
                        s.getP1Handler().sendMessage(new Message("END_GAME", "SERVER", p1Content));
                        System.out.println("[SERVER LOG] Sent END_GAME to P1: " + s.getP1());
                    }
                    if (s.getP2Handler() != null) {
                        s.getP2Handler().sendMessage(new Message("END_GAME", "SERVER", p2Content));
                        System.out.println("[SERVER LOG] Sent END_GAME to P2: " + s.getP2());
                    }
                } catch (Exception e) {
                    System.out.println("[SERVER LOG] Error sending END_GAME: " + e.getMessage());
                    e.printStackTrace();
                }
            }
//        }
    }

    public void startGame(String p1, ClientHandler p1Handler, String p2, ClientHandler p2Handler, String gameId) {
        GameSession s = createGameSession(gameId, p1Handler, p1, p2Handler, p2);

        // Create separate GameStart for each player (with correct perspective)
        GameStart gs1 = new GameStart(gameId, s.getSeeds(), s.getSeed(), 30, p1, p2);
        GameStart gs2 = new GameStart(gameId, s.getSeeds(), s.getSeed(), 30, p2, p1);

        // Insert game into database with initial scores 0
        try {
            if (gameDAO == null) gameDAO = new GameDAO();
            boolean ok = gameDAO.insertGame(gameId, p1, p2, 0, 0);
            if (!ok) System.out.println("[SERVER LOG] Failed to insert game into DB");
            else System.out.println("[SERVER LOG] Successfully inserted game into DB");
        } catch (Exception e) {
            System.out.println("[SERVER LOG] DB error when inserting game: " + e.getMessage());
        }
        try {
            System.out.println("[SERVER LOG] Sending start game message to clients");
            if (p1Handler != null) p1Handler.sendMessage(new Message("START_GAME", "SERVER", gs1));
            if (p2Handler != null) p2Handler.sendMessage(new Message("START_GAME", "SERVER", gs2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Gửi info game cho 2 người chơi bằng socket
    }

    private GameResult finalizeSession(GameSession s) {
        // compute winner, score diff
        int score1 = s.getScores().get(s.getP1());
        int score2 = s.getScores().get(s.getP2());
        int diff = Math.abs(score1 - score2);
        int winnerId = score1 > score2 ? 1 : (score2 > score1 ? 2 : 0);
        String winner = winnerId == 1 ? s.getP1() : winnerId == 2 ? s.getP2() : "Draw";
        int eloChange = diff / 2;
        int eloChange1 = winnerId == 1 ? eloChange : winnerId == 0 ? 0 : -eloChange;
        int eloChange2 = winnerId == 2 ? eloChange : winnerId == 0 ? 0 : -eloChange;

        // persist via DB if possible, otherwise fallback to CSV
        if (gameDAO == null) gameDAO = new GameDAO();
        boolean ok = gameDAO.updateGame(s.getId(), score1, score2);
        if (!ok) throw new RuntimeException("DB update failed");

        // update leaderboard
        LeaderboardEntry e1 = leaderboard.getOrDefault(s.getP1(), new LeaderboardEntry(s.getP1()));
        LeaderboardEntry e2 = leaderboard.getOrDefault(s.getP2(), new LeaderboardEntry(s.getP2()));
        e1.totalPoints += score1; e2.totalPoints += score2;
        if (winnerId == 1) { e1.wins++; }
        else if (winnerId == 2) { e2.wins++; }
        leaderboard.put(s.getP1(), e1); leaderboard.put(s.getP2(), e2);

        return new GameResult(winner, score1, score2, eloChange1, eloChange2);
    }

    public void endGame(String username, String gameId) {
        System.out.println("[SERVER LOG] endGame called for user: " + username + ", gameId: " + gameId);
        GameSession s = sessions.get(gameId);
        if (s == null) {
            System.out.println("[SERVER LOG] GameSession not found for gameId: " + gameId);
            return;
        }
        synchronized (s) {
            System.out.println("[SERVER LOG] Finalizing session for gameId: " + gameId);
            GameResult result = finalizeSession(s);

            // Create separate END_GAME messages for each player with their perspective
            String p1Content = "gameId=" + gameId + ",winner=" + result.getWinner() +
                ",yourScore=" + result.getScore1() + ",opponentScore=" + result.getScore2() +
                ",yourEloChange=" + result.getEloChange1() + ",opponentEloChange=" + result.getEloChange2();

            String p2Content = "gameId=" + gameId + ",winner=" + result.getWinner() +
                ",yourScore=" + result.getScore2() + ",opponentScore=" + result.getScore1() +
                ",yourEloChange=" + result.getEloChange2() + ",opponentEloChange=" + result.getEloChange1();

            System.out.println("[SERVER LOG] Sending END_GAME to P1 (" + s.getP1() + "): " + p1Content);
            System.out.println("[SERVER LOG] Sending END_GAME to P2 (" + s.getP2() + "): " + p2Content);

            try {
                if (s.getP1Handler() != null) {
                    s.getP1Handler().sendMessage(new Message("END_GAME", "SERVER", p1Content));
                    System.out.println("[SERVER LOG] Sent END_GAME to P1: " + s.getP1());
                }
                if (s.getP2Handler() != null) {
                    s.getP2Handler().sendMessage(new Message("END_GAME", "SERVER", p2Content));
                    System.out.println("[SERVER LOG] Sent END_GAME to P2: " + s.getP2());
                }
            } catch (Exception e) {
                System.out.println("[SERVER LOG] Error sending END_GAME: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Map<String, LeaderboardEntry> getLeaderboard() { return leaderboard; }

}

class LeaderboardEntry {
    String username;
    int totalPoints = 0;
    int wins = 0;
    LeaderboardEntry(String u) { username = u; }
}
