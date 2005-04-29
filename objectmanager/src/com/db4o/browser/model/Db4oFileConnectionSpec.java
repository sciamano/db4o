package com.db4o.browser.model;

import com.db4o.*;

public class Db4oFileConnectionSpec extends Db4oConnectionSpec {
	private String filePath;
	
	public Db4oFileConnectionSpec(String path,boolean readOnly,int depth, String[] classpath) {
		super(readOnly,depth, classpath);
		this.filePath=path;
	}

	public String path() {
		return filePath;
	}

	protected ObjectContainer connectInternal() {
		return Db4o.openFile(filePath);
	}
}
