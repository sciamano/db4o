/* Copyright (C) 2006   db4objects Inc.   http://www.db4o.com */

package com.db4o.events;

import com.db4o.*;
import com.db4o.foundation.*;
import com.db4o.internal.*;
import com.db4o.internal.callbacks.*;
import com.db4o.internal.cs.ClientObjectContainer;
import com.db4o.internal.events.*;

/**
 * Provides an interface for getting an {@link EventRegistry} from an {@link ObjectContainer}. 
 */
public class EventRegistryFactory {
	
	/**
	 * Returns an {@link EventRegistry} for registering events with the specified container.
	 */
	public static EventRegistry forObjectContainer(ObjectContainer objectContainer) {
		if (null == objectContainer) {
			throw new ArgumentNullException();
		}
		
		InternalObjectContainer container = ((InternalObjectContainer)objectContainer);
		Callbacks callbacks = container.callbacks();
		if (callbacks instanceof EventRegistry) {
			return (EventRegistry)callbacks;
		}		
		if (callbacks instanceof NullCallbacks) {
			EventRegistryImpl impl = newEventRegistryFor(container);
			container.callbacks(impl);
			return impl;
		}
		
		// TODO: create a MulticastingCallbacks and register both
		// the current one and the new one
		throw new IllegalArgumentException();
	}

	private static EventRegistryImpl newEventRegistryFor(InternalObjectContainer container) {
		return (container instanceof ClientObjectContainer)	? 
							new ClientEventRegistryImpl(container) :
							new EventRegistryImpl(container);
	}
}
