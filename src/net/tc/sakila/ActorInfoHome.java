package net.tc.sakila;
// Generated 24-Apr-2019 8:11:51 PM by Hibernate Tools 5.4.2.Final

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class ActorInfo.
 * @see net.tc.sakila.ActorInfo
 * @author Hibernate Tools
 */
public class ActorInfoHome {

	private static final Log log = LogFactory.getLog(ActorInfoHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext().lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException("Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(ActorInfo transientInstance) {
		log.debug("persisting ActorInfo instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(ActorInfo instance) {
		log.debug("attaching dirty ActorInfo instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(ActorInfo instance) {
		log.debug("attaching clean ActorInfo instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(ActorInfo persistentInstance) {
		log.debug("deleting ActorInfo instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public ActorInfo merge(ActorInfo detachedInstance) {
		log.debug("merging ActorInfo instance");
		try {
			ActorInfo result = (ActorInfo) sessionFactory.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public ActorInfo findById(net.tc.sakila.ActorInfoId id) {
		log.debug("getting ActorInfo instance with id: " + id);
		try {
			ActorInfo instance = (ActorInfo) sessionFactory.getCurrentSession().get("net.tc.sakila.ActorInfo", id);
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

	public List findByExample(ActorInfo instance) {
		log.debug("finding ActorInfo instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria("net.tc.sakila.ActorInfo")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: " + results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
