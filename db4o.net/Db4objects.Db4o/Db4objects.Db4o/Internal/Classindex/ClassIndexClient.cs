/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Classindex;

namespace Db4objects.Db4o.Internal.Classindex
{
	/// <summary>client class index.</summary>
	/// <remarks>
	/// client class index. Largly intended to do nothing or
	/// redirect functionality to the server.
	/// </remarks>
	internal sealed class ClassIndexClient : ClassIndex
	{
		internal ClassIndexClient(ClassMetadata aYapClass) : base(aYapClass)
		{
		}

		public override void Add(int a_id)
		{
			throw Exceptions4.VirtualException();
		}

		internal void EnsureActive()
		{
		}

		public override void Read(Transaction a_trans)
		{
		}

		internal override void SetDirty(ObjectContainerBase a_stream)
		{
		}

		public sealed override void WriteOwnID(Transaction trans, Db4objects.Db4o.Internal.Buffer
			 a_writer)
		{
			a_writer.WriteInt(0);
		}
	}
}
