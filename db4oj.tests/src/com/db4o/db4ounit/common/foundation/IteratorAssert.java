/* Copyright (C) 2007   db4objects Inc.   http://www.db4o.com */
package com.db4o.db4ounit.common.foundation;

import com.db4o.foundation.*;

import db4ounit.Assert;

public class IteratorAssert {

	public static void areEqual(Iterator4 expected, Iterator4 actual) {
		if (null == expected) {
			Assert.isNull(actual);
		}
		Assert.isNotNull(actual);		
		while (expected.moveNext()) {
			Assert.isTrue(actual.moveNext(), "'" + expected.current() + "' expected.");
			Assert.areEqual(expected.current(), actual.current());
		}
		Assert.isFalse(actual.moveNext());
	}

	public static void areEqual(Object[] expected, Iterator4 iterator) {
		areEqual(new ArrayIterator4(expected), iterator);
	}

}
