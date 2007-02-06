namespace Db4objects.Db4o.Internal.Marshall
{
	public class PrimitiveMarshaller0 : Db4objects.Db4o.Internal.Marshall.PrimitiveMarshaller
	{
		public override bool UseNormalClassRead()
		{
			return true;
		}

		public override int WriteNew(Db4objects.Db4o.Internal.Transaction trans, Db4objects.Db4o.Internal.PrimitiveFieldHandler
			 yapClassPrimitive, object obj, bool topLevel, Db4objects.Db4o.Internal.StatefulBuffer
			 parentWriter, bool withIndirection, bool restoreLinkOffset)
		{
			int id = 0;
			if (obj != null)
			{
				Db4objects.Db4o.Internal.ITypeHandler4 handler = yapClassPrimitive.i_handler;
				Db4objects.Db4o.Internal.ObjectContainerBase stream = trans.Stream();
				id = stream.NewUserObject();
				int address = -1;
				int length = ObjectLength(handler);
				if (!stream.IsClient())
				{
					address = trans.i_file.GetSlot(length);
				}
				trans.SetPointer(id, address, length);
				Db4objects.Db4o.Internal.StatefulBuffer writer = new Db4objects.Db4o.Internal.StatefulBuffer
					(trans, length);
				writer.UseSlot(id, address, length);
				writer.WriteInt(yapClassPrimitive.GetID());
				handler.WriteNew(_family, obj, false, writer, true, false);
				writer.WriteEnd();
				stream.WriteNew(yapClassPrimitive, writer);
			}
			if (parentWriter != null)
			{
				parentWriter.WriteInt(id);
			}
			return id;
		}

		public override Sharpen.Util.Date ReadDate(Db4objects.Db4o.Internal.Buffer a_bytes
			)
		{
			long longValue = Db4objects.Db4o.Internal.Handlers.LongHandler.ReadLong(a_bytes);
			if (longValue == long.MaxValue)
			{
				return null;
			}
			return new Sharpen.Util.Date(longValue);
		}
	}
}
