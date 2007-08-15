/* Copyright (C) 2007   db4objects Inc.   http://www.db4o.com */
package com.db4o.db4ounit.common.handlers;

import com.db4o.internal.*;
import com.db4o.internal.handlers.StringHandler;
import com.db4o.internal.slots.Slot;
import com.db4o.marshall.*;

import db4ounit.Assert;

public class StringHandlerTestCase extends TypeHandlerTestCaseBase {
    
    public static void main(String[] arguments) {
        new StringHandlerTestCase().runSolo();
    }

	public void testIndexMarshalling() {
		Buffer reader=new Buffer(2*Const4.INT_LENGTH);
		final Slot original = new Slot(0xdb,0x40);
		stringHandler().writeIndexEntry(reader,original);
		reader._offset=0;
		Slot retrieved = (Slot) stringHandler().readIndexEntry(reader);
		Assert.areEqual(original.address(), retrieved.address());
		Assert.areEqual(original.length(), retrieved.length());
	}

    private StringHandler stringHandler() {
        return new StringHandler(stream(), stream().stringIO());
    }
	
	public void _testReadWrite(){
	    MockWriteContext context = new MockWriteContext(db());
	    stringHandler().write(context, "one");
	    stringHandler().read(context);
	    
	}
	
	
	
	
}
