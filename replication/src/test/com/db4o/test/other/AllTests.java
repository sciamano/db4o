package com.db4o.test.other;

import com.db4o.test.replication.db4ounit.DrsTestSuite;

public class AllTests extends DrsTestSuite {
	
	protected Class[] testCases() {
		return new Class[] {
				// Bugs
				BugDrs8.class,

				// Simple
				TheSimplest.class,
				ReplicationEventTest.class,
				ReplicationProviderTest.class,
				ReplicationAfterDeletionTest.class,
				SimpleArrayTest.class,
				SimpleParentChild.class,
				GetByUUID.class,

				// Collection
				MapTest.class,
				ArrayReplicationTest.class,
				//CollectionUuidTest.class,
				ListTest.class, Db4oListTest.class, MapTest.class,
				SingleTypeCollectionReplicationTest.class,
				MixedTypesCollectionReplicationTest.class,

				// Complex
				R0to4Runner.class, ReplicationFeaturesMain.class,

				// General
				CollectionHandlerImplTest.class,
				ReplicationTraversalTest.class, DatabaseUnicityTest.class
		};
	}
	
	public static void main(String[] args) {

		new AllTests().runDb4oDb4o();
		new AllTests().rundb4oCS();
		new AllTests().runCSdb4o();
		new AllTests().runCSCS();
		 
	}

}
