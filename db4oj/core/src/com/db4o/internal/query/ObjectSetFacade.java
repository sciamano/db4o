/* Copyright (C) 2004 - 2005  Versant Inc.  http://www.db4o.com */

package com.db4o.internal.query;

import java.util.*;

import com.db4o.ext.*;
import com.db4o.foundation.*;
import com.db4o.internal.query.result.*;
import com.db4o.query.*;

/**
 * @exclude
 * @sharpen.ignore 
 */
@decaf.IgnoreExtends(decaf.Platform.JDK11)
public class ObjectSetFacade extends AbstractList implements ExtObjectSet {
    
    public final StatefulQueryResult _delegate;
    
    public ObjectSetFacade(QueryResult queryResult){
        _delegate = new StatefulQueryResult(queryResult);
    }
    
	public void sort(QueryComparator cmp) {
		_delegate.sort(cmp);
	}
	
	public void skip(int count) {
		_delegate.skip(count);
	}
	
	@decaf.ReplaceFirst(value="return _delegate.iterator();", platform=decaf.Platform.JDK11)
    public Iterator iterator() {
    	return new JDKIterator();
    }
	
	@decaf.Ignore(decaf.Platform.JDK11)
	class JDKIterator extends Iterable4Adaptor implements Iterator {
		public JDKIterator() {
			super(_delegate);
		}
		
		protected boolean moveNext() {
			synchronized (_delegate.lock()) {
				return super.moveNext();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
    
    public long[] getIDs() {
        return _delegate.getIDs();
    }

    public ExtObjectSet ext() {
        return this;
    }

    public boolean hasNext() {
        return _delegate.hasNext();
    }

    public Object next() {
        return _delegate.next();
    }

    public void reset() {
        _delegate.reset();
    }

    public int size() {
        return _delegate.size();
    }
    
    /**
     */
    @decaf.Ignore(decaf.Platform.JDK11)
    public boolean contains(Object obj) {
        return indexOf(obj) >= 0;
    }

    public Object get(int index) {
        return _delegate.get(index);
    }

    /**
     */
    @decaf.Ignore(decaf.Platform.JDK11)
    public int indexOf(Object obj) {
    	return _delegate.indexOf(obj);
    }
    
    /**
     */
    @decaf.Ignore(decaf.Platform.JDK11)
    public int lastIndexOf(Object obj) {
        return indexOf(obj);
    }
    
    /**
     */
    @decaf.Ignore(decaf.Platform.JDK11)
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
