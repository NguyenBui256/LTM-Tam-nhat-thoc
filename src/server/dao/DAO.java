package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DAO {
    protected Connection conn;

    public DAO() {
        try {
        	String url = "jdbc:mysql://localhost:3306/ltm?useSSL=false&serverTimezone=UTC&PublicKeyRetrieval=true";
        	String user = "root";
        	String pass = "12345678";
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void closeConnection() {
        try {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
