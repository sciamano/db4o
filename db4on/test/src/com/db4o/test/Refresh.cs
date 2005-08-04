/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

using System;
using com.db4o.ext;
using com.db4o.query;

namespace com.db4o.test {
    /// <summary>
    /// Summary description for Refresh.
    /// </summary>
    public class Refresh {

        String  name;

        Refresh child;

        public Refresh() {

        }

        public Refresh(String name, Refresh child) {
            this.name = name;
            this.child = child;
        }

        public void store() {
            Refresh r3 = new Refresh("o3", null);
            Refresh r2 = new Refresh("o2", r3);
            Refresh r1 = new Refresh("o1", r2);
            Tester.store(r1);
        }

        public void test() {
            ExtObjectContainer oc1 = Tester.objectContainer();
            Refresh r11 = getRoot(oc1);
            r11.name = "cc";
            oc1.refresh(r11, 0);
            Tester.ensure(r11.name.Equals("cc"));
            oc1.refresh(r11, 1);
            Tester.ensure(r11.name.Equals("o1"));
            r11.child.name = "cc";
            oc1.refresh(r11, 1);
            Tester.ensure(r11.child.name.Equals("cc"));
            oc1.refresh(r11, 2);
            Tester.ensure(r11.child.name.Equals("o2"));

            if (Tester.isClientServer()) {
                ExtObjectContainer oc2 = null;
                try {
                    oc2 = Db4o.openClient(AllTests.SERVER_HOSTNAME, AllTests.SERVER_PORT, AllTests.DB4O_USER,
                        AllTests.DB4O_PASSWORD).ext();
                } catch (Exception e) {
                    Console.WriteLine(e);
                    return;
                }

                Refresh r12 = getRoot(oc2);
                Refresh r32 = r12.child.child;

                r11.child.name = "n2";
                r11.child.child.name = "n3";
                r11.child.child.child = new Refresh("n4", null);
                oc1.set(r11.child.child);
                oc1.set(r11.child);
                oc1.set(r11);

                oc2.refresh(r12, int.MaxValue);
                Tester.ensure(r12.child.name.Equals("o2"));

                Tester.commitSync(oc1, oc2);

                oc2.refresh(r12, int.MaxValue);
                Tester.ensure(r12.child.name.Equals("n2"));
                Tester.ensure(r12.child.child.name.Equals("n3"));
                Tester.ensure(r12.child.child.child.name.Equals("n4"));

                r11.child.child.child = null;
                oc1.set(r11.child.child);
                Tester.commitSync(oc1, oc2);

                oc2.refresh(r12, int.MaxValue);
                Tester.ensure(r12.child.child.child == null);

                r11.child.child = new Refresh("nn2", null);
                oc1.set(r11.child);
                Tester.commitSync(oc1, oc2);

                oc2.refresh(r12, int.MaxValue);
                Tester.ensure(r12.child.child != r32);
                Tester.ensure(r12.child.child.name.Equals("nn2"));

                oc2.close();
            }

        }

        private Refresh getRoot(ObjectContainer oc) {
            Query q = oc.query();
            q.constrain(typeof(Refresh));
            q.descend("name").constrain("o1");
            ObjectSet objectSet = q.execute();
            return (Refresh) objectSet.next();
        }
    }
}
