/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal;

/**
 * 
 * @decaf.ignore
 */
class ReferenceQueue4 extends java.lang.ref.ReferenceQueue
{
	ActiveObjectReference yapPoll(){
		return (ActiveObjectReference)super.poll();
	}
	
}
