package net.tc;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.criterion.CriteriaQuery;
import org.jboss.logging.Logger;

import net.tc.world.*;

public class WorldSimple {
	SessionFactory sessionFactory = null;
	org.apache.log4j.Logger logger =null;

public WorldSimple() {
	PropertyConfigurator.configure("log4j.properties");
	logger = LogManager.getLogger("APPLICATION");
	logger.info("Logger Test");
	sessionFactory = getSFactory();
	
	getCitys(sessionFactory);
	
}

private void getCitys(SessionFactory sessionFactory2) {
	
	City myCity = new City();
	Country myCountry = new Country();
	myCountry.setCode("ITA");
	Session se = sessionFactory2.openSession();
	se.beginTransaction();
	List<Country> countries = se.createQuery("from Country" ).list();
	List<City> cities = se.createQuery("from City").list();
	Iterator it = cities.iterator();
	
	while(it.hasNext()) {
		myCity = (City) it.next();
		logger.info("City name = " + myCity.getName());
	}
	
	Criteria cr = se.createCriteria(myCountry.getClass());
	List<Country> countries1 = cr.list();
	myCountry = se.get(Country.class, myCountry.getCode());
	
	logger.info("");
	
	se.close();
	se.disconnect();
}

private SessionFactory getSFactory() {
	// A SessionFactory is set up once for an application!
	
	
	final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
			.configure("hibernate.cfg.xml") // configures settings from hibernate.cfg.xml
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
	
	return null;
}

public static void main(String[] args) {
	
	WorldSimple ws = new WorldSimple();
//	ws.getSFactory();
	
}	
	
	
}
