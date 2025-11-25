package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DAO {
    protected Connection conn;

    public DAO() {
        try {

        	String url = "jdbc:mysql://127.0.0.1:3307/ltm";
        	String user = "root";
        	String pass = "nam2110do";
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
