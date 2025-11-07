package client.model;

public class Player {
    private int rank;
    private String name;
    private int elo;
    private String status;
    private int wins;


    public Player(String name, String status, int elo, int wins) {
        this.name = name;
        this.elo = elo;
        this.status = status;
        this.wins = wins;
    }


    public Player(int rank, String name, int elo, int wins) {
        this.rank = rank;
        this.name = name;
        this.elo = elo;
        this.wins = wins;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public int getElo() {
        return elo;
    }

    public String getStatus() {
        return status;
    }

    public int getWins() {
        return wins;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }
}
