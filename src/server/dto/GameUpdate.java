package server.dto;

import java.io.Serializable;
import java.util.Map;

public class GameUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private Map<Integer, String> takenBy;
    private int score1;
    private int score2;

    public GameUpdate(String gameId, Map<Integer, String> takenBy, int score1, int score2) {
        this.gameId = gameId; this.takenBy = takenBy; this.score1 = score1; this.score2 = score2;
    }
    public String getGameId() { return gameId; }
    public Map<Integer, String> getTakenBy() { return takenBy; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
}
