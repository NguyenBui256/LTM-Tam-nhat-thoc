package server.dao;

import server.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends DAO {
	public boolean insertUser(User user) {
		String sql = "INSERT INTO User(username,password,email,name) VALUES(?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getName());
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

	public Integer getUserIdByUsername(String username) {
		String sql = "SELECT id FROM user WHERE username = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean updateEloAndWins(int userId, int eloDelta, boolean win) {
		try {
			// For simplicity assume there's columns `elo` and `wins` in user table; if not
			// present, this will fail and needs migration
			String sql = "UPDATE user SET elo = IFNULL(elo,1000) + ?, wins = IFNULL(wins,0) + ? WHERE id = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, eloDelta);
				ps.setInt(2, win ? 1 : 0);
				ps.setInt(3, userId);
				return ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
