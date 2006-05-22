﻿/* Copyright (C) 2004	db4objects Inc.	  http://www.db4o.com */

using System;
using System.Threading;
using j4o.io;
using System.Reflection;

namespace j4o.lang 
{
	public class JavaSystem 
	{
		public static PrintStream _out = new ConsoleWriter();
		public static PrintStream err = new ConsoleWriter();

		public static long CurrentTimeMillis() 
		{
			return j4o.util.Date.ToJavaMilliseconds(DateTime.Now.ToUniversalTime());
		}

		public static int FloatToIntBits(float value) 
		{
			return BitConverter.ToInt32(BitConverter.GetBytes(value), 0);
		}

		public static void Gc() 
		{
			System.GC.Collect();
		}
		
		public static bool EqualsIgnoreCase(string lhs, string rhs) 
		{
			return 0 == string.Compare(lhs, rhs, true);
		}

		public static string Substring(String s, int startIndex)
		{
			return s.Substring(startIndex);
		}

		public static string Substring(String s, int startIndex, int endIndex)
		{
			return s.Substring(startIndex, endIndex-startIndex);
		}

		public static char GetCharAt(string str, int index) 
		{
			return str[index];
		}

		public static void GetCharsForString(string str, int start, int end, char[] destination, int destinationStart) 
		{
			str.CopyTo(start, destination, 0, end-start);
		}
		
		public static string GetStringValueOf(object value) 
		{
			return null == value
				? "null"
				: value.ToString();
		}

		public static String GetProperty(String key) 
		{
#if CF_1_0 || CF_2_0
			return key.Equals("line.separator") ? "\n" : null;
#else
			return key.Equals("line.separator")
				? Environment.NewLine
				: null;
#endif
		}

		public static object GetReferenceTarget(WeakReference reference) 
		{
			return reference.Target;
		}

		public static long GetTimeForDate(DateTime dateTime) 
		{
			return j4o.util.Date.ToJavaMilliseconds(dateTime);
		}

		public static int IdentityHashCode(object obj) 
		{
			return IdentityHashCodeProvider.IdentityHashCode(obj);
		}

		public static float IntBitsToFloat(int value) 
		{
			return BitConverter.ToSingle(BitConverter.GetBytes(value), 0);
		}

		public static void Wait(object obj, long timeout) 
		{
#if !CF_1_0 && !CF_2_0
			Monitor.Wait(obj, (int) timeout);
#endif
		}

		public static void Notify(object obj) 
		{
#if !CF_1_0 && !CF_2_0
			Monitor.Pulse(obj);
#endif
		}

		public static void NotifyAll(object obj) 
		{
#if !CF_1_0 && !CF_2_0
			Monitor.PulseAll(obj);
#endif
		}

		public static void PrintStackTrace(Exception exception) 
		{
			err.Println(exception);
		}

		public static void PrintStackTrace(Exception exception, PrintStream printStream) 
		{
			printStream.Println(exception);
		}

		public static void RunFinalization() 
		{
			System.GC.WaitForPendingFinalizers();
		}

		public static void RunFinalizersOnExit(bool flag) 
		{
			// do nothing
		}
	}
}