package com.db.tradefinder.service.factory;

import com.db.tradefinder.persistence.PMException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionFactory {

	private static final Logger logger = Logger.getLogger(ConnectionFactory.class.getName());
	
	@Resource(mappedName = "tradefinder_owner")
	Connection conn;
	
	public static final String TRADEFINDER = "";

	public static Connection getConnection(String cfID) throws PMException {
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/tradefinder");
			return ds.getConnection();
		} catch (NamingException | SQLException e) {
			logger.severe(e.getMessage());
		}
		return null;
	}
	
	public static Connection getConnectionLocally() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://localhost:3306/tradefinder_owner", "root", "root");
		} catch (ClassNotFoundException | SQLException ex) {
			logger.severe(ex.getMessage());
		}
		return null;
	}
}
