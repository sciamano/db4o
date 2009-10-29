﻿/* Copyright (C) 2009 Versant Inc.   http://www.db4o.com */
using System;
using Db4oUnit.Extensions;

namespace Db4objects.Db4o.Tests.CLI1.CrossPlatform
{
	public class AllTests : Db4oTestSuite
	{
		protected override Type[] TestCases()
		{
			return new Type[]
			       	{
#if !SILVERLIGHT
						typeof(DotnetServerCrossplatformTestCase),
			       		typeof(JavaServerCrossplatformTestCase),
						typeof(JavaSupporAliasesTestCase),
#endif
			       	};
		}
	}
}
