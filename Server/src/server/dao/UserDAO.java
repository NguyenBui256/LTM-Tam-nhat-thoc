package server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import server.model.User;
import dto.PlayerRank;
import server.model.Game;
import dto.PlayerStatus;

public class UserDAO extends DAO {
    public boolean insertUser(User user) {
        String sql = "INSERT INTO User(username,password,email,name,elo) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getName());
            ps.setInt(5, 1000);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean checkExistUser(String username) {
        String sql = "SELECT 1 FROM User WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean checkLogin(String username, String password) {
        String sql = "SELECT 1 FROM User WHERE username =? and password =?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Game> getHistoryByUsername(String username) {
        String sql = """
			    SELECT g.id, 
			           g.userResult_1, 
			           g.userResult_2, 
			           g.scoreDiff, 
			           g.time, 
			           
			           u1.username AS userId_1, 
			           u2.username AS userId_2  
			           
			    FROM games g
			    JOIN user u1 ON u1.username = g.userId_1
                JOIN user u2 ON u2.username = g.userId_2
			    WHERE u1.username = ? OR u2.username = ?
			    ORDER BY g.time DESC
			""";
        List<Game> games = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Game game = new Game();
                    game.setId(rs.getString("id"));
                    game.setUserId_1(rs.getString("userId_1"));
                    game.setUserId_2(rs.getString("userId_2"));
                    game.setUserResult_1(rs.getString("userResult_1"));
                    game.setUserResult_2(rs.getString("UserResult_2"));
                    game.setScoreDiff(rs.getInt("scoreDiff"));
                    game.setTime(rs.getObject("time", LocalDateTime.class));
                    games.add(game);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return games;
    }

    public List<PlayerStatus> getRanking() {
        String sql = "SELECT username, elo FROM user ORDER BY elo DESC";
        List<PlayerStatus> ranking = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                int elo = rs.getInt("elo");
                // default status will be resolved by server-side manager when needed
                PlayerStatus p = new PlayerStatus(username, "OFFLINE", elo);
                ranking.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ranking;
    }
    public List<PlayerRank> getAllUser() {
        List<PlayerRank> users = new ArrayList<>();
        String sql = "SELECT \r\n"
                + "    u.username AS name,\r\n"
                + "    u.elo,\r\n"
                + "    COALESCE(SUM(CASE WHEN g.winnerId = u.id THEN 1 ELSE 0 END), 0) AS wins\r\n"
                + "FROM \r\n"
                + "    User u\r\n"
                + "LEFT JOIN \r\n"
                + "    game g ON u.id IN (g.userId_1, g.userId_2)\r\n"
                + "WHERE \r\n"
                + "    u.username != 'admin'\r\n"
                + "GROUP BY \r\n"
                + "    u.username, u.elo\r\n"
                + "ORDER BY \r\n"
                + "    u.elo DESC;";
        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                PlayerRank u = new PlayerRank(rs.getString("name"),"OFFLINE",rs.getInt("elo"),rs.getInt("wins"));
                users.add(u);
                System.out.println("UserDAO" + u.getName());
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

}