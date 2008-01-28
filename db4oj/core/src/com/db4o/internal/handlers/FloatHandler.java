/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o.internal.handlers;

import com.db4o.CorruptionException;
import com.db4o.foundation.*;
import com.db4o.internal.ByteArrayBuffer;
import com.db4o.internal.ObjectContainerBase;
import com.db4o.internal.StatefulBuffer;
import com.db4o.internal.marshall.MarshallerFamily;
import com.db4o.marshall.ReadContext;
import com.db4o.marshall.WriteContext;
import com.db4o.reflect.ReflectClass;



public class FloatHandler extends IntHandler {
    
    private static final Float i_primitive = new Float(0);
    
    public FloatHandler(ObjectContainerBase stream) {
        super(stream);
    }
    
    public Object coerce(ReflectClass claxx, Object obj) {
    	return Coercion4.toFloat(obj);
    }

	public Object defaultValue(){
		return i_primitive;
	}
	
	protected Class primitiveJavaClass() {
		return float.class;
	}

	public Object primitiveNull() {
		return i_primitive;
	}
	
	public Object read(MarshallerFamily mf, StatefulBuffer writer, boolean redirect) throws CorruptionException {
    	return mf._primitive.readFloat(writer);
    }

	Object read1(ByteArrayBuffer a_bytes) {
		return primitiveMarshaller().readFloat(a_bytes);
	}

	public void write(Object a_object, ByteArrayBuffer a_bytes) {
		writeInt(
			Float.floatToIntBits(((Float) a_object).floatValue()),
			a_bytes);
	}

    public Object read(ReadContext context) {
        return new Float(Float.intBitsToFloat(context.readInt()));
    }

    public void write(WriteContext context, Object obj) {
        context.writeInt(Float.floatToIntBits(((Float)obj).floatValue()));
    }
    
    public PreparedComparison internalPrepareComparison(Object source) {
    	final float sourceFloat = ((Float)source).floatValue();
    	return new PreparedComparison() {
			public int compareTo(Object target) {
				if(target == null){
					return 1;
				}
				float targetFloat = ((Float)target).floatValue();
				return sourceFloat == targetFloat ? 0 : (sourceFloat < targetFloat ? - 1 : 1); 
			}
		};
    }

}
