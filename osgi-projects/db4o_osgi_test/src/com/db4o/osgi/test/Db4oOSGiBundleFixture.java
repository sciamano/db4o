/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.osgi.test;

import java.io.*;

import org.osgi.framework.*;

import com.db4o.*;
import com.db4o.config.*;
import com.db4o.foundation.*;
import com.db4o.osgi.*;

import db4ounit.extensions.fixtures.*;

class Db4oOSGiBundleFixture extends AbstractSoloDb4oFixture {

	private final BundleContext _context;
	private final String _fileName;
	
	public Db4oOSGiBundleFixture(BundleContext context, String fileName) {
		super(new IndependentConfigurationSource());
		_context = context;
		_fileName = fileName;
	}

	protected ObjectContainer createDatabase(Configuration config) {
		// hack around sticky stream reference, should actually be fixed in config API
		if(config instanceof DeepClone) {
			config = (Configuration) ((DeepClone)config).deepClone(null);
		}
	    ServiceReference sRef = _context.getServiceReference(Db4oService.class.getName());
	    Db4oService dbs = (Db4oService)_context.getService(sRef);
	    return dbs.openFile(config,_fileName);
	}

	protected void doClean() {
		new File(_fileName).delete();
	}

	public void defragment() throws Exception {
		defragment(_fileName);
	}

	public String getLabel() {
		return "OSGi/bundle";
	}

	public boolean accept(Class clazz) {
		return super.accept(clazz)&&(!(OptOutNoFileSystemData.class.isAssignableFrom(clazz)));
	}
}
