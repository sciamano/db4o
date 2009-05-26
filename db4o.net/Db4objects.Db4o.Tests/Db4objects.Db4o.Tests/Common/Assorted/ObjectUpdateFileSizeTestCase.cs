/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using Db4oUnit;
using Db4oUnit.Extensions;
using Db4oUnit.Extensions.Fixtures;
using Db4objects.Db4o.Tests.Common.Assorted;

namespace Db4objects.Db4o.Tests.Common.Assorted
{
	public class ObjectUpdateFileSizeTestCase : AbstractDb4oTestCase, IOptOutCS, IOptOutDefragSolo
	{
		public static void Main(string[] args)
		{
			new ObjectUpdateFileSizeTestCase().RunAll();
		}

		public class Item
		{
			public string _name;

			public Item(string name)
			{
				_name = name;
			}
		}

		/// <exception cref="System.Exception"></exception>
		protected override void Store()
		{
			ObjectUpdateFileSizeTestCase.Item item = new ObjectUpdateFileSizeTestCase.Item("foo"
				);
			Store(item);
		}

		/// <exception cref="System.Exception"></exception>
		public virtual void TestFileSize()
		{
			WarmUp();
			AssertFileSizeConstant();
		}

		/// <exception cref="System.Exception"></exception>
		private void AssertFileSizeConstant()
		{
			Defragment();
			long beforeUpdate = DbSize();
			for (int i = 0; i < 15; ++i)
			{
				UpdateItem();
			}
			Defragment();
			long afterUpdate = DbSize();
			Assert.IsTrue(afterUpdate - beforeUpdate < 2);
		}

		/// <exception cref="System.Exception"></exception>
		/// <exception cref="System.IO.IOException"></exception>
		private void WarmUp()
		{
			for (int i = 0; i < 3; ++i)
			{
				UpdateItem();
			}
		}

		/// <exception cref="System.Exception"></exception>
		/// <exception cref="System.IO.IOException"></exception>
		private void UpdateItem()
		{
			ObjectUpdateFileSizeTestCase.Item item = ((ObjectUpdateFileSizeTestCase.Item)RetrieveOnlyInstance
				(typeof(ObjectUpdateFileSizeTestCase.Item)));
			Store(item);
			Db().Commit();
		}

		private long DbSize()
		{
			return Db().SystemInfo().TotalSize();
		}
	}
}