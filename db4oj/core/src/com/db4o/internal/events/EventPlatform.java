/* Copyright (C) 2006   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal.events;

import com.db4o.events.*;
import com.db4o.internal.*;
import com.db4o.query.Query;

/**
 * Platform dependent code for dispatching events.
 * 
 * @sharpen.ignore
 */
public class EventPlatform {

	public static void triggerQueryEvent(Transaction transaction, Event4Impl e, Query q) {
		e.trigger(new QueryEventArgs(transaction, q));
	}

	public static void triggerClassEvent(Event4Impl e, ClassMetadata clazz) {
		e.trigger(new ClassEventArgs(clazz));
	}

	public static boolean triggerCancellableObjectEventArgs(Transaction transaction, Event4Impl e, Object o) {
		CancellableObjectEventArgs args = new CancellableObjectEventArgs(transaction, o);
		e.trigger(args);
		return !args.isCancelled();
	}
	
	public static void triggerObjectEvent(Transaction transaction, Event4Impl e, Object o) {
		e.trigger(new ObjectEventArgs(transaction, o));
	}

	public static void triggerCommitEvent(Transaction transaction, Event4Impl e, CallbackObjectInfoCollections collections) {
		e.trigger(new CommitEventArgs(transaction, collections));
	}

	public static boolean hasListeners(Event4Impl e) {
		return e.hasListeners();
	}
}
