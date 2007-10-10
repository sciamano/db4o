/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.db4ounit.jre5.collections;

import com.db4o.collections.*;
import com.db4o.ext.*;

import db4ounit.*;
import db4ounit.extensions.fixtures.*;

/**
 * @exclude
 */
public class ArrayList4TAMultiClientsTestCase extends ArrayList4TATestCaseBase implements OptOutSolo {
	public static void main(String[] args) {
		new ArrayList4TAMultiClientsTestCase().runEmbeddedClientServer();
	}

	private static final ArrayList4Operation <Integer> _addOp = new ArrayList4Operation<Integer>() {
		public void operate(ArrayList4<Integer> list) {
			list.add(new Integer(ArrayList4Asserter.CAPACITY));
		}	
	};
	
	private static final ArrayList4Operation<Integer> _removeOp = new ArrayList4Operation<Integer>() {
		public void operate(ArrayList4<Integer> list) {
			list.remove(0);
		}	
	};	
	
	private static final ArrayList4Operation<Integer> _setOp = new ArrayList4Operation<Integer>() {
		public void operate(ArrayList4<Integer> list) {
			list.set(0, new Integer(1));
		}	
	};	
	
	private static final ArrayList4Operation<Integer> _clearOp = new ArrayList4Operation<Integer>() {
		public void operate(ArrayList4<Integer> list) {
			list.clear();
		}	
	};	

	private static final ArrayList4Operation<Integer> _containsOp = new ArrayList4Operation<Integer>() {
		public void operate(ArrayList4<Integer> list) {
			Assert.isFalse(list.contains(new Integer(ArrayList4Asserter.CAPACITY)));
		}	
	};	
	
	public void testAddAdd() throws Exception {
		ArrayList4Operation<Integer> anotherAddOp = new ArrayList4Operation<Integer>() {
			public void operate(ArrayList4<Integer> list) {
				list.add(new Integer(ArrayList4Asserter.CAPACITY + 42));
			}	
		};	
		operate(anotherAddOp, _addOp);
		checkAdd();
	}

	public void testSetAdd() throws Exception {
		operate(_setOp, _addOp);
		checkAdd();
	}
	
	public void testRemoveAdd() throws Exception {
		operate(_removeOp, _addOp);
		checkAdd();
	}
	
	private void checkAdd() throws Exception {
		checkListSize(ArrayList4Asserter.CAPACITY+1);
	}
	
	private void checkNotModified() throws Exception {
		checkListSize(ArrayList4Asserter.CAPACITY);
	}
	
	private void checkListSize(int expectedSize) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4();
		Assert.areEqual(expectedSize, list.size());
		for (int i = 0; i < expectedSize; ++i) {
			Assert.areEqual(new Integer(i), list.get(i));
		}
	}
	
	

	public void testAddRemove() throws Exception {
		operate(_addOp, _removeOp);
		checkRemove();
	}
	
	public void testsetRemove() throws Exception {
		operate(_setOp, _removeOp);
		checkRemove();
	}
	
	public void testRemoveRemove() throws Exception {
		ArrayList4Operation<Integer> anotherRemoveOp = new ArrayList4Operation<Integer>() {
			public void operate(ArrayList4<Integer> list) {
				list.remove(1);
			}	
		};	
		operate(anotherRemoveOp, _removeOp);
		checkRemove();
	}
	
	private void checkRemove() throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4();
		Assert.areEqual(ArrayList4Asserter.CAPACITY - 1, list.size());
		for (int i = 0; i < ArrayList4Asserter.CAPACITY - 1; ++i) {
			Assert.areEqual(new Integer(i + 1), list.get(i));
		}
	}

	public void testAddSet() throws Exception {
		operate(_addOp, _setOp);
		checkSet();
	}
	
	public void testRemoveSet() throws Exception {
		operate(_removeOp, _setOp);
		checkSet();
	}
	
	public void testSetSet() throws Exception {
		ArrayList4Operation<Integer>  anotherSetOp = new ArrayList4Operation<Integer>() {
			public void operate(ArrayList4<Integer> list) {
				list.set(0, new Integer(2));
			}	
		};
		operate(anotherSetOp, _setOp);
		checkSet();
	}
	
	public void testClearSet() throws Exception {
		operate(_clearOp, _setOp);
		checkSet();
	}
	
	public void testSetClear() throws Exception {
		operate(_setOp, _clearOp);
		checkClear();
	}
	
	public void testClearRemove() throws Exception {
		operate(_clearOp, _removeOp);
		checkRemove();
	}
	
	public void testRemoveClear() throws Exception {
		operate(_removeOp, _clearOp);
		checkClear();
	}
	
	public void testContainsAdd() throws Exception {
		operate(_addOp, _containsOp);
		checkNotModified();
	}
	
	public void testAddContains() throws Exception {
		operate(_containsOp, _addOp);
		checkAdd();
	}
	
	private void checkClear() throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4();
		Assert.areEqual(0, list.size());
	}

	private void checkSet() throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4();
		Assert.areEqual(ArrayList4Asserter.CAPACITY, list.size());
		Assert.areEqual(new Integer(1), list.get(0));
		for (int i = 1; i < ArrayList4Asserter.CAPACITY; ++i) {
			Assert.areEqual(new Integer(i), list.get(i));
		}
	}
	
	private void operate(ArrayList4Operation <Integer> op1, ArrayList4Operation<Integer> op2) throws Exception {
		ExtObjectContainer client1 = openNewClient();
		ExtObjectContainer client2 = openNewClient();
		ArrayList4<Integer> list1 = retrieveAndAssertNullArrayList4(client1);
		ArrayList4<Integer> list2 = retrieveAndAssertNullArrayList4(client2);
		op1.operate(list1);
		op2.operate(list2);
		client1.set(list1);
		client2.set(list2);
		client1.commit();
		client2.commit();
		client1.close();
		client2.close();
	}

}
