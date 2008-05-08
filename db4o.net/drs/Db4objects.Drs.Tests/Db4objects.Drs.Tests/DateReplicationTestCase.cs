/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com */

using System;
using Db4oUnit;
using Db4objects.Db4o;
using Db4objects.Drs.Tests;
using Sharpen.Util;

namespace Db4objects.Drs.Tests
{
	public class DateReplicationTestCase : DrsTestCase
	{
		public sealed class Item
		{
			public DateTime _date1;

			public DateTime _date2;

			public DateTime[] _dateArray;

			public Item(DateTime date1, DateTime date2)
			{
				_date1 = date1;
				_date2 = date2;
				_dateArray = new DateTime[] { _date1, _date2 };
			}

			public override bool Equals(object obj)
			{
				DateReplicationTestCase.Item other = (DateReplicationTestCase.Item)obj;
				if (!other._date1.Equals(_date1))
				{
					return false;
				}
				if (!other._date2.Equals(_date2))
				{
					return false;
				}
				return Arrays.Equals(_dateArray, other._dateArray);
			}
		}

		public virtual void Test()
		{
			DateReplicationTestCase.Item item1 = new DateReplicationTestCase.Item(new DateTime
				(1988, 7, 4), new DateTime(1999, 12, 31));
			DateReplicationTestCase.Item item2 = new DateReplicationTestCase.Item(new DateTime
				(1995, 7, 12), new DateTime(2001, 11, 8));
			A().Provider().StoreNew(item1);
			A().Provider().StoreNew(item2);
			A().Provider().Commit();
			ReplicateAll(A().Provider(), B().Provider());
			IObjectSet found = B().Provider().GetStoredObjects(typeof(DateReplicationTestCase.Item
				));
			Iterator4Assert.SameContent(new object[] { item2, item1 }, ReplicationTestPlatform
				.Adapt(found.GetEnumerator()));
		}
	}
}