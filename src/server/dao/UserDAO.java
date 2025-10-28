package server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import server.model.User;

public class UserDAO extends DAO {
	public boolean insertUser(User user) {
		String sql = "INSERT INTO Users(username,password,email,name,elo) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getName());
            ps.setInt(5, user.getElo());
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
	public String getUserId(String username) {
	    String sql = "SELECT id FROM Users WHERE username = ?";
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, username);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return rs.getString("id");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	public int getUserElo(String userId) {
	    String sql = "SELECT elo FROM Users WHERE id = ?";
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, userId);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("elo");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 1000; // default
	}
	public boolean updateUserElo(String userId, int newElo) {
	    String sql = "UPDATE Users SET elo = ? WHERE id = ?";
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, newElo);
	        ps.setString(2, userId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
}
