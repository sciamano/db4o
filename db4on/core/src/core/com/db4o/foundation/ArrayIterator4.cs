namespace com.db4o.foundation
{
	public class ArrayIterator4 : com.db4o.foundation.Iterator4
	{
		internal object[] _elements;

		internal int _next;

		public ArrayIterator4(object[] elements)
		{
			_elements = elements;
			_next = -1;
		}

		public virtual bool MoveNext()
		{
			if (_next < LastIndex())
			{
				++_next;
				return true;
			}
			_next = _elements.Length;
			return false;
		}

		public virtual object Current()
		{
			return _elements[_next];
		}

		private int LastIndex()
		{
			return _elements.Length - 1;
		}
	}
}
