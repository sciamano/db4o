/* Copyright (C) 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.db4ounit.jre12.soda;

import com.db4o.db4ounit.common.util.*;
import com.db4o.db4ounit.jre12.soda.collections.*;
import com.db4o.db4ounit.jre12.soda.deepOR.*;
import com.db4o.db4ounit.jre12.soda.experiments.*;
import com.db4o.internal.*;

import db4ounit.extensions.*;

/**
 * @decaf.ignore.jdk11
 */
public class AllTests  extends Db4oTestSuite {
	protected Class[] testCases() {
		 
		return Db4oUnitTestUtil.mergeClasses(new Class[]{
				HashtableModifiedUpdateDepthTestCase.class,
				ObjectSetListAPITestCase.class,
				STArrayListTTestCase.class,
				STArrayListUTestCase.class,
				STHashSetTTestCase.class,
				STHashSetUTestCase.class,
				STHashtableETTestCase.class,
				STHashtableEUTestCase.class,
				STHashtableTTestCase.class,
				STHashtableUTestCase.class,
				STLinkedListTTestCase.class,
				STLinkedListUTestCase.class,
				STOwnCollectionTTestCase.class,
				STTreeSetTTestCase.class,
				STTreeSetUTestCase.class,
				STVectorTTestCase.class,
				STVectorUTestCase.class,
				STOrContainsTestCase.class,
				STCurrentTestCase.class,
				STIdentityEvaluationTestCase.class,
				STNullOnPathTestCase.class,
		}, vectorQbeTestCases());
	}
	
	public static void main(String[] args) {
		new AllTests().runAll();
	}
	
	private Class[] vectorQbeTestCases () {
		
		if(true){
			
			//  QBE with vector and Hashtable is not expressible as SODA and 
			//  it will no longer work with new collection Typehandlers

			return new Class[] {};
		}
		
		return new Class[] {

				STVectorDTestCase.class,
				STVectorEDTestCase.class,
				STHashtableDTestCase.class,
				STHashtableEDTestCase.class,

		};
	}
}