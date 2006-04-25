namespace com.db4o
{
	/// <exclude></exclude>
	public class JDK
	{
		internal virtual j4o.lang.Thread addShutdownHook(j4o.lang.Runnable a_runnable)
		{
			return null;
		}

		internal virtual com.db4o.types.Db4oCollections collections(com.db4o.YapStream a_stream
			)
		{
			return null;
		}

		internal virtual j4o.lang.Class constructorClass()
		{
			return null;
		}

		internal virtual object createReferenceQueue()
		{
			return null;
		}

		public virtual object createWeakReference(object obj)
		{
			return obj;
		}

		internal virtual object createYapRef(object a_queue, com.db4o.YapObject a_yapObject
			, object a_object)
		{
			return null;
		}

		internal virtual object deserialize(byte[] bytes)
		{
			throw new com.db4o.ext.Db4oException(com.db4o.Messages.NOT_IMPLEMENTED);
		}

		internal virtual void forEachCollectionElement(object a_object, com.db4o.foundation.Visitor4
			 a_visitor)
		{
		}

		internal virtual string format(j4o.util.Date date, bool showTime)
		{
			return date.ToString();
		}

		internal virtual object getContextClassLoader()
		{
			return null;
		}

		internal virtual object getYapRefObject(object a_object)
		{
			return null;
		}

		internal virtual bool isCollectionTranslator(com.db4o.Config4Class a_config)
		{
			return false;
		}

		public virtual int ver()
		{
			return 1;
		}

		internal virtual void killYapRef(object obj)
		{
		}

		internal virtual void lockFile(object file)
		{
			lock (this)
			{
			}
		}

		/// <summary>
		/// use for system classes only, since not ClassLoader
		/// or Reflector-aware
		/// </summary>
		internal virtual bool methodIsAvailable(string className, string methodName, j4o.lang.Class[]
			 _params)
		{
			return false;
		}

		internal virtual void pollReferenceQueue(com.db4o.YapStream a_stream, object a_referenceQueue
			)
		{
		}

		public virtual void registerCollections(com.db4o.reflect.generic.GenericReflector
			 reflector)
		{
		}

		internal virtual void removeShutdownHook(j4o.lang.Thread a_thread)
		{
		}

		public virtual j4o.lang.reflect.Constructor serializableConstructor(j4o.lang.Class
			 clazz)
		{
			return null;
		}

		internal virtual byte[] serialize(object obj)
		{
			throw new com.db4o.ext.Db4oException(com.db4o.Messages.NOT_IMPLEMENTED);
		}

		internal virtual void setAccessible(object a_accessible)
		{
		}

		internal virtual bool isEnum(com.db4o.reflect.Reflector reflector, com.db4o.reflect.ReflectClass
			 clazz)
		{
			return false;
		}

		internal virtual void unlockFile(object file)
		{
			lock (this)
			{
			}
		}

		public virtual object weakReferenceTarget(object weakRef)
		{
			return weakRef;
		}

		public virtual com.db4o.reflect.Reflector createReflector(object classLoader)
		{
			return null;
		}
	}
}
