/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal.marshall;

import com.db4o.*;
import com.db4o.foundation.*;
import com.db4o.internal.*;
import com.db4o.marshall.*;


/**
 * @exclude
 */
public class MarshallingContext implements FieldListInfo, WriteContext {
    
    private static final byte VERSION = (byte)1;
    
    private final Transaction _transaction;
    
    private final ObjectReference _reference;
    
    private int _updateDepth;
    
    private final boolean _isNew;
    
    private final BitMap4 _nullBitMap;
    
    private final MarshallingBuffer _fixedLengthBuffer;
    
    private MarshallingBuffer _payLoadBuffer;
    
    private MarshallingBuffer _currentBuffer;
    
    private int _fieldWriteCount;
    

    public MarshallingContext(Transaction trans, ObjectReference ref, int updateDepth, boolean isNew) {
        _transaction = trans;
        _reference = ref;
        _nullBitMap = new BitMap4(fieldCount());
        _updateDepth = classMetadata().adjustUpdateDepth(trans, updateDepth);
        _isNew = isNew;
        _fixedLengthBuffer = new MarshallingBuffer();
        _currentBuffer = _fixedLengthBuffer;
    }

    private int fieldCount() {
        return classMetadata().fieldCount();
    }

    public ClassMetadata classMetadata() {
        return _reference.classMetadata();
    }

    public boolean isNew() {
        return _isNew;
    }

    public boolean isNull(int fieldIndex) {
        // TODO Auto-generated method stub
        return false;
    }

    public void isNull(int fieldIndex, boolean flag) {
        _nullBitMap.set(fieldIndex, flag);
    }

    public Transaction transaction() {
        return _transaction;
    }

    public StatefulBuffer ToWriteBuffer() {
        StatefulBuffer buffer = new StatefulBuffer(_transaction, marshalledLength());
        writeObjectClassID(buffer, classMetadata().getID());
        buffer.writeByte(VERSION);
        buffer.writeInt(fieldCount());
        buffer.writeBitMap(_nullBitMap);
        
        

        if (Deploy.debug) {
            buffer.writeEnd();
            buffer.debugCheckBytes();
        }
        return buffer;
    }

    private int marshalledLength() {
        int length = requiredLength(_fixedLengthBuffer);
        if(_payLoadBuffer != null){
            length += requiredLength(_payLoadBuffer);
        }
        return length;
    }

    private int requiredLength(MarshallingBuffer buffer) {
        return container().blockAlignedBytes(buffer.length());
    }
    
    private void writeObjectClassID(Buffer reader, int id) {
        reader.writeInt(-id);
    }

    public Object getObject() {
        return _reference.getObject();
    }

    public Config4Class classConfiguration() {
        return classMetadata().config();
    }

    public int updateDepth() {
        return _updateDepth;
    }

    public void updateDepth(int depth) {
        _updateDepth = depth;
    }

    public int objectID() {
        return _reference.getID();
    }

    public Object currentIndexEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    public ObjectContainerBase container() {
        return transaction().container();
    }

    public ObjectContainer objectContainer() {
        return transaction().objectContainer();
    }

	public void writeByte(byte b) {
	    prepareWrite();
	    _currentBuffer.writeByte(b);
	}

    public void writeInt(int i) {
        prepareWrite();
        _currentBuffer.writeInt(i);
    }
    
	private void prepareWrite() {
	    if(_currentBuffer == _payLoadBuffer){
	        return;
	    }
        if(! isFirstWriteToField()){
            usePayloadBuffer();
        }
        _fieldWriteCount++;
    }

    private void usePayloadBuffer() {
        throw new NotImplementedException();
    }

    private boolean isFirstWriteToField() {
        return _fieldWriteCount < 1;
    }
    
    public void nextField(){
        _fieldWriteCount = 0;
    }

}
