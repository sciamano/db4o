/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4oUnit;
using Db4oUnit.Extensions;
using Db4objects.Db4o.Tests.Common.Sampledata;
using Db4objects.Db4o.Tests.Common.Types.Arrays;

namespace Db4objects.Db4o.Tests.Common.Types.Arrays
{
	public class TypedDerivedArrayTestCase : AbstractDb4oTestCase
	{
		private static readonly MoleculeData[] ARRAY = new MoleculeData[] { new MoleculeData
			("TypedDerivedArray") };

		public class Data
		{
			public AtomData[] _array;

			public Data(AtomData[] AtomDatas)
			{
				this._array = AtomDatas;
			}
		}

		protected override void Store()
		{
			Db().Set(new TypedDerivedArrayTestCase.Data(ARRAY));
		}

		public virtual void Test()
		{
			TypedDerivedArrayTestCase.Data data = (TypedDerivedArrayTestCase.Data)RetrieveOnlyInstance
				(typeof(TypedDerivedArrayTestCase.Data));
			Assert.IsTrue(data._array is MoleculeData[], "Expected instance of " + typeof(MoleculeData[])
				 + ", but got " + data._array);
			ArrayAssert.AreEqual(ARRAY, data._array);
		}
	}
}
