package com.db4odoc.f1.diagnostics;

import com.db4o.diagnostic.*;

public class IndexDiagListener  implements DiagnosticListener
{
	   public void onDiagnostic(Diagnostic d) {
		   if (d.getClass().equals(LoadedFromClassIndex.class)){
	        System.out.println(d.toString());
		   }
	    }
}
