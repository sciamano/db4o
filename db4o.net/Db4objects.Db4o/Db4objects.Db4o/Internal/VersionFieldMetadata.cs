/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o.Ext;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Handlers;
using Db4objects.Db4o.Internal.Marshall;
using Db4objects.Db4o.Internal.Slots;
using Db4objects.Db4o.Marshall;

namespace Db4objects.Db4o.Internal
{
	/// <exclude></exclude>
	public class VersionFieldMetadata : VirtualFieldMetadata
	{
		internal VersionFieldMetadata(ObjectContainerBase stream) : base()
		{
			SetName(VirtualField.VERSION);
			i_handler = new LongHandler(stream);
		}

		public override void AddFieldIndex(MarshallerFamily mf, ClassMetadata yapClass, StatefulBuffer
			 writer, Slot oldSlot)
		{
			writer.WriteLong(writer.GetStream().GenerateTimeStampId());
		}

		public override void Delete(MarshallerFamily mf, StatefulBuffer a_bytes, bool isUpdate
			)
		{
			a_bytes.IncrementOffset(LinkLength());
		}

		internal override void Instantiate1(Transaction a_trans, ObjectReference a_yapObject
			, Db4objects.Db4o.Internal.Buffer a_bytes)
		{
			a_yapObject.VirtualAttributes().i_version = a_bytes.ReadLong();
		}

		internal override void Marshall(Transaction trans, ObjectReference @ref, IWriteBuffer
			 buffer, bool isMigrating, bool isNew)
		{
			VirtualAttributes attr = @ref.VirtualAttributes();
			if (!isMigrating)
			{
				attr.i_version = trans.Container()._parent.GenerateTimeStampId();
			}
			if (attr == null)
			{
				buffer.WriteLong(0);
			}
			else
			{
				buffer.WriteLong(attr.i_version);
			}
		}

		public override int LinkLength()
		{
			return Const4.LONG_LENGTH;
		}

		internal override void MarshallIgnore(IWriteBuffer buffer)
		{
			buffer.WriteLong(0);
		}
	}
}
