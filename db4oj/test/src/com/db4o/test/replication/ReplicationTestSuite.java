package com.db4o.test.replication;

import com.db4o.test.*;

public class ReplicationTestSuite extends TestSuite{

	public Class[] tests() {
		return new Class[] {
            R0to4Runner.class,
			ReplicationFeatures.class,
			ReplicationFeaturesMain.class
		};
	}
	
	

}
