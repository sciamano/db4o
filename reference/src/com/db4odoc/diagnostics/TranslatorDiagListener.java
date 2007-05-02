/* Copyright (C) 2004 - 2007 db4objects Inc. http://www.db4o.com */
package com.db4odoc.diagnostics;

import com.db4o.diagnostic.*;

public class TranslatorDiagListener  implements DiagnosticListener
{
	   public void onDiagnostic(Diagnostic d) {
		   if (d.getClass().equals(DescendIntoTranslator.class)){
	        System.out.println(d.toString());
		   }
	    }
}
