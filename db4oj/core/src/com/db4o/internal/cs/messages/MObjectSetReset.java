/* Copyright (C) 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal.cs.messages;

import com.db4o.internal.cs.*;


/**
 * @exclude
 */
public class MObjectSetReset extends MObjectSet {
	
	public boolean processAtServer(ServerMessageDispatcher serverThread) {
		stub(serverThread, readInt()).reset();
		return true;
	}

}
