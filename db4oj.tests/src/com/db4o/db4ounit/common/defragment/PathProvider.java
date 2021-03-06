/* Copyright (C) 2004 - 2006 Versant Inc. http://www.db4o.com */

package com.db4o.db4ounit.common.defragment;

import java.io.*;

import com.db4o.db4ounit.util.*;


/**
 * @sharpen.ignore
 */
@decaf.Ignore(decaf.Platform.JDK11)
public class PathProvider {
	/**
	 * @return the folder where the compiled test case classes can be found
	 */
	public static File testCasePath() {
		return WorkspaceServices.configurableWorkspacePath("db4ojdk1.2.bin", "db4ojdk1.2/bin/tests");
	}
}
