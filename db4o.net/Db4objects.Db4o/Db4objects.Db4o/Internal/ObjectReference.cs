/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using System;
using Db4objects.Db4o.Activation;
using Db4objects.Db4o.Ext;
using Db4objects.Db4o.Foundation;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Marshall;
using Db4objects.Db4o.Internal.Slots;
using Db4objects.Db4o.Reflect;
using Sharpen;

namespace Db4objects.Db4o.Internal
{
	/// <exclude></exclude>
	public class ObjectReference : PersistentBase, IObjectInfo, IActivator
	{
		private Db4objects.Db4o.Internal.ClassMetadata _class;

		private object _object;

		private Db4objects.Db4o.Internal.VirtualAttributes _virtualAttributes;

		private Db4objects.Db4o.Internal.ObjectReference _idPreceding;

		private Db4objects.Db4o.Internal.ObjectReference _idSubsequent;

		private int _idSize;

		private Db4objects.Db4o.Internal.ObjectReference _hcPreceding;

		private Db4objects.Db4o.Internal.ObjectReference _hcSubsequent;

		private int _hcSize;

		private int _hcHashcode;

		private int _lastTopLevelCallId;

		public ObjectReference()
		{
		}

		public ObjectReference(int a_id)
		{
			_id = a_id;
		}

		public ObjectReference(Db4objects.Db4o.Internal.ClassMetadata classMetadata, int 
			id)
		{
			_class = classMetadata;
			_id = id;
		}

		public virtual void Activate()
		{
			if (IsActive())
			{
				return;
			}
			Activate(Container().Transaction(), GetObject(), 1, false);
		}

		public virtual void Activate(Db4objects.Db4o.Internal.Transaction ta, object obj, 
			int depth, bool isRefresh)
		{
			Activate1(ta, obj, depth, isRefresh);
			ta.Container().Activate3CheckStill(ta);
		}

		internal virtual void Activate1(Db4objects.Db4o.Internal.Transaction ta, object obj
			, int depth, bool isRefresh)
		{
			if (obj is IDb4oTypeImpl)
			{
				depth = ((IDb4oTypeImpl)obj).AdjustReadDepth(depth);
			}
			if (depth > 0)
			{
				ObjectContainerBase container = ta.Container();
				if (isRefresh)
				{
					LogActivation(container, "refresh");
				}
				else
				{
					if (IsActive())
					{
						if (obj != null)
						{
							if (depth > 1)
							{
								if (_class.Config() != null)
								{
									depth = _class.Config().AdjustActivationDepth(depth);
								}
								_class.ActivateFields(ta, obj, depth);
							}
							return;
						}
					}
					LogActivation(container, "activate");
				}
				Read(ta, null, obj, depth, Const4.ADD_MEMBERS_TO_ID_TREE_ONLY, false);
			}
		}

		private void LogActivation(ObjectContainerBase container, string @event)
		{
			LogEvent(container, @event, Const4.ACTIVATION);
		}

		private void LogEvent(ObjectContainerBase container, string @event, int level)
		{
			if (container.ConfigImpl().MessageLevel() > level)
			{
				container.Message(string.Empty + GetID() + " " + @event + " " + _class.GetName());
			}
		}

		public void AddExistingReferenceToIdTree(Db4objects.Db4o.Internal.Transaction trans
			)
		{
			if (!(_class is PrimitiveFieldHandler))
			{
				trans.ReferenceSystem().AddExistingReferenceToIdTree(this);
			}
		}

