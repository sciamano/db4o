/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal;

import com.db4o.CorruptionException;
import com.db4o.foundation.Visitor4;
import com.db4o.internal.marshall.*;
import com.db4o.internal.query.processor.QConObject;
import com.db4o.internal.replication.*;
import com.db4o.internal.slots.Slot;
import com.db4o.marshall.*;
import com.db4o.reflect.*;


/**
 * TODO: refactor for symmetric inheritance - don't inherit from YapField and override,
 * instead extract an abstract superclass from YapField and let both YapField and this class implement
 * 
 * @exclude
 */
public abstract class VirtualFieldMetadata extends FieldMetadata {
    
    private static final Object ANY_OBJECT = new Object();
    
    private final ReflectClass _classReflector;

    VirtualFieldMetadata(int handlerID, BuiltinTypeHandler handler) {
        super(handlerID, handler);
        _classReflector = handler.classReflector();
    }
    
    public abstract void addFieldIndex(MarshallerFamily mf, ClassMetadata yapClass, StatefulBuffer a_writer, Slot oldSlot) throws FieldIndexException;
    
    public boolean alive() {
        return true;
    }
    
    boolean canAddToQuery(String fieldName){
        return fieldName.equals(getName()); 
    }
    
    public boolean canUseNullBitmap(){
        return false;
    }
    
    public ReflectClass classReflector(){
        return _classReflector;
    }
    
    void collectConstraints(Transaction a_trans, QConObject a_parent,
        Object a_template, Visitor4 a_visitor) {
        
        // QBE constraint collection call
        // There isn't anything useful to do here, since virtual fields
        // are not on the actual object.
        
    }
    
    void deactivate(Transaction a_trans, Object a_onObject, int a_depth) {
        // do nothing
    }
    
    public abstract void delete(MarshallerFamily mf, StatefulBuffer a_bytes, boolean isUpdate);
    
    public Object getOrCreate(Transaction a_trans, Object a_OnObject) {
        // This is the first part of marshalling
        // Virtual fields do it all in #marshall(), the object is never used.
        // Returning any object here prevents triggering null handling.
        return ANY_OBJECT;
    }
    
    public boolean needsArrayAndPrimitiveInfo(){
        return false;
    }

    public boolean needsHandlerId(){
        return false;
    }

    public void instantiate(MarshallerFamily mf, ObjectReference a_yapObject, Object a_onObject, StatefulBuffer a_bytes)
        throws CorruptionException {
    	a_yapObject.produceVirtualAttributes();
        instantiate1(a_bytes.getTransaction(), a_yapObject, a_bytes);
    }
    
    public void instantiate(UnmarshallingContext context) {
        context.reference().produceVirtualAttributes();
        instantiate1(context.transaction(), context.reference(), context.buffer());
    }

    abstract void instantiate1(Transaction a_trans, ObjectReference a_yapObject, Buffer a_bytes);
    
    public void loadHandler(ObjectContainerBase a_stream){
    	// do nothing
    }
    
    public void marshall(MarshallingContext context, Object obj){
        context.doNotIndirectWrites();
        marshall(context.transaction(), context.reference(), context, context.isNew());
    }

    private final void marshall(
            Transaction trans,
            ObjectReference ref, 
            WriteBuffer buffer,
            boolean isNew) {
        
        if(! trans.supportsVirtualFields()){
            marshallIgnore(buffer);
            return;
        }
        
        ObjectContainerBase stream = trans.container();
        HandlerRegistry handlers = stream._handlers;
        boolean migrating = false;
        
        
        if (stream._replicationCallState != Const4.NONE) {
            if (stream._replicationCallState == Const4.OLD) {
                
                // old replication code 

                migrating = true;
                if (ref.virtualAttributes() == null) {
                    Object obj = ref.getObject();
                    ObjectReference migratingRef = null;
                    MigrationConnection mgc = handlers.i_migration;
                    if(mgc != null){
                        migratingRef = mgc.referenceFor(obj);
                        if(migratingRef == null){
                            ObjectContainerBase peer = mgc.peer(stream);
                            migratingRef = peer.transaction().referenceForObject(obj);
                        }
                    }
                    if (migratingRef != null){
                    	VirtualAttributes migrateAttributes = migratingRef.virtualAttributes();
                    	if(migrateAttributes != null && migrateAttributes.i_database != null){
	                        migrating = true;
	                        ref.setVirtualAttributes((VirtualAttributes)migrateAttributes.shallowClone());
                            migrateAttributes.i_database.bind(trans);
                    	}
                    }
                }
            }else {
                
                // new dRS replication
                
                Db4oReplicationReferenceProvider provider = handlers._replicationReferenceProvider;
                Object parentObject = ref.getObject();
                Db4oReplicationReference replicationReference = provider.referenceFor(parentObject); 
                if(replicationReference != null){
                    migrating = true;
                    VirtualAttributes va = ref.produceVirtualAttributes();
                    va.i_version = replicationReference.version();
                    va.i_uuid = replicationReference.longPart();
                    va.i_database = replicationReference.signaturePart();
                }
            }
        }
        
        if (ref.virtualAttributes() == null) {
        	ref.produceVirtualAttributes();
            migrating = false;
        }
	    marshall(trans, ref, buffer, migrating, isNew);
    }
    
    abstract void marshall(Transaction trans, ObjectReference ref, WriteBuffer buffer, boolean migrating, boolean isNew);
    
    abstract void marshallIgnore(WriteBuffer writer);
    
    public void readVirtualAttribute(Transaction trans, Buffer buffer, ObjectReference ref) {
        if(! trans.supportsVirtualFields()){
            incrementOffset(buffer);
            return;
        }
        instantiate1(trans, ref, buffer);
    }
    
    public boolean isVirtual() {
        return true;
    }

    protected Object indexEntryFor(Object indexEntry) {
    	return indexEntry;
    }
    
    protected Indexable4 indexHandler(ObjectContainerBase stream) {
    	return (Indexable4)_handler;
    }
}