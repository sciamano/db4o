/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using System;
using System.Text;
using Db4objects.Db4o.Foundation;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Delete;
using Db4objects.Db4o.Internal.Handlers;
using Db4objects.Db4o.Marshall;
using Db4objects.Db4o.Reflect;
using Db4objects.Db4o.Typehandlers;

namespace Db4objects.Db4o.Internal.Handlers
{
	public abstract class StringBasedValueTypeHandlerBase : IValueTypeHandler, IBuiltinTypeHandler
		, IVariableLengthTypeHandler, IQueryableTypeHandler
	{
		public readonly Type _clazz;

		private IReflectClass _classReflector;

		public StringBasedValueTypeHandlerBase(Type clazz)
		{
			_clazz = clazz;
		}

		public virtual void Defragment(IDefragmentContext context)
		{
			StringHandler(context).Defragment(context);
		}

		/// <exception cref="Db4objects.Db4o.Ext.Db4oIOException"></exception>
		public virtual void Delete(IDeleteContext context)
		{
			StringHandler(context).Delete(context);
		}

		public virtual object Read(IReadContext context)
		{
			object read = StringHandler(context).Read(context);
			if (null == read)
			{
				return null;
			}
			string str = (string)read;
			return ConvertString(str);
		}

		protected abstract object ConvertString(string str);

		public virtual void Write(IWriteContext context, object obj)
		{
			StringHandler(context).Write(context, ConvertObject((object)obj));
		}

		protected abstract string ConvertObject(object obj);

		private Db4objects.Db4o.Internal.Handlers.StringHandler StringHandler(IContext context
			)
		{
			return Handlers(context)._stringHandler;
		}

		private HandlerRegistry Handlers(IContext context)
		{
			return ((IInternalObjectContainer)context.ObjectContainer()).Handlers();
		}

		public virtual IPreparedComparison PrepareComparison(IContext context, object obj
			)
		{
			return StringHandler(context).PrepareComparison(context, obj);
		}

		public virtual IReflectClass ClassReflector()
		{
			return _classReflector;
		}

		public virtual void RegisterReflector(IReflector reflector)
		{
			_classReflector = reflector.ForClass(typeof(StringBuilder));
		}

		public virtual bool IsSimple()
		{
			return true;
		}

		public virtual bool DescendsIntoMembers()
		{
			return false;
		}

		public virtual bool CanHold(IReflectClass type)
		{
			return type.Equals(ClassReflector());
		}
	}
}