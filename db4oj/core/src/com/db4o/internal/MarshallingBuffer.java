/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal;

import com.db4o.foundation.*;
import com.db4o.marshall.*;

/**
 * @exclude
 */
public class MarshallingBuffer implements WriteBuffer{
    
    private static final int SIZE_NEEDED = Const4.LONG_LENGTH;
    
    private static final int LINK_LENGTH = Const4.INT_LENGTH + Const4.ID_LENGTH;
    
    private static final int NO_PARENT = -1;
    
    private Buffer _delegate;
    
    private int _lastOffSet;
    
    private int _addressInParent = NO_PARENT;
    
    private List4 _children;
    
    private List4 _indexEntries;

    public int length() {
        return offset();
    }
    
    public int offset(){
        if(_delegate == null){
            return 0;
        }
        return _delegate.offset();
    }
    
    public void writeByte(byte b) {
        prepareWrite();
        _delegate.writeByte(b);
    }
    
    public void writeBytes(byte[] bytes) {
        prepareWrite(bytes.length);
        _delegate.writeBytes(bytes);
    }

    public void writeInt(int i) {
        prepareWrite();
        _delegate.writeInt(i);
    }
    
    public void writeLong(long l) {
        prepareWrite();
        _delegate.writeLong(l);
    }
    
    private void prepareWrite(){
        prepareWrite(SIZE_NEEDED);
    }
    
    private void prepareWrite(int sizeNeeded){
        if(_delegate == null){
            _delegate = new Buffer(sizeNeeded); 
        }
        _lastOffSet = _delegate.offset();
        if(remainingSize() < sizeNeeded){
            resize(sizeNeeded);
        }
    }

    private int remainingSize() {
        return _delegate.length() - _delegate.offset();
    }

    private void resize(int sizeNeeded) {
        int newSize = _delegate.length() * 2;
        if(newSize - _lastOffSet < sizeNeeded){
            newSize += sizeNeeded;
        }
        Buffer temp = new Buffer(newSize);
        temp.offset(_lastOffSet);
        _delegate.copyTo(temp, 0, 0, _lastOffSet);
        _delegate = temp;
    }
    
    public void transferLastWriteTo(MarshallingBuffer other){
        other._addressInParent = _lastOffSet;
        int length = _delegate.offset() - _lastOffSet;
        other.prepareWrite(length);
        int otherOffset = other._delegate.offset();
        System.arraycopy(_delegate._buffer, _lastOffSet, other._delegate._buffer, otherOffset, length);
        _delegate.offset(_lastOffSet);
        other._delegate.offset(otherOffset + length);
        other._lastOffSet = otherOffset;
    }
    
    public void transferContentTo(Buffer buffer){
        System.arraycopy(_delegate._buffer, 0, buffer._buffer, buffer._offset, length());
        buffer._offset += length();
    }
    
    public Buffer testDelegate(){
        return _delegate;
    }

    public MarshallingBuffer addChild(boolean reserveLinkSpace) {
        MarshallingBuffer child = new MarshallingBuffer();
        child._addressInParent = offset();
        _children = new List4(_children, child);
        if(reserveLinkSpace){
            reserveChildLinkSpace();
        }
        return child;
    }

    public void reserveChildLinkSpace() {
        prepareWrite(LINK_LENGTH);
        _delegate.incrementOffset(LINK_LENGTH);
    }
    
    public void mergeChildren(int linkOffset) {
        mergeChildren(this, this, linkOffset);
    }

    private static void mergeChildren(MarshallingBuffer writeBuffer, MarshallingBuffer parentBuffer, int linkOffset) {
        if(parentBuffer._children == null){
            return;
        }
        Iterator4 i = new Iterator4Impl(parentBuffer._children);
        while(i.moveNext()){
            merge(writeBuffer, parentBuffer, (MarshallingBuffer) i.current(), linkOffset);
        }
    }
    
    private static void merge(MarshallingBuffer writeBuffer, MarshallingBuffer parentBuffer, MarshallingBuffer childBuffer, int linkOffset) {
        
        int childLength = childBuffer.length();
        int childPosition = writeBuffer.offset();
        writeBuffer.reserve(childLength);
        
        mergeChildren(writeBuffer, childBuffer, linkOffset);
        
        int savedWriteBufferOffset = writeBuffer.offset();
        writeBuffer.seek(childPosition);
        childBuffer.transferContentTo(writeBuffer._delegate);
        writeBuffer.seek(savedWriteBufferOffset);
        
        parentBuffer.writeLink(childBuffer, childPosition + linkOffset, childLength);
    }
    
    public void seek(int offset) {
        _delegate.offset(offset);
    }

    private void reserve(int length) {
        prepareWrite(length);
        _delegate.offset(_delegate.offset() + length );
    }

    private void writeLink(MarshallingBuffer child, int position, int length){
        int offset = offset();
        _delegate.offset(child._addressInParent);
        _delegate.writeInt(position);
        _delegate.writeInt(length);
        _delegate.offset(offset);
    }
    
    public void debugDecrementLastOffset(int count){
        _lastOffSet -= count;
    }
    
    public boolean hasParent(){
        return _addressInParent != NO_PARENT;
    }

    public void addIndexEntry(FieldMetadata fieldMetadata) {
        
        // TODO Auto-generated method stub
    }


}
