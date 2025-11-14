package server.dto;

import java.io.Serializable;
import java.util.List;

public class GameStart implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private List<Integer> seeds;
    private long seed;
    private int timeLimitSec;
    private String currentPlayerName;
    private String opponentName;

    public GameStart(String gameId, List<Integer> seeds, long seed, int timeLimitSec, String currentPlayerName, String opponentName) {
        this.gameId = gameId;
        this.seeds = seeds;
        this.seed = seed;
        this.timeLimitSec = timeLimitSec;
        this.currentPlayerName = currentPlayerName;
        this.opponentName = opponentName;
    }

    public String getGameId() { return gameId; }
    public List<Integer> getSeeds() { return seeds; }
    public long getSeed() { return seed; }
    public int getTimeLimitSec() { return timeLimitSec; }
    public String getCurrentPlayerName() { return currentPlayerName; }
    public String getOpponentName() { return opponentName; }
}
