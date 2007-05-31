/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.config;

import java.io.*;

import com.db4o.ObjectContainer;
import com.db4o.internal.ReflectException;
import com.db4o.reflect.jdk.JdkReflector;

/**
 * @exclude
 * 
 * @sharpen.ignore
 */
public class TSerializable implements ObjectConstructor {

	public Object onStore(ObjectContainer con, Object object) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteStream);
			out.writeObject(object);
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new ReflectException(e);
		}
	}

	public void onActivate(ObjectContainer con, Object object, Object members) {
		// do nothing
	}

	public Object onInstantiate(final ObjectContainer con, Object storedObject) {
		try {
			ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream((byte[]) storedObject)) {
				protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
					return JdkReflector.toNative(con.ext().reflector().forName(v.getName()));
				}
			};
			Object in = inStream.readObject();
			return in;
		} catch (IOException e) {
			throw new ReflectException(e);
		} catch (ClassNotFoundException e) {
			throw new ReflectException(e);
		}
	}

	public Class storedClass() {
		return byte[].class;
	}
}
