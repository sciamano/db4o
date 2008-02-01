/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Fieldhandlers;
using Db4objects.Db4o.Reflect;

namespace Db4objects.Db4o.Internal
{
	/// <exclude></exclude>
	internal class ObjectAnalyzer
	{
		private readonly PartialObjectContainer _container;

		private readonly object _obj;

		private Db4objects.Db4o.Internal.ClassMetadata _classMetadata;

		private Db4objects.Db4o.Internal.ObjectReference _ref;

		private bool _notStorable;

		internal ObjectAnalyzer(PartialObjectContainer container, object obj)
		{
			_container = container;
			_obj = obj;
		}

		internal virtual void Analyze(Transaction trans)
		{
			_ref = trans.ReferenceForObject(_obj);
			if (_ref == null)
			{
				IReflectClass claxx = _container.Reflector().ForObject(_obj);
				if (claxx == null)
				{
					NotStorable(_obj, claxx);
					return;
				}
				if (!DetectClassMetadata(trans, claxx))
				{
					return;
				}
			}
			else
			{
				_classMetadata = _ref.ClassMetadata();
			}
			if (IsPlainObjectOrPrimitive(_classMetadata))
			{
				NotStorable(_obj, _classMetadata.ClassReflector());
			}
		}

		private bool DetectClassMetadata(Transaction trans, IReflectClass claxx)
		{
			_classMetadata = _container.GetActiveClassMetadata(claxx);
			if (_classMetadata == null)
			{
				IFieldHandler fieldHandler = _container.FieldHandlerForClass(claxx);
				if (fieldHandler is ISecondClassTypeHandler)
				{
					NotStorable(_obj, claxx);
				}
				_classMetadata = _container.ProduceClassMetadata(claxx);
				if (_classMetadata == null)
				{
					NotStorable(_obj, claxx);
					return false;
				}
				// The following may return a reference if the object is held
				// in a static variable somewhere ( often: Enums) that gets
				// stored or associated on initialization of the ClassMetadata.
				_ref = trans.ReferenceForObject(_obj);
			}
			return true;
		}

		private void NotStorable(object obj, IReflectClass claxx)
		{
			_container.NotStorable(claxx, obj);
			_notStorable = true;
		}

		internal virtual bool NotStorable()
		{
			return _notStorable;
		}

		private bool IsPlainObjectOrPrimitive(Db4objects.Db4o.Internal.ClassMetadata classMetadata
			)
		{
			return classMetadata.GetID() == Handlers4.UntypedId || classMetadata.IsPrimitive(
				);
		}

		internal virtual Db4objects.Db4o.Internal.ObjectReference ObjectReference()
		{
			return _ref;
		}

		public virtual Db4objects.Db4o.Internal.ClassMetadata ClassMetadata()
		{
			return _classMetadata;
		}
	}
}
