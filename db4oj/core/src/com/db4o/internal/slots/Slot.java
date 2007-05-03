/* Copyright (C) 2004 - 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal.slots;

import com.db4o.internal.*;

/**
 * @exclude
 */
public class Slot {
    
    private final int _address;
    
    private int _length;

    public Slot(int address, int length){
        _address = address;
        _length = length;
    }
    
    public int address() {
        return _address;
    }

	public int length() {
		return _length;
	}

    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(! (obj instanceof Slot)){
            return false;
        }
        Slot other = (Slot) obj;
        return (_address == other._address) && (length() == other.length());
    }
    
    public int hashCode() {
        return _address ^ length();
    }
    
	public Slot subSlot(int offset) {
		return new Slot(_address + offset, length() - offset);
	}

    public String toString() {
    	return "[A:"+_address+",L:"+length()+"]";
    }
    
	public void truncate(int requiredLength) {
		_length = requiredLength;
	}
    
    public static int MARSHALLED_LENGTH = Const4.INT_LENGTH * 2;

	public int compareByAddress(Slot slot) {
		return slot._address - _address;
	}
	
	public int compareByLength(Slot slot) {
		int res = slot.length() - length();
		if(res != 0){
			return res;
		}
		return compareByAddress(slot);
	}

	public boolean isDirectlyPreceding(Slot other) {
		return _address + length() == other._address;
	}

	public Slot append(Slot slot) {
		_length += slot.length();
		return this;
	}

	
}
