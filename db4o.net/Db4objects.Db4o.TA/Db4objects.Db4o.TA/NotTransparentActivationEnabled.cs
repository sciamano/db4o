/* Copyright (C) 2004 - 2007  db4objects Inc.  http://www.db4o.com */

using Db4objects.Db4o.Diagnostic;
using Db4objects.Db4o.Internal;

namespace Db4objects.Db4o.TA
{
	public class NotTransparentActivationEnabled : DiagnosticBase
	{
		private ClassMetadata _class;

		public NotTransparentActivationEnabled(ClassMetadata clazz)
		{
			_class = clazz;
		}

		public override string Problem()
		{
			return "An object of class " + _class + " was stored. Instances of this class very likely are not subject to transparent activation.";
		}

		public override object Reason()
		{
			return _class;
		}

		public override string Solution()
		{
			return "Use a TA aware class with equivalent functionality or ensure that this class provides a sensible implementation of the Activatable interface and the implicit TA hooks, either manually or by applying instrumentation.";
		}
	}
}
