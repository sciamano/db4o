using System;

namespace com.db4o.reflect.net
{

	/// <remarks>Reflection implementation for Array to map to .NET reflection.</remarks>
	public class NetArray : com.db4o.reflect.ReflectArray
	{
		private readonly com.db4o.reflect.Reflector _reflector;

		internal NetArray(com.db4o.reflect.Reflector reflector)
		{
			_reflector = reflector;
		}

        public virtual int[] dimensions(object obj) {
            System.Array array = (System.Array)obj;
            int[] dim = new int[array.Rank];
            for(int i = 0; i < dim.Length; i ++){
                dim[i] = array.GetLength(i);
            }
            return dim;
        }

      
        public virtual int flatten(
            object shaped,
            int[] dimensions,
            int currentDimension,
            object[] flat,
            int flatElement) {
            int[] currentDimensions = new int[dimensions.Length];
            flatten1((System.Array)shaped, dimensions, 0, currentDimensions, flat, 0);
            return 0;
        }

        protected virtual int flatten1(
            System.Array shaped,
            int[] allDimensions,
            int currentDimension,
            int[] currentDimensions,
            object[] flat,
            int flatElement) {
            if (currentDimension == (allDimensions.Length - 1)) {
                for (currentDimensions[currentDimension] = 0; currentDimensions[currentDimension] < allDimensions[currentDimension]; currentDimensions[currentDimension]++) {
                    flat[flatElement++] = shaped.GetValue(currentDimensions);
                }
            }else{
                for (currentDimensions[currentDimension] = 0; currentDimensions[currentDimension] < allDimensions[currentDimension]; currentDimensions[currentDimension]++) {
                    flatElement =
                        flatten1(
                        shaped,
                        allDimensions,
                        currentDimension + 1,
                        currentDimensions,
                        flat,
                        flatElement);
                }
            }
            return flatElement;
        }

		public virtual object get(object onArray, int index)
		{
            return ((System.Array)onArray).GetValue(index);
		}

		public virtual com.db4o.reflect.ReflectClass getComponentType(com.db4o.reflect.ReflectClass
			 a_class)
		{
            return a_class.getComponentType();
		}

		public virtual int getLength(object array)
		{
            return ((System.Array)array).GetLength(0);
		}

		public virtual bool isNDimensional(com.db4o.reflect.ReflectClass a_class)
		{
			Type type = getNetType(a_class);
			return getArrayRank(type) > 1;
		}

		private static Type getNetType(ReflectClass a_class)
		{
			return ((NetClass)a_class).getNetType();
		}

		private int getArrayRank(Type type)
		{
#if CF_1_0 || CF_2_0
			return ((j4o.lang.ArrayTypeReference)j4o.lang.TypeReference.FromType(type)).Rank;
#else
			return type.GetArrayRank();
#endif
		}

		public virtual object newInstance(com.db4o.reflect.ReflectClass componentType, int
			 length)
		{
            return System.Array.CreateInstance(getNetType(componentType), length);
		}

		public virtual object newInstance(com.db4o.reflect.ReflectClass componentType, int[]
			 dimensions)
		{
            return System.Array.CreateInstance(getNetType(componentType), dimensions);
		}

		public virtual void set(object onArray, int index, object element)
		{
            ((System.Array)onArray).SetValue(element, index);
		}

        public virtual int shape(
            object[] flat,
            int flatElement,
            object shaped,
            int[] allDimensions,
            int currentDimension) {
            int[] currentDimensions = new int[allDimensions.Length];
            shape1(flat, 0, (System.Array)shaped, allDimensions, 0, currentDimensions);
            return 0;
        }

        public virtual int shape1(
            object[] flat,
            int flatElement,
            System.Array shaped,
            int[] allDimensions,
            int currentDimension,
            int[] currentDimensions) {
            if (currentDimension == (allDimensions.Length - 1)) {
                for (currentDimensions[currentDimension] = 0; currentDimensions[currentDimension] < allDimensions[currentDimension]; currentDimensions[currentDimension]++) {
                    shaped.SetValue(flat[flatElement++], currentDimensions);
                }
            }else{
                for (currentDimensions[currentDimension] = 0; currentDimensions[currentDimension] < allDimensions[currentDimension]; currentDimensions[currentDimension]++) {
                    flatElement =
                        shape1(
                        flat,
                        flatElement,
                        shaped,
                        allDimensions,
                        currentDimension + 1,
                        currentDimensions
                        );
                }
            }
            return flatElement;
        }
    }
}
