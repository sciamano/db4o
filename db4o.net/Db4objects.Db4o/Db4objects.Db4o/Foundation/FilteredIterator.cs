using System.Collections;
using Db4objects.Db4o.Foundation;

namespace Db4objects.Db4o.Foundation
{
	public class FilteredIterator : MappingIterator
	{
		private readonly IPredicate4 _filter;

		public FilteredIterator(IEnumerator iterator, IPredicate4 filter) : base(iterator
			)
		{
			_filter = filter;
		}

		protected override object Map(object current)
		{
			return _filter.Match(current) ? current : MappingIterator.SKIP;
		}
	}
}
