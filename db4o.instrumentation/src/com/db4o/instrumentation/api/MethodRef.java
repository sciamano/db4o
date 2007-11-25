/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package com.db4o.instrumentation.api;

public interface MethodRef {
	
	/**
	 * @sharpen.property
	 */
	String name();

	/**
	 * @sharpen.property
	 */
	TypeRef returnType();
	
	/**
	 * @sharpen.property
	 */
	TypeRef[] paramTypes();

	/**
	 * @sharpen.property
	 */
	TypeRef declaringType();
}