		/// <summary>return false if class not completely initialized, otherwise true *</summary>
		internal virtual bool ContinueSet(Db4objects.Db4o.Internal.Transaction trans, int
			 updateDepth)
		{
			if (!BitIsTrue(Const4.CONTINUE))
			{
				return true;
			}
			if (!_class.StateOKAndAncestors())
			{
				return false;
			}
			BitFalse(Const4.CONTINUE);
			MarshallingContext context = new MarshallingContext(trans, this, updateDepth, true
				);
			MarshallerFamily.Current()._object.Marshall(GetObject(), context);
			Pointer4 pointer = context.AllocateSlot();
			Db4objects.Db4o.Internal.Buffer buffer = context.ToWriteBuffer(pointer);
			ObjectContainerBase container = trans.Container();
			container.WriteNew(trans, pointer, _class, buffer);
			object obj = _object;
			ObjectOnNew(trans, obj);
			if (!_class.IsPrimitive())
			{
				_object = container._references.CreateYapRef(this, obj);
			}
			SetStateClean();
			EndProcessing();
			return true;
		}

		private void ObjectOnNew(Db4objects.Db4o.Internal.Transaction transaction, object
			 obj)
		{
			ObjectContainerBase container = transaction.Container();
			container.Callbacks().ObjectOnNew(transaction, obj);
			_class.DispatchEvent(container, obj, EventDispatcher.NEW);
		}

		public virtual void Deactivate(Db4objects.Db4o.Internal.Transaction trans, int depth
			)
		{
			if (depth > 0)
			{
				object obj = GetObject();
				if (obj != null)
				{
					if (obj is IDb4oTypeImpl)
					{
						((IDb4oTypeImpl)obj).PreDeactivate();
					}
					ObjectContainerBase container = trans.Container();
					LogActivation(container, "deactivate");
					SetStateDeactivated();
					_class.Deactivate(trans, obj, depth);
				}
			}
		}

		public override byte GetIdentifier()
		{
			return Const4.YAPOBJECT;
		}

		public virtual long GetInternalID()
		{
			return GetID();
		}

		public virtual object GetObject()
		{
			if (Platform4.HasWeakReferences())
			{
				return Platform4.GetYapRefObject(_object);
			}
			return _object;
		}

		public virtual object GetObjectReference()
		{
			return _object;
		}

		public virtual ObjectContainerBase Container()
		{
			if (_class == null)
			{
				return null;
			}
			return _class.Container();
		}

		public virtual Db4objects.Db4o.Internal.Transaction Transaction()
		{
			ObjectContainerBase container = Container();
			if (container != null)
			{
				return container.Transaction();
			}
			return null;
		}

		public virtual Db4oUUID GetUUID()
		{
			Db4objects.Db4o.Internal.VirtualAttributes va = VirtualAttributes(Transaction());
			if (va != null && va.i_database != null)
			{
				return new Db4oUUID(va.i_uuid, va.i_database.i_signature);
			}
			return null;
		}

		public virtual long GetVersion()
		{
			Db4objects.Db4o.Internal.VirtualAttributes va = VirtualAttributes(Transaction());
			if (va == null)
			{
				return 0;
			}
			return va.i_version;
		}

		public Db4objects.Db4o.Internal.ClassMetadata ClassMetadata()
		{
			return _class;
		}

		public virtual void ClassMetadata(Db4objects.Db4o.Internal.ClassMetadata classMetadata
			)
		{
			_class = classMetadata;
		}

		public override int OwnLength()
		{
			throw Exceptions4.ShouldNeverBeCalled();
		}

		public virtual Db4objects.Db4o.Internal.VirtualAttributes ProduceVirtualAttributes
			()
		{
			if (_virtualAttributes == null)
			{
				_virtualAttributes = new Db4objects.Db4o.Internal.VirtualAttributes();
			}
			return _virtualAttributes;
		}

		internal object PeekPersisted(Db4objects.Db4o.Internal.Transaction trans, int depth
			)
		{
			return Read(trans, depth, Const4.TRANSIENT, false);
		}

		internal object Read(Db4objects.Db4o.Internal.Transaction trans, int instantiationDepth
			, int addToIDTree, bool checkIDTree)
		{
			return Read(trans, null, null, instantiationDepth, addToIDTree, checkIDTree);
		}

