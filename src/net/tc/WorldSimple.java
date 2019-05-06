package net.tc;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import org.hibernate.criterion.CriteriaQuery;
import org.jboss.logging.Logger;

import net.tc.employees.Employees;
import net.tc.employees.Titles;
import net.tc.world.*;

public class WorldSimple {
	SessionFactory sessionFactory = null;
	SessionFactory sessionFactoryEmp = null;
	SessionFactory sessionFactorySakila = null;
	org.apache.log4j.Logger logger =null;

public WorldSimple() {
	PropertyConfigurator.configure("log4j.properties");
	logger = LogManager.getLogger("APPLICATION");
	logger.info("Logger Test");
	sessionFactory = getSFactory("hibernate.cfg.xml");
	sessionFactoryEmp = getSFactory("hibernate_employees.cfg.xml");
	
	long startTime = System.nanoTime();
//	getCitys(sessionFactory);
//	getEmployees(sessionFactoryEmp);
	setEmployees(sessionFactoryEmp);
	deleteEmployees(sessionFactoryEmp);
	
//	setEmployeesBatch(sessionFactoryEmp);
//	deleteEmployeesBatch(sessionFactoryEmp);

	
	
	long endTime = System.nanoTime();
	long perf = ((endTime - startTime) / 1000000000);
	logger.info("Total Time in Sec = " + perf + " nanosec =  " +(endTime - startTime));
	
	
	
}

private void setEmployees(SessionFactory sessionFactoryEmp2) {
	Session se = sessionFactoryEmp2.openSession();
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
	
}


private void setEmployeesBatch(SessionFactory sessionFactoryEmp2) {
	Session se = sessionFactoryEmp2.openSession();
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
		if ( ++i % 50 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        se.flush();
		        se.clear();
		    }
		
		
	}
	se.getTransaction().commit(); 
	se.close();
	
}

private void deleteEmployees(SessionFactory sessionFactoryEmp2) {
	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees where emp_no < 501" ).list();
	
	Iterator it = employees.iterator();

	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
		logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
		se.delete(myEmp);

	}
	
	se.getTransaction().commit(); 
	se.close();
	
}


private void deleteEmployeesBatch(SessionFactory sessionFactoryEmp2) {
	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();
//	ScrollableResults employees = se.createQuery("from Employees where emp_no < 501" )
//									.setCacheMode(CacheMode.IGNORE)
//									.scroll(ScrollMode.FORWARD_ONLY);
	int i = 0;
	
	List<Employees> employees = se.createQuery("from Employees where emp_no < 501" ).list();
	
	Iterator it = employees.iterator();
	
	
	while(it.hasNext()) {
		Employees myEmp = (Employees) it.next();
		logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
		se.delete(myEmp);
		if ( ++i % 50 == 0 ) { //20, same as the JDBC batch size
	        //flush a batch of inserts and release memory:
	        se.flush();
	        se.clear();
	    }
	
	}
	
		
//	while(employees.next()) {
//		Employees myEmp = (Employees) employees.get(0);
//		logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
//		se.delete(myEmp);
//		if ( ++i % 50 == 0 ) { //20, same as the JDBC batch size
//	        //flush a batch of inserts and release memory:
//	        se.flush();
//	        se.clear();
//	    }
//	
//	}
	
	se.getTransaction().commit(); 
	se.close();
	
}


private void getEmployees(SessionFactory sessionFactoryEmp2) {
	Session se = sessionFactoryEmp2.openSession();
	se.beginTransaction();
	List<Employees> employees = se.createQuery("from Employees order by lastName desc, firstName asc" ).setHibernateMaxResults(10).list();
	List<Titles> titles = null;
//			se.createQuery("from City where CountryCode = 'ITA'").list();
	
	int i = 0;
	while(i < 100) {
		Iterator it = employees.iterator();
		
		while(it.hasNext()) {
			Employees myEmp = (Employees) it.next();
			logger.info("Employee name = " + myEmp.getFirstName()+" "+ myEmp.getLastName());
			Iterator it2 = myEmp.getTitleses().iterator();
			while(it2.hasNext()) {
				Titles myTitle = (Titles) it2.next();
				logger.info("\t\t "+ myTitle.getId().getTitle() +"  Up to "+ myTitle.getToDate().toString());
				
			}
		}
		
				
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("------------");
//		this.getCitys(sessionFactoryEmp2);
		i++;
	}
	se.disconnect();
	se.close();
	
}

private void getCitys(SessionFactory sessionFactory2) {
	
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
	
	return null;
}

public static void main(String[] args) {
	
	WorldSimple ws = new WorldSimple();
//	ws.getSFactory();
	
}	
	
	
}
