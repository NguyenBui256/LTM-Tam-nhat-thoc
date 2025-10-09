package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DAO {
	protected Connection conn; 
	public DAO() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=TestDB", "root", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
