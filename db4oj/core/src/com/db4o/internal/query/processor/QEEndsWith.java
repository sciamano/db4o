/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal.query.processor;


/**
 * @exclude
 */
public class QEEndsWith extends QEStringCmp
{
	public QEEndsWith(boolean caseSensitive_) {
		super(caseSensitive_);
	}

	protected boolean compareStrings(String candidate, String constraint) {
		return candidate.lastIndexOf(constraint)==candidate.length()-constraint.length();
	}
}
