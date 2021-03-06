/* Copyright (C) 2004 - 2011  Versant Inc.  http://www.db4o.com */

using Db4objects.Db4o.Activation;
using Db4objects.Db4o.Tests.Common.TA;

namespace Db4objects.Db4o.Tests.Common.TA.TA
{
	public class TAIntItem : ActivatableImpl
	{
		public int value;

		public object obj;

		public int i;

		public TAIntItem()
		{
		}

		public virtual int Value()
		{
			Activate(ActivationPurpose.Read);
			return value;
		}

		public virtual int IntegerValue()
		{
			Activate(ActivationPurpose.Read);
			return i;
		}

		public virtual object Object()
		{
			Activate(ActivationPurpose.Read);
			return obj;
		}
	}
}
