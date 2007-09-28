/* Copyright (C) 2007  db4objects Inc.  http://www.db4o.com */

package com.db4o.collections;

import java.lang.reflect.*;
import java.util.*;

import com.db4o.foundation.*;

/**
 * @exclude
 */
public class Db4oArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;

	public E[] elements;

	public int capacity;

	public int listSize;
	
	protected transient int modCount;

	public Db4oArrayList() {
		this(10);
	}

	@SuppressWarnings("unchecked")
	public Db4oArrayList(Collection<? extends E> c) {
		super(c);
		Object[] data = c.toArray();
		capacity = data.length;
		elements = (E[]) new Object[capacity];
		listSize = data.length;
		System.arraycopy(data, 0, elements, 0, data.length);
	}

	@SuppressWarnings("unchecked")
	public Db4oArrayList(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException();
		}
		capacity = initialCapacity;
		elements = (E[]) new Object[initialCapacity];
	}

	public void add(int index, E element) {
		checkIndex(index, 0, size());
		ensureCapacity(size() + 1);
		System.arraycopy(elements, index,
				elements, index + 1, listSize - index);
		elements[index] = element;
		increaseSize(1);
		markModified();
	}

	public boolean add(E e) {
		ensureCapacity(size() + 1);
		elements[listSize] = e;
		increaseSize(1);
		markModified();
		return true;
	}

	public boolean addAll(Collection<? extends E> c) {
		int length = c.size();
		if(length == 0) {
			return false;
		}
		ensureCapacity(size() + length);
		Object[] toBeAdded = c.toArray();
		System.arraycopy(toBeAdded, 0, elements, size(), toBeAdded.length);
		increaseSize(length);
		markModified();
		return true;		
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		checkIndex(index, 0, size());
		int length = c.size();
		if(length == 0) {
			return false;
		}
		ensureCapacity(size() + length);
		Object[] data = c.toArray();
		System.arraycopy(elements, index, elements, index+length, size() - index);
		System.arraycopy(data, 0, elements, index, length);
		increaseSize(length);
		markModified();
		return true;
	}

	public void clear() {
		setSize(0);
		markModified();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		Db4oArrayList <E> clonedList = (Db4oArrayList<E>) super.clone();
		clonedList.elements = elements.clone();
		return clonedList;
	}

	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	public void ensureCapacity(int minCapacity) {
		if (minCapacity <= capacity) {
			return;
		}
		resize(minCapacity);
	}

	public E get(int index) {
		checkIndex(index, 0, size() - 1);
		return elements[index];
	}

	public int indexOf(Object o) {
		for (int index = 0; index < size(); ++index) {
			E element = get(index);
			if (o == null ? element == null : o.equals(element)) {
				return index;
			}
		}
		return -1;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int lastIndexOf(Object o) {
		for (int index = size() - 1; index >= 0; --index) {
			E element = get(index);
			if (o == null ? element == null : o.equals(element)) {
				return index;
			}
		}
		return -1;
	}

	public E remove(int index) {
		checkIndex(index, 0, size() - 1);
		E element = elements[index];
		System.arraycopy(elements, index + 1, 
				elements, index, size() - index	- 1);
		decreaseSize(1);
		markModified();
		return element;
	}

	public boolean remove(Object o) {
		int index = indexOf(o);
		if (index == -1) {
			return false;
		}
		remove(index);
		return true;
	}


	protected void removeRange(int fromIndex, int toIndex) {
		if ((fromIndex < 0 || fromIndex >= size() || toIndex > size() || toIndex < fromIndex)) {
			throw new IndexOutOfBoundsException();
		}
		if (fromIndex == toIndex) {
			return;
		}
		int count = toIndex - fromIndex;
		System.arraycopy(elements, toIndex, elements, fromIndex, size()
				- toIndex);
		decreaseSize(count);
		markModified();
	}

	public E set(int index, E element) {
		checkIndex(index, 0, size() - 1);
		E oldValue = elements[index];
		elements[index] = element;
		return oldValue;
	}

	public int size() {
		return listSize;
	}

	public Object[] toArray() {
		int size = size();
		Object[] data = new Object[size];
		System.arraycopy(elements, 0, data, 0, size);
		return data;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int size = size();
		if(a.length < size) {
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		}
		System.arraycopy(elements, 0, a, 0, size);
		return a;
	}

	public void trimToSize() {
		resize(size());
	}

	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof List)) {
			return false;
		}
		List<?> otherList = (List<?>) other;
		if (otherList.size() != size()) {
			return false;
		}
		Iterator<E> iter = iterator();
		Iterator<?> otherIter = otherList.iterator();
		while (iter.hasNext()) {
			E e1 = iter.next();
			Object e2 = otherIter.next();
			if (!(e1 == null ? e2 == null : e1.equals(e2))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see List#hashCode()
	 */
	public int hashCode() {
		int hashCode = 1;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

	public Iterator<E> iterator() {
		 return new Db4oArrayListIterator(-1);
	}

	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	public ListIterator<E> listIterator(int index) {
		checkIndex(index, 0, size());
		return new Db4oArrayListIndexIterator(index);
	}

	public List<E> subList(int fromIndex, int toIndex) {
		throw new NotImplementedException();
	}

	public boolean containsAll(Collection<?> c) {
		Iterator<?> iter = c.iterator();
		while(iter.hasNext()) {
			if(!contains(iter.next())) {
				return false;
			}
		}
		return true;
	}

	public boolean removeAll(Collection <?> c) {
		boolean changed = false;
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			if (c.contains(it.next())) {
				it.remove();
				changed = true;
			}
		}
		return changed;
	}

	public boolean retainAll(Collection <?> c) {
		boolean changed = false;
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			if (!c.contains(it.next())) {
				it.remove();
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * @see Collection#toString()
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append('[');
		Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			E element = iter.next();
			if (element != this) {
				buffer.append(element);
			} else {
				buffer.append("(this Collection)"); //$NON-NLS-1$
			}
            if(iter.hasNext()) {
                buffer.append(", "); //$NON-NLS-1$
            }
		}
		buffer.append(']');
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	private void resize(int minCapacity) {
		markModified();
		E[] temp = (E[]) new Object[minCapacity];
		System.arraycopy(elements, 0, temp, 0, size());
		elements = temp;
		capacity = minCapacity;
	}

	private void checkIndex(int index, int from, int to) {
		if (index < from || index > to) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	private void setSize(int count) {
		listSize = count;
	}
	
	private void increaseSize(int count) {
		listSize += count;
	}
	
	private void decreaseSize(int count) {
		listSize -= count;
	}
	
	private void markModified() {
		++modCount;
	}

	private class Db4oArrayListIterator implements Iterator<E> {

		protected int currentIndex;
		
		private int _iteratorModCount;
		
		protected boolean canOperate;
		
		public Db4oArrayListIterator (int pos) {
			currentIndex = pos;
			syncModCount();
		}
		
		public boolean hasNext() {
			return currentIndex + 1 < size();
		}

		public E next() {
			checkConcurrentModification();
			try {
				E element = get(currentIndex + 1);
				++currentIndex;
				setCanOperateFlag(true);
				return element;
			} catch (IndexOutOfBoundsException e) {
				checkConcurrentModification();
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			checkCanOperate();
			checkConcurrentModification();
			Db4oArrayList.this.remove(currentIndex);
			--currentIndex;
			syncModCount();
			setCanOperateFlag(false);
		}
		
		protected void syncModCount() {
			_iteratorModCount = modCount;
		}

		protected void checkCanOperate() {
			if(!canOperate) {
				throw new IllegalStateException();
			}
		}
		
		protected void setCanOperateFlag(boolean enabled) {
			canOperate = enabled;
		}
		
		protected void checkConcurrentModification() {
			if(_iteratorModCount != modCount) {
				throw new ConcurrentModificationException();
			}
		}

	}
	
	private class Db4oArrayListIndexIterator extends Db4oArrayListIterator
			implements ListIterator<E> {
		public Db4oArrayListIndexIterator(int index) {
			super(index - 1);
		}

		public void add(E element) {
			checkCanOperate();
			checkConcurrentModification();
			try {
				Db4oArrayList.this.add(currentIndex, element);
				++currentIndex;
				syncModCount();
				setCanOperateFlag(false);
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		public boolean hasPrevious() {
			return currentIndex != -1;
		}

		public int nextIndex() {
			return currentIndex + 1;
		}

		public E previous() {
			checkConcurrentModification();
			try {
				E element = get(currentIndex);
				--currentIndex;
				setCanOperateFlag(true);
				return element;
			} catch (IndexOutOfBoundsException e) {
				checkConcurrentModification();
				throw new NoSuchElementException();
			}
		}

		public int previousIndex() {
			return currentIndex;
		}

		public void set(E element) {
			checkCanOperate();
			checkConcurrentModification();
			try {
				Db4oArrayList.this.set(currentIndex, element);
				setCanOperateFlag(false);
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}
	}
}
