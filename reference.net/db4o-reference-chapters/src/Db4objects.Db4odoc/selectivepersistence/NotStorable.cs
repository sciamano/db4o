using Db4objects.Db4o.Types;

namespace Db4objects.Db4odoc.selectivepersistence
{
    class NotStorable: ITransientClass
    {
        public override string ToString()
        {
            return "NotStorable class";
        } 
    }
}
