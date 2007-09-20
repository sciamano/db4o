/* Copyright (C) 2007   db4objects Inc.   http://www.db4o.com */
package com.db4o.ta.instrumentation.test;

public class ToBeInstrumented {
	
	protected static int _xx;
	protected int _x;

	public void foo() {
		int y = _x;
	}

	protected void bar() {
		int y = _x;
	}

	void baz() {
		int y = _x;
	}
	
	private void boo() {
		int y = _x;
	}
	
	public static void fooStatic() {
		int yy = _xx;
	}

	public boolean accessNotToBeInstrumented(NotToBeInstrumented other) {
		return _x == other._x;
	}
}
