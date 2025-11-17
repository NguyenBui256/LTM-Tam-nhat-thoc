package dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GameUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private Map<Integer, String> takenBy;
    private Map<String, Integer> scores;
    private List<Integer> seeds;
    private int lastIndex = -1;
    private int lastChoice = -1;
    private int timeLeft = 30;
    private String currentPlayerName;
    private String opponentName;
    private int currentPlayerScore;
    private int opponentScore;

    public GameUpdate(String gameId, Map<String, Integer> scores, Map<Integer, String> takenBy, String currentPlayerName, String opponentName, int currentPlayerScore, int opponentScore) {
        this.gameId = gameId;
        this.scores = scores;
        this.takenBy = takenBy;
        this.currentPlayerName = currentPlayerName;
        this.opponentName = opponentName;
        this.currentPlayerScore = currentPlayerScore;
        this.opponentScore = opponentScore;
    }

    public String getGameId() { return gameId; }
    public Map<Integer, String> getTakenBy() { return takenBy; }
    public Map<String, Integer> getScores() { return scores; }
    public List<Integer> getSeeds() { return seeds; }
    public int getLastIndex() { return lastIndex; }
    public int getLastChoice() { return lastChoice; }
    public int getTimeLeft() { return timeLeft; }
    public String getCurrentPlayerName() { return currentPlayerName; }
    public String getOpponentName() { return opponentName; }
    public int getCurrentPlayerScore() { return currentPlayerScore; }
    public int getOpponentScore() { return opponentScore; }

    // === Setters ===
    public void setSeeds(List<Integer> seeds) { this.seeds = seeds; }
    public void setLastIndex(int lastIndex) { this.lastIndex = lastIndex; }
    public void setLastChoice(int lastChoice) { this.lastChoice = lastChoice; }
    public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }
}
