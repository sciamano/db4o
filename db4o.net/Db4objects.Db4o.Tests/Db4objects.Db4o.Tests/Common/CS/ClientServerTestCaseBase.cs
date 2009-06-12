/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

#if !SILVERLIGHT
using Db4oUnit.Extensions;
using Db4objects.Db4o.CS.Internal;
using Db4objects.Db4o.Foundation;

namespace Db4objects.Db4o.Tests.Common.CS
{
	public class ClientServerTestCaseBase : Db4oClientServerTestCase
	{
		protected virtual IServerMessageDispatcher ServerDispatcher()
		{
			ObjectServerImpl serverImpl = (ObjectServerImpl)ClientServerFixture().Server();
			return (IServerMessageDispatcher)Iterators.Next(serverImpl.IterateDispatchers());
		}

		protected virtual ClientObjectContainer Client()
		{
			return (ClientObjectContainer)Db();
		}
	}
}
#endif // !SILVERLIGHT
