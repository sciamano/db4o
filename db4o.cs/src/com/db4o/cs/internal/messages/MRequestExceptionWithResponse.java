/* Copyright (C) 2009   Versant Inc.   http://www.db4o.com */

package com.db4o.cs.internal.messages;


public class MRequestExceptionWithResponse extends MsgD implements MessageWithResponse {
	
	public Msg replyFromServer() {
		throw ((RuntimeException)readSingleObject());
	}

}
