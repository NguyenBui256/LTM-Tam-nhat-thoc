package server.dto;

import java.io.Serializable;
import java.util.List;
import server.model.Seed;

public class GameUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private List<Seed> seeds;
    private double score1;
    private double score2;

    public GameUpdate(String gameId, List<Seed> seeds, double score1, double score2) {
        this.gameId = gameId; this.seeds = seeds; this.score1 = score1; this.score2 = score2;
    }
    public String getGameId() { return gameId; }
    public List<Seed> getSeeds() { return seeds; }
    public double getScore1() { return score1; }
    public double getScore2() { return score2; }
}
