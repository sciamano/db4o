/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.db4ounit.common.internal;

import com.db4o.internal.*;

import db4ounit.*;


public class MarshallingBufferTestCase implements TestCase {
    
    private static final int DATA_1 = 777;  
    private static final byte DATA_2 = (byte)5; 
    
    public void testWrite(){
        MarshallingBuffer buffer = new MarshallingBuffer();
        
        buffer.writeInt(DATA_1);
        buffer.writeByte(DATA_2);
        
        Buffer content = inspectContent(buffer);
        
        Assert.areEqual(DATA_1, content.readInt());
        Assert.areEqual(DATA_2, content.readByte());
    }

    public void testTransferLastWrite(){
        
        MarshallingBuffer buffer = new MarshallingBuffer();
        
        buffer.writeInt(DATA_1);
        int lastOffset = offset(buffer);
        buffer.writeByte(DATA_2);
        
        MarshallingBuffer other = new MarshallingBuffer();
        
        buffer.transferLastWriteTo(other);
        
        Assert.areEqual(lastOffset, offset(buffer));
        
        Buffer content = inspectContent(other);
        Assert.areEqual(DATA_2, content.readByte());
    }

    private int offset(MarshallingBuffer buffer) {
        return buffer.testDelegate().offset();
    }
    
    private Buffer inspectContent(MarshallingBuffer buffer) {
        Buffer bufferDelegate = buffer.testDelegate();
        bufferDelegate.offset(0);
        return bufferDelegate;
    }
    
    
    

}
