package server.game;

import java.util.*;

import server.ClientHandler;

public class GameSession {
    private String id;
    private String p1;
    private String p2;
    private List<Integer> seeds; // 0..2
    private long seed;
    private ClientHandler p1Handler;
    private ClientHandler p2Handler;
    private Map<Integer, String> takenBy = new HashMap<>(); // seedIndex -> username
    private int score1 = 0;
    private int score2 = 0;

    public GameSession(String id, String p1, String p2, List<Integer> seeds, long seed, ClientHandler p1Handler, ClientHandler p2Handler) {
        this.id = id; this.p1 = p1; this.p2 = p2; this.seeds = seeds; this.seed = seed; this.p1Handler = p1Handler; this.p2Handler = p2Handler;
    }

    public String getId() { return id; }
    public String getP1() { return p1; }
    public String getP2() { return p2; }
    public ClientHandler getP1Handler() { return p1Handler; }
    public ClientHandler getP2Handler() { return p2Handler; }

    public void processPick(String username, int index, int choice) {
        if (index < 0 || index >= seeds.size()) return;
        if (takenBy.containsKey(index)) return; // already taken
        takenBy.put(index, username);
        int actual = seeds.get(index);
        boolean correct = (actual == choice);
        int delta = correct ? 1 : -1; // simple config; extendable
        if (username.equals(p1)) score1 += delta; else if (username.equals(p2)) score2 += delta;
    }

    public boolean isFinished() {
        // finish when all seeds taken or some time condition (handled client-side)
        return takenBy.size() >= seeds.size();
    }

    public int getScore1() { return score1; }
    public int getScore2() { return score2; }

    public dto.GameUpdate buildUpdate() {
        dto.GameUpdate u = new dto.GameUpdate(id, takenBy, score1, score2);
        return u;
    }
}
