package net.tc;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.stat.Statistics;
import org.jboss.logging.Logger;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.tc.employees.Departments;
import net.tc.employees.DeptEmp;
import net.tc.employees.DeptEmpId;
import net.tc.employees.Employees;
import net.tc.employees.EmployeesSummary;
import net.tc.employees.Salaries;
import net.tc.employees.SalariesId;
import net.tc.employees.Titles;
import net.tc.employees.TitlesId;
import net.tc.world.*;

public class ExampleWithHibernateVsSQL {
	SessionFactory sessionFactory = null;
	SessionFactory sessionFactoryEmp = null;
	SessionFactory sessionFactoryEmpHikari = null;
	SessionFactory sessionFactorySakila = null;
	org.apache.log4j.Logger logger =null;
	SimpleDateFormat simpleDateFormat;
	TreeMap<String,Long> performance =new TreeMap<String,Long>();

	String user ;
	String password;
	Connection conn;
	String serverUrl;
	int serverPort;
	String database;

	
	
public ExampleWithHibernateVsSQL() {
}

public static void main(String[] args) {
	ExampleWithHibernateVsSQL eVS =new ExampleWithHibernateVsSQL();
	eVS.init();
	
//	eVS.getEmployeeACID(eVS.getSessionFactoryEmp(),eVS.getSimpleMySQLConnection());

	eVS.getEmployees(eVS.getSessionFactoryEmp());
	eVS.getEmployeesNotLazy(eVS.getSessionFactoryEmp());
	eVS.getEmployees(eVS.getSessionFactoryEmpHikari());
	eVS.getEmployeesNotLazy(eVS.getSessionFactoryEmpHikari());
	
	
	eVS.getEmployees(eVS.getSimpleMySQLConnection());
	eVS.getEmployeesNotLazy(eVS.getSimpleMySQLConnection());
	eVS.getEmployees(eVS.getHikariConnection());
	eVS.getEmployeesNotLazy(eVS.getHikariConnection());
	eVS.printStats();
		
}	
	
private void init() {
	PropertyConfigurator.configure("log4j.properties");
	logger = LogManager.getLogger("PL2019");
//	logger.info("Logger Test");
//	this.setSessionFactory(getSFactory("hibernate.cfg.xml"));
	this.setSessionFactoryEmp(getSFactory("hibernate_employees.cfg.xml"));
	this.setSessionFactoryEmpHikari(getSFactory("hibernate_employees_hikari.cfg.xml"));
	
	String pattern = "yyyy-MM-dd";
	simpleDateFormat = new SimpleDateFormat(pattern);
	
	
	this.setPassword("password");
	this.setUser("hibernatee");
	this.setDatabase("employees");
	this.setServerUrl("127.0.0.1");
	this.setServerPort(3306);

	
}

private void getEmployees(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees where emp_no >10000 and emp_no < 20020 " ).list();
	Iterator it = employees.iterator();
		
	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
		logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
	}
	logger.info("------------");

	se.disconnect();
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT1\t Read employees Hibernate - Lazy\t" , (System.nanoTime()-startTime));
}

