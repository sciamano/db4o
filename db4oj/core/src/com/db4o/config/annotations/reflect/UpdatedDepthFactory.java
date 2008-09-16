/* Copyright (C) 2004 - 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.config.annotations.reflect;

import java.lang.annotation.*;
import java.lang.reflect.*;

import com.db4o.config.annotations.*;

/**
 * @exclude
 * @decaf.ignore
 * @sharpen.ignore
 */
public class UpdatedDepthFactory implements Db4oConfiguratorFactory {

	public Db4oConfigurator configuratorFor(AnnotatedElement element,
			Annotation annotation) {
		
		if (!annotation.annotationType().equals(UpdatedDepth.class)) {
			return null;
		}
		String className=null;
		
		if(element instanceof Class) {
			className=((Class)element).getName();
		}
		
		int updateDepth= ((UpdatedDepth) annotation).value();
		return new UpdatedDepthConfigurator(className, updateDepth);
	}

}