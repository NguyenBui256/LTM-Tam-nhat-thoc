package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight DAO to persist game results into MySQL database at jdbc:mysql://localhost:3306/game_server
 *
 * Default credentials used: root / 123456. Change the URL/credentials below if your DB differs.
 */
public class GameDAO extends DAO {

    public GameDAO() {
        try {
            ensureTables();
        } catch (SQLException e) {
            e.printStackTrace();
            conn = null;
        }
    }

    private void ensureTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS games ("
                + "id VARCHAR(128) PRIMARY KEY,"
                + "user1 VARCHAR(128),"
                + "user2 VARCHAR(128),"
                + "score1 INT,"
                + "score2 INT,"
                + "winner VARCHAR(128),"
                + "score_diff INT,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB;";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    public boolean insertGame(String id, String user1, String user2, int score1, int score2) {
        if (conn == null) return false;
        String winner = null;
        if (score1 > score2) winner = user1;
        else if (score2 > score1) winner = user2;
        int diff = Math.abs(score1 - score2);

        String sql = "INSERT INTO games(id,user1,user2,score1,score2,winner,score_diff) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, user1);
            ps.setString(3, user2);
            ps.setInt(4, score1);
            ps.setInt(5, score2);
            ps.setString(6, winner);
            ps.setInt(7, diff);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGame(String id, int score1, int score2) {
        if (conn == null) return false;
        
        // Lấy user1 và user2 từ database để xác định winner
        String user1 = null, user2 = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT user1, user2 FROM games WHERE id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user1 = rs.getString("user1");
                user2 = rs.getString("user2");
            } else {
                return false; // Game không tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String winner = null;
        if (score1 > score2) winner = user1;
        else if (score2 > score1) winner = user2;
        int diff = Math.abs(score1 - score2);

        String sql = "UPDATE games SET score1=?, score2=?, winner=?, score_diff=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score1);
            ps.setInt(2, score2);
            ps.setString(3, winner);
            ps.setInt(4, diff);
            ps.setString(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getLeaderboardByPoints() {
        List<Map<String, Object>> out = new ArrayList<>();
        if (conn == null) return out;
        String sql = "SELECT player, SUM(points) AS total FROM ("
                + " SELECT user1 AS player, score1 AS points FROM games"
                + " UNION ALL"
                + " SELECT user2 AS player, score2 AS points FROM games"
                + " ) t GROUP BY player ORDER BY total DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("player"));
                row.put("totalPoints", rs.getInt("total"));
                out.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<Map<String, Object>> getLeaderboardByWins() {
        List<Map<String, Object>> out = new ArrayList<>();
        if (conn == null) return out;
        String sql = "SELECT winner AS player, COUNT(*) AS wins FROM games WHERE winner IS NOT NULL GROUP BY winner ORDER BY wins DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("player"));
                row.put("wins", rs.getInt("wins"));
                out.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}
