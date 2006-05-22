﻿/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

using System;
using j4o.lang;
using com.db4o;
using com.db4o.query;
using com.db4o.test.soda;
namespace com.db4o.test.soda.classes.wrapper.untyped {

   public class STLongWU : STClass {
      [Transient] public static SodaTest st;
      internal Object i_long;
      
      public STLongWU() : base() {
      }
      
      internal STLongWU(long a_long) : base() {
         i_long = System.Convert.ToInt64(a_long);
      }
      
      public Object[] Store() {
         return new Object[]{
            new STLongWU(Int64.MinValue),
new STLongWU(-1),
new STLongWU(0),
new STLongWU(Int64.MaxValue - 1)         };
      }
      
      public void TestEquals() {
         Query q1 = st.Query();
         q1.Constrain(new STLongWU(Int64.MinValue));
         st.Expect(q1, new Object[]{
            new STLongWU(Int64.MinValue)         });
      }
      
      public void TestGreater() {
         Query q1 = st.Query();
         q1.Constrain(new STLongWU(-1));
         q1.Descend("i_long").Constraints().Greater();
         Object[] r1 = Store();
         st.Expect(q1, new Object[]{
            r1[2],
r1[3]         });
      }
      
      public void TestSmaller() {
         Query q1 = st.Query();
         q1.Constrain(new STLongWU(1));
         q1.Descend("i_long").Constraints().Smaller();
         Object[] r1 = Store();
         st.Expect(q1, new Object[]{
            r1[0],
r1[1],
r1[2]         });
      }
      
      public void TestBetween() {
         Query q1 = st.Query();
         q1.Constrain(new STLongWU());
         Query sub1 = q1.Descend("i_long");
         sub1.Constrain(System.Convert.ToInt64(-3)).Greater();
         sub1.Constrain(System.Convert.ToInt64(3)).Smaller();
         Object[] r1 = Store();
         st.Expect(q1, new Object[]{
            r1[1],
r1[2]         });
      }
      
      public void TestAnd() {
         Query q1 = st.Query();
         q1.Constrain(new STLongWU());
         Query sub1 = q1.Descend("i_long");
         sub1.Constrain(System.Convert.ToInt64(-3)).Greater().And(sub1.Constrain(System.Convert.ToInt64(3)).Smaller());
         Object[] r1 = Store();
         st.Expect(q1, new Object[]{
            r1[1],
r1[2]         });
      }
      
      public void TestOr() {
         Query q1 = st.Query();
         q1.Constrain(new STLongWU());
         Query sub1 = q1.Descend("i_long");
         sub1.Constrain(System.Convert.ToInt64(3)).Greater().Or(sub1.Constrain(System.Convert.ToInt64(-3)).Smaller());
         Object[] r1 = Store();
         st.Expect(q1, new Object[]{
            r1[0],
r1[3]         });
      }
   }
}