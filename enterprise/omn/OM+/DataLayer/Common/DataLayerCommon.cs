using System;
using System.Collections;
using System.Collections.Generic;
using Db4objects.Db4o;
using Db4objects.Db4o.Ext;
using Db4objects.Db4o.Reflect;
using Db4objects.Db4o.Reflect.Generic;
using OManager.BusinessLayer.Common;
using OManager.DataLayer.Connection;
using OME.Logging.Common;

namespace OManager.DataLayer.CommonDatalayer
{
    public class DataLayerCommon
    {
    	public static string Db4oVersion
    	{
			get { return Db4oFactory.Version(); }
    	}

        public static IReflectField GetDeclaredFieldInHeirarchy(IReflectClass aClass, string attribute)
        {
            try
            {
                IReflectField refField = null;
                if (aClass == null)
                    return null;

                while (aClass != null)
                {

                    refField = GetDeclaredField(aClass, attribute);
                    if (refField != null)
                        break;
                    aClass = aClass.GetSuperclass();

                }
                return refField;
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;

        }

        public static IStoredField GetDeclaredStoredFieldInHeirarchy(IStoredClass aClass, string attribute)
        {
            try
            {
                IStoredField sField = null;
                if (aClass == null)
                    return null;

                while (aClass != null)
                {

                    sField = aClass.StoredField(attribute, null);
                    if (sField != null)
                        break;
                    aClass = aClass.GetParentStoredClass();

                }
                return sField;
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;

        }

        public static IReflectField GetDeclaredField(IReflectClass aClass, string attribute)
        {

            try
            {
                IReflectField fields = aClass.GetDeclaredField(attribute);
                return fields;
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;
        }

        public static IReflectField[] GetDeclaredFieldsInHeirarchy(IReflectClass aClass) //67
        {
            try
            {
                return GetDeclaredFieldsListInHeirarchy(aClass).ToArray();
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;
        }

        public static List<IReflectField> GetDeclaredFieldsListInHeirarchy(IReflectClass aClass)
        {
            try
            {
                if (aClass == null)
                    return null;

                List<IReflectField> ret = GetDeclaredFieldsList(aClass);
                IReflectClass parent = aClass.GetSuperclass();
                if (parent != null && !(parent.GetName().StartsWith("System.")))
                    ret.AddRange(GetDeclaredFieldsListInHeirarchy(parent));

				return ret;
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;
        }

        public static List<IReflectField> GetDeclaredFieldsList(IReflectClass aClass)
        {
            try
            {
                List<IReflectField> ret = new List<IReflectField>();
            	foreach (IReflectField field in aClass.GetDeclaredFields())
            	{
					if (!(field is GenericVirtualField))
						ret.Add(field);
            	}
                return ret;
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;
        }
       

        public static string RemoveGFromClassName(string name)
        {
			return name.Contains("(G) ") ? name.Replace("(G) ", "") : name;
        }

        public static IReflectClass ReturnReflectClass(string classname)
        {
            try
            {
                IObjectContainer objectContainer = Db4oClient.Client;
                if (classname != string.Empty)
                {
                   classname= RemoveGFromClassName(classname);
                    return objectContainer.Ext().Reflector().ForName(classname);
                }
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;
        }
        public static IReflectClass returnReflectClassfromObject(object obj)
        {
            try
            {
                if (obj != null)
                {
                    IObjectContainer objectContainer = Db4oClient.Client;                   

                    return objectContainer.Ext().Reflector().ForObject(obj);
                }
            }
            catch (Exception e)
            {
                LoggingHelper.HandleException(e);
            }
            return null;
           
        }
        public static bool IsCollection(object expandedObj)
        {
            try
            {
                if (expandedObj != null)
                {
                    IObjectContainer objectContainer = Db4oClient.Client;
                    IReflectClass refClass = objectContainer.Ext().Reflector().ForObject(expandedObj);
                    return refClass.IsCollection();
                }
                return false; 
            }
            catch (Exception oEx)
            {
                LoggingHelper.HandleException(oEx);
                return false;
            }
        }

        public static bool IsArray(object expandedObj)
        {
            try
            {
                if (expandedObj != null)
                {
                    IReflectClass refClass = returnReflectClassfromObject(expandedObj);
                    if (refClass != null)
                    {
                        return refClass.IsArray();
                    }
                }
                return false; 

            }
            catch (Exception oEx)
            {
                LoggingHelper.HandleException(oEx);
                return false;
            }
        }


        public static  bool IsPrimitive(object expandedObj)
        {
            try
            {
               IReflectClass refClass = returnReflectClassfromObject(expandedObj);// objectContainer.Ext().Reflector().ForObject(expandedObj);              
               if (refClass != null)
               {
                   string type = PrimitiveType(refClass.GetName());
                   return CommonValues.IsPrimitive(type);
               }
               return false;
            }
            catch (Exception oEx)
            {
                LoggingHelper.HandleException(oEx);
                return false;
            }
        }
        public static bool CheckForDatetimeOrString(object expandedObj)
        {
            try
            {
                IReflectClass refClass = returnReflectClassfromObject(expandedObj);// objectContainer.Ext().Reflector().ForObject(expandedObj);              
                if (refClass != null)
                {
                    string type = PrimitiveType(refClass.GetName());
                    return CommonValues.IsDateTimeOrString(type); 
                }
                return false;
            }
            catch (Exception oEx)
            {
                LoggingHelper.HandleException(oEx);
                return false;
            }
        }

        public static string PrimitiveType(string type)
        {
            if (type.Contains(","))
            {
                int Index = type.LastIndexOf(',');
                type = type.Substring(0, Index);
            }
            return type;
        }
       
    }
}
