/* Copyright (C) 2006  Versant Inc.  http://www.db4o.com */

package com.db4o.cs.internal.messages;



/**
 * @exclude
 */
public class MObjectSetReset extends MObjectSet implements ServerSideMessage {
	
	public boolean processAtServer() {
		stub(readInt()).reset();
		return true;
	}

}