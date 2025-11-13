package client.model;

import javafx.beans.property.*;

public class PlayerRankView {
    private final StringProperty name;
    private final StringProperty status;
    private final IntegerProperty elo;
    private final IntegerProperty wins;

    public PlayerRankView(String name, String status, int elo, int wins) {
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
        this.elo = new SimpleIntegerProperty(elo);
        this.wins = new SimpleIntegerProperty(wins);
    }

    // property getters
    public StringProperty nameProperty() { return name; }
    public StringProperty statusProperty() { return status; }
    public IntegerProperty eloProperty() { return elo; }
    public IntegerProperty winsProperty() { return wins; }

    // normal getters/setters
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public int getElo() { return elo.get(); }
    public void setElo(int elo) { this.elo.set(elo); }

    public int getWins() { return wins.get(); }
    public void setWins(int wins) { this.wins.set(wins); }
}
