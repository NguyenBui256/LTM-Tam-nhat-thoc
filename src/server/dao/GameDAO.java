package server.dao;

import server.model.Game;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameDAO extends DAO {
    public boolean insertGame(Game g) {
        String sql = "INSERT INTO game(userId_1,userId_2,userResult_1,userResult_2,winnerId,scoreDiff) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getUserId_1());
            ps.setInt(2, g.getUserId_2());
            ps.setString(3, g.getUserResult_1());
            ps.setString(4, g.getUserResult_2());
            // winnerId may be 0 for draw, setNull otherwise
            if (g.getWinnerId() <= 0)
                ps.setNull(5, java.sql.Types.INTEGER);
            else
                ps.setInt(5, g.getWinnerId());
            ps.setInt(6, g.getScoreDiff());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
