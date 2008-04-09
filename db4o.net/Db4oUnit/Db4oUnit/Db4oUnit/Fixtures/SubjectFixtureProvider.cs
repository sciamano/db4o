/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com */

using System.Collections;
using Db4oUnit.Fixtures;
using Db4objects.Db4o.Foundation;

namespace Db4oUnit.Fixtures
{
	public class SubjectFixtureProvider : IFixtureProvider
	{
		public static object Value()
		{
			return _variable.Value;
		}

		private static readonly FixtureVariable _variable = new FixtureVariable("subject"
			);

		private readonly IEnumerable _values;

		public SubjectFixtureProvider(IEnumerable values)
		{
			_values = values;
		}

		public SubjectFixtureProvider(object[] values)
		{
			_values = Iterators.Iterable(values);
		}

		public virtual FixtureVariable Variable()
		{
			return _variable;
		}

		public virtual IEnumerator GetEnumerator()
		{
			return _values.GetEnumerator();
		}
	}
}
