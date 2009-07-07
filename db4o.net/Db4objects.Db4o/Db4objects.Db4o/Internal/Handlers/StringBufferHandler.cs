/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using System.Text;
using Db4objects.Db4o.Internal.Handlers;

namespace Db4objects.Db4o.Internal.Handlers
{
	public sealed class StringBufferHandler : StringBasedValueTypeHandlerBase
	{
		public StringBufferHandler() : base(typeof(StringBuilder))
		{
		}

		protected override object ConvertString(string str)
		{
			return new StringBuilder(str);
		}

		protected override string ConvertObject(object obj)
		{
			return ((StringBuilder)obj).ToString();
		}
	}
}
