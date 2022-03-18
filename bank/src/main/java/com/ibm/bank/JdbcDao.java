package com.ibm.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Repository;

@Repository("dao")
public class JdbcDao {
	
	Connection dbCon;

	void connectToDb() {

		try {
//			Load the driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			
//			Establish the connection
			dbCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank_java", "root", "");
			
			System.out.println("Connected to Db...");
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Issues while connecting to db : " + e.getMessage());
		} 
	}
}