		public object Read(Db4objects.Db4o.Internal.Transaction trans, StatefulBuffer buffer
			, object obj, int instantiationDepth, int addToIDTree, bool checkIDTree)
		{
			if (NewTypeHandlerReading.enabled)
			{
				UnmarshallingContext context = new UnmarshallingContext(trans, this, addToIDTree, 
					checkIDTree);
				context.Buffer(buffer);
				context.PersistentObject(obj);
				context.ActivationDepth(instantiationDepth);
				return context.Read();
			}
			if (BeginProcessing())
			{
				ObjectContainerBase container = trans.Container();
				int id = GetID();
				if (buffer == null && id > 0)
				{
					buffer = container.ReadWriterByID(trans, id);
				}
				if (buffer != null)
				{
					ObjectHeader header = new ObjectHeader(container, buffer);
					_class = header.ClassMetadata();
					if (_class == null)
					{
						return null;
					}
					if (checkIDTree)
					{
						object objectInCacheFromClassCreation = trans.ObjectForIdFromCache(GetID());
						if (objectInCacheFromClassCreation != null)
						{
							return objectInCacheFromClassCreation;
						}
					}
					buffer.SetInstantiationDepth(instantiationDepth);
					buffer.SetUpdateDepth(addToIDTree);
					if (addToIDTree == Const4.TRANSIENT)
					{
						obj = _class.InstantiateTransient(this, obj, header._marshallerFamily, header._headerAttributes
							, buffer);
					}
					else
					{
						obj = _class.Instantiate(this, obj, header._marshallerFamily, header._headerAttributes
							, buffer, addToIDTree == Const4.ADD_TO_ID_TREE);
					}
				}
				EndProcessing();
			}
			return obj;
		}

		public object ReadPrefetch(ObjectContainerBase container, StatefulBuffer buffer)
		{
			object readObject = null;
			if (BeginProcessing())
			{
				ObjectHeader header = new ObjectHeader(container, buffer);
				_class = header.ClassMetadata();
				if (_class == null)
				{
					return null;
				}
				buffer.SetInstantiationDepth(_class.ConfigOrAncestorConfig() == null ? 1 : 0);
				readObject = _class.Instantiate(this, GetObject(), header._marshallerFamily, header
					._headerAttributes, buffer, true);
				EndProcessing();
			}
			return readObject;
		}

		public sealed override void ReadThis(Db4objects.Db4o.Internal.Transaction trans, 
			Db4objects.Db4o.Internal.Buffer buffer)
		{
		}

		public virtual void SetObjectWeak(ObjectContainerBase container, object obj)
		{
			if (container._references._weak)
			{
				if (_object != null)
				{
					Platform4.KillYapRef(_object);
				}
				_object = Platform4.CreateActiveObjectReference(container._references._queue, this
					, obj);
			}
			else
			{
				_object = obj;
			}
		}

		public virtual void SetObject(object obj)
		{
			_object = obj;
		}

		internal void Store(Db4objects.Db4o.Internal.Transaction trans, Db4objects.Db4o.Internal.ClassMetadata
			 classMetadata, object obj)
		{
			_object = obj;
			_class = classMetadata;
			WriteObjectBegin();
			int id = trans.Container().NewUserObject();
			trans.SlotFreePointerOnRollback(id);
			SetID(id);
			BeginProcessing();
			BitTrue(Const4.CONTINUE);
		}

		public virtual void FlagForDelete(int callId)
		{
			_lastTopLevelCallId = -callId;
		}

		public virtual bool IsFlaggedForDelete()
		{
			return _lastTopLevelCallId < 0;
		}

		public virtual void FlagAsHandled(int callId)
		{
			_lastTopLevelCallId = callId;
		}

		public bool IsFlaggedAsHandled(int callID)
		{
			return _lastTopLevelCallId == callID;
		}

		public bool IsValid()
		{
			return IsValidId(GetID()) && GetObject() != null;
		}

		public static bool IsValidId(int id)
		{
			return id > 0;
		}

