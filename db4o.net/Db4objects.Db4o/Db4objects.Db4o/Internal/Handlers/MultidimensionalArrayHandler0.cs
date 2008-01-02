/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using System.IO;
using Db4objects.Db4o.Ext;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Handlers;
using Db4objects.Db4o.Internal.Marshall;
using Db4objects.Db4o.Internal.Slots;
using Db4objects.Db4o.Marshall;

namespace Db4objects.Db4o.Internal.Handlers
{
	/// <exclude></exclude>
	public class MultidimensionalArrayHandler0 : MultidimensionalArrayHandler
	{
		public MultidimensionalArrayHandler0(ArrayHandler template, HandlerRegistry registry
			, int version) : base(template, registry, version)
		{
		}

		public override object Read(IReadContext readContext)
		{
			IInternalReadContext context = (IInternalReadContext)readContext;
			BufferImpl buffer = ReadIndirectedBuffer(context);
			if (buffer == null)
			{
				return null;
			}
			IBuffer contextBuffer = context.Buffer(buffer);
			object array = base.Read(context);
			context.Buffer(contextBuffer);
			return array;
		}

		public override void Defragment(IDefragmentContext context)
		{
			int sourceAddress = context.SourceBuffer().ReadInt();
			int length = context.SourceBuffer().ReadInt();
			if (sourceAddress == 0 && length == 0)
			{
				context.TargetBuffer().WriteInt(0);
				context.TargetBuffer().WriteInt(0);
				return;
			}
			Slot slot = context.AllocateMappedTargetSlot(sourceAddress, length);
			BufferImpl sourceBuffer = null;
			try
			{
				sourceBuffer = context.SourceBufferByAddress(sourceAddress, length);
			}
			catch (IOException exc)
			{
				throw new Db4oIOException(exc);
			}
			DefragmentContextImpl payloadContext = new DefragmentContextImpl(sourceBuffer, (DefragmentContextImpl
				)context);
			Defrag1(payloadContext);
			payloadContext.WriteToTarget(slot.Address());
			context.TargetBuffer().WriteInt(slot.Address());
			context.TargetBuffer().WriteInt(length);
		}
	}
}
