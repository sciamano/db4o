﻿/* Copyright (C) 2007   db4objects Inc.   http://www.db4o.com */
package com.db4o.instrumentation;

public interface ClassFilter {
	boolean accept(String className);
}
