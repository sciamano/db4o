namespace com.db4o.foundation
{
	/// <exclude></exclude>
	public class IntIterator4Adaptor : com.db4o.foundation.IntIterator4
	{
		private readonly System.Collections.IEnumerator _iterator;

		public IntIterator4Adaptor(System.Collections.IEnumerator iterator)
		{
			_iterator = iterator;
		}

		public virtual int CurrentInt()
		{
			return ((int)Current);
		}

		public virtual object Current
		{
			get
			{
				return _iterator.Current;
			}
		}

		public virtual bool MoveNext()
		{
			return _iterator.MoveNext();
		}

		public virtual void Reset()
		{
			_iterator.Reset();
		}
	}
}
