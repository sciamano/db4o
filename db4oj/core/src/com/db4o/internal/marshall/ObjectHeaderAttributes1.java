/* Copyright (C) 2004 - 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal.marshall;

import com.db4o.foundation.*;
import com.db4o.internal.*;

/**
 * @exclude
 */
public class ObjectHeaderAttributes1 extends ObjectHeaderAttributes{
    
    private static final byte VERSION = (byte)1;
    
    private final int _fieldCount;
    
    private final BitMap4 _nullBitMap;
    
    private int _baseLength;
    
    private int _payLoadLength;
    
    
    public ObjectHeaderAttributes1(ObjectReference ref) {
        _fieldCount = ref.classMetadata().fieldCount();
        _nullBitMap = new BitMap4(_fieldCount);
        calculateLengths(ref);
    }
    
    public ObjectHeaderAttributes1(Buffer reader){
        _fieldCount = reader.readInt();
        _nullBitMap = reader.readBitMap(_fieldCount);
    }
    
    public void addBaseLength(int length){
        _baseLength += length;
    }
    
    public void addPayLoadLength(int length){
        _payLoadLength += length;
    }
    
    private void calculateLengths(ObjectReference ref) {
        _baseLength = headerLength() + nullBitMapLength();
        _payLoadLength = 0;
        ClassMetadata yc = ref.classMetadata();
        Transaction trans = ref.transaction();
        Object obj = ref.getObject();
        calculateLengths(trans, yc, obj, 0);
        _baseLength = ref.container().blockAlignedBytes(_baseLength);        
    }
    
    private void calculateLengths(Transaction trans, ClassMetadata yc, Object obj, int fieldIndex) {
        _baseLength += Const4.INT_LENGTH;  // the int for the number of fields
        if (yc.i_fields != null) {
            for (int i = 0; i < yc.i_fields.length; i++) {
                FieldMetadata yf = yc.i_fields[i];
                Object child = yf.getOrCreate(trans, obj);
                if( child == null && yf.canUseNullBitmap()){
                    _nullBitMap.setTrue(fieldIndex);
                }else{
                    yf.calculateLengths(trans, this, child);
                }
                fieldIndex ++;
            }
        }
        if (yc.i_ancestor == null) {
            return;
        }
        calculateLengths(trans, yc.i_ancestor, obj, fieldIndex);
    }

    private int headerLength(){
        return Const4.OBJECT_LENGTH 
            + Const4.ID_LENGTH  // YapClass ID 
            + 1; // Marshaller Version 
    }
    
    public boolean isNull(int fieldIndex){
        return _nullBitMap.isTrue(fieldIndex);
    }
    
    private int nullBitMapLength(){
        return Const4.INT_LENGTH + _nullBitMap.marshalledLength();
    }

    public int objectLength(){
        return _baseLength + _payLoadLength;
    }
    
    public void prepareIndexedPayLoadEntry(Transaction trans){
        _payLoadLength =  trans.container().blockAlignedBytes(_payLoadLength);
    }
    
    public void write(StatefulBuffer writer){
        writer.writeByte(VERSION);
        writer.writeInt(_fieldCount);
        writer.writeBitMap(_nullBitMap);
        writer._payloadOffset = _baseLength;
    }

}
