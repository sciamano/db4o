/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.instrumentation;

/**
 * @exclude
 */
public class AssignableClassFilter implements ClassFilter {

	private Class _targetClazz;
	
	public AssignableClassFilter(Class targetClazz) {
		_targetClazz = targetClazz;
	}
	
	public boolean accept(Class clazz) {
		return _targetClazz.isAssignableFrom(clazz);
	}

}
