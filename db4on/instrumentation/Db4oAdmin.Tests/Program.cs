/* Copyright (C) 2004 - 2006  db4objects Inc.   http://www.db4o.com */

using System;
using Db4oUnit;

namespace Db4oAdmin.Tests
{
	class Program
	{	
	    public static int Main(string[] args)
		{
	        Type[] tests = new Type[]
	            {
	                typeof (ILPatternTestCase),
	                typeof (CFNQRuntimeOptimizationTestCase),
	                typeof (PredicateBuildTimeOptimizationTestCase),
	            };

            try
            {
                ReflectionTestSuiteBuilder builder = new ReflectionTestSuiteBuilder(tests);
                return new TestRunner(builder).Run();
            }
	        catch (Exception x)
	        {
	            Console.WriteLine(x);
                return 255;
	        }
		}
	}
}
