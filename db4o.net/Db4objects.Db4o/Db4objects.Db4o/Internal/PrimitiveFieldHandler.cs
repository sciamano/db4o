namespace Db4objects.Db4o.Internal
{
	/// <exclude></exclude>
	public class PrimitiveFieldHandler : Db4objects.Db4o.Internal.ClassMetadata
	{
		public readonly Db4objects.Db4o.Internal.ITypeHandler4 i_handler;

		internal PrimitiveFieldHandler(Db4objects.Db4o.Internal.ObjectContainerBase a_stream
			, Db4objects.Db4o.Internal.ITypeHandler4 a_handler) : base(a_stream, a_handler.ClassReflector
			())
		{
			i_fields = Db4objects.Db4o.Internal.FieldMetadata.EMPTY_ARRAY;
			i_handler = a_handler;
		}

		internal override void ActivateFields(Db4objects.Db4o.Internal.Transaction a_trans
			, object a_object, int a_depth)
		{
		}

		internal sealed override void AddToIndex(Db4objects.Db4o.Internal.LocalObjectContainer
			 a_stream, Db4objects.Db4o.Internal.Transaction a_trans, int a_id)
		{
		}

		internal override bool AllowsQueries()
		{
			return false;
		}

		internal override void CacheDirty(Db4objects.Db4o.Foundation.Collection4 col)
		{
		}

		public override bool CanHold(Db4objects.Db4o.Reflect.IReflectClass claxx)
		{
			return i_handler.CanHold(claxx);
		}

		public override Db4objects.Db4o.Reflect.IReflectClass ClassReflector()
		{
			return i_handler.ClassReflector();
		}

		public override void DeleteEmbedded(Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, Db4objects.Db4o.Internal.StatefulBuffer a_bytes)
		{
			if (mf._primitive.UseNormalClassRead())
			{
				base.DeleteEmbedded(mf, a_bytes);
				return;
			}
		}

		public override void DeleteEmbedded1(Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, Db4objects.Db4o.Internal.StatefulBuffer a_bytes, int a_id)
		{
			if (i_handler is Db4objects.Db4o.Internal.Handlers.ArrayHandler)
			{
				Db4objects.Db4o.Internal.Handlers.ArrayHandler ya = (Db4objects.Db4o.Internal.Handlers.ArrayHandler
					)i_handler;
				if (ya.i_isPrimitive)
				{
					ya.DeletePrimitiveEmbedded(a_bytes, this);
					a_bytes.SlotDelete();
					return;
				}
			}
			if (i_handler is Db4objects.Db4o.Internal.UntypedFieldHandler)
			{
				a_bytes.IncrementOffset(i_handler.LinkLength());
			}
			else
			{
				i_handler.DeleteEmbedded(mf, a_bytes);
			}
			Free(a_bytes, a_id);
		}

		internal override void DeleteMembers(Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, Db4objects.Db4o.Internal.Marshall.ObjectHeaderAttributes attributes, Db4objects.Db4o.Internal.StatefulBuffer
			 a_bytes, int a_type, bool isUpdate)
		{
			if (a_type == Db4objects.Db4o.Internal.Const4.TYPE_ARRAY)
			{
				new Db4objects.Db4o.Internal.Handlers.ArrayHandler(a_bytes.GetStream(), this, true
					).DeletePrimitiveEmbedded(a_bytes, this);
			}
			else
			{
				if (a_type == Db4objects.Db4o.Internal.Const4.TYPE_NARRAY)
				{
					new Db4objects.Db4o.Internal.Handlers.MultidimensionalArrayHandler(a_bytes.GetStream
						(), this, true).DeletePrimitiveEmbedded(a_bytes, this);
				}
			}
		}

		internal void Free(Db4objects.Db4o.Internal.Transaction a_trans, int a_id, int a_address
			, int a_length)
		{
			a_trans.SlotFreePointerOnCommit(a_id, a_address, a_length);
		}

		internal void Free(Db4objects.Db4o.Internal.StatefulBuffer a_bytes, int a_id)
		{
			a_bytes.GetTransaction().SlotFreePointerOnCommit(a_id, a_bytes.GetAddress(), a_bytes
				.GetLength());
		}

		public override bool HasIndex()
		{
			return false;
		}

		internal override object Instantiate(Db4objects.Db4o.Internal.ObjectReference a_yapObject
			, object a_object, Db4objects.Db4o.Internal.Marshall.MarshallerFamily mf, Db4objects.Db4o.Internal.Marshall.ObjectHeaderAttributes
			 attributes, Db4objects.Db4o.Internal.StatefulBuffer a_bytes, bool a_addToIDTree
			)
		{
			if (a_object == null)
			{
				try
				{
					a_object = i_handler.Read(mf, a_bytes, true);
				}
				catch (Db4objects.Db4o.CorruptionException)
				{
					return null;
				}
				catch (System.IO.IOException)
				{
					return null;
				}
				a_yapObject.SetObjectWeak(a_bytes.GetStream(), a_object);
			}
			a_yapObject.SetStateClean();
			return a_object;
		}

		internal override object InstantiateTransient(Db4objects.Db4o.Internal.ObjectReference
			 a_yapObject, object a_object, Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, Db4objects.Db4o.Internal.Marshall.ObjectHeaderAttributes attributes, Db4objects.Db4o.Internal.StatefulBuffer
			 a_bytes)
		{
			try
			{
				return i_handler.Read(mf, a_bytes, true);
			}
			catch (Db4objects.Db4o.CorruptionException)
			{
				return null;
			}
			catch (System.IO.IOException)
			{
				return null;
			}
		}

		internal override void InstantiateFields(Db4objects.Db4o.Internal.ObjectReference
			 a_yapObject, object a_onObject, Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, Db4objects.Db4o.Internal.Marshall.ObjectHeaderAttributes attributes, Db4objects.Db4o.Internal.StatefulBuffer
			 a_bytes)
		{
			object obj = null;
			try
			{
				obj = i_handler.Read(mf, a_bytes, true);
			}
			catch (Db4objects.Db4o.CorruptionException)
			{
			}
			catch (System.IO.IOException)
			{
			}
			if (obj != null)
			{
				i_handler.CopyValue(obj, a_onObject);
			}
		}

		public override bool IsArray()
		{
			return i_id == Db4objects.Db4o.Internal.HandlerRegistry.ANY_ARRAY_ID || i_id == Db4objects.Db4o.Internal.HandlerRegistry
				.ANY_ARRAY_N_ID;
		}

		public override bool IsPrimitive()
		{
			return true;
		}

		public override Db4objects.Db4o.Foundation.TernaryBool IsSecondClass()
		{
			return Db4objects.Db4o.Foundation.TernaryBool.UNSPECIFIED;
		}

		public override bool IsStrongTyped()
		{
			return false;
		}

		public override void CalculateLengths(Db4objects.Db4o.Internal.Transaction trans, 
			Db4objects.Db4o.Internal.Marshall.ObjectHeaderAttributes header, bool topLevel, 
			object obj, bool withIndirection)
		{
			i_handler.CalculateLengths(trans, header, topLevel, obj, withIndirection);
		}

		public override Db4objects.Db4o.Internal.IComparable4 PrepareComparison(object a_constraint
			)
		{
			i_handler.PrepareComparison(a_constraint);
			return i_handler;
		}

		public sealed override Db4objects.Db4o.Reflect.IReflectClass PrimitiveClassReflector
			()
		{
			return i_handler.PrimitiveClassReflector();
		}

		public override object Read(Db4objects.Db4o.Internal.Marshall.MarshallerFamily mf
			, Db4objects.Db4o.Internal.StatefulBuffer a_bytes, bool redirect)
		{
			if (mf._primitive.UseNormalClassRead())
			{
				return base.Read(mf, a_bytes, redirect);
			}
			return i_handler.Read(mf, a_bytes, false);
		}

		public override Db4objects.Db4o.Internal.ITypeHandler4 ReadArrayHandler(Db4objects.Db4o.Internal.Transaction
			 a_trans, Db4objects.Db4o.Internal.Marshall.MarshallerFamily mf, Db4objects.Db4o.Internal.Buffer[]
			 a_bytes)
		{
			if (IsArray())
			{
				return i_handler;
			}
			return null;
		}

		public override object ReadQuery(Db4objects.Db4o.Internal.Transaction trans, Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, bool withRedirection, Db4objects.Db4o.Internal.Buffer reader, bool toArray)
		{
			if (mf._primitive.UseNormalClassRead())
			{
				return base.ReadQuery(trans, mf, withRedirection, reader, toArray);
			}
			return i_handler.ReadQuery(trans, mf, withRedirection, reader, toArray);
		}

		public override Db4objects.Db4o.Internal.Query.Processor.QCandidate ReadSubCandidate
			(Db4objects.Db4o.Internal.Marshall.MarshallerFamily mf, Db4objects.Db4o.Internal.Buffer
			 reader, Db4objects.Db4o.Internal.Query.Processor.QCandidates candidates, bool withIndirection
			)
		{
			return i_handler.ReadSubCandidate(mf, reader, candidates, withIndirection);
		}

		internal override void RemoveFromIndex(Db4objects.Db4o.Internal.Transaction ta, int
			 id)
		{
		}

		public override bool SupportsIndex()
		{
			return true;
		}

		public sealed override bool WriteObjectBegin()
		{
			return false;
		}

		public override object WriteNew(Db4objects.Db4o.Internal.Marshall.MarshallerFamily
			 mf, object a_object, bool topLevel, Db4objects.Db4o.Internal.StatefulBuffer a_bytes
			, bool withIndirection, bool restoreLinkOffset)
		{
			mf._primitive.WriteNew(a_bytes.GetTransaction(), this, a_object, topLevel, a_bytes
				, withIndirection, restoreLinkOffset);
			return a_object;
		}

		public override string ToString()
		{
			return "Wraps " + i_handler.ToString() + " in YapClassPrimitive";
		}

		public override void Defrag(Db4objects.Db4o.Internal.Marshall.MarshallerFamily mf
			, Db4objects.Db4o.Internal.ReaderPair readers, bool redirect)
		{
			if (mf._primitive.UseNormalClassRead())
			{
				base.Defrag(mf, readers, redirect);
			}
			else
			{
				i_handler.Defrag(mf, readers, false);
			}
		}
	}
}
