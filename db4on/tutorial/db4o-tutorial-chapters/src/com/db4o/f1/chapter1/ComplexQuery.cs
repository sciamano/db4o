﻿using com.db4o.query;

namespace com.db4o.f1.chapter1
{
	public class ComplexQuery : Predicate
    {
    	public bool Match(Pilot pilot)
    	{
	    	return pilot.Points > 99
                && pilot.Points < 199
                || pilot.Name=="Rubens Barrichello";
    	}
    }
}
