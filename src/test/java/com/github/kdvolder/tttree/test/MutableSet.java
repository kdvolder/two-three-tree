package com.github.kdvolder.tttree.test;

import java.util.Iterator;

import com.github.kdvolder.tttree.TTTSet;

/**
 * Like {@link MutableMap} but provides the kind of interface you would expect from a Set rather than
 * a Map.
 */
public abstract class MutableSet<E> implements Iterable<E> {

	public abstract void add(E e);
	public abstract boolean contains(E e);
	public abstract void remove(E e);
	public abstract void dump();
	public abstract boolean isEmpty();

	public static <E extends Comparable<E>> MutableSet<E> from(TTTSet<E> immutableSet) {
		return new MutableSet<E>() {

			private TTTSet<E> set = immutableSet;

			@Override
			public Iterator<E> iterator() {
				return set.iterator();
			}

			@Override
			public void add(E e) {
				set = set.insert(e);
			}

			@Override
			public boolean contains(E e) {
				return set.contains(e);
			}

			@Override
			public void remove(E e) {
				set = set.delete(e);
			}

			@Override
			public void dump() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

}
