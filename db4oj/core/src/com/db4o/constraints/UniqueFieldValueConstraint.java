/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.constraints;

import com.db4o.config.*;
import com.db4o.events.*;
import com.db4o.ext.*;
import com.db4o.foundation.*;
import com.db4o.internal.*;
import com.db4o.internal.btree.*;
import com.db4o.reflect.*;

/**
 * configures a field of a class to allow unique values only.
 */
public class UniqueFieldValueConstraint implements ConfigurationItem {
	
	private final Object _clazz;
	private final String _fieldName;
	
	/**
	 * constructor to create a UniqueFieldValueConstraint. 
	 * @param clazz can be a class (Java) / Type (.NET) / instance of the class / fully qualified class name
	 * @param fieldName the name of the field that is to be unique. 
	 */
	public UniqueFieldValueConstraint(Object clazz, String fieldName) {
		_clazz = clazz;
		_fieldName = fieldName;
	}
	
	/**
	 * internal method, public for implementation reasons.
	 */
	public void apply(final ObjectContainerBase objectContainer) {
		
		EventRegistryFactory.forObjectContainer(objectContainer).committing().addListener(
				new EventListener4() {

			private FieldMetadata _fieldMetaData;
			
			private void ensureSingleOccurence(Transaction trans, ObjectInfoCollection col){
				Iterator4 i = col.iterator();
				while(i.moveNext()){
					ObjectInfo info = (ObjectInfo) i.current();
					int id = (int)info.getInternalID();
					HardObjectReference ref = HardObjectReference.peekPersisted(trans, id, 1);
					Object fieldValue = fieldMetadata().getOn(trans, ref._object);
					if(fieldValue == null){
						continue;
					}
					BTreeRange range = fieldMetadata().search(trans, fieldValue);
					if(range.size() > 1){
						throw new UniqueFieldValueConstraintViolationException(classMetadata().getName(), fieldMetadata().getName()); 
					}
				}
			}
			
			private FieldMetadata fieldMetadata() {
				if(_fieldMetaData != null){
					return _fieldMetaData;
				}
				_fieldMetaData = classMetadata().fieldMetadataForName(_fieldName);
				return _fieldMetaData;
			}
			
			private ClassMetadata classMetadata() {
				ReflectClass reflectClass = ReflectorUtils.reflectClassFor(objectContainer.reflector(), _clazz);
				return objectContainer.classMetadataForReflectClass(reflectClass); 
			}
	
			public void onEvent(Event4 e, EventArgs args) {
				CommitEventArgs commitEventArgs = (CommitEventArgs) args;
				Transaction trans = (Transaction) commitEventArgs.transaction();
				ensureSingleOccurence(trans, commitEventArgs.added());
				ensureSingleOccurence(trans, commitEventArgs.updated());
			}
		});
	}
}
