/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using Db4objects.Db4o.CS.Internal;
using Db4objects.Db4o.CS.Internal.Messages;
using Db4objects.Db4o.Foundation;
using Db4objects.Db4o.Internal;

namespace Db4objects.Db4o.CS.Internal
{
	public sealed class ClientTransaction : Transaction
	{
		private readonly ClientObjectContainer _client;

		protected Tree _objectRefrencesToGC;

		internal ClientTransaction(ClientObjectContainer container, Transaction parentTransaction
			, TransactionalReferenceSystem referenceSystem) : base(container, parentTransaction
			, referenceSystem)
		{
			_client = container;
		}

		public override void Commit()
		{
			CommitTransactionListeners();
			ClearAll();
			if (IsSystemTransaction())
			{
				_client.Write(Msg.CommitSystemtrans);
			}
			else
			{
				_client.Write(Msg.Commit);
				_client.ExpectedResponse(Msg.Ok);
			}
		}

		protected override void Clear()
		{
			RemoveObjectReferences();
		}

		private void RemoveObjectReferences()
		{
			if (_objectRefrencesToGC != null)
			{
				_objectRefrencesToGC.Traverse(new _IVisitor4_37(this));
			}
			_objectRefrencesToGC = null;
		}

		private sealed class _IVisitor4_37 : IVisitor4
		{
			public _IVisitor4_37(ClientTransaction _enclosing)
			{
				this._enclosing = _enclosing;
			}

			public void Visit(object a_object)
			{
				ObjectReference yo = (ObjectReference)((TreeIntObject)a_object)._object;
				this._enclosing.RemoveReference(yo);
			}

			private readonly ClientTransaction _enclosing;
		}

		public override bool Delete(ObjectReference @ref, int id, int cascade)
		{
			if (!base.Delete(@ref, id, cascade))
			{
				return false;
			}
			MsgD msg = Msg.TaDelete.GetWriterForInts(this, new int[] { id, cascade });
			_client.WriteBatchedMessage(msg);
			return true;
		}

		public override bool IsDeleted(int a_id)
		{
			// This one really is a hack.
			// It only helps to get information about the current
			// transaction.
			// We need a better strategy for C/S concurrency behaviour.
			MsgD msg = Msg.TaIsDeleted.GetWriterForInt(this, a_id);
			_client.Write(msg);
			int res = _client.ExpectedByteResponse(Msg.TaIsDeleted).ReadInt();
			return res == 1;
		}

		public sealed override HardObjectReference GetHardReferenceBySignature(long a_uuid
			, byte[] a_signature)
		{
			int messageLength = Const4.LongLength + Const4.IntLength + a_signature.Length;
			MsgD message = Msg.ObjectByUuid.GetWriterForLength(this, messageLength);
			message.WriteLong(a_uuid);
			message.WriteInt(a_signature.Length);
			message.WriteBytes(a_signature);
			_client.Write(message);
			message = (MsgD)_client.ExpectedResponse(Msg.ObjectByUuid);
			int id = message.ReadInt();
			if (id > 0)
			{
				return Container().GetHardObjectReferenceById(this, id);
			}
			return HardObjectReference.Invalid;
		}

		public override void ProcessDeletes()
		{
			IVisitor4 deleteVisitor = new _IVisitor4_85(this);
			TraverseDelete(deleteVisitor);
			_client.WriteBatchedMessage(Msg.ProcessDeletes);
		}

		private sealed class _IVisitor4_85 : IVisitor4
		{
			public _IVisitor4_85(ClientTransaction _enclosing)
			{
				this._enclosing = _enclosing;
			}

			public void Visit(object a_object)
			{
				DeleteInfo info = (DeleteInfo)a_object;
				if (info._reference != null)
				{
					this._enclosing._objectRefrencesToGC = Tree.Add(this._enclosing._objectRefrencesToGC
						, new TreeIntObject(info._key, info._reference));
				}
			}

			private readonly ClientTransaction _enclosing;
		}

		public override void Rollback()
		{
			_objectRefrencesToGC = null;
			RollBackTransactionListeners();
			ClearAll();
		}

		public override void WriteUpdateAdjustIndexes(int id, ClassMetadata classMetadata
			, ArrayType arrayType, int cascade)
		{
		}
		// do nothing
	}
}