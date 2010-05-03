/* Copyright (C) 2004 - 2009  Versant Inc.  http://www.db4o.com */

using System;
using System.Text;
using Db4objects.Db4o;
using Db4objects.Db4o.Foundation;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Freespace;
using Db4objects.Db4o.Internal.Slots;

namespace Db4objects.Db4o.Internal.Freespace
{
	public class InMemoryFreespaceManager : AbstractFreespaceManager
	{
		private readonly TreeIntObject _finder = new TreeIntObject(0);

		private Tree _freeByAddress;

		private Tree _freeBySize;

		private IFreespaceListener _listener = NullFreespaceListener.Instance;

		public InMemoryFreespaceManager(IProcedure4 slotFreedCallback, int discardLimit) : 
			base(slotFreedCallback, discardLimit)
		{
		}

		private void AddFreeSlotNodes(int address, int length)
		{
			FreeSlotNode addressNode = new FreeSlotNode(address);
			addressNode.CreatePeer(length);
			_freeByAddress = Tree.Add(_freeByAddress, addressNode);
			AddToFreeBySize(addressNode._peer);
		}

		private void AddToFreeBySize(FreeSlotNode node)
		{
			_freeBySize = Tree.Add(_freeBySize, node);
			_listener.SlotAdded(node._key);
		}

		public override Slot AllocateTransactionLogSlot(int length)
		{
			FreeSlotNode sizeNode = (FreeSlotNode)Tree.Last(_freeBySize);
			if (sizeNode == null || sizeNode._key < length)
			{
				return null;
			}
			// We can just be appending to the end of the file, using one
			// really big contigous slot that keeps growing. Let's limit.
			int limit = length + 100;
			if (sizeNode._key > limit)
			{
				return AllocateSlot(limit);
			}
			RemoveFromBothTrees(sizeNode);
			return new Slot(sizeNode._peer._key, sizeNode._key);
		}

		public override void FreeTransactionLogSlot(Slot slot)
		{
			Free(slot);
		}

		public override void BeginCommit()
		{
		}

		// do nothing
		public override void Commit()
		{
		}

		// do nothing
		public override void EndCommit()
		{
		}

		// do nothing
		public override void Free(Slot slot)
		{
			int address = slot.Address();
			if (address <= 0)
			{
				throw new ArgumentException();
			}
			int length = slot.Length();
			if (DTrace.enabled)
			{
				DTrace.FreespacemanagerRamFree.LogLength(address, length);
			}
			_finder._key = address;
			FreeSlotNode sizeNode;
			FreeSlotNode addressnode = (FreeSlotNode)Tree.FindSmaller(_freeByAddress, _finder
				);
			if ((addressnode != null) && ((addressnode._key + addressnode._peer._key) == address
				))
			{
				sizeNode = addressnode._peer;
				RemoveFromFreeBySize(sizeNode);
				sizeNode._key += length;
				FreeSlotNode secondAddressNode = (FreeSlotNode)Tree.FindGreaterOrEqual(_freeByAddress
					, _finder);
				if ((secondAddressNode != null) && (address + length == secondAddressNode._key))
				{
					sizeNode._key += secondAddressNode._peer._key;
					RemoveFromBothTrees(secondAddressNode._peer);
				}
				sizeNode.RemoveChildren();
				AddToFreeBySize(sizeNode);
			}
			else
			{
				addressnode = (FreeSlotNode)Tree.FindGreaterOrEqual(_freeByAddress, _finder);
				if ((addressnode != null) && (address + length == addressnode._key))
				{
					sizeNode = addressnode._peer;
					RemoveFromBothTrees(sizeNode);
					sizeNode._key += length;
					addressnode._key = address;
					addressnode.RemoveChildren();
					sizeNode.RemoveChildren();
					_freeByAddress = Tree.Add(_freeByAddress, addressnode);
					AddToFreeBySize(sizeNode);
				}
				else
				{
					if (CanDiscard(length))
					{
						return;
					}
					AddFreeSlotNodes(address, length);
				}
			}
			SlotFreed(slot);
		}

		public override void FreeSelf()
		{
		}

		// Do nothing.
		// The RAM manager frees itself on reading.
		public override Slot AllocateSlot(int length)
		{
			_finder._key = length;
			_finder._object = null;
			_freeBySize = FreeSlotNode.RemoveGreaterOrEqual((FreeSlotNode)_freeBySize, _finder
				);
			if (_finder._object == null)
			{
				return null;
			}
			FreeSlotNode node = (FreeSlotNode)_finder._object;
			_listener.SlotRemoved(node._key);
			int blocksFound = node._key;
			int address = node._peer._key;
			_freeByAddress = _freeByAddress.RemoveNode(node._peer);
			int remainingBlocks = blocksFound - length;
			if (CanDiscard(remainingBlocks))
			{
				length = blocksFound;
			}
			else
			{
				AddFreeSlotNodes(address + length, remainingBlocks);
			}
			if (DTrace.enabled)
			{
				DTrace.FreespacemanagerGetSlot.LogLength(address, length);
			}
			return new Slot(address, length);
		}

