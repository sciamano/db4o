/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using System;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.CS.Messages;
using Db4objects.Db4o.Internal.Slots;

namespace Db4objects.Db4o.Internal.CS.Messages
{
	public sealed class MReadBytes : MsgD, IServerSideMessage
	{
		public sealed override Db4objects.Db4o.Internal.Buffer GetByteLoad()
		{
			int address = _payLoad.ReadInt();
			int length = _payLoad.GetLength() - (Const4.INT_LENGTH);
			Slot slot = new Slot(address, length);
			_payLoad.RemoveFirstBytes(Const4.INT_LENGTH);
			_payLoad.UseSlot(slot);
			return this._payLoad;
		}

		public sealed override MsgD GetWriter(StatefulBuffer bytes)
		{
			MsgD message = GetWriterForLength(bytes.GetTransaction(), bytes.GetLength() + Const4
				.INT_LENGTH);
			message._payLoad.WriteInt(bytes.GetAddress());
			message._payLoad.Append(bytes._buffer);
			return message;
		}

		public bool ProcessAtServer()
		{
			int address = ReadInt();
			int length = ReadInt();
			lock (StreamLock())
			{
				StatefulBuffer bytes = new StatefulBuffer(this.Transaction(), address, length);
				try
				{
					Stream().ReadBytes(bytes._buffer, address, length);
					Write(GetWriter(bytes));
				}
				catch (Exception)
				{
					Write(Msg.NULL);
				}
			}
			return true;
		}
	}
}
