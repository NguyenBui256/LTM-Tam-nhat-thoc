package server.game;

import java.io.Serializable;
import java.util.*;
import server.dto.GameUpdate;

import server.ClientHandler;

public class GameSession implements Serializable {
    private final String id;
    private String p1;
    private String p2;
    private final List<Integer> seeds; // 0..2
    private long seed;
    private final ClientHandler p1Handler;
    private final ClientHandler p2Handler;
    private final Map<Integer, String> takenBy = new HashMap<>(); // seedIndex -> username
    private final Map<String, Integer> scores = new HashMap<>(); // username -> score

    public GameSession(String id, String p1, String p2, List<Integer> seeds, long seed, ClientHandler p1Handler, ClientHandler p2Handler) {
        this.id = id; this.p1 = p1; this.p2 = p2; this.seeds = seeds; this.seed = seed; this.p1Handler = p1Handler; this.p2Handler = p2Handler;
        scores.put(p1, 0);
        scores.put(p2, 0);
        System.out.println("[LOG] GameSession created - id=" + id + ", p1=" + p1 + ", p2=" + p2);
        System.out.println("[LOG] Seeds generated with seed=" + seed + ", first 10 seeds: " + seeds.subList(0, Math.min(10, seeds.size())));
        startTimer();
    }

    private void startTimer() {
        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(30000); // 30 seconds
                // Time up, end game
                System.out.println("[SERVER LOG] Game " + id + " time up, ending game");
                GameManager.getInstance().endGame(p1, id);
            } catch (InterruptedException e) {
                // Timer interrupted, game ended early
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    public String getId() { return id; }
    public String getP1() { return p1; }
    public String getP2() { return p2; }
    public ClientHandler getP1Handler() { return p1Handler; }
    public ClientHandler getP2Handler() { return p2Handler; }

    private int lastIndex = -1;
    private int lastChoice = -1;

    public void processPick(String username, int index, int choice) {
        System.out.println("[LOG] processPick() called - username=" + username + ", index=" + index + ", choice=" + choice);
        if (index < 0 || index >= seeds.size()) {
            System.out.println("[LOG] ERROR: Index out of bounds! index=" + index + ", seeds.size()=" + seeds.size());
            return;
        }
        System.out.println("[LOG] DEBUG: takenBy map currently has " + takenBy.size() + " entries: " + takenBy.keySet());
        if (takenBy.containsKey(index)) {
            System.out.println("[LOG] ❌ SKIP: Seed index=" + index + " already taken by=" + takenBy.get(index));
            return; // already taken
        }
        int actual = seeds.get(index);
        System.out.println("[LOG] DEBUG: seeds.get(" + index + ") = " + actual);
        boolean correct = choice == actual;
        int delta = correct ? (actual + 1) : -1; // gạo (0) +1=1, thóc (1) +2=2, ngô (2) +3=3; sai -1
        System.out.println("[LOG] USER: " + username + " picks seed#" + index + " as type " + choice + " (actual: " + actual + ") = " + (correct ? "✓ CORRECT" : "✗ WRONG") + ", delta=" + delta);

        // Chỉ add vào takenBy nếu nhặt đúng (hạt bị remove khỏi bàn)
        // Nếu nhặt sai, hạt vẫn còn và người khác có thể nhặt lại
        if (correct) {
            takenBy.put(index, username);
            System.out.println("[LOG] ✓ CORRECT PICK! takenBy now has " + takenBy.size() + " entries. Adding seed #" + index + " for user " + username);
            System.out.println("[LOG] Updated takenBy: " + takenBy);
        } else {
            System.out.println("[LOG] ✗ WRONG PICK! Seed #" + index + " (actual type: " + actual + ", choice: " + choice + ") vẫn còn trên bàn");
        }

        int currentScore = scores.getOrDefault(username, 0);
        scores.put(username, currentScore + delta);
        lastIndex = index;
        lastChoice = choice;

        System.out.println("[LOG] Current scores after update: " + scores);
    }

    public boolean isFinished() {
        // finish when all seeds taken or some time condition (handled client-side)
        return takenBy.size() >= seeds.size();
    }

    public int getScore(String username) { return scores.getOrDefault(username, 0); }
    public Map<String, Integer> getScores() { return scores; }

    public GameUpdate buildUpdate(String username, String opponent) {
        System.out.println("[LOG] buildUpdate for " + username + " - takenBy size=" + takenBy.size() + ", contents=" + takenBy);
        // Deep copy takenBy map to avoid serialization reference issues
        Map<Integer, String> takenByCopy = new HashMap<>(takenBy);
        Map<String, Integer> scoresCopy = new HashMap<>(scores);
        GameUpdate u = new GameUpdate(id, scoresCopy, takenByCopy, username, opponent, this.scores.get(username), this.scores.get(opponent));
        u.setLastIndex(lastIndex);
        u.setLastChoice(lastChoice);
        return u;
    }

    public List<Integer> getSeeds() {
        return seeds;
    }

    public long getSeed() {
        return seed;
    }

    public Map<Integer, String> getTakenBy() {
        return takenBy;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public int getLastChoice() {
        return lastChoice;
    }
}
