/* Copyright (C) 2008   db4objects Inc.   http://www.db4o.com */

package com.db4o.db4ounit.jre12.defragment;

import java.io.*;
import java.lang.reflect.*;

import com.db4o.*;
import com.db4o.config.*;
import com.db4o.defragment.*;
import com.db4o.foundation.io.*;
import com.db4o.internal.*;
import com.db4o.test.util.*;

import db4ounit.*;

/**
 * @decaf.ignore.jdk11
 * @sharpen.ignore
 */
public class DefragUnknownClassTestCase implements TestLifeCycle {

	public static class Unknown {
	}
	
	public static class ClassHolder {
		public Class _clazz;
		public Unknown _unknown;

		public ClassHolder(Class clazz, Unknown unknown) {
			_clazz = clazz;
			_unknown = unknown;
		}
	}

	private static final String FILENAME = Path4.getTempFileName();
	
	public void testUnknownClassDefrag() throws Exception {
		store();
		defragment();
		assertRetrieveClass();
	}

	private void defragment() throws Exception {
		ClassLoader loader = new ExcludingClassLoader(getClass().getClassLoader(), new Class[]{ Unknown.class });
		Class starterClazz = loader.loadClass(DefragStarter.class.getName());
		Method defragMethod = starterClazz.getDeclaredMethod("defrag", new Class[]{ String.class });
		defragMethod.invoke(null, new Object[]{ FILENAME });
	}

	public static class DefragStarter {
		public static void defrag(String fileName) throws IOException {
			DefragmentConfig defragConfig = new DefragmentConfig(fileName);
			defragConfig.db4oConfig(config());
			defragConfig.forceBackupDelete(true);
			defragConfig.readOnly(false);
			Defragment.defrag(defragConfig);
		}
	}
	
	private void store() {
		ObjectContainer db = openDatabase();
		db.store(new ClassHolder(Unknown.class, new Unknown()));
		db.close();
	}

	private void assertRetrieveClass() {
		ObjectContainer db = openDatabase();
		ObjectSet result = db.query(ClassHolder.class);
		Assert.areEqual(1, result.size());
		ClassHolder trans = (ClassHolder) result.next();
		Assert.areEqual(Unknown.class, trans._clazz);
		db.close();
	}

	private ObjectContainer openDatabase() {
		return Db4oEmbedded.openFile(config(), FILENAME);
	}
	
	public static Configuration config() {
		Configuration config = Db4oEmbedded.newConfiguration();
		config.reflectWith(Platform4.reflectorForType(ClassHolder.class));
		return config;
	}

	public void setUp() throws Exception {
		deleteDatabaseFile();
	}

	public void tearDown() throws Exception {
		deleteDatabaseFile();
	}

	private void deleteDatabaseFile() {
		new File(FILENAME).delete();
	}

}
