/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal;

/**
 * 
 * @decaf.ignore
 */
class JDK_1_3 extends JDK_1_2{

	Thread addShutdownHook(Runnable runnable){
		Thread thread = new Thread(runnable);
	    Reflection4.invoke(Runtime.getRuntime(), "addShutdownHook", new Object[]{thread});
		return thread;
	}
	
	void removeShutdownHook(Thread thread){
	    Reflection4.invoke(Runtime.getRuntime(), "removeShutdownHook", new Object[]{thread});
	}
	
	public int ver(){
	    return 3;
	}
	
}
