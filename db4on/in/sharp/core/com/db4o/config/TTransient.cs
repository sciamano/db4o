/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

using System;
using j4o.lang;
using com.db4o.config;

namespace com.db4o
{

    /// <exclude />
    public class TTransient : ObjectConstructor
    {
        public void OnActivate(ObjectContainer objectContainer, object obj, object members)
        {
        }

        public object OnStore(ObjectContainer objectContainer, object obj)
        {
            return null;
        }

        public Class StoredClass()
        {
            return Class.GetClassForType(typeof(object));
        }

        public object OnInstantiate(ObjectContainer objectContainer, object storedObject)
        {
            return null;
        }

    }
}
