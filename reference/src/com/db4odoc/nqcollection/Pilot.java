/* Copyright (C) 2004 - 2007 db4objects Inc. http://www.db4o.com */

package com.db4odoc.nqcollection;

public class Pilot implements Person {
	private String name;

	private int points;

	public Pilot(String name, int points) {
		this.name = name;
		this.points = points;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public int getPoints() {
		return points;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Pilot) {
			return (((Pilot) obj).getName().equals(name) && 
					((Pilot) obj).getPoints() == points);
		}
		return false;
	}

	public String toString() {
		return name + "/" + points;
	}

	public int hashCode() {
		return name.hashCode() + points;
	}
}