		public virtual Db4objects.Db4o.Internal.VirtualAttributes VirtualAttributes()
		{
			return _virtualAttributes;
		}

		public virtual Db4objects.Db4o.Internal.VirtualAttributes VirtualAttributes(Db4objects.Db4o.Internal.Transaction
			 trans)
		{
			if (trans == null)
			{
				return _virtualAttributes;
			}
			lock (trans.Container().Lock())
			{
				if (_virtualAttributes == null)
				{
					if (_class.HasVirtualAttributes())
					{
						_virtualAttributes = new Db4objects.Db4o.Internal.VirtualAttributes();
						_class.ReadVirtualAttributes(trans, this);
					}
				}
				else
				{
					if (!_virtualAttributes.SuppliesUUID())
					{
						if (_class.HasVirtualAttributes())
						{
							_class.ReadVirtualAttributes(trans, this);
						}
					}
				}
				return _virtualAttributes;
			}
		}

		public virtual void SetVirtualAttributes(Db4objects.Db4o.Internal.VirtualAttributes
			 at)
		{
			_virtualAttributes = at;
		}

		public override void WriteThis(Db4objects.Db4o.Internal.Transaction trans, Db4objects.Db4o.Internal.Buffer
			 buffer)
		{
		}

		public virtual void WriteUpdate(Db4objects.Db4o.Internal.Transaction transaction, 
			int updatedepth)
		{
			ContinueSet(transaction, updatedepth);
			if (!BeginProcessing())
			{
				return;
			}
			object obj = GetObject();
			if (!ObjectCanUpdate(transaction, obj) || !IsActive() || obj == null)
			{
				EndProcessing();
				return;
			}
			ObjectContainerBase container = transaction.Container();
			LogEvent(container, "update", Const4.STATE);
			SetStateClean();
			transaction.WriteUpdateDeleteMembers(GetID(), _class, container._handlers.ArrayType
				(obj), 0);
			MarshallingContext context = new MarshallingContext(transaction, this, updatedepth
				, false);
			MarshallerFamily.Current()._object.Marshall(obj, context);
			Pointer4 pointer = context.AllocateSlot();
			Db4objects.Db4o.Internal.Buffer buffer = context.ToWriteBuffer(pointer);
			container.WriteUpdate(transaction, pointer, ClassMetadata(), buffer);
			if (IsActive())
			{
				SetStateClean();
			}
			EndProcessing();
			container.Callbacks().ObjectOnUpdate(transaction, obj);
			ClassMetadata().DispatchEvent(container, obj, EventDispatcher.UPDATE);
		}

		private bool ObjectCanUpdate(Db4objects.Db4o.Internal.Transaction transaction, object
			 obj)
		{
			ObjectContainerBase container = transaction.Container();
			return container.Callbacks().ObjectCanUpdate(transaction, obj) && _class.DispatchEvent
				(container, obj, EventDispatcher.CAN_UPDATE);
		}

		/// <summary>HCTREE ****</summary>
		public virtual Db4objects.Db4o.Internal.ObjectReference Hc_add(Db4objects.Db4o.Internal.ObjectReference
			 newRef)
		{
			if (newRef.GetObject() == null)
			{
				return this;
			}
			newRef.Hc_init();
			return Hc_add1(newRef);
		}

