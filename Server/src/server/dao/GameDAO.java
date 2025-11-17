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
        if (conn != null) {
            try {
                ensureTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("[GameDAO] Warning: inherited DB connection is null. Ensure DAO can connect to DB.");
        }
    }

    private void ensureTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS games ("
                + "id VARCHAR(128) PRIMARY KEY,"
                + "userId_1 VARCHAR(128),"
                + "userId_2 VARCHAR(128),"
                + "userResult_1 INT,"
                + "userResult_2 INT,"
                + "winnerId  VARCHAR(128),"
                + "scoreDiff INT,"
                + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB;";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    public boolean insertGame(String id, String userId_1, String userId_2, int userResult_1, int userResult_2) {
        if (conn == null) return false;
        String winnerId = null;
        if (userResult_1 > userResult_2) winnerId = userId_1;
        else if (userResult_2 > userResult_1) winnerId = userId_2;
        int diff = Math.abs(userResult_1 - userResult_2);

        String sql = "INSERT INTO games(id,userId_1,userId_2,userResult_1,userResult_2,winnerId,scoreDiff) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId_1);
            ps.setString(3, userId_2);
            ps.setInt(4, userResult_1);
            ps.setInt(5, userResult_2);
            ps.setString(6, winnerId);
            ps.setInt(7, diff);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGame(String id, int userResult_1, int userResult_2) {
        if (conn == null) return false;

        // Lấy userId_1 và userId_2 từ database để xác định winnerId
        String userId_1 = null, userId_2 = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT userId_1, userId_2 FROM games WHERE id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userId_1 = rs.getString("userId_1");
                userId_2 = rs.getString("userId_2");
            } else {
                return false; // Game không tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String winnerId = null;
        if (userResult_1 > userResult_2) winnerId = userId_1;
        else if (userResult_2 > userResult_1) winnerId = userId_2;
        int diff = Math.abs(userResult_1 - userResult_2);

        String sql = "UPDATE games SET userResult_1=?, userResult_2=?, winnerId=?, scoreDiff=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userResult_1);
            ps.setInt(2, userResult_2);
            ps.setString(3, winnerId);
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
                + " SELECT userId_1 AS player, userResult_1 AS points FROM games"
                + " UNION ALL"
                + " SELECT userId_2 AS player, userResult_2 AS points FROM games"
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
        String sql = "SELECT winnerId AS player, COUNT(*) AS wins FROM games WHERE winnerId IS NOT NULL GROUP BY winnerId ORDER BY wins DESC";
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

    public Map<String, Integer> getWinsForAllUsers() {
        // Count wins by joining game.winnerId to user.id
        String sql = "SELECT u.username, COALESCE(COUNT(g.id),0) AS wins "
                + "FROM user u LEFT JOIN games g ON g.winnerId = u.username "
                + "GROUP BY u.id";
        Map<String, Integer> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                int wins = rs.getInt("wins");
                map.put(username, wins);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
