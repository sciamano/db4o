/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o.Activation;
using Db4objects.Db4o.Tests.Common.TA;

namespace Db4objects.Db4o.Tests.Common.TA.Collections
{
	public class Page : ActivatableImpl
	{
		public const int Pagesize = 100;

		private object[] _data = new object[Pagesize];

		private int _top = 0;

		private int _pageIndex;

		[System.NonSerialized]
		private bool _dirty = false;

		public Page(int pageIndex)
		{
			_pageIndex = pageIndex;
		}

		public virtual bool Add(object obj)
		{
			Activate(ActivationPurpose.Read);
			_dirty = true;
			_data[_top++] = obj;
			return true;
		}

		public virtual int Size()
		{
			Activate(ActivationPurpose.Read);
			return _top;
		}

		public virtual object Get(int indexInPage)
		{
			Activate(ActivationPurpose.Read);
			_dirty = true;
			return _data[indexInPage];
		}

		public virtual bool IsDirty()
		{
			return _dirty;
		}

		public virtual void SetDirty(bool dirty)
		{
			_dirty = dirty;
		}

		public virtual int GetPageIndex()
		{
			Activate(ActivationPurpose.Read);
			return _pageIndex;
		}

		public virtual bool AtCapacity()
		{
			return Capacity() == 0;
		}

		public virtual int Capacity()
		{
			Activate(ActivationPurpose.Read);
			return Db4objects.Db4o.Tests.Common.TA.Collections.Page.Pagesize - Size();
		}
	}
}
