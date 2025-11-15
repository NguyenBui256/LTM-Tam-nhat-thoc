package client.model;

public class MatchRecord {
    private String opponent;
    private String result;
    private String score;
    private String scoreOpp;
    private String eloChange;
    private String startTime;

    public MatchRecord(String opponent, String result,
                       String score, String eloChange, String scoreOpp) {

        this.opponent = opponent;
        this.result = result;
        this.score = score;
        this.eloChange = eloChange;
        this.scoreOpp = scoreOpp;
    }

    public String getOpponent() { return opponent; }
    public String getResult() { return result; }
    public String getScore() { return score; }
    public String getScoreOpp() { return scoreOpp; }
    public String getEloChange() { return eloChange; }
    public String getStartTime() { return startTime; }

    public void setOpponent(String s) { opponent = s; }
    public void setResult(String s) { result = s; }
    public void setScore(String s) { score = s; }
    public void setScoreOpp(String s) { scoreOpp = s; }
    public void setEloChange(String s) { eloChange = s; }
    public void setStartTime(String t) { startTime = t; }
}
