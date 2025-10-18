package server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import server.model.User;

public class UserDAO extends DAO {
	public boolean insertUser(User user) {
		UUID uuid = UUID.randomUUID();

		String sql = "INSERT INTO User(id,username,password,email,name) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setString(1,uuid.toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
	}
	public boolean checkLogin(String username, String password) {
		String sql = "Select * FROM User WHERE username = ?  AND PASSWORD = ?";
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
}
