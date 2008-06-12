/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o;
using Db4objects.Db4o.Foundation;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Marshall;

namespace Db4objects.Db4o.Internal
{
	/// <exclude></exclude>
	public abstract class AbstractBufferContext : IBufferContext
	{
		private IReadBuffer _buffer;

		protected readonly Db4objects.Db4o.Internal.Transaction _transaction;

		public AbstractBufferContext(Db4objects.Db4o.Internal.Transaction transaction, IReadBuffer
			 buffer)
		{
			_transaction = transaction;
			_buffer = buffer;
		}

		public virtual IReadBuffer Buffer(IReadBuffer buffer)
		{
			IReadBuffer temp = _buffer;
			_buffer = buffer;
			return temp;
		}

		public virtual IReadBuffer Buffer()
		{
			return _buffer;
		}

		public virtual byte ReadByte()
		{
			return _buffer.ReadByte();
		}

		public virtual void ReadBytes(byte[] bytes)
		{
			_buffer.ReadBytes(bytes);
		}

		public virtual int ReadInt()
		{
			return _buffer.ReadInt();
		}

		public virtual long ReadLong()
		{
			return _buffer.ReadLong();
		}

		public virtual int Offset()
		{
			return _buffer.Offset();
		}

		public virtual void Seek(int offset)
		{
			_buffer.Seek(offset);
		}

		public virtual ObjectContainerBase Container()
		{
			return _transaction.Container();
		}

		public virtual IObjectContainer ObjectContainer()
		{
			return Container();
		}

		public virtual Db4objects.Db4o.Internal.Transaction Transaction()
		{
			return _transaction;
		}

		public virtual ITypeHandler4 CorrectHandlerVersion(ITypeHandler4 handler)
		{
			return Container().Handlers().CorrectHandlerVersion(handler, HandlerVersion());
		}

		public abstract int HandlerVersion();

		public virtual bool IsLegacyHandlerVersion()
		{
			return HandlerVersion() == 0;
		}

		public virtual BitMap4 ReadBitMap(int bitCount)
		{
			return _buffer.ReadBitMap(bitCount);
		}

		public virtual void SeekCurrentInt()
		{
			Seek(ReadInt());
		}
	}
}