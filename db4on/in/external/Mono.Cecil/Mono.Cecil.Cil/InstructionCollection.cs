//
// InstructionCollection.cs
//
// Author:
//   Jb Evain (jbevain@gmail.com)
//
// Generated by /CodeGen/cecil-gen.rb do not edit
// Mon Feb 27 22:10:21 CET 2006
//
// (C) 2005 Jb Evain
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

namespace Mono.Cecil.Cil {

	using System;
	using System.Collections;

	using Mono.Cecil.Cil;

	public sealed class InstructionCollection : IInstructionCollection {

		IList m_items;
		MethodBody m_container;

		public event InstructionEventHandler OnInstructionAdded;
		public event InstructionEventHandler OnInstructionRemoved;

		public Instruction this [int index] {
			get { return m_items [index] as Instruction; }
			set { m_items [index] = value; }
		}

		public MethodBody Container {
			get { return m_container; }
		}

		public int Count {
			get { return m_items.Count; }
		}

		public bool IsSynchronized {
			get { return false; }
		}

		public object SyncRoot {
			get { return this; }
		}

		public InstructionCollection (MethodBody container)
		{
			m_container = container;
			m_items = new ArrayList ();
		}

		internal void Add (Instruction value)
		{
			if (OnInstructionAdded != null && !this.Contains (value))
				OnInstructionAdded (this, new InstructionEventArgs (value));
			m_items.Add (value);
		}

		public void Clear ()
		{
			if (OnInstructionRemoved != null)
				foreach (Instruction item in this)
					OnInstructionRemoved (this, new InstructionEventArgs (item));
			m_items.Clear ();
		}

		public bool Contains (Instruction value)
		{
			return m_items.Contains (value);
		}

		public int IndexOf (Instruction value)
		{
			return m_items.IndexOf (value);
		}

		internal void Insert (int index, Instruction value)
		{
			if (OnInstructionAdded != null && !this.Contains (value))
				OnInstructionAdded (this, new InstructionEventArgs (value));
			m_items.Insert (index, value);
		}

		internal void Remove (Instruction value)
		{
			if (OnInstructionRemoved != null && this.Contains (value))
				OnInstructionRemoved (this, new InstructionEventArgs (value));
			m_items.Remove (value);
		}

		internal void RemoveAt (int index)
		{
			if (OnInstructionRemoved != null)
				OnInstructionRemoved (this, new InstructionEventArgs (this [index]));
			m_items.Remove (index);
		}

		public void CopyTo (Array ary, int index)
		{
			m_items.CopyTo (ary, index);
		}

		public IEnumerator GetEnumerator ()
		{
			return m_items.GetEnumerator ();
		}

		public void Accept (ICodeVisitor visitor)
		{
			visitor.VisitInstructionCollection (this);
		}
	}
}
