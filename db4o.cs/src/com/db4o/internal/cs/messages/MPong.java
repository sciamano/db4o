/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal.cs.messages;


/**
 * @exclude
 */
public class MPong extends Msg implements ClientSideMessage {

	public boolean processAtClient() {
		return true;
	}

}