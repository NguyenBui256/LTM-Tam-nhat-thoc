package server.model;

import java.io.Serializable;

public class Seed implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private SeedType type;
    private SeedStatus status;

    public Seed(int id, SeedType type) {
        this.id = id;
        this.type = type;
        this.status = SeedStatus.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public SeedType getType() {
        return type;
    }

    public SeedStatus getStatus() {
        return status;
    }

    public void setStatus(SeedStatus status) {
        this.status = status;
    }

    public enum SeedType {
        GAO(1.0), THOC(1.5), NGO(2.0);

        private final double points;

        SeedType(double points) {
            this.points = points;
        }

        public double getPoints() {
            return points;
        }
    }

    public enum SeedStatus {
        AVAILABLE, PLAYER1, PLAYER2
    }
}
