package net.tc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

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
import org.hibernate.stat.Statistics;
import org.jboss.logging.Logger;

import net.tc.employees.Employees;
import net.tc.employees.Titles;
import net.tc.world.*;

public class ExampleWithHibernate {
	SessionFactory sessionFactory = null;
	SessionFactory sessionFactoryEmp = null;
	SessionFactory sessionFactoryEmpHikari = null;
	SessionFactory sessionFactorySakila = null;
	org.apache.log4j.Logger logger =null;
	SimpleDateFormat simpleDateFormat;
	TreeMap<String,Long> performance =new TreeMap<String,Long>();
	
public ExampleWithHibernate() {
	
	init();
	
	
	long startTime = System.nanoTime();
//	getCitys(sessionFactory);
	

	setEmployees(sessionFactoryEmp);
	getEmployees(sessionFactoryEmp);
	updateEmployees(sessionFactoryEmp);
	deleteEmployees(sessionFactoryEmp);

	
	setEmployeesBatch(sessionFactoryEmp);
	getEmployees(sessionFactoryEmp);
	updateEmployeesBatch(sessionFactoryEmp);
	deleteEmployeesBatch(sessionFactoryEmp);
	
	
	setEmployees(sessionFactoryEmpHikari);
	getEmployees(sessionFactoryEmpHikari);
	updateEmployees(sessionFactoryEmpHikari);
	deleteEmployees(sessionFactoryEmpHikari);

	
	setEmployeesBatch(sessionFactoryEmpHikari);
	getEmployees(sessionFactoryEmpHikari);
	updateEmployeesBatch(sessionFactoryEmpHikari);
	deleteEmployeesBatch(sessionFactoryEmpHikari);

	
	
	this.printStats();
	
	
	
}

private void init() {
	PropertyConfigurator.configure("log4j.properties");
	logger = LogManager.getLogger("PL2019");
//	logger.info("Logger Test");
//	sessionFactory = getSFactory("hibernate.cfg.xml");
	sessionFactoryEmp = getSFactory("hibernate_employees.cfg.xml");
	sessionFactoryEmpHikari = getSFactory("hibernate_employees_hikari.cfg.xml");
	
	String pattern = "yyyy-MM-dd";
	simpleDateFormat = new SimpleDateFormat(pattern);
}

private void setEmployees(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}
	
	long startTime = System.nanoTime();

	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(1);
	se.beginTransaction();
	int i=1;
	while(++i < 500) {
		Employees employee = new Employees();
		employee.setBirthDate(new Date());
		employee.setHireDate(new Date());
		employee.setGender('M');
		employee.setEmpNo(i);
		employee.setFirstName("Franco" + 1);
		employee.setLastName("Castagna"+i);
		se.save(employee);
	
	}
	se.getTransaction().commit(); 
	se.close();
	
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT1\t insert employees Hibernate - no batch\t"  , (System.nanoTime()-startTime));	
}

private void getEmployees(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(1);
	
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees where emp_no <999 " ).list();
	se.disconnect();
	se.close();

	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT2\t Read employees Hibernate - no batch\t" , (System.nanoTime()-startTime));
}

private void updateEmployees(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(1);
	
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees where emp_no <999 " ).list();
	List<Titles> titles = null;

	/*
	 * Loop employees
	 */
	int i = 0;
	Iterator it = employees.iterator();
	
	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
		
		try{myEmp.setHireDate(this.getSimpleDateFormat().parse("2015-"+ getRandomNumberInRange(1,12) +"-10"));}catch(Exception ae ) {ae.printStackTrace();}
		se.update(myEmp);
	}
	se.getTransaction().commit();
			
	se.disconnect();
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT3\t update employees Hibernate - no batch\t" , (System.nanoTime()-startTime));
}


private void deleteEmployees(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(1);
	
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees where emp_no < 999" ).list();
	
	Iterator it = employees.iterator();

	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
		se.delete(myEmp);
//		logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
		
	}
	
	se.getTransaction().commit(); 
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT4\tdelete employees Hibernate - no batch\t" , (System.nanoTime()-startTime));
}


