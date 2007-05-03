/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal.classindex;

import java.io.IOException;

import com.db4o.Deploy;
import com.db4o.foundation.Debug4;
import com.db4o.foundation.Tree;
import com.db4o.internal.Buffer;
import com.db4o.internal.ClassIndexException;
import com.db4o.internal.ClassMetadata;
import com.db4o.internal.Const4;
import com.db4o.internal.Exceptions4;
import com.db4o.internal.LocalTransaction;
import com.db4o.internal.ObjectContainerBase;
import com.db4o.internal.PersistentBase;
import com.db4o.internal.ReadWriteable;
import com.db4o.internal.Transaction;
import com.db4o.internal.TreeInt;
import com.db4o.internal.TreeReader;
import com.db4o.internal.slots.Slot;

/**
 * representation to collect and hold all IDs of one class
 */
 public class ClassIndex extends PersistentBase implements ReadWriteable {
     
     
    private final ClassMetadata _clazz;
     
	/**
	 * contains TreeInt with object IDs 
	 */
	private TreeInt i_root;
    
    ClassIndex(ClassMetadata yapClass){
        _clazz = yapClass;
    }
	
	public void add(int a_id){
		i_root = TreeInt.add(i_root, a_id);
	}

    public final int marshalledLength() {
    	return Const4.INT_LENGTH * (Tree.size(i_root) + 1);
    }

    public final void clear() {
        i_root = null;
    }
    
    void ensureActive(Transaction trans){
        if (!isActive()) {
            setStateDirty();
            read(trans);
        }
    }

    int entryCount(Transaction ta){
        if(isActive() || isNew()){
            return Tree.size(i_root);
        }
        Slot slot = ((LocalTransaction)ta).getCurrentSlotOfID(getID());
        int length = Const4.INT_LENGTH;
        if(Deploy.debug){
            length += Const4.LEADING_LENGTH;
        }
        Buffer reader = new Buffer(length);
        try {
			reader.readEncrypt(ta.stream(), slot.address());
		} catch (IOException exc) {
			throw new ClassIndexException(exc, _clazz.getName());
		}
        if (Deploy.debug) {
            reader.readBegin(getIdentifier());
        }
        return reader.readInt();
    }
    
    public final byte getIdentifier() {
        return Const4.YAPINDEX;
    }
    
    TreeInt getRoot(){
        return i_root;
    }
    
    public final int ownLength() {
        return Const4.OBJECT_LENGTH + marshalledLength();
    }

    public final Object read(Buffer a_reader) {
    	throw Exceptions4.virtualException();
    }

    public final void readThis(Transaction a_trans, Buffer a_reader) {
    	i_root = (TreeInt)new TreeReader(a_reader, new TreeInt(0)).read();
    }

	public void remove(int a_id){
		i_root = TreeInt.removeLike(i_root, a_id);
	}

    void setDirty(ObjectContainerBase a_stream) {
    	// TODO: get rid of the setDirty call
        a_stream.setDirtyInSystemTransaction(this);
    }

    public void write(Buffer a_writer) {
        writeThis(null, a_writer);
    }

    public final void writeThis(Transaction trans, final Buffer a_writer) {
    	TreeInt.write(a_writer, i_root);
    }
    
    public String toString(){
        if(! Debug4.prettyToStrings){
            return super.toString();
        }
        return _clazz + " index";  
    }
}