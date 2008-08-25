﻿/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com */

using System.Collections;
using Db4objects.Db4o.Foundation;
using Db4objects.Db4o.Foundation.Collections;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Activation;
using Db4objects.Db4o.Internal.Delete;
using Db4objects.Db4o.Internal.Handlers;
using Db4objects.Db4o.Internal.Marshall;
using Db4objects.Db4o.Marshall;

namespace Db4objects.Db4o.Typehandlers
{
	/// <summary>TypeHandler for LinkedList class.<br /><br /></summary>
	public class GenericCollectionTypeHandler : IFirstClassHandler, ICanHoldAnythingHandler, IVariableLengthTypeHandler
	{
		public virtual IPreparedComparison PrepareComparison(IContext context, object obj)
		{
			return null;
		}

		public virtual void Write(IWriteContext context, object obj)
		{
			ICollection collection = (ICollection)obj;
			WriteElementCount(context, collection);
			WriteElements(context, collection);
		}

		public virtual object Read(IReadContext context)
		{
			object collection = ((UnmarshallingContext)context).PersistentObject();
			ICollectionInitializer initializer = CollectionInitializer.For(collection);
			//initializer.Clear();

			int elementCount = context.ReadInt();
			ITypeHandler4 elementHandler = ElementTypeHandler(context);

			for (int i = 0; i < elementCount; i++)
			{
				initializer.Add(context.ReadObject(elementHandler));
			}

			return collection;
		}

		private static void WriteElementCount(IWriteBuffer context, ICollection collection)
		{
			context.WriteInt(collection.Count);
		}

		private static void WriteElements(IWriteContext context, IEnumerable collection)
		{
			ITypeHandler4 elementHandler = ElementTypeHandler(context);
			IEnumerator elements = collection.GetEnumerator();
			while (elements.MoveNext())
			{
				context.WriteObject(elementHandler, elements.Current);
			}
		}

		private static ObjectContainerBase Container(IContext context)
		{
			return ((IInternalObjectContainer)context.ObjectContainer()).Container();
		}

		private static ITypeHandler4 ElementTypeHandler(IContext context)
		{
			return Container(context).Handlers().UntypedObjectHandler();
		}

		public virtual void Delete(IDeleteContext context)
		{
			if (!context.CascadeDelete()) return;

			ITypeHandler4 handler = ElementTypeHandler(context);
			int elementCount = context.ReadInt();
			for (int i = elementCount; i > 0; i--)
			{
				handler.Delete(context);
			}
		}

		public virtual void Defragment(IDefragmentContext context)
		{
			ITypeHandler4 handler = ElementTypeHandler(context);
			int elementCount = context.ReadInt();
			for (int i = 0; i < elementCount; i++)
			{
				handler.Defragment(context);
			}
		}

		public void CascadeActivation(ActivationContext4 context)
		{
			ICollection collection = ((ICollection)context.TargetObject());
			foreach(object item in collection)
			{
				context.CascadeActivationToChild(item);
			}
		}

		public virtual ITypeHandler4 ReadCandidateHandler(QueryingReadContext context)
		{
			return this;
		}

		private static ITypeHandler4 UntypedObjectHandlerFrom(IContext context)
		{
			return context.Transaction().Container().Handlers().UntypedObjectHandler();
		}

		public virtual void CollectIDs(QueryingReadContext context)
		{
			int elementCount = context.ReadInt();
			ITypeHandler4 elementHandler = UntypedObjectHandlerFrom(context);
			for (int i = 0; i < elementCount; i++)
			{
				context.ReadId(elementHandler);
			}
		}
	}
}