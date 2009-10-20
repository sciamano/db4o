/* Copyright (C) 2009  Versant Inc.   http://www.db4o.com */
package com.db4o.db4ounit.optional.monitoring;

import javax.management.*;

import com.db4o.*;
import com.db4o.config.*;
import com.db4o.internal.*;
import com.db4o.io.*;
import com.db4o.monitoring.*;

import db4ounit.*;

@decaf.Remove
public class Db4oMBeanRegistryTestCase implements TestCase {

	private static interface Mock1MBean {
	}

	private static interface Mock2MBean {
	}

	private static abstract class MockMBean extends MBeanRegistrationSupport {
		public boolean registered = false;

		public MockMBean(ObjectContainer db, Class<?> type) {
			super(db, type);
		}

		@Override
		public void register() throws JMException {
			super.register();
			registered = true;
		}
		
		@Override
		public void unregister() {
			super.unregister();
			registered = false;
		}
	}

	private static class Mock1 extends MockMBean implements Mock1MBean {
		public Mock1(ObjectContainer db, Class<?> type) {
			super(db, type);
		}
	}

	private static class Mock2 extends MockMBean implements Mock2MBean {
		public Mock2(ObjectContainer db, Class<?> type) {
			super(db, type);
		}
	}

	public void test() {
		final MockMBean[] beans = new MockMBean[2];
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.file().storage(new MemoryStorage());
		config.common().add(new ConfigurationItem() {
			public void prepare(Configuration configuration) {
			}
			
			public void apply(InternalObjectContainer container) {
					beans[0] = new Mock1(container, Mock1MBean.class);
					beans[1] = new Mock2(container, Mock2MBean.class);
			}
		});
		final ExternalObjectContainer db = (ExternalObjectContainer) Db4oEmbedded.openFile(config, "");
		assertRegistered(beans, true);
		db.close();
		assertRegistered(beans, false);
	}
	
	private void assertRegistered(MockMBean[] beans, boolean expected) {
		for (MockMBean bean: beans) {
			Assert.areEqual(expected, bean.registered);
		}
	}
}