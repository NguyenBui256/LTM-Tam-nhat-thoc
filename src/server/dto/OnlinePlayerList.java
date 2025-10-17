package server.dto;

import java.io.Serializable;
import java.util.List;

public class OnlinePlayerList implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<PlayerStatus> players;

    public OnlinePlayerList(List<PlayerStatus> players) {
        this.players = players;
    }

    public List<PlayerStatus> getPlayers() {
        return players;
    }
}
