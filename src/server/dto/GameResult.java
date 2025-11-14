package server.dto;

import java.io.Serializable;

public class GameResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String winner;
    private int score1;
    private int score2;
    private int eloChange1;
    private int eloChange2;

    public GameResult(String winner, int score1, int score2, int eloChange1, int eloChange2) {
        this.winner = winner;
        this.score1 = score1;
        this.score2 = score2;
        this.eloChange1 = eloChange1;
        this.eloChange2 = eloChange2;
    }

    public String getWinner() { return winner; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
    public int getEloChange1() { return eloChange1; }
    public int getEloChange2() { return eloChange2; }
}
