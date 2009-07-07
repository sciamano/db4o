/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using Db4objects.Db4o.Marshall;
using Db4objects.Db4o.Typehandlers;

namespace Db4objects.Db4o.Marshall
{
	/// <summary>
	/// this interface is passed to internal class
	/// <see cref="com.db4o.internal.TypeHandler4">com.db4o.internal.TypeHandler4</see>
	/// when instantiating objects.
	/// </summary>
	public interface IReadContext : IContext, IReadBuffer
	{
		/// <summary>
		/// Interprets the current position in the context as
		/// an ID and returns the object with this ID.
		/// </summary>
		/// <remarks>
		/// Interprets the current position in the context as
		/// an ID and returns the object with this ID.
		/// </remarks>
		/// <returns>the object</returns>
		object ReadObject();

		/// <summary>
		/// reads sub-objects, in cases where the
		/// <see cref="com.db4o.internal.TypeHandler4">com.db4o.internal.TypeHandler4</see>
		/// is known.
		/// </summary>
		object ReadObject(ITypeHandler4 handler);
	}
}
