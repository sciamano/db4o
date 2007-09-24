/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4oUnit;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Handlers;
using Db4objects.Db4o.Internal.Slots;
using Db4objects.Db4o.Tests.Common.Handlers;

namespace Db4objects.Db4o.Tests.Common.Handlers
{
	public class StringHandlerTestCase : TypeHandlerTestCaseBase
	{
		public static void Main(string[] arguments)
		{
			new StringHandlerTestCase().RunSolo();
		}

		public class Item
		{
			public string _string;

			public Item(string s)
			{
				_string = s;
			}

			public override bool Equals(object obj)
			{
				if (obj == this)
				{
					return true;
				}
				if (!(obj is StringHandlerTestCase.Item))
				{
					return false;
				}
				StringHandlerTestCase.Item other = (StringHandlerTestCase.Item)obj;
				return this._string.Equals(other._string);
			}

			public override int GetHashCode()
			{
				int hash = 7;
				hash = 31 * hash + (null == _string ? 0 : _string.GetHashCode());
				return hash;
			}

			public override string ToString()
			{
				return "[" + _string + "]";
			}
		}

		public virtual void TestIndexMarshalling()
		{
			Db4objects.Db4o.Internal.Buffer reader = new Db4objects.Db4o.Internal.Buffer(2 * 
				Const4.INT_LENGTH);
			Slot original = new Slot(unchecked((int)(0xdb)), unchecked((int)(0x40)));
			StringHandler().WriteIndexEntry(reader, original);
			reader._offset = 0;
			Slot retrieved = (Slot)StringHandler().ReadIndexEntry(reader);
			Assert.AreEqual(original.Address(), retrieved.Address());
			Assert.AreEqual(original.Length(), retrieved.Length());
		}

		private StringHandler StringHandler()
		{
			return new StringHandler2(Stream());
		}

		public virtual void TestReadWrite()
		{
			MockWriteContext writeContext = new MockWriteContext(Db());
			StringHandler().Write(writeContext, "one");
			MockReadContext readContext = new MockReadContext(writeContext);
			string str = (string)StringHandler().Read(readContext);
			Assert.AreEqual("one", str);
		}

		public virtual void TestStoreObject()
		{
			DoTestStoreObject(new StringHandlerTestCase.Item("one"));
		}
	}
}
