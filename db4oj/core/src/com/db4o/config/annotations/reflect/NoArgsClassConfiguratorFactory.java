/* Copyright (C) 2004 - 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.config.annotations.reflect;

import java.lang.annotation.*;
import java.lang.reflect.*;


/**
 * @exclude
 * @decaf.ignore
 * @sharpen.ignore
 */
public class NoArgsClassConfiguratorFactory implements Db4oConfiguratorFactory {
	private Constructor _constructor;

	public NoArgsClassConfiguratorFactory(Class<?> configuratorClass) throws NoSuchMethodException {
		_constructor=configuratorClass.getConstructor(new Class[]{String.class});
	}

	public Db4oConfigurator configuratorFor(AnnotatedElement element, Annotation annotation) {
		try {
			if(!(element instanceof Class)) {
				return null;
			}
			Class clazz=(Class)element;
			String className=clazz.getName();
			return (Db4oConfigurator)_constructor.newInstance(new Object[]{className});
		} catch (Exception exc) {
			return null;
		}
	}
}