/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com */

package com.db4o.drs.foundation;

public interface DrsUUID {
	
	// might become: 64-Bit id (db4o format)
	long getLongPart();
	
	byte[] getSignaturePart();

}
