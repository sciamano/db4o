/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package db4ounit.extensions;

import com.db4o.ObjectSet;
import com.db4o.ext.ExtObjectContainer;
import com.db4o.query.Query;

import db4ounit.Assert;

public class Db4oUtil {
	public static Object getOne(ExtObjectContainer oc, Object obj) {
		Query q = oc.query();
		q.constrain(classOf(obj));
		ObjectSet set = q.execute();
		Assert.areEqual(1, set.size());
		return set.next();
	}

	public static int occurrences(ExtObjectContainer oc, Class clazz) {
		Query q = oc.query();
		q.constrain(clazz);
		return q.execute().size();
	}

	public static void assertOccurrences(ExtObjectContainer oc, Class clazz,
			int expected) {
		Assert.areEqual(expected, occurrences(oc, clazz));
	}

	private static Class classOf(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Class) {
			return (Class) obj;
		}
		return obj.getClass();
	}
}