private void setEmployeesBatch(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(20);
	se.beginTransaction();
	int i=1;
	while(i < 500) {
		Employees employee = new Employees();
		employee.setBirthDate(new Date());
		employee.setHireDate(new Date());
		employee.setGender('M');
		employee.setEmpNo(i);
		employee.setFirstName("Franco" + 1);
		employee.setLastName("Castagna"+i);
		se.save(employee);
		if ( ++i % 20 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        se.flush();
		        se.clear();
		    }
	}
	se.getTransaction().commit(); 
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT5\tinsert employees Hibernate - batch\t", (System.nanoTime()-startTime));
}


private void updateEmployeesBatch(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(20);
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees where emp_no <999 " ).list();

	/*
	 * Loop employees
	 */
	int i = 0;
	Iterator it = employees.iterator();
	
	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
		
		try{myEmp.setHireDate(this.getSimpleDateFormat().parse("2015-"+ getRandomNumberInRange(1,12) +"-10"));}catch(Exception ae ) {ae.printStackTrace();}
		se.update(myEmp);
		if ( ++i % 50 == 0 ) { //20, same as the JDBC batch size
	        //flush a batch of inserts and release memory:
	        se.flush();
	        se.clear();
	    }
	}
	se.getTransaction().commit();
			
	se.disconnect();
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT7\tupdate employees Hibernate - batch\t" , (System.nanoTime()-startTime));
}

private void deleteEmployeesBatch(SessionFactory sessionFactoryEmp2) {
	String connName ="";
	Map properties = sessionFactoryEmp2.getProperties();
	if(properties != null && properties.get("sessionFactoryName") !=null) {
		connName = "-"+properties.get("sessionFactoryName");	
	}

	long startTime = System.nanoTime();
	
	Session se = sessionFactoryEmp2.openSession();
	se.setJdbcBatchSize(20);
	se.beginTransaction();
//	ScrollableResults employees = se.createQuery("from Employees where emp_no < 501" )
//									.setCacheMode(CacheMode.IGNORE)
//									.scroll(ScrollMode.FORWARD_ONLY);
	int i = 0;
	
	List<Employees> employees = se.createQuery("from Employees where emp_no < 999" ).list();
	
	Iterator it = employees.iterator();
	
	
	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
//		logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
		se.delete(myEmp);
		if ( ++i % 20 == 0 ) { //20, same as the JDBC batch size
	        //flush a batch of inserts and release memory:
	        se.flush();
	        se.clear();
	    }
	
	}
	

	se.getTransaction().commit(); 
	se.close();
	this.getPerformance().put((connName.indexOf("Hikari")>0?" Hikari ":" Basic ")+"\tT8\tdelete employees Hibernate - batch\t" , (System.nanoTime()-startTime));
}

private void getCitys(SessionFactory sessionFactory2) {
	String connName = sessionFactory2.getClass().toString();
	long startTime = System.nanoTime();
	
	
	City myCity = new City();
	Country myCountry = new Country();
	myCountry.setCode("ITA");
	Session se = sessionFactory2.openSession();
	se.beginTransaction();
	List<Country> countries = se.createQuery("from Country" ).list();
	List<City> cities = se.createQuery("from City where CountryCode = 'ITA'").list();
	while(1<2) {
		Iterator it = cities.iterator();
		
		while(it.hasNext()) {
			myCity = (City) it.next();
			logger.info("City name = " + myCity.getName() + "\t\t\t\t Population = " + myCity.getPopulation());
		}
		
		
		Criteria cr = se.createCriteria(myCountry.getClass());
		List<Country> countries1 = cr.list();
		myCountry = se.get(Country.class, myCountry.getCode());
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("------------");
//		this.getCitys(sessionFactory2);
		
	}
	
//	se.disconnect();
//	se.close();


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

public static void main(String[] args) {
	
	ExampleWithHibernate ws = new ExampleWithHibernate();
//	ws.getSFactory();
	
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

}
