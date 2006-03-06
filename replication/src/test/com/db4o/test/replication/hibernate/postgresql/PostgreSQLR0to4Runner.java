package com.db4o.test.replication.hibernate.postgresql;

import com.db4o.inside.replication.TestableReplicationProviderInside;
import com.db4o.replication.hibernate.ref_as_columns.RefAsColumnsReplicationProvider;
import com.db4o.test.replication.R0;
import com.db4o.test.replication.hibernate.HibernateConfigurationFactory;
import com.db4o.test.replication.hibernate.HibernateR0to4Runner;

public class PostgreSQLR0to4Runner extends HibernateR0to4Runner {
	protected TestableReplicationProviderInside prepareProviderA() {
		cfgA = HibernateConfigurationFactory.producePostgreSQLConfigA();
		cfgA.addClass(R0.class);
		return new RefAsColumnsReplicationProvider(cfgA, "A");
	}

	protected TestableReplicationProviderInside prepareProviderB() {
		cfgB = HibernateConfigurationFactory.producePostgreSQLConfigB();
		cfgB.addClass(R0.class);
		return new RefAsColumnsReplicationProvider(cfgB, "B");
	}

	public void test() {
		super.test();
	}
}
