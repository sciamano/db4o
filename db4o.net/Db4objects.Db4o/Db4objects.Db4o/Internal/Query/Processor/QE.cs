/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Query.Processor;
using Db4objects.Db4o.Types;

namespace Db4objects.Db4o.Internal.Query.Processor
{
	/// <summary>Query Evaluator - Represents such things as &gt;, &gt;=, &lt;, &lt;=, EQUAL, LIKE, etc.
	/// 	</summary>
	/// <remarks>Query Evaluator - Represents such things as &gt;, &gt;=, &lt;, &lt;=, EQUAL, LIKE, etc.
	/// 	</remarks>
	/// <exclude></exclude>
	public class QE : IUnversioned
	{
		internal static readonly QE DEFAULT = new QE();

		public const int NULLS = 0;

		public const int SMALLER = 1;

		public const int EQUAL = 2;

		public const int GREATER = 3;

		internal virtual QE Add(QE evaluator)
		{
			return evaluator;
		}

		public virtual bool Identity()
		{
			return false;
		}

		internal virtual bool IsDefault()
		{
			return true;
		}

		internal virtual bool Evaluate(QConObject a_constraint, QCandidate a_candidate, object
			 a_value)
		{
			if (a_value == null)
			{
				return a_constraint.GetComparator(a_candidate) is Null;
			}
			return a_constraint.GetComparator(a_candidate).IsEqual(a_value);
		}

		public override bool Equals(object obj)
		{
			return obj != null && obj.GetType() == this.GetType();
		}

		public override int GetHashCode()
		{
			return GetType().GetHashCode();
		}

		internal virtual bool Not(bool res)
		{
			return res;
		}

		/// <summary>Specifies which part of the index to take.</summary>
		/// <remarks>
		/// Specifies which part of the index to take.
		/// Array elements:
		/// [0] - smaller
		/// [1] - equal
		/// [2] - greater
		/// [3] - nulls
		/// </remarks>
		/// <param name="bits"></param>
		public virtual void IndexBitMap(bool[] bits)
		{
			bits[QE.EQUAL] = true;
		}

		public virtual bool SupportsIndex()
		{
			return true;
		}
	}
}
