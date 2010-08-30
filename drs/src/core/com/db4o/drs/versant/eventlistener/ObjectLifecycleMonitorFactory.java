/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com */

package com.db4o.drs.versant.eventlistener;

import com.db4o.drs.foundation.*;
import com.db4o.drs.versant.*;
import com.versant.event.*;

public class ObjectLifecycleMonitorFactory {
	
	public static ObjectLifecycleMonitorImpl newInstance (EventConfiguration eventConfiguration,
			LinePrinter linePrinter) {
		VodCobra cobra = new VodCobra(new VodDatabase(eventConfiguration.databaseName));
		VodEventClient client = new VodEventClient(eventConfiguration, new ExceptionListener (){
	        public void exceptionOccurred (Throwable exception){
	        	ObjectLifecycleMonitorImpl.unrecoverableExceptionOccurred(exception);
	        }
	    });
		ObjectLifecycleMonitorImpl eventProcessor = new ObjectLifecycleMonitorImpl(client, linePrinter, cobra);
		return eventProcessor;
	}

}