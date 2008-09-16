/* Copyright (C) 2004 - 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.config.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * turns on storing static field values for this class. <br>
 * <br>
 * By default, static field values of classes are not stored to the database
 * file. By decoration a specific class with this switch, all non-simple-typed
 * static field values of this class are stored the first time an object of the
 * class is stored, and restored, every time a database file is opened
 * afterwards. <br>
 * <br>
 * This annotation will be ignored for simple types. <br>
 * <br>
 * Use {@code @PersistedStaticFieldValues } for constant static object members.
 * <br>
 * <br>
 * <br>
 * <br>
 * This option will slow down the process of opening database files and the
 * stored objects will occupy space in the database file.
 * @exclude
 * @decaf.ignore
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistedStaticFieldValues {
}