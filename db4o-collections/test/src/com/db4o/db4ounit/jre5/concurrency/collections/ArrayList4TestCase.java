/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.db4ounit.jre5.concurrency.collections;

import com.db4o.collections.*;
import com.db4o.config.*;
import com.db4o.db4ounit.jre5.collections.*;
import com.db4o.ext.*;
import com.db4o.reflect.*;
import com.db4o.ta.*;

import db4ounit.*;
import db4ounit.extensions.*;

/**
 * @exclude
 */
public class ArrayList4TestCase extends Db4oConcurrenyTestCase {
	public static void main(String[] args) {
		new ArrayList4TestCase().runEmbeddedConcurrency();
	}

	protected void store() throws Exception {
		ArrayList4<Integer> list = new ArrayList4<Integer>();
		ArrayList4Asserter.createList(list);
		store(list);
	}

	@Override
	protected void configure(Configuration config) throws Exception {
		config.add(new TransparentActivationSupport());
		config.activationDepth(0);
		super.configure(config);
	}
	
	public void conc(ExtObjectContainer oc) throws Exception {
		retrieveAndAssertNullArrayList4(oc);
	}
	
	public void concAdd(ExtObjectContainer oc, int seq) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.assertAdd(list);
		markTaskDone(seq, true);
		waitForAllTasksDone();
		oc.set(list);
	}
	
	public void checkAdd(ExtObjectContainer oc) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.checkAdd(list);
	}
	
	public void concAdd_LObject(ExtObjectContainer oc, int seq) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.assertAdd_LObject(list);
		markTaskDone(seq, true);
		waitForAllTasksDone();
		oc.set(list);
	}

	public void checkAdd_LObject(ExtObjectContainer oc) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.checkAdd_LObject(list);
	}
	
	public void concAddAll_LCollection(ExtObjectContainer oc, int seq) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.assertAddAll_LCollection(list);
		markTaskDone(seq, true);
		waitForAllTasksDone();
		oc.set(list);
	}

	public void checkAddAll_LCollection(ExtObjectContainer oc) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.checkAddAll_LCollection(list);
	}
	
	public void concClear(ExtObjectContainer oc, int seq) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.assertClear(list);
		markTaskDone(seq, true);
		waitForAllTasksDone();
		oc.set(list);
	}
	
	public void checkClear(ExtObjectContainer oc) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.checkClear(list);
	}

	public void concContains(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertContains(retrieveAndAssertNullArrayList4(oc));
	}
	
	public void concContainsAll(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertContainsAll(retrieveAndAssertNullArrayList4(oc));
	}

	public void concIndexOf(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertIndexOf(retrieveAndAssertNullArrayList4(oc));
	}

	public void concIsEmpty(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertIsEmpty(retrieveAndAssertNullArrayList4(oc));
	}

	public void concIterator(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertIterator(retrieveAndAssertNullArrayList4(oc));
	}

	public void concLastIndexOf(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertLastIndexOf(retrieveAndAssertNullArrayList4(oc));
	}

	public void concRemove_LObject(ExtObjectContainer oc, int seq) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.assertRemove_LObject(list);
		markTaskDone(seq, true);
		waitForAllTasksDone();
		oc.set(list);
	}
	
	public void checkRemove_LObject(ExtObjectContainer oc) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.checkRemove_LObject(list);
	}
	

	public void concRemoveAll(ExtObjectContainer oc, int seq) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.assertRemoveAll(list);
		markTaskDone(seq, true);
		waitForAllTasksDone();
		oc.set(list);
	}
	
	public void checkRemoveAll(ExtObjectContainer oc) throws Exception {
		ArrayList4<Integer> list = retrieveAndAssertNullArrayList4(oc);
		ArrayList4Asserter.checkRemoveAll(list);
	}
//
//	public void testSet() throws Exception {
//		ArrayList4Asserter.assertSet(retrieveAndAssertNullArrayList4());
//	}
//
	public void concSize(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertSize(retrieveAndAssertNullArrayList4(oc));
	}
	
	public void concToArray(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertToArray(retrieveAndAssertNullArrayList4(oc));
	}
	
	public void concToArray_LObject(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertToArray_LObject(retrieveAndAssertNullArrayList4(oc));
	}
	
	public void concToString(ExtObjectContainer oc) throws Exception {
		ArrayList4Asserter.assertToString(retrieveAndAssertNullArrayList4(oc));
	}
//	
//	public void testTrimToSize_EnsureCapacity() throws Exception {
//		ArrayList4Asserter.assertTrimToSize_EnsureCapacity(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testTrimToSize_Remove() throws Exception {
//		ArrayList4Asserter.assertTrimToSize_Remove(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testTrimToSize_Iterator() throws Exception {
//		ArrayList4Asserter.assertTrimToSize_Iterator(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testEnsureCapacity_Iterator() throws Exception {
//		ArrayList4Asserter.assertEnsureCapacity_Iterator(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testClear_Iterator() throws Exception {
//		ArrayList4Asserter.assertClear_Iterator(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testClone() throws Exception {
//		ArrayList4Asserter.assertClone(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testEquals() throws Exception {
//		ArrayList4Asserter.assertEquals(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testIteratorNext_NoSuchElementException() throws Exception {
//		ArrayList4Asserter.assertIteratorNext_NoSuchElementException(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testIteratorNext_ConcurrentModificationException() throws Exception {
//		ArrayList4Asserter.assertIteratorNext_ConcurrentModificationException(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testIteratorNext() throws Exception {
//		ArrayList4Asserter.assertIteratorNext(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testIteratorRemove() throws Exception {
//		ArrayList4Asserter.assertIteratorRemove(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testRemove_IllegalStateException() throws Exception {
//		ArrayList4Asserter.assertRemove_IllegalStateException(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testIteratorRemove_ConcurrentModificationException() throws Exception {
//		ArrayList4Asserter.assertIteratorRemove_ConcurrentModificationException(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testSubList() throws Exception {
//		ArrayList4Asserter.assertSubList(retrieveAndAssertNullArrayList4());
//	}
//	
//	public void testSubList_ConcurrentModification() throws Exception {
//		ArrayList4Asserter.assertSubList_ConcurrentModification(retrieveAndAssertNullArrayList4());
//	}
//	
	@SuppressWarnings("unchecked")
	private ArrayList4<Integer> retrieveAndAssertNullArrayList4(ExtObjectContainer oc) throws Exception{
		ArrayList4<Integer> list = (ArrayList4<Integer>) retrieveOnlyInstance(oc, ArrayList4.class);
		assertNullArrayList4(list);
		return list;
	}
	
	private void assertNullArrayList4(ArrayList4<Integer> list) throws Exception {
		Assert.isNull(getField(list, "elements"));
		Assert.areEqual(0, getField(list, "capacity"));
		Assert.areEqual(0, getField(list, "listSize"));
	}
	
	private Object getField(Object parent, String fieldName) {
		ReflectClass parentClazz = reflector().forObject(parent);
		ReflectField field = parentClazz.getDeclaredField(fieldName);
		field.setAccessible();
		return field.get(parent);
	}
}
