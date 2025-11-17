package dto;

import java.io.Serializable;

public class PlayerStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String status; // ONLINE, WAITING, IN_GAME, BUSY
    private int elo;
    private int wins;
    private String currentRoomId;

    public PlayerStatus(String username, String status, int elo) {
        this.username = username;
        this.status = status;
        this.elo = elo;
        this.wins = 0;
    }

    public PlayerStatus(String username, String status, int elo, int wins) {
        this.username = username;
        this.status = status;
        this.elo = elo;
        this.wins = wins;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public int getElo() {
        return elo;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }
}
