package dto;

import java.io.Serializable;

public class PlayerRank implements Serializable {
	private static final long serialVersionUID = 1L;
    private String name;
    private int elo;
    private String status;
    private int wins;


    public PlayerRank(String name, String status, int elo, int wins) {
        this.name = name;
        this.elo = elo;
        this.status = status;
        this.wins = wins;
    }



    
    public void setName(String name) {
		this.name = name;
	}




	public void setElo(int elo) {
		this.elo = elo;
	}




	public void setStatus(String status) {
		this.status = status;
	}




	public void setWins(int wins) {
		this.wins = wins;
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
    
}