package net.tc.world;
// Generated 24-Apr-2019 6:29:44 PM by Hibernate Tools 5.4.2.Final

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class CountryLanguage.
 * @see net.tc.world.proxysqlcp.world.CountryLanguage
 * @author Hibernate Tools
 */
public class CountryLanguageHome {

	private static final Log log = LogFactory.getLog(CountryLanguageHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext().lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException("Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(CountryLanguage transientInstance) {
		log.debug("persisting CountryLanguage instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(CountryLanguage instance) {
		log.debug("attaching dirty CountryLanguage instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(CountryLanguage instance) {
		log.debug("attaching clean CountryLanguage instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(CountryLanguage persistentInstance) {
		log.debug("deleting CountryLanguage instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public CountryLanguage merge(CountryLanguage detachedInstance) {
		log.debug("merging CountryLanguage instance");
		try {
			CountryLanguage result = (CountryLanguage) sessionFactory.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public CountryLanguage findById(net.tc.world.CountryLanguageId id) {
		log.debug("getting CountryLanguage instance with id: " + id);
		try {
			CountryLanguage instance = (CountryLanguage) sessionFactory.getCurrentSession()
					.get("net.tc.proxysqlcp.CountryLanguage", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(CountryLanguage instance) {
		log.debug("finding CountryLanguage instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria("net.tc.proxysqlcp.CountryLanguage")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: " + results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
