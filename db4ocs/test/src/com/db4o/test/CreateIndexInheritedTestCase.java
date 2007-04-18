/* Copyright (C) 2004 - 2007  db4objects Inc.   http://www.db4o.com */

package com.db4o.test;

import com.db4o.*;
import com.db4o.config.*;
import com.db4o.ext.*;
import com.db4o.query.*;

import db4ounit.*;
import db4ounit.extensions.*;

public class CreateIndexInheritedTestCase extends Db4oClientServerTestCase {

	public static void main(String[] args) {
		new CreateIndexInheritedTestCase().runConcurrency();
	}

	public int i_int;

	public CreateIndexInheritedTestCase() {
	}

	public CreateIndexInheritedTestCase(int a_int) {
		i_int = a_int;
	}

	public void store() {
		store(new CreateIndexFor("a"));
		store(new CreateIndexFor("c"));
		store(new CreateIndexFor("b"));
		store(new CreateIndexFor("f"));
		store(new CreateIndexFor("e"));

		store(new CreateIndexFor(1));
		store(new CreateIndexFor(5));
		store(new CreateIndexFor(7));
		store(new CreateIndexFor(3));
		store(new CreateIndexFor(2));
		store(new CreateIndexFor(3));
	}

	protected void configure(Configuration config) {
		config.objectClass(CreateIndexInheritedTestCase.class).objectField("i_int")
				.indexed(true);
		config.objectClass(CreateIndexFor.class).objectField("i_name").indexed(
				true);
	}

	public void conc1(ExtObjectContainer oc) throws Exception {
		tQueryB(oc);
		tQueryInts(oc, 5);
	}

	public void conc2(ExtObjectContainer oc) {
		oc.set(new CreateIndexFor("d"));
		tQueryB(oc);
		tUpdateB(oc);
		oc.set(new CreateIndexFor("z"));
		oc.set(new CreateIndexFor("y"));
	}

	public void check2(ExtObjectContainer oc) {
		tQueryB(oc);
		tQueryInts(oc, 5 + threadCount() * 3);
	}

	private void tQueryInts(ExtObjectContainer oc, int expectedZeroSize) {
		Query q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(0));
		int zeroSize = q.execute().size();
		Assert.areEqual(expectedZeroSize, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(4)).greater().equal();
		tExpectInts(q, new int[] { 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(4)).greater();
		tExpectInts(q, new int[] { 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(3)).greater();
		tExpectInts(q, new int[] { 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(3)).greater().equal();
		tExpectInts(q, new int[] { 3, 3, 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(2)).greater().equal();
		tExpectInts(q, new int[] { 2, 3, 3, 5, 7 });
		q = oc.query();

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(2)).greater();
		tExpectInts(q, new int[] { 3, 3, 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(1)).greater().equal();
		tExpectInts(q, new int[] { 1, 2, 3, 3, 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(1)).greater();
		tExpectInts(q, new int[] { 2, 3, 3, 5, 7 });

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(4)).smaller();
		tExpectInts(q, new int[] { 1, 2, 3, 3 }, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(4)).smaller().equal();
		tExpectInts(q, new int[] { 1, 2, 3, 3 }, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(3)).smaller();
		tExpectInts(q, new int[] { 1, 2 }, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(3)).smaller().equal();
		tExpectInts(q, new int[] { 1, 2, 3, 3 }, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(2)).smaller().equal();
		tExpectInts(q, new int[] { 1, 2 }, zeroSize);
		q = oc.query();

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(2)).smaller();
		tExpectInts(q, new int[] { 1 }, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(1)).smaller().equal();
		tExpectInts(q, new int[] { 1 }, zeroSize);

		q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_int").constrain(new Integer(1)).smaller();
		tExpectInts(q, new int[] {}, zeroSize);

	}

	private void tExpectInts(Query q, int[] ints, int zeroSize) {
		ObjectSet res = q.execute();
		Assert.areEqual(ints.length + zeroSize, res.size());
		while (res.hasNext()) {
			CreateIndexFor ci = (CreateIndexFor) res.next();
			for (int i = 0; i < ints.length; i++) {
				if (ints[i] == ci.i_int) {
					ints[i] = 0;
					break;
				}
			}
		}
		for (int i = 0; i < ints.length; i++) {
			Assert.areEqual(0, ints[i]);
		}
	}

	private void tExpectInts(Query q, int[] ints) {
		tExpectInts(q, ints, 0);
	}

	private void tQueryB(ExtObjectContainer oc) {
		ObjectSet res = query(oc, "b");
		Assert.areEqual(1, res.size());
		CreateIndexFor ci = (CreateIndexFor) res.next();
		Assert.areEqual("b", ci.i_name);
	}

	private void tUpdateB(ExtObjectContainer oc) {
		ObjectSet res = query(oc, "b");
		CreateIndexFor ci = (CreateIndexFor) res.next();
		ci.i_name = "j";
		oc.set(ci);
		res = query(oc, "b");
		Assert.areEqual(0, res.size());
		res = query(oc, "j");
		Assert.areEqual(1, res.size());
		ci.i_name = "b";
		oc.set(ci);
		tQueryB(oc);
	}

	private ObjectSet query(ExtObjectContainer oc, String n) {
		Query q = oc.query();
		q.constrain(CreateIndexFor.class);
		q.descend("i_name").constrain(n);
		return q.execute();
	}

	public static class CreateIndexFor extends CreateIndexInheritedTestCase {

		public String i_name;

		public CreateIndexFor() {
		}

		public CreateIndexFor(String name) {
			this.i_name = name;
		}

		public CreateIndexFor(int a_int) {
			super(a_int);
		}

	}

}
