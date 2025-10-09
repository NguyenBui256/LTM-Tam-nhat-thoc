package server.model;

public class Game {
    private String id;
    private int userId_1;
    private int userId_2;
    private String userResult_1;
    private String userResult_2;
    private int winnerId;
    private int scoreDiff;

    public Game() {
    }

    public Game(String id, int userId_1, int userId_2, String userResult_1, String userResult_2, int winnerId, int scoreDiff) {
        this.id = id;
        this.userId_1 = userId_1;
        this.userId_2 = userId_2;
        this.userResult_1 = userResult_1;
        this.userResult_2 = userResult_2;
        this.winnerId = winnerId;
        this.scoreDiff = scoreDiff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId_1() {
        return userId_1;
    }

    public void setUserId_1(int userId_1) {
        this.userId_1 = userId_1;
    }

    public int getUserId_2() {
        return userId_2;
    }

    public void setUserId_2(int userId_2) {
        this.userId_2 = userId_2;
    }

    public String getUserResult_1() {
        return userResult_1;
    }

    public void setUserResult_1(String userResult_1) {
        this.userResult_1 = userResult_1;
    }

    public String getUserResult_2() {
        return userResult_2;
    }

    public void setUserResult_2(String userResult_2) {
        this.userResult_2 = userResult_2;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public int getScoreDiff() {
        return scoreDiff;
    }

    public void setScoreDiff(int scoreDiff) {
        this.scoreDiff = scoreDiff;
    }
}