		public virtual void Hc_init()
		{
			_hcPreceding = null;
			_hcSubsequent = null;
			_hcSize = 1;
			_hcHashcode = Hc_getCode(GetObject());
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_add1(Db4objects.Db4o.Internal.ObjectReference
			 newRef)
		{
			int cmp = Hc_compare(newRef);
			if (cmp < 0)
			{
				if (_hcPreceding == null)
				{
					_hcPreceding = newRef;
					_hcSize++;
				}
				else
				{
					_hcPreceding = _hcPreceding.Hc_add1(newRef);
					if (_hcSubsequent == null)
					{
						return Hc_rotateRight();
					}
					return Hc_balance();
				}
			}
			else
			{
				if (_hcSubsequent == null)
				{
					_hcSubsequent = newRef;
					_hcSize++;
				}
				else
				{
					_hcSubsequent = _hcSubsequent.Hc_add1(newRef);
					if (_hcPreceding == null)
					{
						return Hc_rotateLeft();
					}
					return Hc_balance();
				}
			}
			return this;
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_balance()
		{
			int cmp = _hcSubsequent._hcSize - _hcPreceding._hcSize;
			if (cmp < -2)
			{
				return Hc_rotateRight();
			}
			else
			{
				if (cmp > 2)
				{
					return Hc_rotateLeft();
				}
				else
				{
					_hcSize = _hcPreceding._hcSize + _hcSubsequent._hcSize + 1;
					return this;
				}
			}
		}

		private void Hc_calculateSize()
		{
			if (_hcPreceding == null)
			{
				if (_hcSubsequent == null)
				{
					_hcSize = 1;
				}
				else
				{
					_hcSize = _hcSubsequent._hcSize + 1;
				}
			}
			else
			{
				if (_hcSubsequent == null)
				{
					_hcSize = _hcPreceding._hcSize + 1;
				}
				else
				{
					_hcSize = _hcPreceding._hcSize + _hcSubsequent._hcSize + 1;
				}
			}
		}

		private int Hc_compare(Db4objects.Db4o.Internal.ObjectReference toRef)
		{
			int cmp = toRef._hcHashcode - _hcHashcode;
			if (cmp == 0)
			{
				cmp = toRef._id - _id;
			}
			return cmp;
		}

		public virtual Db4objects.Db4o.Internal.ObjectReference Hc_find(object obj)
		{
			return Hc_find(Hc_getCode(obj), obj);
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_find(int id, object obj)
		{
			int cmp = id - _hcHashcode;
			if (cmp < 0)
			{
				if (_hcPreceding != null)
				{
					return _hcPreceding.Hc_find(id, obj);
				}
			}
			else
			{
				if (cmp > 0)
				{
					if (_hcSubsequent != null)
					{
						return _hcSubsequent.Hc_find(id, obj);
					}
				}
				else
				{
					if (obj == GetObject())
					{
						return this;
					}
					if (_hcPreceding != null)
					{
						Db4objects.Db4o.Internal.ObjectReference inPreceding = _hcPreceding.Hc_find(id, obj
							);
						if (inPreceding != null)
						{
							return inPreceding;
						}
					}
					if (_hcSubsequent != null)
					{
						return _hcSubsequent.Hc_find(id, obj);
					}
				}
			}
			return null;
		}

		private int Hc_getCode(object obj)
		{
			int hcode = Runtime.IdentityHashCode(obj);
			if (hcode < 0)
			{
				hcode = ~hcode;
			}
			return hcode;
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_rotateLeft()
		{
			Db4objects.Db4o.Internal.ObjectReference tree = _hcSubsequent;
			_hcSubsequent = tree._hcPreceding;
			Hc_calculateSize();
			tree._hcPreceding = this;
			if (tree._hcSubsequent == null)
			{
				tree._hcSize = 1 + _hcSize;
			}
			else
			{
				tree._hcSize = 1 + _hcSize + tree._hcSubsequent._hcSize;
			}
			return tree;
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_rotateRight()
		{
			Db4objects.Db4o.Internal.ObjectReference tree = _hcPreceding;
			_hcPreceding = tree._hcSubsequent;
			Hc_calculateSize();
			tree._hcSubsequent = this;
			if (tree._hcPreceding == null)
			{
				tree._hcSize = 1 + _hcSize;
			}
			else
			{
				tree._hcSize = 1 + _hcSize + tree._hcPreceding._hcSize;
			}
			return tree;
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_rotateSmallestUp()
		{
			if (_hcPreceding != null)
			{
				_hcPreceding = _hcPreceding.Hc_rotateSmallestUp();
				return Hc_rotateRight();
			}
			return this;
		}

		internal virtual Db4objects.Db4o.Internal.ObjectReference Hc_remove(Db4objects.Db4o.Internal.ObjectReference
			 findRef)
		{
			if (this == findRef)
			{
				return Hc_remove();
			}
			int cmp = Hc_compare(findRef);
			if (cmp <= 0)
			{
				if (_hcPreceding != null)
				{
					_hcPreceding = _hcPreceding.Hc_remove(findRef);
				}
			}
			if (cmp >= 0)
			{
				if (_hcSubsequent != null)
				{
					_hcSubsequent = _hcSubsequent.Hc_remove(findRef);
				}
			}
			Hc_calculateSize();
			return this;
		}

		public virtual void Hc_traverse(IVisitor4 visitor)
		{
			if (_hcPreceding != null)
			{
				_hcPreceding.Hc_traverse(visitor);
			}
			if (_hcSubsequent != null)
			{
				_hcSubsequent.Hc_traverse(visitor);
			}
			visitor.Visit(this);
		}

		private Db4objects.Db4o.Internal.ObjectReference Hc_remove()
		{
			if (_hcSubsequent != null && _hcPreceding != null)
			{
				_hcSubsequent = _hcSubsequent.Hc_rotateSmallestUp();
				_hcSubsequent._hcPreceding = _hcPreceding;
				_hcSubsequent.Hc_calculateSize();
				return _hcSubsequent;
			}
			if (_hcSubsequent != null)
			{
				return _hcSubsequent;
			}
			return _hcPreceding;
		}

		/// <summary>IDTREE ****</summary>
		internal virtual Db4objects.Db4o.Internal.ObjectReference Id_add(Db4objects.Db4o.Internal.ObjectReference
			 newRef)
		{
			newRef._idPreceding = null;
			newRef._idSubsequent = null;
			newRef._idSize = 1;
			return Id_add1(newRef);
		}

		private Db4objects.Db4o.Internal.ObjectReference Id_add1(Db4objects.Db4o.Internal.ObjectReference
			 newRef)
		{
			int cmp = newRef._id - _id;
			if (cmp < 0)
			{
				if (_idPreceding == null)
				{
					_idPreceding = newRef;
					_idSize++;
				}
				else
				{
					_idPreceding = _idPreceding.Id_add1(newRef);
					if (_idSubsequent == null)
					{
						return Id_rotateRight();
					}
					return Id_balance();
				}
			}
			else
			{
				if (cmp > 0)
				{
					if (_idSubsequent == null)
					{
						_idSubsequent = newRef;
						_idSize++;
					}
					else
					{
						_idSubsequent = _idSubsequent.Id_add1(newRef);
						if (_idPreceding == null)
						{
							return Id_rotateLeft();
						}
						return Id_balance();
					}
				}
			}
			return this;
		}

		private Db4objects.Db4o.Internal.ObjectReference Id_balance()
		{
			int cmp = _idSubsequent._idSize - _idPreceding._idSize;
			if (cmp < -2)
			{
				return Id_rotateRight();
			}
			else
			{
				if (cmp > 2)
				{
					return Id_rotateLeft();
				}
				else
				{
					_idSize = _idPreceding._idSize + _idSubsequent._idSize + 1;
					return this;
				}
			}
		}

		private void Id_calculateSize()
		{
			if (_idPreceding == null)
			{
				if (_idSubsequent == null)
				{
					_idSize = 1;
				}
				else
				{
					_idSize = _idSubsequent._idSize + 1;
				}
			}
			else
			{
				if (_idSubsequent == null)
				{
					_idSize = _idPreceding._idSize + 1;
				}
				else
				{
					_idSize = _idPreceding._idSize + _idSubsequent._idSize + 1;
				}
			}
		}

		internal virtual Db4objects.Db4o.Internal.ObjectReference Id_find(int id)
		{
			int cmp = id - _id;
			if (cmp > 0)
			{
				if (_idSubsequent != null)
				{
					return _idSubsequent.Id_find(id);
				}
			}
			else
			{
				if (cmp < 0)
				{
					if (_idPreceding != null)
					{
						return _idPreceding.Id_find(id);
					}
				}
				else
				{
					return this;
				}
			}
			return null;
		}

		private Db4objects.Db4o.Internal.ObjectReference Id_rotateLeft()
		{
			Db4objects.Db4o.Internal.ObjectReference tree = _idSubsequent;
			_idSubsequent = tree._idPreceding;
			Id_calculateSize();
			tree._idPreceding = this;
			if (tree._idSubsequent == null)
			{
				tree._idSize = _idSize + 1;
			}
			else
			{
				tree._idSize = _idSize + 1 + tree._idSubsequent._idSize;
			}
			return tree;
		}

		private Db4objects.Db4o.Internal.ObjectReference Id_rotateRight()
		{
			Db4objects.Db4o.Internal.ObjectReference tree = _idPreceding;
			_idPreceding = tree._idSubsequent;
			Id_calculateSize();
			tree._idSubsequent = this;
			if (tree._idPreceding == null)
			{
				tree._idSize = _idSize + 1;
			}
			else
			{
				tree._idSize = _idSize + 1 + tree._idPreceding._idSize;
			}
			return tree;
		}

		private Db4objects.Db4o.Internal.ObjectReference Id_rotateSmallestUp()
		{
			if (_idPreceding != null)
			{
				_idPreceding = _idPreceding.Id_rotateSmallestUp();
				return Id_rotateRight();
			}
			return this;
		}

		internal virtual Db4objects.Db4o.Internal.ObjectReference Id_remove(int id)
		{
			int cmp = id - _id;
			if (cmp < 0)
			{
				if (_idPreceding != null)
				{
					_idPreceding = _idPreceding.Id_remove(id);
				}
			}
			else
			{
				if (cmp > 0)
				{
					if (_idSubsequent != null)
					{
						_idSubsequent = _idSubsequent.Id_remove(id);
					}
				}
				else
				{
					return Id_remove();
				}
			}
			Id_calculateSize();
			return this;
		}

		private Db4objects.Db4o.Internal.ObjectReference Id_remove()
		{
			if (_idSubsequent != null && _idPreceding != null)
			{
				_idSubsequent = _idSubsequent.Id_rotateSmallestUp();
				_idSubsequent._idPreceding = _idPreceding;
				_idSubsequent.Id_calculateSize();
				return _idSubsequent;
			}
			if (_idSubsequent != null)
			{
				return _idSubsequent;
			}
			return _idPreceding;
		}

		public override string ToString()
		{
			return base.ToString();
			try
			{
				int id = GetID();
				string str = "ObjectReference\nID=" + id;
				if (_class != null)
				{
					ObjectContainerBase container = _class.Container();
					if (container != null && id > 0)
					{
						StatefulBuffer writer = container.ReadWriterByID(container.Transaction(), id);
						if (writer != null)
						{
							str += "\nAddress=" + writer.GetAddress();
						}
						ObjectHeader oh = new ObjectHeader(Container(), writer);
						Db4objects.Db4o.Internal.ClassMetadata yc = oh.ClassMetadata();
						if (yc != _class)
						{
							str += "\nYapClass corruption";
						}
						else
						{
							str += yc.ToString(oh._marshallerFamily, writer, this, 0, 5);
						}
					}
				}
				object obj = GetObject();
				if (obj == null)
				{
					str += "\nfor [null]";
				}
				else
				{
					string objToString = string.Empty;
					try
					{
						objToString = obj.ToString();
					}
					catch (Exception)
					{
					}
					IReflectClass claxx = ClassMetadata().Reflector().ForObject(obj);
					str += "\n" + claxx.GetName() + "\n" + objToString;
				}
				return str;
			}
			catch (Exception)
			{
			}
			return "Exception in YapObject analyzer";
		}
	}
}
