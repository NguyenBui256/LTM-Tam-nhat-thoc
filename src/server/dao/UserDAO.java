package server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import server.model.User;
import server.model.Game;
public class UserDAO extends DAO {
	public boolean insertUser(User user) {
		String sql = "INSERT INTO Users(username,password,email,name,elo) VALUES(?,?,?,?,?)";
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
	    String sql = "SELECT 1 FROM Users WHERE username = ?";
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
		String sql = "SELECT 1 FROM Users WHERE username =? and password =?";
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
	        SELECT g.*
	        FROM Game g
	        JOIN Users u1 ON u1.id = g.user1Id
	        JOIN Users u2 ON u2.id = g.user2Id
	        WHERE u1.username = ? OR u2.username = ?
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
	                game.setUserId_2(rs.getString("user2Id"));
	                game.setUserResult_1(rs.getString("userResult_1"));
	                game.setUserResult_2(rs.getString("UserResult_2"));
	                game.setTime(rs.getObject("time", LocalDateTime.class));
	                games.add(game);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return games;
	}

}
