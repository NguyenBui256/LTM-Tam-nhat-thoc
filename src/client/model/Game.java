package client.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Game implements Serializable {
	private static final long serialVersionUID = 99L;
    private String id;
    private String userId_1;
    private String userId_2;
    private String userResult_1;
    private String userResult_2;
    private int winnerId;
    private int scoreDiff;
    private LocalDateTime time;
    public Game() {
    }

    public Game(String id, String userId_1, String userId_2, String userResult_1, String userResult_2, int winnerId,
            int scoreDiff) {
        this.id = id;
        this.userId_1 = userId_1;
        this.userId_2 = userId_2;
        this.userResult_1 = userResult_1;
        this.userResult_2 = userResult_2;
        this.winnerId = winnerId;
        this.scoreDiff = scoreDiff;
    }
    public LocalDateTime getTime() {
    	return time;
    }
    public void setTime(LocalDateTime time) {
    	this.time = time;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId_1() {
        return userId_1;
    }

    public void setUserId_1(String string) {
        this.userId_1 = string;
    }

    public String getUserId_2() {
        return userId_2;
    }

    public void setUserId_2(String userId_2) {
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