package com.ibm.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
	public static final String URLTOCONNECT = "jdbc:mysql://localhost:3306/bank_java";

	public static final String USERNAME = "root";

	public static final String PASSWORD = "";

	public static final String DRIVERNAME = "com.mysql.cj.jdbc.Driver";
	
	static Statement theStatement;
	
	static ResultSet theResultSet;
	
    public static void main( String[] args) throws SQLException{
    	int choice =0;
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("bankconfig.xml");
    	do {
    	    		try {
						Class.forName(DRIVERNAME);
						Connection dbCon = DriverManager.getConnection(URLTOCONNECT,USERNAME,PASSWORD);
						JdbcDao dao = context.getBean("dao", JdbcDao.class);
			    		dao.connectToDb();
			    		Scanner in = new Scanner(System.in);
			    		System.out.println("1. Show all members: ");
			    		System.out.println("2. create account: ");
			    		System.out.println("3. transfer money: ");
			    		System.out.println("4. Show acc balance by acc id: ");
			    		System.out.println("5. Deposit: ");
			    		System.out.println("6. Withdraw: ");
			    		System.out.println("7. Transaction View ");
			    		System.out.println("8. Exit: ");
			    		choice = in.nextInt();
			    		switch(choice) {
			    			case 1:  
			    				getAllMembers(dbCon);
			    				break;
			    			case 2:
			    				in.nextLine();
			    				System.out.println("Enter name to create Acc: ");
			    				String name = in.nextLine();
			    				System.out.println("Enter Account number to create Acc: ");
			    				int accountnumber = in.nextInt();
			    				in.nextLine();
			    				System.out.println("Enter deposit amount to your Acc: ");
			    				int deposit = in.nextInt();
			    				in.nextLine();
			    				int accbalance = 0;
			    				accbalance+=deposit;
			    				createAccount(dbCon,name,accountnumber,deposit,accbalance);
			    				break;
			    			case 3:
			    				TransferFunds(dbCon);
			    				break;
			    			case 4:
			    				ShowAccBalById(dbCon);
			    				break;
			    			case 5:
			    				DepositAmount(dbCon);
			    				break;
			    			case 6:
			    				Withdraw(dbCon);
			    				break;
			    			case 7:
			    				int accNO = in.nextInt();
			    				TransactionView(dbCon,accNO);
			    				break;
			    		}
					} catch (ClassNotFoundException e) {
						System.out.println("Issues while connecting: "+e.getMessage());
					}
		    		
		    	}while(choice !=8);
    	
    	
    }	
	private static void getAllMembers(Connection dbCon) throws SQLException {
    	String qry = "SELECT * FROM bank_members";
    	theStatement = dbCon.createStatement();
    	theResultSet = theStatement.executeQuery(qry);
    	
    	System.out.println("Members already registered :");
    	while (theResultSet.next()) {
			System.out.print("Name: " + theResultSet.getString("name"));
			System.out.print(", AccNo: " + theResultSet.getInt("AccNo"));
			System.out.print(" Deposited Amount : " + theResultSet.getInt("Deposit"));
			System.out.println(" Acc Balance : " + theResultSet.getInt("AccBalance"));
		}
	}
	private static void createAccount(Connection dbCon, String name, int accountnumber, int deposit, int accbalance) throws SQLException {
    	String qry = "insert into bank_members values('" + name + "','" + accountnumber + "', '" + deposit + "', '" +accbalance + "')";
    	theStatement = dbCon.createStatement();
		if (theStatement.executeUpdate(qry) > 0)
		System.out.println("New account registered...");
		}

	
	private static void TransferFunds(Connection dbCon) throws SQLException {
		Scanner in =new Scanner(System.in);
		System.out.println("Enter the sender's account id: ");
		int senderid = in.nextInt();
		in.nextLine();
		System.out.println("Enter ther receiver account id: ");
		int receiverid = in.nextInt();
		in.nextLine();
		System.out.println("Enter amount to transfer ");
		int amount = in.nextInt();
		String qry = "select AccBalance from bank_members where AccNo =?";
		PreparedStatement preparedStatement = dbCon.prepareStatement(qry);
		preparedStatement.setInt(1, senderid);
		theResultSet = preparedStatement.executeQuery();
		int newBalance =0;
		while(theResultSet.next()) {
			newBalance = theResultSet.getInt("AccBalance");
		}
		if(newBalance-amount > 1000) {
			newBalance=newBalance-amount;
            System.out.println(newBalance);
            String qry2 ="update Bank_members set AccBalance =? where AccNo = ?";
            dbCon.prepareStatement(qry2);
            PreparedStatement preparedStatement2 =dbCon.prepareStatement(qry2);
            preparedStatement2.setInt(1,newBalance);
            preparedStatement2.setInt(2, senderid);
            preparedStatement2.executeUpdate();
                                 
            String qry3 = "select AccBalance from Bank_members where AccNo = ?";
            PreparedStatement preparedStatement3 =dbCon.prepareStatement(qry3);
            preparedStatement3 = dbCon.prepareStatement(qry3);
            preparedStatement3.setInt(1,receiverid);
            ResultSet theResultSet2= preparedStatement3.executeQuery();
            int newbalance2 = 0;
            while (theResultSet2.next()) {
    			newbalance2= theResultSet2.getInt("AccBalance");
            }               
            newbalance2 =newbalance2 + amount;
            String qry4 = "update Bank_members set AccBalance = ? where AccNo = ?";
            dbCon.prepareStatement(qry4);
            PreparedStatement preparedStatement4 =dbCon.prepareStatement(qry4);
            preparedStatement4.setInt(1,newbalance2);
            preparedStatement4.setInt(2,receiverid);
            preparedStatement4.executeUpdate();
            if (preparedStatement4.executeUpdate() > 0) {
     			System.out.println("Transfered succesfully");
     			String operation = "Transfer";
     			Transaction(dbCon,senderid,operation,receiverid,amount);
     			
     		} 
		}
	}
	private static void ShowAccBalById(Connection dbCon) throws SQLException {
		Scanner in = new Scanner(System.in);
		String qry = "select AccBalance from bank_members where AccNo = ?";
		System.out.println("Enter account Number to get your balance: ");
		int accno = in.nextInt();
		PreparedStatement preparedStatement = dbCon.prepareStatement(qry);
		preparedStatement.setInt(1,accno);
		theResultSet = preparedStatement.executeQuery();
		while(theResultSet.next()) {
			System.out.println(theResultSet.getInt("AccBalance"));
		}
	}
	private static void DepositAmount(Connection dbCon) throws SQLException {
		Scanner in = new Scanner(System.in);
		int balance=0;
		System.out.println("Enter ur acc number to deposit: ");
		int accnumber = in.nextInt();
		in.nextLine();
		System.out.println("Enter the amount to deposit: ");
		int amount = in.nextInt();
		String qry = "select AccBalance from bank_members where AccNo =?";
		PreparedStatement preparedStatement = dbCon.prepareStatement(qry);
		preparedStatement.setInt(1, accnumber);
		theResultSet = preparedStatement.executeQuery();
		while(theResultSet.next()) {
			 balance = theResultSet.getInt("AccBalance");
		}
		int bal = balance +amount;
		String qry2 = "update bank_members set AccBalance=? where AccNo = ?";
		PreparedStatement preparedStatement2 = dbCon.prepareStatement(qry2);
		preparedStatement2.setInt(1, bal);
		preparedStatement2.setInt(2, accnumber);
		if (preparedStatement2.executeUpdate() > 0) {
			System.out.println("Amount  Deposited Successfully");
		}
	}
	private static void Withdraw(Connection dbCon) throws SQLException {
		Scanner in = new Scanner(System.in);
		int balance=0;
		System.out.println("Enter ur acc number to Withdraw: ");
		int accnumber = in.nextInt();
		in.nextLine();
		System.out.println("Enter the amount to withdraw: ");
		int amount = in.nextInt();
		String qry = "select AccBalance from bank_members where AccNo =?";
		PreparedStatement preparedStatement = dbCon.prepareStatement(qry);
		preparedStatement.setInt(1, accnumber);
		theResultSet = preparedStatement.executeQuery();
		while(theResultSet.next()) {
			 balance = theResultSet.getInt("AccBalance");
		}
		int bal = balance - amount;
		String qry2 = "update bank_members set AccBalance=? where AccNo = ?";
		PreparedStatement preparedStatement2 = dbCon.prepareStatement(qry2);
		preparedStatement2.setInt(1, bal);
		preparedStatement2.setInt(2, accnumber);
		if (preparedStatement2.executeUpdate() > 0) {
			System.out.println("Amount  Withdrawed Successfully");
		}
	}
	private static void Transaction(Connection dbCon,int senderid,String operation,int receiverid,int amount) throws SQLException {
		String qry = "insert into transaction values(?,?,?,?)";
		PreparedStatement preparedStatement = dbCon.prepareStatement(qry);
		preparedStatement.setInt(1, senderid);
		preparedStatement.setInt(2, amount);
		preparedStatement.setString(3, operation);
		preparedStatement.setInt(4, receiverid);
		preparedStatement.executeUpdate();
		
	}
	private static void TransactionView(Connection dbCon,int Accno) throws SQLException {
		String qry = "select * from transaction where SenderAccount = ?";
		PreparedStatement preparedStatement = dbCon.prepareStatement(qry);
		preparedStatement.setInt(1, Accno);
		theResultSet = preparedStatement.executeQuery();
		while(theResultSet.next()) {
			System.out.println("AccNo "+theResultSet.getInt("SenderAccount"));
			System.out.println("AMount "+theResultSet.getInt("Amount"));
			System.out.println("Operation "+theResultSet.getString("Operation"));
			System.out.println("receiverAccount "+theResultSet.getInt("ReceiverAccount"));
			
	}
		String qry2 = "select * from transaction where ReceiverAccount = ?";
		PreparedStatement preparedStatement2 = dbCon.prepareStatement(qry2);
		preparedStatement2.setInt(1, Accno);
		theResultSet = preparedStatement.executeQuery();
		while(theResultSet.next()) {
			System.out.println("AccNo "+theResultSet.getInt("SenderAccount"));
			System.out.println("AMount "+theResultSet.getInt("Amount"));
			System.out.println("Operation Received " );
			System.out.println("received Account from "+theResultSet.getInt("ReceiverAccount"));
	}
}
}
   