using System;
using System.Collections;

using com.db4o.query;

namespace com.db4o.test {

    public class HoldsAnArrayList {

        public ArrayList something = new ArrayList();

        public HoldsAnArrayList (){
            something.AddRange(new char[] {'1', '2'});
        }

        public void configure(){

            // Both of the following configuration settings work.
            // They can be used alternatively.

            Db4o.configure().updateDepth(3);

            // Db4o.configure().objectClass(typeof(HoldsAnArrayList)).cascadeOnUpdate(true);
        }

        public void store(){
            Test.store(new HoldsAnArrayList());
        }

        public void test(){
            Query q = Test.query();
            q.constrain(typeof(HoldsAnArrayList));
            ObjectSet objectSet = q.execute();
            while(objectSet.hasNext()){
                HoldsAnArrayList obj = (HoldsAnArrayList)objectSet.next();
                obj.something.Add('3');
                Test.store(obj);

                Test.reOpen();
                q = Test.query();
                q.constrain(typeof(HoldsAnArrayList));
                objectSet = q.execute();
                while(objectSet.hasNext()){
                    HoldsAnArrayList haal = (HoldsAnArrayList)objectSet.next();
                    Test.ensure(haal.something.Count > 2);
                }

            }
        }
    }
}