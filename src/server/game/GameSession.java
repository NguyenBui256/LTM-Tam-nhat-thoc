package server.game;

import java.util.*;

import server.ClientHandler;
import server.model.Seed;

public class GameSession {
    private String id;
    private String p1;
    private String p2;
    private String player1Id;
    private String player2Id;
    private List<Seed> seeds;
    private long seed;
    private ClientHandler p1Handler;
    private ClientHandler p2Handler;
    private double score1 = 0.0;
    private double score2 = 0.0;

    public GameSession(String id, String p1, String p2, String player1Id, String player2Id, List<Seed> seeds, long seed, ClientHandler p1Handler, ClientHandler p2Handler) {
        this.id = id; this.p1 = p1; this.p2 = p2; this.player1Id = player1Id; this.player2Id = player2Id; this.seeds = seeds; this.seed = seed; this.p1Handler = p1Handler; this.p2Handler = p2Handler;
    }

    public String getId() { return id; }
    public String getP1() { return p1; }
    public String getP2() { return p2; }
    public String getPlayer1Id() { return player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public ClientHandler getP1Handler() { return p1Handler; }
    public ClientHandler getP2Handler() { return p2Handler; }

    public void processPick(String username, int seedId, int choice, int basketType) {
        Seed seed = null;
        for (Seed s : seeds) {
            if (s.getId() == seedId) {
                seed = s;
                break;
            }
        }
        if (seed == null || seed.getStatus() != Seed.SeedStatus.AVAILABLE) return;
        int actual = seed.getType().ordinal(); // GAO=0, THOC=1, NGO=2
        double delta;
        if (actual == choice && actual == basketType) {
            delta = seed.getType().getPoints(); // đúng rổ và đúng hạt
        } else {
            delta = -1.0; // sai rổ hoặc sai hạt
        }
        if (username.equals(p1)) {
            score1 += delta;
            seed.setStatus(Seed.SeedStatus.PLAYER1);
        } else if (username.equals(p2)) {
            score2 += delta;
            seed.setStatus(Seed.SeedStatus.PLAYER2);
        }
    }

    public boolean isFinished() {
        // finish when all seeds taken
        for (Seed s : seeds) {
            if (s.getStatus() == Seed.SeedStatus.AVAILABLE) return false;
        }
        return true;
    }

    public double getScore1() { return score1; }
    public double getScore2() { return score2; }

    public server.dto.GameUpdate buildUpdate() {
        server.dto.GameUpdate u = new server.dto.GameUpdate(id, seeds, score1, score2);
        return u;
    }
}
