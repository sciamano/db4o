/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com */

using Db4objects.Db4o.CS.Internal.Messages;

namespace Db4objects.Db4o.CS.Internal.Messages
{
	/// <exclude></exclude>
	public class MIsAlive : Msg, IMessageWithResponse
	{
		public virtual bool ProcessAtServer()
		{
			Write(Msg.IsAlive);
			return true;
		}
	}
}