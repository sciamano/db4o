﻿/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

using System;
using j4o.lang;
using com.db4o;
using com.db4o.query;
using com.db4o.test.soda;
namespace com.db4o.test.soda.classes.typedhierarchy {

   /**
    * RTH: Roundtrip Typed Hierarchy 
    */
   public class STRTH1 : STClass {
      [Transient] public static SodaTest st;
      internal STRTH2 h2;
      internal String foo1;
      
      public STRTH1() : base() {
      }
      
      public STRTH1(STRTH2 a2) : base() {
         h2 = a2;
      }
      
      public STRTH1(String str) : base() {
         foo1 = str;
      }
      
      public STRTH1(STRTH2 a2, String str) : base() {
         h2 = a2;
         foo1 = str;
      }
      
      public Object[] Store() {
         STRTH1[] objects1 = {
            new STRTH1(),
new STRTH1("str1"),
new STRTH1(new STRTH2()),
new STRTH1(new STRTH2("str2")),
new STRTH1(new STRTH2(new STRTH3("str3"))),
new STRTH1(new STRTH2(new STRTH3("str3"), "str2"))         };
         for (int i1 = 0; i1 < objects1.Length; i1++) {
            objects1[i1].AdjustParents();
         }
         return objects1;
      }
      
      /**
       * this is the special part of this test: circular references 
       */
      internal void AdjustParents() {
         if (h2 != null) {
            h2.parent = this;
            if (h2.h3 != null) {
               h2.h3.parent = h2;
               h2.h3.grandParent = this;
            }
         }
      }
      
      public void TestStrNull() {
         Query q1 = st.Query();
         q1.Constrain(new STRTH1());
         q1.Descend("foo1").Constrain(null);
         Object[] r1 = Store();
         st.Expect(q1, new Object[]{
            r1[0],
r1[2],
r1[3],
r1[4],
r1[5]         });
      }
      
      public void TestBothNull() {
         Query q1 = st.Query();
         q1.Constrain(new STRTH1());
         q1.Descend("foo1").Constrain(null);
         q1.Descend("h2").Constrain(null);
         st.ExpectOne(q1, Store()[0]);
      }
      
      public void TestDescendantNotNull() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1());
         q1.Descend("h2").Constrain(null).Not();
         st.Expect(q1, new Object[]{
            r1[2],
r1[3],
r1[4],
r1[5]         });
      }
      
      public void TestDescendantDescendantNotNull() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1());
         q1.Descend("h2").Descend("h3").Constrain(null).Not();
         st.Expect(q1, new Object[]{
            r1[4],
r1[5]         });
      }
      
      public void TestDescendantExists() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(r1[2]);
         st.Expect(q1, new Object[]{
            r1[2],
r1[3],
r1[4],
r1[5]         });
      }
      
      public void TestDescendantValue() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(r1[3]);
         st.Expect(q1, new Object[]{
            r1[3],
r1[5]         });
      }
      
      public void TestDescendantDescendantExists() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1(new STRTH2(new STRTH3())));
         st.Expect(q1, new Object[]{
            r1[4],
r1[5]         });
      }
      
      public void TestDescendantDescendantValue() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1(new STRTH2(new STRTH3("str3"))));
         st.Expect(q1, new Object[]{
            r1[4],
r1[5]         });
      }
      
      public void TestDescendantDescendantStringPath() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1());
         q1.Descend("h2").Descend("h3").Descend("foo3").Constrain("str3");
         st.Expect(q1, new Object[]{
            r1[4],
r1[5]         });
      }
      
      public void TestSequentialAddition() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1());
         Query cur1 = q1.Descend("h2");
         cur1.Constrain(new STRTH2());
         cur1.Descend("foo2").Constrain("str2");
         cur1 = cur1.Descend("h3");
         cur1.Constrain(new STRTH3());
         cur1.Descend("foo3").Constrain("str3");
         st.ExpectOne(q1, Store()[5]);
      }
      
      public void TestTwoLevelOr() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1("str1"));
         q1.Descend("foo1").Constraints().Or(q1.Descend("h2").Descend("h3").Descend("foo3").Constrain("str3"));
         st.Expect(q1, new Object[]{
            r1[1],
r1[4],
r1[5]         });
      }
      
      public void TestThreeLevelOr() {
         Query q1 = st.Query();
         Object[] r1 = Store();
         q1.Constrain(new STRTH1("str1"));
         q1.Descend("foo1").Constraints().Or(q1.Descend("h2").Descend("foo2").Constrain("str2")).Or(q1.Descend("h2").Descend("h3").Descend("foo3").Constrain("str3"));
         st.Expect(q1, new Object[]{
            r1[1],
r1[3],
r1[4],
r1[5]         });
      }
   }
}