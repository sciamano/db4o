﻿/* Copyright (C) 2011 Versant Inc.   http://www.db4o.com */
using System;
using System.IO;
using Db4objects.Db4o.IO;

namespace Db4objects.Db4o.Internal.CLI
{
	internal class Silverlight3 : SilverlightCLIBase
	{
		public override void Flush(FileStream stream)
		{
			stream.Flush();
		}
	}
}