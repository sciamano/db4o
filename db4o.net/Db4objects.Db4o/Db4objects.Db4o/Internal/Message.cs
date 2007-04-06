using System.IO;
using Db4objects.Db4o;
using Db4objects.Db4o.Internal;
using Db4objects.Db4o.Internal.Handlers;

namespace Db4objects.Db4o.Internal
{
	internal sealed class Message
	{
		internal readonly TextWriter stream;

		internal Message(ObjectContainerBase a_stream, string msg)
		{
			stream = a_stream.ConfigImpl().OutStream();
			Print(msg, true);
		}

		internal Message(string a_StringParam, int a_intParam, TextWriter a_stream, bool 
			header)
		{
			stream = a_stream;
			Print(Db4objects.Db4o.Internal.Messages.Get(a_intParam, a_StringParam), header);
		}

		internal Message(string a_StringParam, int a_intParam, TextWriter a_stream) : this
			(a_StringParam, a_intParam, a_stream, true)
		{
		}

		private void Print(string msg, bool header)
		{
			if (stream != null)
			{
				if (header)
				{
					stream.WriteLine("[" + Db4oFactory.Version() + "   " + DateHandler.Now() + "] ");
				}
				stream.WriteLine(" " + msg);
			}
		}
	}
}
