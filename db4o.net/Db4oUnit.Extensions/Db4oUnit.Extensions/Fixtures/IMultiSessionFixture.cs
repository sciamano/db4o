/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using Db4oUnit.Extensions;
using Db4objects.Db4o.Ext;

namespace Db4oUnit.Extensions.Fixtures
{
	public interface IMultiSessionFixture : IDb4oFixture
	{
		IExtObjectContainer OpenNewSession();
	}
}