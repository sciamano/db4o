package com.db4o.test.replication.hibernate;

import com.db4o.inside.replication.TestableReplicationProviderInside;
import com.db4o.replication.hibernate.HibernateReplicationProvider;
import com.db4o.replication.hibernate.ref_as_columns.RefAsColumnsReplicationProvider;
import com.db4o.test.replication.Replicated;
import com.db4o.test.replication.ReplicationFeaturesMain;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

public class HibernateReplicationFeaturesMain extends ReplicationFeaturesMain {
	protected Configuration cfgA;
	protected Configuration cfgB;
	protected HibernateReplicationProvider pA;
	protected HibernateReplicationProvider pB;


	public HibernateReplicationFeaturesMain() {

	}

	protected TestableReplicationProviderInside prepareProviderA() {
		delete(pA.getObjectSession());
		return pA;
	}

	protected TestableReplicationProviderInside prepareProviderB() {
		delete(pA.getObjectSession());
		return pB;
	}

	public void test() {
		cfgA = HibernateConfigurationFactory.createNewDbConfig();
		cfgA.addClass(Replicated.class);
		pA = new RefAsColumnsReplicationProvider(cfgA, "A");

		cfgB = HibernateConfigurationFactory.createNewDbConfig();
		cfgB.addClass(Replicated.class);
		pB = new RefAsColumnsReplicationProvider(cfgB, "B");

		super.test();
	}

	protected void clean() {
		pA.closeIfOpened();
		pB.closeIfOpened();
	}

	protected void delete(Session session) {
		session.createQuery("delete from Replicated").executeUpdate();
	}
}
