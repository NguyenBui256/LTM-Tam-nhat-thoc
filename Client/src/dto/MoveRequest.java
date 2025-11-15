package dto;

import java.io.Serializable;

public class MoveRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private int selectedSeedIndex;
    private int basketType;

    public MoveRequest(String gameId, int selectedSeedIndex, int basketType) {
        this.gameId = gameId;
        this.selectedSeedIndex = selectedSeedIndex;
        this.basketType = basketType;
    }

    public String getGameId() { return gameId; }
    public int getSelectedSeedIndex() { return selectedSeedIndex; }
    public int getBasketType() { return basketType; }
}
