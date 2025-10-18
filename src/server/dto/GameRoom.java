package server.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    private String roomId;
    private String player1;
    private String player2;
    private String status; // WAITING, PLAYING, FINISHED
    private List<String> players = new ArrayList<>();

    public GameRoom(String roomId, String player1, String player2) {
        this.roomId = roomId;
        this.player1 = player1;
        this.player2 = player2;
        this.status = "WAITING";
        this.players.add(player1);
        this.players.add(player2);
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getPlayers() {
        return players;
    }
}