private void getEmployeesNotLazy(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();
	
	List<EmployeesSummary> employees = se.createQuery("from EmployeesSummary where emp_no >10000 and emp_no < 20020 " ).list();
//	List<EmployeesSummary> employees = se.createQuery("from EmployeesSummary where emp_no >10000 and emp_no < 10003 " ).list();
	/*
	 * Loop employees
	 */
	Iterator it = employees.iterator();
		
	while(it.hasNext()) {
		EmployeesSummary myEmp = (EmployeesSummary) it.next();
		logger.info("EmployeeSummary name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
		Iterator it2 = myEmp.getTitleses().iterator();
		Iterator it3 = myEmp.getSalarieses().iterator();
		Iterator it4 = myEmp.getDeptEmps().iterator();
//			while(it2.hasNext()) {
//				Titles myTitle = (Titles) it2.next();
//				logger.info("\t\t Title "+ myTitle.getId().getTitle() +"  Up to "+ myTitle.getToDate().toString());
//			}
//			while(it3.hasNext()) {
//				Salaries mySalary = (Salaries) it3.next();
//				logger.info("\t\t Salary "+ mySalary.getSalary() +"  Up to "+ mySalary.getToDate().toString());
//			}
//
//			while(it4.hasNext()) {
//				DeptEmp myDeps = (DeptEmp) it4.next();
//				logger.info("\t\t Deps "+ myDeps.getDepartments().getDeptName()+"  Up to "+ myDeps.getToDate().toString());
//			}

	}
		
		logger.info("------------");
	se.disconnect();
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT2\t Read employees Hibernate - NOT Lazy\t" , (System.nanoTime()-startTime));
}

private void getEmployees(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();
	try {
		Statement stmt = conn.createStatement();
		

		String sqlHead="SELECT emp_no,birth_date,first_name,last_name,gender,hire_date FROM employees where emp_no >10000 and emp_no < 20020";
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
//				logger.info("Employee details :" +employee.toString());

			employees.add(employee);
			this.logger.info("Retrieved Employee " + employee.getFirstName() +" " + employee.getLastName());
		}
		rs.last();
		rowReturned =rowReturned + rs.getRow();
//			logger.info("SQL: " +sqlHead+sb.toString());

		

		conn.commit();
		
	}
	catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put( (connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT3\t Read employees NO Hibernate - Lazy\t", (System.nanoTime()-startTime));
}

private void getEmployeesNotLazy(Connection conn) {
	String connName = conn.getClass().toString();
	long startTime = System.nanoTime();
	try {
		Statement stmt = conn.createStatement();
		Statement stmtT = conn.createStatement();
		Statement stmtS = conn.createStatement();
		Statement stmtD = conn.createStatement();
		
		String sqlEmp="SELECT emp_no,birth_date,first_name,last_name,gender,hire_date FROM employees where emp_no >10000 and emp_no < 20020";
		String sqlTitle="SELECT  t.emp_no, t.title,t.from_date,t.to_date from titles as t where emp_no=";
		String sqlSal="SELECT s.emp_no,s.salary,s.from_date,s.to_date from salaries as s where emp_no=";
		String sqlDep="select emp_no,from_date,to_date,d.dept_no,dept_name from dept_emp de  join departments d on de.dept_no=d.dept_no where de.emp_no=";
		
		int rowReturned=0;
//		stmt.execute("START TRANSACTION");
//		while(++i < 500) {
//			sb.append(i);
		ResultSet rs = stmt.executeQuery(sqlEmp);
		HashSet employees = new HashSet(); 
		
		while(rs.next()) {
			EmployeesSummary employee = new EmployeesSummary();
			employee.setBirthDate(rs.getDate("birth_date"));
			employee.setHireDate(rs.getDate("hire_date"));
			employee.setGender( rs.getString("gender").charAt(0));
			employee.setEmpNo(rs.getInt("emp_no"));
			employee.setFirstName(rs.getString("first_name"));
			employee.setLastName(rs.getString("last_name"));
//				logger.info("Employee details :" +employee.toString());
			ResultSet rsT = stmtT.executeQuery(sqlTitle + employee.getEmpNo());
			ResultSet rsS = stmtS.executeQuery(sqlSal + employee.getEmpNo());
			ResultSet rsD = stmtD.executeQuery(sqlDep + employee.getEmpNo());
			HashSet titles = new HashSet();
			HashSet salaries = new HashSet();
			HashSet deps = new HashSet();
			
			while(rsT.next()) {
				Titles tl =new Titles();
				TitlesId id = new TitlesId();
				id.setEmpNo(rsT.getInt("emp_no"));
				id.setFromDate(rsT.getDate("from_date"));
				id.setTitle(rsT.getString("title"));
				tl.setId(id);
				tl.setToDate(rsT.getDate("to_date"));
				titles.add(tl);		
			}
			employee.setTitleses(titles);
			while(rsS.next()) {
				Salaries sal = new Salaries();
				SalariesId saId = new SalariesId();
				saId.setEmpNo(rsS.getInt("emp_no"));
				saId.setFromDate(rsS.getDate("from_date"));
				sal.setId(saId);
				sal.setSalary(rsS.getInt("salary"));
				sal.setToDate(rsS.getDate("to_date"));
				salaries.add(sal);
			}
			employee.setSalarieses(salaries);
			while(rsD.next()) {
				Departments dep = new Departments();
				DeptEmp demp = new DeptEmp();
				DeptEmpId dempId = new DeptEmpId();
				dep.setDeptNo(rsD.getString("dept_no"));
				dep.setDeptName("dept_name");
				dempId.setDeptNo(rsD.getString("dept_no"));
				dempId.setEmpNo(rsD.getInt("emp_no"));
				demp.setId(dempId);
				demp.setFromDate(rsD.getDate("from_date"));
				demp.setToDate(rsD.getDate("to_date"));
				demp.setDepartments(dep);
				deps.add(demp);
			}
			employee.setDeptEmps(deps);
			this.logger.info("Retrieved Employee " + employee.getFirstName() +" " + employee.getLastName());
			rsT.close();
			rsD.close();
			rsS.close();
			employees.add(employee);
		}
		rs.last();

		rowReturned =rowReturned + rs.getRow();
		rs.close();
		
		//			logger.info("SQL: " +sqlHead+sb.toString());
	
		
//		conn.commit();
		
	}
	catch(SQLException ex) {
		ex.printStackTrace();
	}
	this.getPerformance().put( (connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT4\t Read employees NO Hibernate - NOT Lazy\t", (System.nanoTime()-startTime));
}

private void getEmployeeACID(SessionFactory sessionFactoryEmp2,Connection conn) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();
	
	logger.info("I am the 1st TRX ");
	EmployeesSummary myEmp = se.find(EmployeesSummary.class,10001, LockModeType.PESSIMISTIC_WRITE);

	Set salaries = null;
	salaries =  myEmp.getSalarieses();
	Iterator itS = salaries.iterator();
//	se.close();
	
	while(itS.hasNext()) {
		Salaries mySal = (Salaries) itS.next();
		if(mySal.getToDate().toString().equals("9999-01-01")){
			logger.info("1TRX Employee name Before = " + myEmp.getFirstName()+" "+ myEmp.getLastName() +" " + mySal.getSalary());
			mySal.setSalary(mySal.getSalary() + 100);
			logger.info("1TRX Employee name After = " + myEmp.getFirstName()+" "+ myEmp.getLastName() +" " + mySal.getSalary());
		}
	}
	logger.info("Another Transaction is modifying the same value ");
//	this.getEmployeeACIDSub(sessionFactoryEmp2);
	
//	se.close();
//	se = sessionFactoryEmp2.openSession();
//	se.beginTransaction();
	
	se.saveOrUpdate(myEmp);
	se.getTransaction().commit();
	se.disconnect();
	se.close();
	logger.info("1TRX COmplete");
	
}

private void getEmployeeACIDSub(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();

	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();

	logger.info("I am the 2nd TRX ");
	EmployeesSummary myEmp = se.find(EmployeesSummary.class,10001, LockModeType.PESSIMISTIC_WRITE);

	Set salaries = null;
	salaries =  myEmp.getSalarieses();
	Iterator itS = salaries.iterator();
	
	while(itS.hasNext()) {
		Salaries mySal = (Salaries) itS.next();
		if(mySal.getToDate().toString().equals("9999-01-01")){
			logger.info("2TRX Employee name Before = " + myEmp.getFirstName()+" "+ myEmp.getLastName() +" " + mySal.getSalary());
			mySal.setSalary(mySal.getSalary() - 400);
			logger.info("2TRX Employee name After = " + myEmp.getFirstName()+" "+ myEmp.getLastName() +" " + mySal.getSalary());
		}
	}

	se.saveOrUpdate(myEmp);
	se.getTransaction().commit();
	se.disconnect();
	se.close();
	logger.info("2TRX COmplete");	
	
}

private SessionFactory getSFactory(String conf) {
	// A SessionFactory is set up once for an application!
	
	if(this.sessionFactory == null) {	
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.configure(conf) // configures settings from hibernate.cfg.xml
				.build();
		
		try {
			sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
			return sessionFactory; 
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			e.printStackTrace();
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}
	else if(this.sessionFactory != null 
			&& this.sessionFactoryEmp == null) {
		final StandardServiceRegistry registry2 = new StandardServiceRegistryBuilder()
				.configure(conf) // configures settings from hibernate.cfg.xml
				.build();
		
		try {
			
			return new MetadataSources( registry2 ).buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			e.printStackTrace();
			StandardServiceRegistryBuilder.destroy( registry2 );
		}
		
	}
	else if(this.sessionFactory != null 
			&& this.sessionFactoryEmp != null
			&& this.sessionFactoryEmpHikari == null) {
		final StandardServiceRegistry registry3 = new StandardServiceRegistryBuilder()
				.configure(conf) // configures settings from hibernate.cfg.xml
				.build();
		
		try {
			
			return new MetadataSources( registry3 ).buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			e.printStackTrace();
			StandardServiceRegistryBuilder.destroy( registry3 );
		}
	}
	
	return null;
}



private void printStats() {
	for(String key: performance.keySet()) {
		this.logger.info("\tns\t" + performance.get(key) + "\t" + key);
		
	}

	
}

private TreeMap<String, Long> getPerformance() {
	return performance;
}

private void setPerformance(TreeMap<String, Long> performance) {
	this.performance = performance;
}

private SimpleDateFormat getSimpleDateFormat() {
	return simpleDateFormat;
}

private void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
	this.simpleDateFormat = simpleDateFormat;
}	

private static int getRandomNumberInRange(int min, int max) {

	if (min >= max) {
		throw new IllegalArgumentException("max must be greater than min");
	}

	Random r = new Random();
	return r.nextInt((max - min) + 1) + min;
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
//			 Properties connectionProps = new Properties();
//			 connectionProps.put("user", this.getUser());
//			 connectionProps.put("password", this.getPassword());
////			 connectionProps.put("useSSL", false);
//
//			 
//			connectionProps.put("cachePrepStmts",true);
//			connectionProps.put("prepStmtCacheSize",250);
//			connectionProps.put("prepStmtCacheSqlLimit",2048);
//			connectionProps.put("useServerPrepStmts",true);
//			connectionProps.put("useLocalSessionState",true);
//			connectionProps.put("rewriteBatchedStatements",true);
//			connectionProps.put("cacheResultSetMetadata",true);
//			connectionProps.put("cacheServerConfiguration",true);
//			connectionProps.put("elideSetAutoCommits",true);
//			connectionProps.put("maintainTimeStats",false);
//			 
//			 
//	         conn = DriverManager.getConnection(
//			                   "jdbc:mysql://" +
//			                   this.getServerUrl() +
//			                   ":" + this.getServerPort() + "/"+this.getDatabase()+"?useSSL=false",
//			                   connectionProps);

		    
		    
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
//		connectionProps.put("useSSL", false);

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

private String getUser() {
	return user;
}

private void setUser(String user) {
	this.user = user;
}

private String getPassword() {
	return password;
}

private void setPassword(String password) {
	this.password = password;
}

private Connection getConn() {
	return conn;
}

private void setConn(Connection conn) {
	this.conn = conn;
}

private String getServerUrl() {
	return serverUrl;
}

private void setServerUrl(String serverUrl) {
	this.serverUrl = serverUrl;
}

private int getServerPort() {
	return serverPort;
}

private void setServerPort(int serverPort) {
	this.serverPort = serverPort;
}

private String getDatabase() {
	return database;
}

private void setDatabase(String database) {
	this.database = database;
}

private SessionFactory getSessionFactory() {
	return sessionFactory;
}

private void setSessionFactory(SessionFactory sessionFactory) {
	this.sessionFactory = sessionFactory;
}

private SessionFactory getSessionFactoryEmp() {
	return sessionFactoryEmp;
}

private void setSessionFactoryEmp(SessionFactory sessionFactoryEmp) {
	this.sessionFactoryEmp = sessionFactoryEmp;
}

private SessionFactory getSessionFactoryEmpHikari() {
	return sessionFactoryEmpHikari;
}

private void setSessionFactoryEmpHikari(SessionFactory sessionFactoryEmpHikari) {
	this.sessionFactoryEmpHikari = sessionFactoryEmpHikari;
}

private SessionFactory getSessionFactorySakila() {
	return sessionFactorySakila;
}

private void setSessionFactorySakila(SessionFactory sessionFactorySakila) {
	this.sessionFactorySakila = sessionFactorySakila;
}
}
