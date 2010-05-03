/* Copyright (C) 2004 - 2009  Versant Inc.  http://www.db4o.com */

using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Activation;

namespace Db4objects.Db4o.Internal.Activation
{
	public class LegacyUnspecifiedUpdateDepth : UnspecifiedUpdateDepth
	{
		public static readonly Db4objects.Db4o.Internal.Activation.LegacyUnspecifiedUpdateDepth
			 Instance = new Db4objects.Db4o.Internal.Activation.LegacyUnspecifiedUpdateDepth
			();

		private LegacyUnspecifiedUpdateDepth()
		{
		}

		public override bool CanSkip(ClassMetadata clazz)
		{
			return false;
		}

		protected override FixedUpdateDepth ForDepth(int depth)
		{
			return new LegacyFixedUpdateDepth(depth);
		}
	}
}