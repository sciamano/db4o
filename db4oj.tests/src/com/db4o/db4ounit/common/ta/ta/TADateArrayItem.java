/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.db4ounit.common.ta.ta;

import java.util.*;

import com.db4o.db4ounit.common.ta.*;

public class TADateArrayItem extends ActivatableImpl {

    public Date[] _typed;

    public Object[] _untyped;

    public Date[] getTyped() {
        activate();
        return _typed;
    }

    public Object[] getUntyped() {
        activate();
        return _untyped;
    }
}
