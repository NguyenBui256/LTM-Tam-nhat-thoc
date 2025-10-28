package server.dto;

import java.io.Serializable;
import java.util.List;
import server.model.Seed;

public class GameStart implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private List<Seed> seeds;
    private long seed;
    private int timeLimitSec;

    public GameStart(String gameId, List<Seed> seeds, long seed, int timeLimitSec) {
        this.gameId = gameId; this.seeds = seeds; this.seed = seed; this.timeLimitSec = timeLimitSec;
    }
    public String getGameId() { return gameId; }
    public List<Seed> getSeeds() { return seeds; }
    public long getSeed() { return seed; }
    public int getTimeLimitSec() { return timeLimitSec; }
}
