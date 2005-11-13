/* Copyright (C) 2004 - 2005  db4objects Inc.  http://www.db4o.com */

package com.db4o.query;

import java.io.*;
import java.lang.reflect.*;

/**
 * Base class for native queries.
 * <br><br>Native Queries provide the ability to run one or more lines
 * of code against all instances of a class. Native query expressions should
 * return true to mark specific instances as part of the result set. 
 * db4o will  attempt to optimize native query expressions and run them 
 * against indexes and without instantiating actual objects, where this is 
 * possible.<br><br>
 * The syntax of the enclosing object for the native query expression varies
 * slightly, depending on the language version used. Here are some examples,
 * how a simple native query will look like in some of the programming languages and
 * dialects that db4o supports:<br><br>
 * 
 * <code>
 * <b>// C# .NET 2.0</b><br>
 * IList &lt;Cat&gt; cats = db.Query &lt;Cat&gt; (delegate(Cat cat) {<br>
 * &#160;&#160;&#160;return cat.Name == "Occam";<br>
 * });<br>
 * <br>
 *<br>
 * <b>// Java JDK 5</b><br>
 * List &lt;Cat&gt; cats = db.query(new Predicate&lt;Cat&gt;() {<br>
 * &#160;&#160;&#160;public boolean match(Cat cat) {<br>
 * &#160;&#160;&#160;&#160;&#160;&#160;return cat.getName().equals("Occam");<br>
 * &#160;&#160;&#160;}<br>
 * });<br>
 * <br>
 * <br>
 * <b>// Java JDK 1.2 to 1.4</b><br>
 * List cats = db.query(new Predicate() {<br>
 * &#160;&#160;&#160;public boolean match(Cat cat) {<br>
 * &#160;&#160;&#160;&#160;&#160;&#160;return cat.getName().equals("Occam");<br>
 * &#160;&#160;&#160;}<br>
 * });<br>
 * <br>
 * <br>
 * <b>// Java JDK 1.1</b><br>
 * ObjectSet cats = db.query(new CatOccam());<br>
 * <br>
 * public static class CatOccam extends Predicate {<br>
 * &#160;&#160;&#160;public boolean match(Cat cat) {<br>
 * &#160;&#160;&#160;&#160;&#160;&#160;return cat.getName().equals("Occam");<br>
 * &#160;&#160;&#160;}<br>
 * });<br>
 * <br>
 * <br>     
 * <b>// C# .NET 1.1</b><br>
 * IList cats = db.Query(new CatOccam());<br>
 * <br>
 * public class CatOccam : Predicate {<br>
 * &#160;&#160;&#160;public boolean Match(Cat cat) {<br>
 * &#160;&#160;&#160;&#160;&#160;&#160;return cat.Name == "Occam";<br>
 * &#160;&#160;&#160;}<br>
 * });<br>
 * </code>
 * <br>
 * Summing up the above:<br>
 * In order to run a Native Query, you can<br>
 * - use the delegate notation for .NET 2.0.<br>
 * - extend the Predicate class for all other language dialects<br><br>
 * A class that extends Predicate is required to 
 * implement the #match() / #Match() method, following the native query
 * conventions:<br>
 * - The name of the method is "#match()" (Java) / "#Match()" (.NET).<br>
 * - The method must be public public.<br>
 * - The method returns a boolean.<br>
 * - The method takes one parameter.<br>
 * - The Type (.NET) / Class (Java) of the parameter specifies the extent.<br>
 * - For all instances of the extent that are to be included into the
 * resultset of the query, the match method should return true. For all
 * instances that are not to be included, the match method should return
 * false.<br><br>
 */
public abstract class Predicate<ExtentType> implements Serializable{
    
    /**
     * public for implementation reasons, please ignore.
     */
	public final static String PREDICATEMETHOD_NAME="match";
	
	private transient Method cachedFilterMethod=null;
	
	private Method getFilterMethod() {
		if(cachedFilterMethod!=null) {
			return cachedFilterMethod;
		}
		Method[] methods=getClass().getMethods();
		for (int methodIdx = 0; methodIdx < methods.length; methodIdx++) {
			Method method=methods[methodIdx];
			if((method.getName().equals(PREDICATEMETHOD_NAME))&&method.getParameterTypes().length==1) {					
				String targetName=method.getParameterTypes()[0].getName();
				if(!"java.lang.Object".equals(targetName)) {
					cachedFilterMethod=method;
					return method;
				}
			}
		}
		throw new IllegalArgumentException("Invalid predicate.");
	}

    /**
     * public for implementation reasons, please ignore.
     */
	public Class<ExtentType> extentType() {
		return (Class<ExtentType>)getFilterMethod().getParameterTypes()[0];
	}

    /**
     * The match method that needs to be implemented by the user.
     * @param candidate the candidate object passed from db4o 
     * @return true to include an object in the resulting ObjectSet
     */
	public abstract boolean match(ExtentType candidate);
	
    /**
     * public for implementation reasons, please ignore.
     */
	public boolean appliesTo(ExtentType candidate) {
		try {
			Method filterMethod=getFilterMethod();
			filterMethod.setAccessible(true);
			Object ret=filterMethod.invoke(this,new Object[]{candidate});
			return ((Boolean)ret).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
