package net.tc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;

import com.google.protobuf.TextFormat.ParseException;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


import net.tc.employees.Employees;
import net.tc.employees.Titles;
import net.tc.world.*;

public class ExampleWithOut {
	SessionFactory sessionFactory = null;
	SessionFactory sessionFactoryEmp = null;
	SessionFactory sessionFactorySakila = null;
	org.apache.log4j.Logger logger =null;
	
	String user ;
	String password;
	Connection conn;
	String serverUrl;
	int serverPort;
	String database;
	SimpleDateFormat simpleDateFormat;
	TreeMap<String,Long> performance =new TreeMap<String,Long>();
	boolean lazy = true;

public ExampleWithOut() {

	
	
}

public static void main(String[] args) {
	
	long startTime = 0;
	long endTime = 0;
	long perf = 0;
	
	ExampleWithOut ws = new ExampleWithOut();
	ws.init();


	/*
	 * Test Standard CRUD with basic connection
	 * [START]
	 */
	
	/** NOT BATCHED **/
//	ws.setEmployees(ws.getSimpleMySQLConnection());
//	ws.getEmployees(ws.getSimpleMySQLConnection());
//	ws.updateEmployees(ws.getSimpleMySQLConnection());
//	ws.deleteEmployeesOneByOne(ws.getSimpleMySQLConnection());
	
	/** BATCHED **/
//	ws.setEmployeesBatch(ws.getSimpleMySQLConnection());
//	ws.getEmployeesBatch(ws.getSimpleMySQLConnection());
//	ws.updateEmployeesBatch(ws.getSimpleMySQLConnection());
//	ws.deleteEmployeesAll(ws.getSimpleMySQLConnection());
	/*
	 * Test Standard CRUD with basic connection
	 * [END]
	 */
	
	/*
	 * Test Standard CRUD with hikari connection
	 * [START]
	 */

	/** NOT BATCHED **/
	ws.setEmployees(ws.getHikariConnection());
	ws.getEmployees(ws.getHikariConnection());
	ws.updateEmployees(ws.getHikariConnection());
	ws.deleteEmployeesOneByOne(ws.getHikariConnection());
	
	/** BATCHED **/
	ws.setEmployeesBatch(ws.getHikariConnection());
	ws.getEmployeesBatch(ws.getHikariConnection());
	ws.updateEmployeesBatch(ws.getHikariConnection());
	ws.deleteEmployeesAll(ws.getHikariConnection());
	
	/*
	 * Test Standard CRUD with hikari connection
	 * [END]
	 */
	
	/*
	 * Print stats
	 */
	ws.printStats();
	
	
	
}	


private void printStats() {
	for(String key: performance.keySet()) {
		this.logger.info("\tns\t" + performance.get(key) + "\t" + key);
		
	}

	
}

private void init() {
	PropertyConfigurator.configure("log4j.properties");
	logger = LogManager.getLogger("PL2019");
//	logger.info("Logger Test");
	
	
	this.setPassword("password");
	this.setUser("hibernatee");
	this.setDatabase("employees");
	this.setServerUrl("127.0.0.1");
	this.setServerPort(3316);
	
	String pattern = "yyyy-MM-dd";
	simpleDateFormat = new SimpleDateFormat(pattern);
}

private void setEmployees(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();

	try {
		Statement stmt = conn.createStatement();
			
		int i=0;
		StringBuffer sb =new StringBuffer();
		String sqlHead="INSERT INTO employees (emp_no,birth_date,first_name,last_name,gender,hire_date) VALUES (";
		
		stmt.execute("START TRANSACTION");
		while(++i < 500) {
			sb.append(i);
			sb.append(",'"+ this.getSimpleDateFormat().format(new Date()) +"'");
			sb.append(",'Franco"+ i +"'");
			sb.append(",'Castagna"+ i +"'");
			sb.append(",'M'");
			sb.append(",'"+ this.getSimpleDateFormat().format(new Date()) +"'");
			sb.append(")");
			stmt.execute(sqlHead+sb.toString());
//			logger.info("SQL: " +sqlHead+sb.toString());
			sb.delete(0,sb.length());
		}
		
		conn.commit();
	}catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT1\t insert employees - no batch\t" , (System.nanoTime()-startTime));
}

private void getEmployees(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();
	try {
		Statement stmt = conn.createStatement();
		
		int i=0;
		StringBuffer sb =new StringBuffer();
		String sqlHead="SELECT emp_no,birth_date,first_name,last_name,gender,hire_date FROM employees where emp_no=";
		int rowReturned=0;
//		stmt.execute("START TRANSACTION");
		while(++i < 500) {
			sb.append(i);
			ResultSet rs = stmt.executeQuery(sqlHead+sb.toString());
			HashSet employees = new HashSet(); 
			while(rs.next()) {
				Employees employee = new Employees();
				employee.setBirthDate(rs.getDate("birth_date"));
				employee.setHireDate(rs.getDate("hire_date"));
				employee.setGender( rs.getString("gender").charAt(0));
				employee.setEmpNo(rs.getInt("emp_no"));
				employee.setFirstName(rs.getString("first_name"));
				employee.setLastName(rs.getString("last_name"));
//				logger.info("Employee details :" +employee.toString());
				employees.add(employee);
			}
			rs.last();
			rowReturned =rowReturned + rs.getRow();
//			logger.info("SQL: " +sqlHead+sb.toString());
			sb.delete(0,sb.length());
		
		}
		
		this.logger.info("Retrieved Employees " + rowReturned);
		conn.commit();
		
	}
	catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put( (connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT2\t read employees - no batch\t", (System.nanoTime()-startTime));
}
private void updateEmployees(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();
	try {
		Statement stmt = conn.createStatement();
		PreparedStatement stmtP;
		
		int i=0;
		StringBuffer sb =new StringBuffer();
		String sqlHead="SELECT emp_no,birth_date,first_name,last_name,gender,hire_date FROM employees where emp_no=";
		String forUpdate = "UPDATE employees SET hire_date = ? where emp_no = ?";
		stmtP = conn.prepareStatement(forUpdate);
		
		int rowReturned=0;
		stmt.execute("START TRANSACTION");
		while(++i < 500) {
			sb.append(i);
			ResultSet rs = stmt.executeQuery(sqlHead+sb.toString());
			HashSet employees = new HashSet(); 
			while(rs.next()) {
				Employees employee = new Employees();
				employee.setBirthDate(rs.getDate("birth_date"));
				employee.setHireDate(rs.getDate("hire_date"));
				employee.setGender( rs.getString("gender").charAt(0));
				employee.setEmpNo(rs.getInt("emp_no"));
				employee.setFirstName(rs.getString("first_name"));
				employee.setLastName(rs.getString("last_name"));
				try{employee.setHireDate(this.getSimpleDateFormat().parse("2015-"+ getRandomNumberInRange(1,12) +"-10"));}catch(Exception pex) {pex.printStackTrace();}
				stmtP.setDate(1, java.sql.Date.valueOf(this.getSimpleDateFormat().format(employee.getHireDate())));
				stmtP.setInt(2, employee.getEmpNo());
				stmtP.executeUpdate();
				employees.add(employee);
//				logger.info("Employee Update details :" +employee.toString());
			}
			rs.last();
			rowReturned =rowReturned + rs.getRow();
//			logger.info("SQL: " +sqlHead+sb.toString());
			sb.delete(0,sb.length());
		
		}
		
		this.logger.info("Retrieved and updated Employees " + rowReturned);
		conn.commit();
		
	}
	catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT3\t Update employees - no batch\t", (System.nanoTime()-startTime));
}

private void deleteEmployeesOneByOne(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();
	try {
		Statement stmt = conn.createStatement();
			
		int i=0;
		String sqlHead="Delete from employees where emp_no=";
		
		stmt.execute("START TRANSACTION");
		while(++i < 500) {
			stmt.execute(sqlHead+i);
//			logger.info("SQL: " +sqlHead+i);
		}
		
		conn.commit();
	}catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT4\t Delete employees - no batch\t", (System.nanoTime()-startTime));
	
}
private void setEmployeesBatch(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();

	try {
		Statement stmt = conn.createStatement();
			
		int i=0;
		StringBuffer sb =new StringBuffer();
		String sqlHead="INSERT INTO employees (emp_no,birth_date,first_name,last_name,gender,hire_date) VALUES (";
		
		stmt.execute("START TRANSACTION");
		while(++i < 500) {
			sb.append(i);
			sb.append(",'"+ this.getSimpleDateFormat().format(new Date()) +"'");
			sb.append(",'Franco"+ i +"'");
			sb.append(",'Castagna"+ i +"'");
			sb.append(",'M'");
			sb.append(",'"+ this.getSimpleDateFormat().format(new Date()) +"'");
			sb.append(")");
			stmt.addBatch(sqlHead+sb.toString());
//			logger.info("SQL: " +sqlHead+sb.toString());
			sb.delete(0,sb.length());
		
		}
		stmt.executeBatch();
		conn.commit();
	}catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT5\t Insert employees - batch\t", (System.nanoTime()-startTime));
}

private void getEmployeesBatch(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();

	try {
		Statement stmt = conn.createStatement();
		
		int i=0;
		StringBuffer sb =new StringBuffer();
		String sqlHead="SELECT emp_no,birth_date,first_name,last_name,gender,hire_date FROM employees where emp_no<999";
		int rowReturned=0;
//		stmt.execute("START TRANSACTION");
		ResultSet rs = stmt.executeQuery(sqlHead);
		HashSet employees = new HashSet();
		while(rs.next()) {
			Employees employee = new Employees();
			employee.setBirthDate(rs.getDate("birth_date"));
			employee.setHireDate(rs.getDate("hire_date"));
			employee.setGender( rs.getString("gender").charAt(0));
			employee.setEmpNo(rs.getInt("emp_no"));
			employee.setFirstName(rs.getString("first_name"));
			employee.setLastName(rs.getString("last_name"));
//			logger.info("Employee details :" +employee.toString());
			employees.add(employee);
		}
		rs.last();
		rowReturned =rs.getRow();
//			logger.info("SQL: " +sqlHead+sb.toString());

		
		this.logger.info("Retrieved and updated Employees " + rowReturned);
		conn.commit();
		
	}
	catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT6\t read employees - batch\t", (System.nanoTime()-startTime));
}
private void updateEmployeesBatch(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();
	try {
		Statement stmt = conn.createStatement();
		PreparedStatement stmtP;
		
		int i=0;
		StringBuffer sb =new StringBuffer();
		String sqlHead="SELECT emp_no,birth_date,first_name,last_name,gender,hire_date FROM employees where emp_no < 999";
		String forUpdate = "UPDATE employees SET hire_date = ? where emp_no = ?";
		stmtP = conn.prepareStatement(forUpdate);
		

		int rowReturned=0;
//		stmt.execute("START TRANSACTION");
		ResultSet rs = stmt.executeQuery(sqlHead);
		HashSet employees = new HashSet();
		while(rs.next()) {
			Employees employee = new Employees();
			employee.setBirthDate(rs.getDate("birth_date"));
			employee.setHireDate(rs.getDate("hire_date"));
			employee.setGender( rs.getString("gender").charAt(0));
			employee.setEmpNo(rs.getInt("emp_no"));
			employee.setFirstName(rs.getString("first_name"));
			employee.setLastName(rs.getString("last_name"));
			
			try{employee.setHireDate(this.getSimpleDateFormat().parse("2015-"+ getRandomNumberInRange(1,12) +"-10"));}catch(Exception pex) {pex.printStackTrace();}
			stmtP.setDate(1, java.sql.Date.valueOf(this.getSimpleDateFormat().format(employee.getHireDate())));
			stmtP.setInt(2, employee.getEmpNo());
			stmtP.addBatch();
//			logger.info("Employee details :" +employee.toString());
			employees.add(employee);
		}
		int[] ip = stmtP.executeBatch();
		rs.last();
		rowReturned =rs.getRow();
//			logger.info("SQL: " +sqlHead+sb.toString());
		this.logger.info("Retrieved Employees " + rowReturned);
		conn.commit();
		
	}
	catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT7\t update employees - batch\t", (System.nanoTime()-startTime));
}







private void deleteEmployeesAll(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();

	try {
		Statement stmt = conn.createStatement();
			
		int i=0;
		StringBuffer sb =new StringBuffer();
		stmt.execute("START TRANSACTION");
		stmt.execute("Delete from employees where emp_no < 999");
		conn.commit();
	}catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT8\t delete employees - batch\t", (System.nanoTime()-startTime));
	
}





	
public Connection getSimpleMySQLConnection() {
		
		if (this.getConn() !=null
				&& this.getConn().getClass().toString().indexOf("Hikari") < 0){
			return this.getConn();

		}
		else{
		
			try{
				 MysqlDataSource datasource =new MysqlDataSource();
				 
				 datasource.setUser(getUser());
				 datasource.setPassword(getPassword());
				 datasource.setUseSSL(false);

				 datasource.setPort(getServerPort());
				 datasource.setServerName(getServerUrl());
				 datasource.setDatabaseName(getDatabase());
				 
				 datasource.setCacheCallableStmts(true);
				 datasource.setCachePrepStmts(true);
				 datasource.setCacheResultSetMetadata(true);
				 datasource.setCacheServerConfiguration(true);
				 datasource.setUseServerPrepStmts(true);
				 datasource.setUseLocalSessionState(true);
				 datasource.setRewriteBatchedStatements(true);
				 datasource.setElideSetAutoCommits(true);
				 datasource.setMaintainTimeStats(false);
				 datasource.setPrepStmtCacheSize(250);
				 datasource.setPrepStmtCacheSqlLimit(2048);
				 
				 conn = datasource.getConnection();
				 conn.setAutoCommit(false);
				 
//				 
//				 Properties connectionProps = new Properties();
//				 connectionProps.put("user", this.getUser());
//				 connectionProps.put("password", this.getPassword());
////				 connectionProps.put("useSSL", false);
//
//				 
//				connectionProps.put("cachePrepStmts",true);
//				connectionProps.put("prepStmtCacheSize",250);
//				connectionProps.put("prepStmtCacheSqlLimit",2048);
//				connectionProps.put("useServerPrepStmts",true);
//				connectionProps.put("useLocalSessionState",true);
//				connectionProps.put("rewriteBatchedStatements",true);
//				connectionProps.put("cacheResultSetMetadata",true);
//				connectionProps.put("cacheServerConfiguration",true);
//				connectionProps.put("elideSetAutoCommits",true);
//				connectionProps.put("maintainTimeStats",false);
//				 
//				 
//		         conn = DriverManager.getConnection(
//				                   "jdbc:mysql://" +
//				                   this.getServerUrl() +
//				                   ":" + this.getServerPort() + "/"+this.getDatabase()+"?useSSL=false",
//				                   connectionProps);

			    
			    
			}catch(Exception ex){ex.printStackTrace();}
			finally{
					try {conn.setAutoCommit(false);} catch (SQLException e) {e.printStackTrace();}
					this.setConn(conn);
				}
			}
		 return this.getConn();
}

	   
		
		   

public Connection getHikariConnection(){
		if (this.getConn() !=null
				&& this.getConn().getClass().toString().indexOf("Hikari") > 0 ){
			return this.getConn();

		}
		else{
			DataSource datasource;

		    HikariConfig config = new HikariConfig();
		    Properties connectionProps = new Properties();
			connectionProps.put("user", this.getUser());
			connectionProps.put("password", this.getPassword());
//			connectionProps.put("useSSL", false);

			String connectionString = "jdbc:mysql://" +
			                   this.getServerUrl() +
			                   ":" + this.getServerPort() + "/"+this.getDatabase();
		    
			
		    
	         
		     config.setJdbcUrl(connectionString);
		     config.setUsername(this.getUser());
		     config.setPassword(this.getPassword());
		     config.setMaximumPoolSize(20);
		     config.setAutoCommit(false);
		     config.setLeakDetectionThreshold(500);
		     config.addDataSourceProperty("cachePrepStmts", true);
		     config.addDataSourceProperty("useSSL", false);
		     config.addDataSourceProperty("cachePrepStmts",true);
			 config.addDataSourceProperty("prepStmtCacheSize",250);
			 config.addDataSourceProperty("prepStmtCacheSqlLimit",2048);
			 config.addDataSourceProperty("useServerPrepStmts",true);
			 config.addDataSourceProperty("useLocalSessionState",true);
			 config.addDataSourceProperty("rewriteBatchedStatements",true);
			 config.addDataSourceProperty("cacheResultSetMetadata",true);
			 config.addDataSourceProperty("cacheServerConfiguration",true);
			 config.addDataSourceProperty("elideSetAutoCommits",true);
			 config.addDataSourceProperty("maintainTimeStats",false);
		     
		     
		     config.setIdleTimeout(1000);
		     
		     datasource = new HikariDataSource(config);
		     try {
		    	conn = datasource.getConnection();
		     	conn.setAutoCommit(false);
		     } catch (SQLException e1) {e1.printStackTrace();}
		     
		     this.setConn(conn);
		     return this.getConn();
		}


}

private static int getRandomNumberInRange(int min, int max) {

	if (min >= max) {
		throw new IllegalArgumentException("max must be greater than min");
	}

	Random r = new Random();
	return r.nextInt((max - min) + 1) + min;
}

public String getUser() {
	return user;
}

public void setUser(String user) {
	this.user = user;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}

public Connection getConn() {
	return conn;
}

public void setConn(Connection conn) {
	this.conn = conn;
}

public String getServerUrl() {
	return serverUrl;
}

public void setServerUrl(String serverUrl) {
	this.serverUrl = serverUrl;
}

public int getServerPort() {
	return serverPort;
}

public void setServerPort(int serverPort) {
	this.serverPort = serverPort;
}

public SimpleDateFormat getSimpleDateFormat() {
	return simpleDateFormat;
}

public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
	this.simpleDateFormat = simpleDateFormat;
}

public String getDatabase() {
	return database;
}

public void setDatabase(String database) {
	this.database = database;
}

private TreeMap<String, Long> getPerformance() {
	return performance;
}

private void setPerformance(TreeMap<String, Long> performance) {
	this.performance = performance;
}

private boolean isLazy() {
	return lazy;
}

private void setLazy(boolean lazy) {
	this.lazy = lazy;
}
}
