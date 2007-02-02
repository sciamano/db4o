/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal.cs.messages;

import com.db4o.*;
import com.db4o.internal.*;
import com.db4o.internal.cs.*;

public final class MPrefetchIDs extends MsgD {
	public final boolean processAtServer(ServerMessageDispatcher serverThread) {
		int prefetchIDCount = readInt();
		MsgD reply =
			Msg.ID_LIST.getWriterForLength(
				transaction(),
				Const4.INT_LENGTH * prefetchIDCount);

		synchronized (streamLock()) {
			for (int i = 0; i < prefetchIDCount; i++) {
				reply.writeInt(((LocalObjectContainer)stream()).prefetchID());
			}
		}
		serverThread.write(reply);
		return true;
	}
}