		internal virtual int MarshalledLength()
		{
			return TreeInt.MarshalledLength((TreeInt)_freeBySize);
		}

		public override void Read(LocalObjectContainer container, int freeSlotsID)
		{
			ReadById(container, freeSlotsID);
		}

		private void Read(ByteArrayBuffer reader)
		{
			FreeSlotNode.sizeLimit = DiscardLimit();
			_freeBySize = new TreeReader(reader, new FreeSlotNode(0), true).Read();
			ByRef addressTree = ByRef.NewInstance();
			if (_freeBySize != null)
			{
				_freeBySize.Traverse(new _IVisitor4_176(addressTree));
			}
			_freeByAddress = ((Tree)addressTree.value);
		}

		private sealed class _IVisitor4_176 : IVisitor4
		{
			public _IVisitor4_176(ByRef addressTree)
			{
				this.addressTree = addressTree;
			}

			public void Visit(object a_object)
			{
				FreeSlotNode node = ((FreeSlotNode)a_object)._peer;
				addressTree.value = Tree.Add(((Tree)addressTree.value), node);
			}

			private readonly ByRef addressTree;
		}

		internal virtual void Read(LocalObjectContainer container, Slot slot)
		{
			if (slot.IsNull())
			{
				return;
			}
			ByteArrayBuffer buffer = container.ReadBufferBySlot(slot);
			if (buffer == null)
			{
				return;
			}
			Read(buffer);
			container.Free(slot);
		}

		private void ReadById(LocalObjectContainer container, int freeSlotsID)
		{
			if (freeSlotsID <= 0)
			{
				return;
			}
			if (DiscardLimit() == int.MaxValue)
			{
				return;
			}
			Read(container, container.ReadPointerSlot(freeSlotsID));
			container.Free(freeSlotsID, Const4.PointerLength);
		}

		private void RemoveFromBothTrees(FreeSlotNode sizeNode)
		{
			RemoveFromFreeBySize(sizeNode);
			_freeByAddress = _freeByAddress.RemoveNode(sizeNode._peer);
		}

		private void RemoveFromFreeBySize(FreeSlotNode node)
		{
			_freeBySize = _freeBySize.RemoveNode(node);
			_listener.SlotRemoved(node._key);
		}

		public override int SlotCount()
		{
			return Tree.Size(_freeByAddress);
		}

		public override void Start(int slotAddress)
		{
		}

		// this is done in read(), nothing to do here
		public override byte SystemType()
		{
			return FmRam;
		}

		public override string ToString()
		{
			StringBuilder sb = new StringBuilder();
			sb.Append("RAM FreespaceManager\n");
			sb.Append("Address Index\n");
			_freeByAddress.Traverse(new InMemoryFreespaceManager.ToStringVisitor(sb));
			sb.Append("Length Index\n");
			_freeBySize.Traverse(new InMemoryFreespaceManager.ToStringVisitor(sb));
			return sb.ToString();
		}

		public override void Traverse(IVisitor4 visitor)
		{
			if (_freeByAddress == null)
			{
				return;
			}
			_freeByAddress.Traverse(new _IVisitor4_249(visitor));
		}

		private sealed class _IVisitor4_249 : IVisitor4
		{
			public _IVisitor4_249(IVisitor4 visitor)
			{
				this.visitor = visitor;
			}

			public void Visit(object a_object)
			{
				FreeSlotNode fsn = (FreeSlotNode)a_object;
				int address = fsn._key;
				int length = fsn._peer._key;
				visitor.Visit(new Slot(address, length));
			}

			private readonly IVisitor4 visitor;
		}

		public override int Write(LocalObjectContainer container)
		{
			int pointerSlot = container.AllocatePointerSlot();
			Slot slot = container.AllocateSlot(MarshalledLength());
			Pointer4 pointer = new Pointer4(pointerSlot, slot);
			Write(container, pointer);
			return pointer._id;
		}

		internal virtual void Write(LocalObjectContainer container, Pointer4 pointer)
		{
			ByteArrayBuffer buffer = new ByteArrayBuffer(pointer.Length());
			TreeInt.Write(buffer, (TreeInt)_freeBySize);
			container.WriteEncrypt(buffer, pointer.Address(), 0);
			container.SyncFiles();
			container.WritePointer(pointer.Id(), pointer._slot);
		}

		internal sealed class ToStringVisitor : IVisitor4
		{
			private readonly StringBuilder _sb;

			internal ToStringVisitor(StringBuilder sb)
			{
				_sb = sb;
			}

			public void Visit(object obj)
			{
				_sb.Append(obj);
				_sb.Append("\n");
			}
		}

		public override void Listener(IFreespaceListener listener)
		{
			_listener = listener;
		}

		public override bool IsStarted()
		{
			return true;
		}
	}
}